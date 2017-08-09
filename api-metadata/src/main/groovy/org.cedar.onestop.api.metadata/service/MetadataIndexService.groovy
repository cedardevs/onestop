package org.cedar.onestop.api.metadata.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

import java.util.regex.Pattern

@Slf4j
@Service
class MetadataIndexService {

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.name}')
  String STAGING_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.name}')
  String SEARCH_INDEX

  @Value('${elasticsearch.index.staging.collectionType}')
  String COLLECTION_TYPE

  @Value('${elasticsearch.index.staging.granuleType}')
  String GRANULE_TYPE

  private RestClient restClient
  private IndexAdminService indexAdminService

  private String invalidFileIdPattern = /.*(?![-._:])\p{Punct}.*|.*\s.*/

  @Autowired
  public MetadataIndexService(RestClient restClient, IndexAdminService indexAdminService) {
    this.restClient = restClient
    this.indexAdminService = indexAdminService
  }

  public Map loadMetadata(MultipartFile[] documents) {

    indexAdminService.ensureStaging()

    def data = []

    def bulkRequest = adminClient.prepareBulk()
    def addRecordToBulk = { id, record, type ->
      def json = JsonOutput.toJson(record)
      def insertRequest = adminClient.prepareIndex(STAGING_INDEX, type, id).setSource(json, XContentType.JSON)
      bulkRequest.add(insertRequest)
    }

    def stagedDate = System.currentTimeMillis()
    documents.each { rawDoc ->
      def document = rawDoc.inputStream.text
      def storageInfo = MetadataParser.parseIdentifierInfo(document)
      def internalId = storageInfo.id
      def externalId = storageInfo.doi ?: storageInfo.id
      def type = storageInfo.parentId ? GRANULE_TYPE : COLLECTION_TYPE

      def dataRecord = [
          id        : externalId,
          type      : type,
          attributes: [
              filename: rawDoc.originalFilename
          ]
      ]

      if (Pattern.matches(invalidFileIdPattern, internalId)) {
        dataRecord.attributes.status = 400
        dataRecord.attributes.error = [
            title : 'Load request failed due to bad fileIdentifier value',
            detail: externalId
        ]
      }
      else {
        try {
          def source = MetadataParser.parseXMLMetadataToMap(document)
          source.stagedDate = stagedDate
          if (type == COLLECTION_TYPE) {
            source.isoXml = document
          }
          addRecordToBulk(internalId, source, type)
        }
        catch (Exception e) {
          dataRecord.attributes.status = 400
          dataRecord.attributes.error = [
              title : 'Load request failed due to malformed XML',
              detail: ExceptionUtils.getRootCauseMessage(e)
          ]
        }
      }
      data.add(dataRecord)
    }

    if (bulkRequest.numberOfActions() > 0) {
      def bulkResponses = bulkRequest.get().items
      bulkResponses.eachWithIndex { response, i ->
        if (response.isFailed()) {
          data[i].attributes.status = response.failure.status.status
          data[i].attributes.error = [
              title : 'Load request failed, elasticsearch rejected document',
              detail: response.failureMessage
          ]
        }
        else {
          data[i].attributes.status = response.status()
          data[i].attributes.created = response.status() == RestStatus.CREATED
        }
      }

      return [
          data: data
      ]
    }
  }

  public Map loadMetadata(String document) {

    indexAdminService.ensureStaging()

    def storageInfo = MetadataParser.parseIdentifierInfo(document)
    def internalId = storageInfo.id
    def externalId = storageInfo.doi ?: storageInfo.id
    if (Pattern.matches(invalidFileIdPattern, internalId)) {
      return [
          errors: [
              [status: 400, title: 'Bad Request', detail: 'Load request failed due to bad fileIdentifier value: ' + externalId]
          ]
      ]
    }
    else {
      def type = storageInfo.parentId ? GRANULE_TYPE : COLLECTION_TYPE
      def source = MetadataParser.parseXMLMetadataToMap(document)
      if (type == COLLECTION_TYPE) {
        source.isoXml = document
      }
      source.stagedDate = System.currentTimeMillis()
      source = JsonOutput.toJson(source)
      def response = adminClient.prepareIndex(STAGING_INDEX, type, internalId)
          .setSource(source, XContentType.JSON)
          .execute().actionGet()
      return [
          data: [
              id        : externalId,
              type      : type,
              attributes: [
                  created: response.status() == RestStatus.CREATED
              ]
          ]
      ]
    }
  }

  public Map getMetadata(String id) {
    // Synthesized granules do not exist in staging so this will only ever match a single record across both types
    String endPoint = "${STAGING_INDEX}/_all/${id}"
    def externalId = id.contains('doi:10.') ? id.replace('-', '/') : id
    def response = parseResponse(restClient.performRequest("GET", endPoint))

    if (response.found) {
      return [
          data: [
              id        : externalId,
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
          detail: "Metadata with ID ${externalId} does not exist" as String
      ]
    }
  }

  public Map deleteMetadata(String internalId, String type) {

    def externalId = internalId.contains('doi:10.') ? internalId.replace('-', '/') : internalId

    def bulkRequest = adminClient.prepareBulk()
    def data = [
        id        : externalId,
        type      : type,
        attributes: [
            successes: [],
            failures : []
        ]
    ]

    def removeCollectionGranules = false
    switch(type) {
      case COLLECTION_TYPE:
        removeCollectionGranules = true
      case GRANULE_TYPE:
        [STAGING_INDEX, SEARCH_INDEX].each { index ->
          bulkRequest.add(adminClient.prepareDelete(index, type, internalId))
        }
        break
      default:
        def docs = adminClient.prepareMultiGet()
          .add(SEARCH_INDEX, COLLECTION_TYPE, internalId)
          .add(SEARCH_INDEX, GRANULE_TYPE, internalId)
          .get().responses
        def foundDocs = docs.count { it.response.exists }
        if (foundDocs == 2) {
          if (docs.any { it.response.source.parentIdentifier == externalId }) {
            // No-granule collection
            data.type = COLLECTION_TYPE
            bulkRequest.add(adminClient.prepareDelete(STAGING_INDEX, COLLECTION_TYPE, internalId))
            [COLLECTION_TYPE, GRANULE_TYPE].each { t ->
              bulkRequest.add((adminClient.prepareDelete(SEARCH_INDEX, t, internalId)))
            }
          }
          else {
            // Collection & unrelated granule
            return [
              errors: [
                id    : externalId,
                status: HttpStatus.CONFLICT.value(),
                title : 'Ambiguous delete request received',
                detail: "Collection and granule metadata found with ID ${externalId}. Try request again with 'type' request param specified." as String
              ]
            ]
          }
        }

        else if (foundDocs == 1) {
          // Collection or granule
          if (docs[0].type == COLLECTION_TYPE) {
            removeCollectionGranules = true
          }
          data.type = docs[0].type
          [STAGING_INDEX, SEARCH_INDEX].each { index ->
            bulkRequest.add(adminClient.prepareDelete(index, docs[0].type, internalId))
          }
        }

        else if (!foundDocs) {
          return [
            errors: [
              id    : externalId,
              status: HttpStatus.NOT_FOUND.value(),
              title : 'No such document',
              detail: "Metadata with ID ${externalId} does not exist" as String
            ]
          ]
        }
        break
    }

    def bulkResponse = bulkRequest.execute().actionGet()
    bulkResponse.items.each { i ->
      if(i.failed) {
        data.attributes.failures.add([
            index : i.index.substring(0, i.index.indexOf('_')),
            type  : i.type,
            detail: i.failureMessage
        ])
      }
      else {
        data.attributes.successes.add([
            index : i.index.substring(0, i.index.indexOf('_')),
            type  : i.type,
            found : i.response.isFound()
        ])
      }
    }

    if (removeCollectionGranules) {
      // TODO: Only keeping the old code here to see what response used to include until we can determine a better uniform response
//      DeleteByQueryResponse deleteResponse = new DeleteByQueryRequestBuilder(adminClient, DeleteByQueryAction.INSTANCE)
//          .setIndices([STAGING_INDEX, SEARCH_INDEX])
//          .setTypes(GRANULE_TYPE)
//          .setQuery(QueryBuilders.termQuery('parentIdentifer', internalId))
//          .execute().actionGet()
//      data.attributes.stagingGranulesFound = deleteResponse.getIndex(STAGING_INDEX).found
//      data.attributes.stagingGranulesDeleted = deleteResponse.getIndex(STAGING_INDEX).deleted
//      data.attributes.searchGranulesFound = deleteResponse.getIndex(SEARCH_INDEX).found
//      data.attributes.searchGranulesDeleted = deleteResponse.getIndex(SEARCH_INDEX).deleted


      BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(adminClient)
          .abortOnVersionConflict(false)
          .filter(QueryBuilders.boolQuery()
          .must(QueryBuilders.typeQuery(GRANULE_TYPE))
          .must(QueryBuilders.termQuery('parentIdentifer', internalId)))
          .source(STAGING_INDEX, SEARCH_INDEX)
          .execute().actionGet()

      // TODO: This is unfortunately inaccurate while there are two indices but once a DB is in place, metadata CRUD will change here anyway
      data.attributes.totalGranulesDeleted = response.deleted
    }

    indexAdminService.refresh(STAGING_INDEX, SEARCH_INDEX)
    return data
  }

  private Map parseResponse(Response response) {
    Map result = [statusCode: response?.getStatusLine()?.getStatusCode() ?: 500]
    try {
      if (response?.getEntity()) {
        result += new JsonSlurper().parse(response?.getEntity()?.getContent()) as Map
      }
    }
    catch (e) {
      log.warn("Failed to parse elasticsearch response as json", e)
    }
    return result
  }


  ///////////////////////////////////////////////////////////////////////
  // FIXME -- Should these methods be consolidated to a separate service?
  void refresh() {
    indexAdminService.refresh(STAGING_INDEX)
  }

  void drop() {
    indexAdminService.drop(STAGING_INDEX)
  }

  public void recreate() {
    drop()
    indexAdminService.ensureStaging()
  }

}
