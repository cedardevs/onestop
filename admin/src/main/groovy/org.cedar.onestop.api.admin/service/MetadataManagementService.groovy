package org.cedar.onestop.api.admin.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.schemas.avro.psi.ParsedRecord
import org.elasticsearch.Version
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import static org.cedar.onestop.elastic.common.DocumentUtil.*

@Slf4j
@Service
class MetadataManagementService {

  private ElasticsearchService esService
  private ElasticsearchConfig esConfig

  @Autowired
  MetadataManagementService(ElasticsearchService esService) {
    this.esService = esService
    this.esConfig = esService.esConfig
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

  Map loadXMLdocuments(List documents) {
    List<Map<String, ?>> parsedRecords = []
    List results = []
    documents.each { document ->
      String fileName = null
      String xml = null
      if (document instanceof MultipartFile) {
        fileName = document.originalFilename
        xml = document.inputStream.text
      }
      else {
        xml = document as String
      }
      Map result = [
          meta      : [
              filename: fileName,
          ]
      ]
      log.info("Loading XML document ${fileName ? fileName.toString() : '(no filename provided)'}")
      Map parseResult = Indexer.xmlToParsedRecord(xml)
      if(parseResult?.parsedRecord){
        Map validationResult = Indexer.validateMessage(fileName, parseResult.parsedRecord)
        if(!validationResult?.title){
          log.debug("Validation success: $fileName is valid")
          parsedRecords << [filename:fileName, parsedRecord: parseResult.parsedRecord as ParsedRecord]
        }
        else{
          log.warn("Validation failed: $fileName is invalid. Cause: ${validationResult.details} ")
          validationResult.status = HttpStatus.BAD_REQUEST.value()
          result.meta.error = validationResult
          results << result
        }
      }
      else {
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
    log.debug("Loading ${parsedRecords.size()} parsedRecords...")
    esService.ensureStagingIndices()
    esService.ensurePipelines()
    esService.refreshAllIndices()
    List<Map> results = []
    def bulkRequest = new StringBuilder()
    Set<Integer>  loadedIndices = []

    parsedRecords.eachWithIndex { record, i ->
      String id = record?.id ?: null
      String filename = record?.filename ?: null
      ParsedRecord avroRecord = record.parsedRecord

      try {
        log.debug("Reformating record for search with [id: $id, filename: $filename]")
        Map source = Indexer.reformatMessageForSearch(avroRecord)
        String fileId = source.fileIdentifier as String
        String doi = source.doi as String

        Map result = [
            type      : source.remove('type'), //have to remove for ES
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
          log.warn("Failed to load ${result.type} due to conflict with identifiers: ${result.meta}")
          result.meta.error = [
              status: HttpStatus.CONFLICT.value(),
              title : title,
              detail: detail
          ]
          results << result
          return
        }

        String esId = id ?: null //PSI is the source of truth, use the ID it gave us
        if(existingIds?.size() == 1){ //this is an update
          esId = updateRecord(existingIds[0], id, result.type)
          log.info("Updating ${result.type} document with ID: $esId")
        }else{
          log.info("Creating new ${result.type} staging document")
        }

        source.stagedDate = System.currentTimeMillis()
        result.id = esId as String
        def index = result.type == ElasticsearchConfig.TYPE_COLLECTION ? esConfig.COLLECTION_STAGING_INDEX_ALIAS : esConfig.GRANULE_STAGING_INDEX_ALIAS
        def bulkCommand = [index: [_index: index, _type: esConfig.TYPE, _id: esId]]
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
      Map bulkResponse = esService.performRequest('POST', '_bulk', bulkRequestBody)
      List<Map> items = bulkResponse.items as List<Map>
      items.eachWithIndex { result, i ->
        Map index = result.index as Map
        Map resultRecord = results.get(loadedIndices[i])
        Map resultRecordMeta = resultRecord.meta as Map
        resultRecord.id = index._id
        resultRecordMeta.status = index.status

        // ES6+ changed the item structure in a batch response
        // `index.result` <string> (e.g. 'created') exists in both versions,
        // but `index.created` <bool> is no longer there (presumably because it's redundant,
        // especially with the `index.status` <int> (e.g. 201)
        if(esConfig.version.onOrAfter(Version.V_6_0_0)) {
          resultRecordMeta.created = index.result == "created"
        }
        else {
          resultRecordMeta.created = index.created
        }

        if (result.error) {
          resultRecordMeta.error = result.error
        }
      }
    }
    log.debug("Update results: ${results}")
    return results
  }

  String updateRecord(String existingId, String newId = null, String type = null){ //records from API dont have an ID
    String esId = newId ?: existingId
    if(esId != existingId){ //the record from PSI was already in the index by another ID, this is the re-key
      log.warn ("Message with id [$newId] contains the same identifiers as an exsiting record [$existingId]. " +
          "Re-keying record from $existingId to $newId")
      boolean isCollectionRekey = type as String == ElasticsearchConfig.TYPE_COLLECTION as String ? true : false
      Map deleteResult = deleteMetadata(existingId, true, isCollectionRekey) //todo more error handling / returning info to user
    }else{
      log.info("Updating document with ID: $esId")
    }
    return esId
  }

  Map getMetadata(String esId, boolean idsOnly = false) {
    esService.refreshAllIndices()
    List<Map> resultsData = []
    [esConfig.COLLECTION_STAGING_INDEX_ALIAS, esConfig.GRANULE_STAGING_INDEX_ALIAS].each { alias ->
      String endpoint = "${alias}/${esConfig.TYPE}/${esId}"
      if (idsOnly) {
        endpoint += '?_source=fileIdentifier,doi'
      }
      def response = esService.performRequest("GET", endpoint)
      
      if (response.found) {
        resultsData.add(
            [
                id        : getId(response),
                type      : esConfig.typeFromIndex(getIndex(response)),
                attributes: getSource(response)
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
  
  Map findMetadata(String fileId, String doi, boolean idsOnly = false) {
    esService.refreshAllIndices()
    String endpoint = "${esConfig.COLLECTION_STAGING_INDEX_ALIAS},${esConfig.GRANULE_STAGING_INDEX_ALIAS}/_search"
    List<Map> searchParams = []
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
    Map response = esService.performRequest('GET', endpoint, requestBody)
    
    if (getHitsTotal(response) > 0) {
      List<Map> documents = getDocuments(response)
      List<Map> resources = documents.collect {
        [
            id        : getId(it),
            type      : esConfig.typeFromIndex(getIndex(it)),
            attributes: getSource(it)
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
  
  Map deleteMetadata(String esId, boolean recursive, boolean isCollectionRekey = false) {
    Map record = getMetadata(esId, true)
    if (record.data) {
      return delete(record, recursive, isCollectionRekey)
    } else {
      // Record does not exist -- return NOT_FOUND response
      return record
    }
  }
  
  Map deleteMetadata(String fileId, String doi, boolean recursive) {
    Map record = findMetadata(fileId, doi, true)
    if (record.data) {
      return delete(record, recursive)
    } else {
      // Record does not exist -- return NOT_FOUND response
      return record
    }
  }

  private Map delete(Map record, boolean recursive, boolean isCollectionRekey = false) {
    // collect up the ids, types, and potential granule parentIds to be deleted
    List<String> ids = []
    List<String> parentIds = []
    List<Map> toBeDeleted = []
    List<Map> data = record.data as List<Map>
    data.each {
      String id = it.id
      String type = it.type
      Map source = it.attributes as Map
      toBeDeleted << [id: id, type: type]
      ids << id
      if (recursive) {
        String fileIdentifier = getFileIdentifierFromSource(source)
        String doi = getDOIFromSource(source)
        if (fileIdentifier) {
          parentIds << fileIdentifier
        }
        if (doi) {
          parentIds << doi
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

    def endpoint = isCollectionRekey ?
        "${esConfig.COLLECTION_STAGING_INDEX_ALIAS},${esConfig.COLLECTION_SEARCH_INDEX_ALIAS},${esConfig.GRANULE_SEARCH_INDEX_ALIAS},${esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS}/_delete_by_query?wait_for_completion=true" :
        "${esConfig.COLLECTION_STAGING_INDEX_ALIAS},${esConfig.GRANULE_STAGING_INDEX_ALIAS},${esConfig.COLLECTION_SEARCH_INDEX_ALIAS},${esConfig.GRANULE_SEARCH_INDEX_ALIAS},${esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS}/_delete_by_query?wait_for_completion=true"

    def deleteResponse = esService.performRequest('POST', endpoint, query)
    
    return [
        response: [
            data: toBeDeleted,
            meta: deleteResponse
        ],
        status  : deleteResponse.failures ? HttpStatus.MULTI_STATUS.value() : HttpStatus.OK.value()
    ]
  }

}
