package org.cedar.onestop.api.metadata.service

import groovy.json.JsonBuilder
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
import org.xml.sax.SAXException
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

  Map loadParsedMetadata(List<Map> payload) {
    esService.ensureStagingIndices()
    esService.ensurePipelines()
    esService.refreshAllIndices()
    def bulkRequest = new StringBuilder()
    def loadedIndices = []
    def results = []
    
    payload.eachWithIndex { record, i ->
      String id = record.id
      ParsedRecord avroRecord = record.parsedRecord as ParsedRecord
  
      try {
        def source = InventoryManagerToOneStopUtil.reformatMessageForSearch(avroRecord, esService.version)
        def type = source.parentIdentifier ? ElasticsearchConfig.TYPE_GRANULE : ElasticsearchConfig.TYPE_COLLECTION
        source.stagedDate = System.currentTimeMillis()
        def result = [
            id        : id,
            type      : type,
            attributes: source,
        ]
      
        def index = type == ElasticsearchConfig.TYPE_COLLECTION ? esConfig.COLLECTION_STAGING_INDEX_ALIAS : esConfig.GRANULE_STAGING_INDEX_ALIAS
        def bulkCommand = [index: [_index: index, _type: esConfig.TYPE, _id: id]]
        bulkRequest << JsonOutput.toJson(bulkCommand)
        bulkRequest << '\n'
        bulkRequest << JsonOutput.toJson(source)
        bulkRequest << '\n'
        results << result
        loadedIndices << i
        
      } catch (Exception e) {
        log.error("Load request failed: ${e}", e)
      }
    }
  
    String bulkRequestBody = bulkRequest.toString()
    if (bulkRequestBody) { // Don't send a request if there is nothing to send
      def bulkResponse = esService.performRequest('POST', '_bulk', bulkRequestBody)
      log.debug("bulkResponse: ${bulkResponse} loadedIndices: ${loadedIndices} ")
    }
    
    return [data: results]
  }
  
  Map loadMetadata(Object[] documents) {
    esService.ensureStagingIndices()
    esService.ensurePipelines()
    esService.refreshAllIndices()
    List<Map> results = []
    def bulkRequest = new StringBuilder()
    Set<Integer> loadedIndices = []
    
    documents.eachWithIndex { document, i ->
      def filename
      if (document instanceof MultipartFile) {
        filename = document.originalFilename
        document = document.inputStream.text
      } else {
        filename = null
        document = document as String
      }
      try {
        def source = MetadataParser.parseXMLMetadataToMap(document)
        def type = source.parentIdentifier ? ElasticsearchConfig.TYPE_GRANULE : ElasticsearchConfig.TYPE_COLLECTION
        def fileId = source.fileIdentifier as String
        def doi = source.doi as String

        def result = [
            type      : type,
            attributes: source,
            meta      : [
                filename: filename,
            ]
        ]

        if (!fileId && !doi) { //do not allow records without an id
          result.meta.error = [
              status: HttpStatus.BAD_REQUEST.value(),
              title : 'Unable to parse FileIdentifier or DOI from document; metadata not loaded.',
              detail: "Please confirm the document contains valid identifiers."
          ]
        } else { //check for existing records
          source.stagedDate = System.currentTimeMillis()
          def existingRecord = findMetadata(fileId, doi, true)
          def existingIds = existingRecord.data*.id
          def esId = existingIds?.size() == 1 ? existingIds[0] : null
          result.id = esId as String
          if (existingIds?.size() > 1) { //if we matched more than one file, there is a conflict
            result.meta.error = [
                status: HttpStatus.CONFLICT.value(),
                title : 'Ambiguous metadata records in existence; metadata not loaded.',
                detail: "The identifiers in this document match more than one existing document. " +
                    "Please GET records with ids [ ${existingIds.join(',')} ] and DELETE any " +
                    "erroneous records. Ambiguity because of fileIdentifier [ ${fileId} ] and/or DOI [ ${doi} ]."
            ]
          } else {
            source.stagedDate = System.currentTimeMillis() //only set stagedDate if the data was staged
            result.attributes = source //update return payload
            def index = type == 'collection' ? PREFIX + COLLECTION_STAGING_INDEX : PREFIX + GRANULE_STAGING_INDEX
            def bulkCommand = [index: [_index: index, _type: TYPE, _id: esId]]
            bulkRequest << JsonOutput.toJson(bulkCommand)
            bulkRequest << '\n'
            bulkRequest << JsonOutput.toJson(source)
            bulkRequest << '\n'
            loadedIndices << i
          }
        }
        results << result
      }
      catch (SAXException e) {
        results << [
            meta: [
                filename: filename,
                error   : [
                    status: HttpStatus.BAD_REQUEST.value(),
                    title : 'Load request failed due to malformed XML.',
                    detail: ExceptionUtils.getRootCauseMessage(e)
                ]
            ]
        ]
      }
      catch (Exception e) {
        results << [
            meta: [
                filename: filename,
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
    
    return [data: results]
  }
  
  Map loadMetadata(String document) {
    String[] documentArray = [document]
    Map result = loadMetadata(documentArray).data[0]
    Map resultMeta = result.meta as Map
    if (resultMeta.error) {
      return [errors: [resultMeta.error]]
    } else {
      return [data: result]
    }
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
  
  Map deleteMetadata(String esId, boolean recursive) {
    Map record = getMetadata(esId, true)
    if (record.data) {
      return delete(record, recursive)
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
  
  private Map delete(Map record, boolean recursive) {
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
    def endpoint = "${esConfig.COLLECTION_STAGING_INDEX_ALIAS},${esConfig.GRANULE_STAGING_INDEX_ALIAS},${esConfig.COLLECTION_SEARCH_INDEX_ALIAS},${esConfig.GRANULE_SEARCH_INDEX_ALIAS},${esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS}/_delete_by_query?wait_for_completion=true"
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
