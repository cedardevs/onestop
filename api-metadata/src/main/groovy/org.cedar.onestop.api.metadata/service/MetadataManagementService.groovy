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

  // FIXME:
  public Map loadMetadata(MultipartFile[] documents) {

    esService.ensureStagingIndex()

    def resultRecordMap = [:]
    def entitiesToLoad = [:]

    def stagedDate = System.currentTimeMillis()
    documents.each { rawDoc ->
      def document = rawDoc.inputStream.text
      def storageInfo = MetadataParser.parseIdentifierInfo(document)
      def internalId = storageInfo.id
      def externalId = storageInfo.doi ?: storageInfo.id
      def type = storageInfo.parentId ? GRANULE_TYPE : COLLECTION_TYPE

      def resultRecord = [
          id        : externalId,
          type      : type,
          attributes: [
              filename: rawDoc.originalFilename
          ]
      ]

      try {
        def source = MetadataParser.parseXMLMetadataToMap(document)
        source.stagedDate = stagedDate
        entitiesToLoad.put(internalId, [source: JsonOutput.toJson(source), type: type])
      }
      catch (Exception e) {
        resultRecord.attributes.status = 400
        resultRecord.attributes.error = [
            title : 'Load request failed due to malformed XML',
            detail: ExceptionUtils.getRootCauseMessage(e)
        ]
      }
      resultRecordMap.put(internalId, resultRecord)
    }

    def results = esService.performMultiLoad(entitiesToLoad)
    results.each { k, v ->
      def resultRecord = [:].putAll(resultRecordMap.get(k))
      resultRecord.attributes.status = v.status
      if (!v.error) {
        resultRecord.attributes.created = v.status == HttpStatus.CREATED.value()
      }
      else {
        resultRecord.attributes.error = v.error
      }
      resultRecordMap.put(k, resultRecord)
    }

    return [ data: resultRecordMap.values() ]
  }

  public Map loadMetadata(String document) {

    esService.ensureStagingIndex()

    def storageInfo = MetadataParser.parseIdentifierInfo(document)
    def fileId = storageInfo.fileId
    def doi = storageInfo.doi
    def type = storageInfo.parentId ? GRANULE_TYPE : COLLECTION_TYPE
    def source = MetadataParser.parseXMLMetadataToMap(document)
    source.stagedDate = System.currentTimeMillis()
    source = JsonOutput.toJson(source)

    def endpoint = "${STAGING_INDEX}/${type}/_search"
    def searchParams = []
    searchParams.add([term: [fileIdentifier: fileId]])
    if (doi) { searchParams.add( [term: [doi: doi]] ) }
    def requestBody = JsonOutput.toJson([
        query: [
            bool: [
                should: searchParams
            ]
        ],
        _source: false
    ])
    def response = esService.performRequest('GET', endpoint, requestBody)
    def internalIds = response.hits.hits*._id
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
          data: [
              id        : response._id,
              type      : response._type,
              attributes: [
                  source: response._source
              ]
          ]
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
    if (fileId) { searchParams.add( [match: [fileIdentifier: fileId]] ) }
    if (doi) { searchParams.add( [match: [doi: doi]] ) }
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

  public Map deleteMetadata(String esId) {

    def record = getMetadata(esId)
    if (record.data) {
      def result = [
          response: [
              data: [
                  id        : esId
              ],
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
      String stagingEndpoint, searchEndpoint
      def stagingResponse, searchResponse

      if (record.data.type == GRANULE_TYPE) {
        // The delete request is for a single GRANULE
        result.response.data.type = GRANULE_TYPE

        stagingEndpoint = "${STAGING_INDEX}/${GRANULE_TYPE}/${esId}"
        searchEndpoint = "${SEARCH_INDEX}/${GRANULE_TYPE}/${esId}"
        stagingResponse = esService.performRequest('DELETE', stagingEndpoint)
        searchResponse = esService.performRequest('DELETE', searchEndpoint)

        result.response.meta.stagingIndex.result = stagingResponse.result
        if (stagingResponse.result == 'deleted') {
          result.response.meta.stagingIndex.totalRecordsFound = 1
          result.response.meta.stagingIndex.recordsDeleted = 1
        }
        // FIXME handle error message if record not deleted

        // Not finding anything in search is okay -- we may be deleting a fresh record that hasn't yet been ETLed
        result.response.meta.searchIndex.result = searchResponse.result
        if (searchResponse.result == 'deleted') {
          result.response.meta.searchIndex.totalRecordsFound = 1
          result.response.meta.searchIndex.recordsDeleted = 1
        }

        result.status = HttpStatus.OK.value()
      }


      else {
        // The delete request is for a COLLECTION
        def fileId = record.data.attributes.source.fileIdentifier
        def doi = record.data.attributes.source.doi

        result.response.data.type = COLLECTION_TYPE
        result.response.meta.fileIdentifier = fileId
        result.response.meta.doi = doi

        // Use delete_by_query to match collection & associated granules all at once
        def query = [
            query: [
                bool: [
                    should: [
                        [match: [ _id: esId ]],
                        [match: [ parentIdentifier: fileId ]]
                    ]
                ]
            ]
        ]
        if (doi) { query.query.bool.should.add( [match: [ doi: doi ]] ) } // Search with null throws error

        def deleteResponses = esService.performDeleteByQuery(JsonOutput.toJson(query), [STAGING_INDEX, SEARCH_INDEX])
        stagingResponse = deleteResponses.get(STAGING_INDEX)
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

        searchResponse = deleteResponses.get(SEARCH_INDEX)
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

        result.status = HttpStatus.MULTI_STATUS.value()
      }

      return result
    }


    else {
      // Record does not exist -- return NOT_FOUND response
      return record
    }
  }

  public findAndDeleteMetadata(String fileIdentifier, String doi, String type) {
    // TODO: Use granule logic from delete by esId function
  }

}
