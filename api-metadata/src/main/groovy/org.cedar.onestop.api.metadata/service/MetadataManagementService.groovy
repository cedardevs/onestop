package org.cedar.onestop.api.metadata.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.xml.sax.SAXException

@Slf4j
@Service
class MetadataManagementService {

  @Value('${elasticsearch.index.search.collection.name}')
  String COLLECTION_SEARCH_INDEX

  @Value('${elasticsearch.index.staging.collection.name}')
  String COLLECTION_STAGING_INDEX

  @Value('${elasticsearch.index.search.granule.name}')
  String GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.staging.granule.name}')
  String GRANULE_STAGING_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.flattened-granule.name}')
  private String FLAT_GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.universal-type}')
  String TYPE

  @Value('${elasticsearch.index.prefix:}')
  String PREFIX

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
  public Map loadMetadata(Object[] documents) {
    esService.ensureStagingIndices()
    esService.ensurePipelines()
    esService.refreshAllIndices()
    def results = []
    def bulkRequest = new StringBuilder()
    def loadedIndices = []

    documents.eachWithIndex { document, i ->
      def filename
      if (document instanceof MultipartFile) {
        filename = document.originalFilename
        document = document.inputStream.text
      }
      else {
        filename = null
        document = document as String
      }
      try {
        def source = MetadataParser.parseXMLMetadataToMap(document)
        def type = source.parentIdentifier ? 'granule' : 'collection'
        def fileId = source.fileIdentifier as String
        def doi = source.doi as String
        source.stagedDate = System.currentTimeMillis()
        def existingRecord = findMetadata(fileId, doi, true)
        def existingIds = existingRecord.data*.id
        def esId = existingIds?.size() == 1 ? existingIds[0] : null
        def result = [
            id: esId,
            type: type,
            attributes: source,
            meta: [
                filename: filename,
            ]
        ]

        if (existingIds?.size() > 1) {
          result.meta.error = [
              status: HttpStatus.CONFLICT.value(),
              title: 'Ambiguous metadata records in existence; metadata not loaded.',
              detail: "Please GET records with ids [ ${existingIds.join(',')} ] and DELETE any " +
                  "erroneous records. Ambiguity because of fileIdentifier [ ${fileId} ] and/or DOI [ ${doi} ]."
          ]
        }
        else {
          def index = type == 'collection' ? PREFIX+COLLECTION_STAGING_INDEX : PREFIX+GRANULE_STAGING_INDEX
          def bulkCommand = [index: [_index: index, _type: TYPE, _id: esId]]
          bulkRequest << JsonOutput.toJson(bulkCommand)
          bulkRequest << '\n'
          bulkRequest << JsonOutput.toJson(source)
          bulkRequest << '\n'
          loadedIndices << i
        }
        results << result
      }
      catch(SAXException e) {
        results << [
            meta: [
                filename: filename,
                error: [
                    status: HttpStatus.BAD_REQUEST.value(),
                    title: 'Load request failed due to malformed XML.',
                    detail: ExceptionUtils.getRootCauseMessage(e)
                ]
            ]
        ]
      }
      catch (Exception e) {
        results << [
            meta: [
                filename: filename,
                error: [
                    status: HttpStatus.BAD_REQUEST.value(),
                    title: 'Load request failed due to malformed data.',
                    detail: ExceptionUtils.getRootCauseMessage(e)
                ]
            ]
        ]
      }
    }

    String bulkRequestBody =  bulkRequest.toString()
    if(bulkRequestBody) { // Don't send a request if there is nothing to send
      def bulkResponse = esService.performRequest('POST', '_bulk', bulkRequestBody)
      bulkResponse.items.eachWithIndex { result, i ->
        def resultRecord = results.get(loadedIndices[i])
        resultRecord.id = result.index._id
        resultRecord.meta.status = result.index.status
        resultRecord.meta.created = result.index.created
        if (result.error) {
          resultRecord.meta.error = result.error
        }
      }
    }

    return [data: results]
  }

  public Map loadMetadata(String document) {
    String[] documentArray = [document]
    def result = loadMetadata(documentArray).data[0]
    if (result.meta.error) {
      return [errors: [result.meta.error]]
    }
    else {
      return [data: result]
    }
  }

  public Map getMetadata(String esId, boolean idsOnly = false) {
    esService.refreshAllIndices()
    def resultsData = []
    [PREFIX+COLLECTION_STAGING_INDEX, PREFIX+GRANULE_STAGING_INDEX].each { index ->
      String endpoint = "${index}/${TYPE}/${esId}"
      if (idsOnly) {
        endpoint += '?_source=fileIdentifier,doi'
      }
      def response = esService.performRequest("GET", endpoint)

      if (response.found) {
        resultsData.add(
            [
                id        : response._id,
                type      : determineType(response._index),
                attributes: response._source
            ]
        )

      }
    }

    if(resultsData) {
      return [
          data: resultsData
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

  public Map findMetadata(String fileId, String doi, boolean idsOnly = false) {
    esService.refreshAllIndices()
    String endpoint = "${PREFIX}${COLLECTION_STAGING_INDEX},${PREFIX}${GRANULE_STAGING_INDEX}/_search"
    def searchParams = []
    if (fileId) { searchParams.add( [term: [fileIdentifier: fileId]] ) }
    if (doi) { searchParams.add( [term: [doi: doi]] ) }
    def requestBody = [
        query: [
            bool: [
                should: searchParams
            ]
        ],
        _source: idsOnly ? ['fileIdentifier', 'doi'] : true
    ]
    def response = esService.performRequest('GET', endpoint, requestBody)

    if (response.hits.total > 0) {
      def resources = response.hits.hits.collect {
        [
            id: it._id,
            type: determineType(it._index),
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
    def record = getMetadata(esId, true)
    if (record.data) { return delete(record, recursive) }
    else {
      // Record does not exist -- return NOT_FOUND response
      return record
    }
  }

  public Map deleteMetadata(String fileId, String doi, boolean recursive) {
    def record = findMetadata(fileId, doi, true)
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
    def endpoint = "${PREFIX}${COLLECTION_STAGING_INDEX},${PREFIX}${GRANULE_STAGING_INDEX},${PREFIX}${COLLECTION_SEARCH_INDEX},${PREFIX}${GRANULE_SEARCH_INDEX},${PREFIX}${FLAT_GRANULE_SEARCH_INDEX}/_delete_by_query?wait_for_completion=true"
    def deleteResponse = esService.performRequest('POST', endpoint, query)

    return [
        response: [
            data: toBeDeleted,
            meta: deleteResponse
        ],
        status: deleteResponse.failures ? HttpStatus.MULTI_STATUS.value() : HttpStatus.OK.value()
    ]
  }

  public String determineType(String index) {

    def parsedIndex = PREFIX ? index.replace(PREFIX, '') : index
    def endPosition = parsedIndex.lastIndexOf('-')
    parsedIndex = endPosition > 0 ? parsedIndex.substring(0, endPosition) : parsedIndex

    def indexToTypeMap = [
        (COLLECTION_SEARCH_INDEX)  : 'collection',
        (COLLECTION_STAGING_INDEX) : 'collection',
        (GRANULE_SEARCH_INDEX)     : 'granule',
        (GRANULE_STAGING_INDEX)    : 'granule',
        (FLAT_GRANULE_SEARCH_INDEX): 'flattenedGranule'
    ]

    return indexToTypeMap[parsedIndex]
  }
}
