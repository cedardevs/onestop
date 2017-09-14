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

  /**
   * Output shape:
   * {
   *   "data": [
   *     {
   *       "id": <>,
   *       "type": <>,
   *       "attributes": <parsed source>,
   *       "meta": {
   *         "filename": "<original filename>,
   *         "status": 200|201|400...
   *         "created": true|false,
   *         "error": null|{...}
   *       }
   *     },
   *     ...
   *   ]
   * }
   *
   * @param documents
   * @return
   */
  public Map loadMetadata(MultipartFile[] documents) {
    esService.ensureStagingIndex()
    def results = []
    def bulkRequest = new StringBuilder()
    def loadedIndices = []

    documents.eachWithIndex { rawDoc, i ->
      def document = rawDoc.inputStream.text
      def source = MetadataParser.parseXMLMetadataToMap(document)
      def type = source.parentIdentifier ? GRANULE_TYPE : COLLECTION_TYPE
      def fileId = source.fileIdentifier as String
      def doi = source.doi as String
      source.stagedDate = System.currentTimeMillis()
      def record = findMetadata(fileId, doi)
      def existingIds = record.data*.id
      def esId = existingIds?.size() == 1 ? existingIds[0] : null
      def result = [
          id: esId,
          type: type,
          attributes: source,
          meta: [
              filename: rawDoc.originalFilename,
          ]
      ]

      if (existingIds?.size() > 1) {
        result.meta.status = HttpStatus.CONFLICT.value()
        result.meta.error = [
            title: 'Ambiguous metadata records in existence; metadata not loaded.',
            detail: "Please GET records with ids [ ${existingIds.join(',')} ] and DELETE any " +
                "erroneous records. Ambiguity because of fileIdentifier [ ${fileId} ] and/or DOI [ ${doi} ]."
        ]
      }
      else {
        def bulkCommand = [index: [_index: STAGING_INDEX, _type: type, _id: esId]]
        bulkRequest << JsonOutput.toJson(bulkCommand)
        bulkRequest << '\n'
        bulkRequest << JsonOutput.toJson(source)
        bulkRequest << '\n'
        loadedIndices << i
      }
      results << result
    }

    def bulkResponse = esService.performRequest('POST', '_bulk', bulkRequest.toString())
    bulkResponse.items.eachWithIndex { result, i ->
      def resultRecord = results.get(loadedIndices[i])
      resultRecord.id = result.index._id
      resultRecord.meta.status = result.index.status
      resultRecord.meta.created = result.index.created
      if (result.error) {
        resultRecord.meta.error = result.error
      }
    }

    return [data: results]
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

  // TODO - optional source filtering to just get ids?
  public Map getMetadata(String esId) {
    String endpoint = "${STAGING_INDEX}/_all/${esId}"
    def response = esService.performRequest("GET", endpoint)

    if (response.found) {
      return [
          data: [[
              id        : response._id,
              type      : response._type,
              attributes: response._source
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

  // TODO - optional source filtering to just get ids?
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

    if (response.hits.total > 0) {
      def resources = response.hits.hits.collect {
        [
            id: it._id,
            type: it._type,
            attributes: it._source
        ]
      }
      return [ data: resources ]
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
    // collect up the ids, types, and potential granule parentIds to be deleted
    def ids = []
    def parentIds = []
    def toBeDeleted = []
    record.data.each {
      toBeDeleted << [id: it.id, type: it.type]
      ids << it.id
      if (recursive) {
        if (it.attributes?.fileIdentifier) {
          parentIds << it.attributes.fileIdentifier
        }
        if (it.attributes?.doi) {
          parentIds << it.attributes.doi
        }
      }
    }

    // Use delete_by_query to match collection & associated granules all at once
    def query = [
        query: [
            bool: [
                should: [
                    [terms: [parentIdentifier: parentIds]],
                    [terms: [_id: ids]]
                ]
            ]
        ]
    ]
    def endpoint = "$STAGING_INDEX,$SEARCH_INDEX/$COLLECTION_TYPE,$GRANULE_TYPE/_delete_by_query?wait_for_completion=true"
    def deleteResponse = esService.performRequest('POST', endpoint, query)

    return [
        response: [
            data: toBeDeleted,
            meta: deleteResponse
        ],
        status: deleteResponse.failures ? HttpStatus.MULTI_STATUS.value() : HttpStatus.OK.value()
    ]
  }

}
