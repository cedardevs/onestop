package org.cedar.onestop.api.metadata.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Slf4j
@Service
class MetadataManagementService {

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.name}')
  String STAGING_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.name}')
  String SEARCH_INDEX

  @Value('${elasticsearch.index.staging.collectionType}')
  String COLLECTION_TYPE

  @Value('${elasticsearch.index.staging.granuleType}')
  String GRANULE_TYPE

  private ElasticsearchService esService

  @Autowired
  public MetadataManagementService(ElasticsearchService esService) {
    this.esService = esService
  }

  public Map loadMetadata(MultipartFile[] documents) {

    esService.ensureStagingIndex()

    def resultRecords = []
    def entitiesToLoad = []

    def stagedDate = System.currentTimeMillis()
    documents.eachWithIndex { rawDoc, i ->
      def resultRecord = [
          id        : null,
          type      : null,
          attributes: [
              filename: rawDoc.originalFilename
          ]
      ]

      try {
        def document = rawDoc.inputStream.text
        def storageInfo = MetadataParser.parseIdentifierInfo(document)
        def fileId = storageInfo.fileId
        def doi = storageInfo.doi
        def type = storageInfo.parentId ? GRANULE_TYPE : COLLECTION_TYPE

        def source = MetadataParser.parseXMLMetadataToMap(document)
        source.stagedDate = stagedDate
        def record = findMetadata(fileId, doi)
        def internalIds = record.data?.collect { it.id }

        def esId = null
        if (internalIds) {
          if (internalIds.size() == 1) {
            // Record exists already
            esId = internalIds[0]
          }
          else {
            resultRecord.attributes.status = HttpStatus.CONFLICT.value()
            resultRecord.attributes.error = [
                title: 'Ambiguous metadata records in existence; metadata not loaded.',
                detail: "Please GET records with ids [ ${String.join(', ', internalIds)} ] and DELETE any " +
                    "erroneous records. Ambiguity because of fileIdentifier [ ${fileId} ] and/or DOI [ ${doi} ]."
            ]
          }
        }
        resultRecord.id = esId
        resultRecord.type = type
        entitiesToLoad.add(i, [source: JsonOutput.toJson(source), type: type, id: esId])
      }

      catch (Exception e) {
        resultRecord.attributes.status = HttpStatus.BAD_REQUEST.value()
        resultRecord.attributes.error = [
            title : 'Load request failed due to malformed XML',
            detail: ExceptionUtils.getRootCauseMessage(e)
        ]
      }
      resultRecords.add(i, resultRecord)
    }


    def results = esService.performMultiLoad(STAGING_INDEX, entitiesToLoad)
    results.eachWithIndex { result, i ->
      def resultRecord = results.get(i)
      resultRecord.attributes.status = result.status
      if (!result.error) {
        resultRecord.attributes.created = result.status == HttpStatus.CREATED.value()
      }
      else {
        resultRecord.attributes.error = result.error
      }
      resultRecords.add(i, resultRecord)
    }

    return [ data: resultRecords ]
  }

  public Map loadMetadata(String document) {

    esService.ensureStagingIndex()

    def fileId, doi, type, source
    try {
      def storageInfo = MetadataParser.parseIdentifierInfo(document)
      fileId = storageInfo.fileId
      doi = storageInfo.doi
      type = storageInfo.parentId ? GRANULE_TYPE : COLLECTION_TYPE
      source = MetadataParser.parseXMLMetadataToMap(document)
    }
    catch (Exception e) {
      return [
          errors: [[
                       status: HttpStatus.BAD_REQUEST.value(),
                       title: 'Load request failed due to malformed XML.',
                       detail: ExceptionUtils.getRootCauseMessage(e)
                   ]]
      ]
    }
    source.stagedDate = System.currentTimeMillis()
    source = JsonOutput.toJson(source)

    def record = findMetadata(fileId, doi)
    def internalIds = record.data?.collect { it.id }
    def endpoint, response
    if (internalIds) {
      if (internalIds.size() == 1) {
        // Record exists, so perform an update
        endpoint = "${STAGING_INDEX}/${type}/${internalIds[0]}"
        response = esService.performRequest('PUT', endpoint, source)
      }
      else {
        return [
            errors: [[
                         status: HttpStatus.CONFLICT.value(),
                         title: 'Ambiguous metadata records in existence; metadata not loaded.',
                         detail: "Please GET records with ids [ ${String.join(', ', internalIds)} ] and DELETE any " +
                             "erroneous records. Ambiguity because of fileIdentifier [ ${fileId} ] and/or DOI [ ${doi} ]."
                     ]]
        ]
      }
    }
    else {
      // Record doesn't exist, so create a new one
      endpoint = "${STAGING_INDEX}/${type}/"
      response = esService.performRequest('POST', endpoint, source)
    }

    def status = response.statusCode
    if (status == HttpStatus.CREATED.value() || status == HttpStatus.OK.value()) {
      return [
          data: [
              id        : response._id,
              type      : response._type,
              attributes: [
                  fileIdentifier: fileId,
                  doi: doi,
                  created: response.created
              ]
          ]
      ]
    }
    else {
      return [
          errors: [[
                       status: status,
                       title: 'Bad Request',
                       detail: "Load request of fileIdentifier [ ${fileId} ] and/or DOI [ ${doi} ] failed due to: ${response.error?.reason}"
                   ]]
      ]
    }
  }

  public Map getMetadata(String esId) {
    String endpoint = "${STAGING_INDEX}/_all/${esId}"
    def response = esService.performRequest("GET", endpoint)

    if (response.found) {
      return [
          data: [[
              id        : response._id,
              type      : response._type,
              attributes: [
                  source: response._source
              ]
          ]]
      ]
    }
    else {
      return [
          status: HttpStatus.NOT_FOUND.value(),
          title : 'No such document',
          detail: "Metadata with Elasticsearch ID [ ${esId} ] does not exist."
      ]
    }
  }

  public Map findMetadata(String fileId, String doi) {
    def endpoint = "${STAGING_INDEX}/_search"
    def searchParams = []
    if (fileId) { searchParams.add( [term: [fileIdentifier: fileId]] ) }
    if (doi) { searchParams.add( [term: [doi: doi]] ) }
    def requestBody = JsonOutput.toJson([
        query: [
            bool: [
                should: searchParams
            ]
        ]
    ])
    def response = esService.performRequest('GET', endpoint, requestBody)

    if (response.hits) {
      def result = [ data: [] ]
      def resources = response.hits.collect {
        [
            id: it._id,
            type: it._type,
            attributes: [
                source: it._soucre
            ]
        ]
      }
      result.data.addAll(resources)
      return result
    }
    else {
      return [
          status: HttpStatus.NOT_FOUND.value(),
          title : 'No such document',
          detail: "Metadata with fileIdentifier [ ${fileId} ] and/or DOI [ ${doi} ] not found."
      ]
    }
  }

  public Map deleteMetadata(String esId, boolean recursive) {
    def record = getMetadata(esId)
    if (record.data) { return delete(record, recursive) }
    else {
      // Record does not exist -- return NOT_FOUND response
      return record
    }
  }

  public Map deleteMetadata(String fileId, String doi, boolean recursive) {
    def record = findMetadata(fileId, doi)
    if (record.data) { return delete(record, recursive) }
    else {
      // Record does not exist -- return NOT_FOUND response
      return record
    }
  }

  private Map delete(Map record, boolean recursive) {

    def result = [
        response: [
            data: [],
            meta: [
                searchIndex: [
                    totalRecordsFound: 0,
                    recordsDeleted: 0,
                    failures: []
                ],
                stagingIndex: [
                    totalRecordsFound: 0,
                    recordsDeleted: 0,
                    failures: []
                ]
            ]
        ]
    ]
    def dataRecords = record.data.collect {
      [
          id: it.id,
          type: it.type
      ]
    }
    result.response.data.addAll(dataRecords)

    def fileId = record.data.attributes.source.fileIdentifier
    def doi = record.data.attributes.source.doi

    result.response.meta.fileIdentifier = fileId
    result.response.meta.doi = doi

    // Use delete_by_query to match collection & associated granules all at once
    def query = [
        query: [
            bool: [
                should: [
                    [term: [ parentIdentifier: fileId ]]
                ]
            ]
        ]
    ]
    dataRecords.each { i -> query.query.bool.should.add( [term: [ _id: i.id ]] ) }
    if (doi) { query.query.bool.should.add( [term: [ doi: doi ]] ) } // Search with null throws error


    def indicesToPurge = [SEARCH_INDEX]
    if (recursive) {
      indicesToPurge.add(STAGING_INDEX)
    }
    else {
      dataRecords.each {
        def response = esService.performRequest('DELETE', "${STAGING_INDEX}/${it.type}/${it.id}")
        if (response.found) { result.response.meta.stagingIndex.totalRecordsFound += 1 }
        if (response.result == 'deleted') { result.response.meta.stagingIndex.recordsDeleted +=1 }
      }
    }
    def deleteResponses = esService.performDeleteByQuery(JsonOutput.toJson(query), indicesToPurge)

    def searchResponse = deleteResponses.get(SEARCH_INDEX)
    if (!searchResponse.error) {
      result.response.meta.searchIndex.totalRecordsFound = searchResponse.total
      result.response.meta.searchIndex.recordsDeleted = searchResponse.deleted
      result.response.meta.searchIndex.failures = searchResponse.failures
      result.response.meta.searchIndex.batches = searchResponse.batches
      result.response.meta.searchIndex.versionConflicts = searchResponse.version_conflicts
      result.response.meta.searchIndex.took = searchResponse.took
    }
    else {
      result.response.meta.searchIndex.failures.add(searchResponse.error)
    }

    if (recursive) {
      def stagingResponse = deleteResponses.get(STAGING_INDEX)
      if (!stagingResponse.error) {
        result.response.meta.stagingIndex.totalRecordsFound = stagingResponse.total
        result.response.meta.stagingIndex.recordsDeleted = stagingResponse.deleted
        result.response.meta.stagingIndex.failures = stagingResponse.failures
        result.response.meta.stagingIndex.batches = stagingResponse.batches
        result.response.meta.stagingIndex.versionConflicts = stagingResponse.version_conflicts
        result.response.meta.stagingIndex.took = stagingResponse.took
      }
      else {
        result.response.meta.stagingIndex.failures.add(stagingResponse.error)
      }
    }

    result.status = HttpStatus.MULTI_STATUS.value()
    return result
  }

}
