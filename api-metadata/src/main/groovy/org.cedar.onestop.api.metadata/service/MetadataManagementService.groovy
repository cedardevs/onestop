package org.cedar.onestop.api.metadata.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.cedar.schemas.avro.psi.ParsedRecord

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile


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

  @Value('${migrationMode:false}')
  boolean MIGRATION_MODE

  private ElasticsearchService esService
  
  @Autowired
  public MetadataManagementService(ElasticsearchService esService) {
    this.esService = esService
  }
  
  /**
   * Output shape:
   *{*   "data": [
   *{*       "id": <>,
   *       "type": <>,
   *       "attributes": <parsed source>,
   *       "meta": {*         "filename": "<original filename>,
   *         "status": 200|201|400...
   *         "created": true|false,
   *         "error": null|{...}*}*},
   *     ...
   *   ]
   *}*
   * @param documents
   * @return
   */

  Map loadMetadata(String document) {
    List<String> documentArray = [document]
    def result = loadXMLdocuments(documentArray).data[0]
    if (result?.meta?.error) {
      return [errors: [result.meta.error]]
    } else {
      return [data: result]
    }
  }

  Map loadMetadata(Object[] documents){
    def result = loadXMLdocuments(documents as List).data[0]
    if (result?.meta?.error) {
      return [errors: [result.meta.error]]
    } else {
      return [data: result]
    }
  }

  Map loadXMLdocuments(List documents){
    List<Map<String, ?>> parsedRecords = []
    List results = []
    documents.each { document ->
      String fileName = null
      String xml = null
      if (document instanceof MultipartFile) {
        fileName = document.originalFilename
        xml = document.inputStream.text
      } else {
        xml = document as String
      }
      Map result = [
          meta      : [
              filename: fileName,
          ]
      ]
      log.info("Loading XML document $fileName")
      Map parseResult = InventoryManagerToOneStopUtil.xmlToParsedRecord(xml)
      if(parseResult?.parsedRecord){
        Map validationResult = InventoryManagerToOneStopUtil.validateMessage(fileName, parseResult.parsedRecord)
        if(!validationResult?.title){
          log.debug("Validation success: $fileName is valid")
          parsedRecords << [filename:fileName, parsedRecord: parseResult.parsedRecord as ParsedRecord]
        }else{
          log.warn("Validation failed: $fileName is invalid. Cause: ${validationResult.details} ")
          validationResult.status = HttpStatus.BAD_REQUEST.value()
          result.meta.error = validationResult
          results << result
        }
      }else {
        String title = "${parseResult?.error?.title ?: 'Parse error'}"
        String parseErrorDetails = "${parseResult?.error?.detail ?: "Unable to generate a ParsedRecord from xml "}"
        result.meta = [error: [title: title, detail: parseErrorDetails, status: HttpStatus.BAD_REQUEST.value()]]
        log.warn(parseErrorDetails)
        results << result
      }
    }
    return [data: results + loadParsedRecords(parsedRecords)]
  }

  List loadParsedRecords(List<Map<String, ?>> parsedRecords){
    esService.ensureStagingIndices()
    esService.ensurePipelines()
    esService.refreshAllIndices()
    def results = []
    def bulkRequest = new StringBuilder()
    def loadedIndices = []

    parsedRecords.eachWithIndex { record, i ->
      String id = record?.id ?: null
      String filename = record?.filename ?: null
      ParsedRecord avroRecord = record.parsedRecord

      try {
        log.debug("Reformating record for search with [id: $id, filename: $filename]")
        Map source = InventoryManagerToOneStopUtil.reformatMessageForSearch(avroRecord)
        String type = source.parentIdentifier ? 'granule' : 'collection'
        String fileId = source.fileIdentifier as String
        String doi = source.doi as String

        Map result = [
          type      : type,
          attributes: source,
          meta      : [
              id : id,
              filename: filename,
              fileId: fileId,
              doi: doi
          ]
        ]

        log.info("Loading ${result.type} record with identifiers: ${result.meta}")

        Map existingRecord = findMetadata(fileId, doi, true)
        List<String> existingIds = existingRecord.data*.id

        log.debug("Records with matching identifiers: ${existingIds}")

        if (existingIds?.size() > 1) { //if we matched more than one file, there is a conflict
          String title = 'Ambiguous metadata records in existence; metadata not loaded.'
          String detail = "The identifiers in this document match more than one existing document. " +
              "Please GET records with ids [ ${existingIds.join(',')} ] and DELETE any " +
              "erroneous records. Ambiguity because of fileIdentifier [ ${fileId} ] and/or DOI [ ${doi} ]."
          log.warn("Failed to load $type due to conflict with identifiers: ${result.meta}")
          result.meta.error = [
              status: HttpStatus.CONFLICT.value(),
              title : title,
              detail: detail
          ]
          results << result
          return
        }

        String esId = id ?: null
        if(existingIds?.size() == 1){ //this is an update
          String existingId = existingIds[0]
          esId = id ?: existingId //PSI is the source of truth, use the ID it gave us
          log.info("Updating ${type} document with ID: $esId")
          if(esId != existingId){ //the record from PSI was already in the index by another ID, this is the re-key
            log.warn ("Message with id [$id] contains the same identifiers as an exsiting record [$existingId]. " +
                "Re-keying record from $existingId to $id")
            Map deleteResult = deleteMetadata(existingId, true) //todo more error handling / returning info to user
          }else{
            log.info("Updating ${type} document with ID: $esId")
          }
        }else{
          log.info("Creating new staging document")
        }

        source.stagedDate = System.currentTimeMillis()
        result.id = esId as String
        def index = type == 'collection' ? PREFIX + COLLECTION_STAGING_INDEX : PREFIX + GRANULE_STAGING_INDEX
        def bulkCommand = [index: [_index: index, _type: TYPE, _id: esId]]
        bulkRequest << JsonOutput.toJson(bulkCommand)
        bulkRequest << '\n'
        bulkRequest << JsonOutput.toJson(source)
        bulkRequest << '\n'
        loadedIndices << i

        results << result
      }
      catch (Exception e) {
        results << [
            meta: [
                error   : [
                    status: HttpStatus.BAD_REQUEST.value(),
                    title : 'Load request failed due to malformed data.',
                    detail: ExceptionUtils.getRootCauseMessage(e)
                ]
            ]
        ]
      }
    }

    String bulkRequestBody = bulkRequest.toString()
    if (bulkRequestBody) { // Don't send a request if there is nothing to send
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
    log.debug("Update results: ${results}")
    return results
  }

  public Map getMetadata(String esId, boolean idsOnly = false) {
    esService.refreshAllIndices()
    def resultsData = []
    [PREFIX + COLLECTION_STAGING_INDEX, PREFIX + GRANULE_STAGING_INDEX].each { index ->
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
    
    if (resultsData) {
      return [
          data: resultsData
      ]
    } else {
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
    if (fileId) {
      searchParams.add([term: [fileIdentifier: fileId]])
    }
    if (doi) {
      searchParams.add([term: [doi: doi]])
    }
    def requestBody = [
        query  : [
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
            id        : it._id,
            type      : determineType(it._index),
            attributes: it._source
        ]
      }
      return [data: resources]
    } else {
      return [
          status: HttpStatus.NOT_FOUND.value(),
          title : 'No such document',
          detail: "Metadata with fileIdentifier [ ${fileId} ] and/or DOI [ ${doi} ] not found."
      ]
    }
  }
  
  public Map deleteMetadata(String esId, boolean recursive) {
    def record = getMetadata(esId, true)
    if (record.data) {
      return delete(record, recursive)
    } else {
      // Record does not exist -- return NOT_FOUND response
      return record
    }
  }
  
  public Map deleteMetadata(String fileId, String doi, boolean recursive) {
    def record = findMetadata(fileId, doi, true)
    if (record.data) {
      return delete(record, recursive)
    } else {
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
        status  : deleteResponse.failures ? HttpStatus.MULTI_STATUS.value() : HttpStatus.OK.value()
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
