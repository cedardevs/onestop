package ncei.onestop.api.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.elasticsearch.action.WriteConsistencyLevel
import org.elasticsearch.action.deletebyquery.DeleteByQueryAction
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

import javax.annotation.PostConstruct
import java.util.regex.Pattern

@Slf4j
@Service
class MetadataIndexService {

  @Value('${elasticsearch.index.staging.name}')
  String STAGING_INDEX

  @Value('${elasticsearch.index.search.name}')
  String SEARCH_INDEX

  @Value('${elasticsearch.index.staging.collectionType}')
  String COLLECTION_TYPE

  @Value('${elasticsearch.index.staging.granuleType}')
  String GRANULE_TYPE

  private Client client
  private IndexAdminService indexAdminService

  @Autowired
  public MetadataIndexService(Client client, IndexAdminService indexAdminService) {
    this.client = client
    this.indexAdminService = indexAdminService
  }

  public Map loadMetadata(MultipartFile[] documents) {

    def data = []

    def bulkRequest = client.prepareBulk()
    def addRecordToBulk = { record, type ->
      def id = record.fileIdentifier as String
      def json = JsonOutput.toJson(record)
      def insertRequest = client.prepareIndex(STAGING_INDEX, type, id).setSource(json)
      bulkRequest.add(insertRequest)
    }

    def stagedDate = System.currentTimeMillis()
    documents.each { rawDoc ->
      def document = rawDoc.inputStream.text
      def storageInfo = MetadataParser.parseIdentifierInfo(document)
      def id = storageInfo.id
      def type = storageInfo.parentId ? GRANULE_TYPE : COLLECTION_TYPE

      def dataRecord = [
          id        : id,
          type      : type,
          attributes: [
              filename: rawDoc.originalFilename
          ]
      ]

      if (Pattern.matches(/.*\s.*/, id)) {
        dataRecord.attributes.status = 400
        dataRecord.attributes.error = [
            title : 'Load request failed due to bad fileIdentifier value',
            detail: id
        ]
      }
      else {
        try {
          def source = MetadataParser.parseXMLMetadataToMap(document)
          source.stagedDate = stagedDate
          if (type == COLLECTION_TYPE) {
            source.isoXml = document
          }
          addRecordToBulk(source, type)
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
          data[i].attributes.status = response.response.isCreated() ? 201 : 200
          data[i].attributes.created = response.response.isCreated()
        }
      }

      return [
          data: data
      ]
    }
  }

  public Map loadMetadata(String document) {

    def storageInfo = MetadataParser.parseIdentifierInfo(document)
    def id = storageInfo.id
    if (Pattern.matches(/.*\s.*/, id)) {
      return [
          errors: [
              status: 400,
              title : 'Load request failed due to bad fileIdentifier value',
              detail: id
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
      def response = client.prepareIndex(STAGING_INDEX, type, id)
          .setSource(source)
          .setConsistencyLevel(WriteConsistencyLevel.QUORUM)
          .execute().actionGet()
      return [
          data: [
              id        : id,
              type      : type,
              attributes: [
                  created: response.created
              ]
          ]
      ]
    }
  }

  public Map getMetadata(String fileIdentifier) {
    def response = client.prepareGet(STAGING_INDEX, null, fileIdentifier).execute().actionGet()
    if (response.exists) {
      return [
          data: [
              id        : response.id,
              type      : response.type,
              attributes: [
                  source: response.source
              ]
          ]
      ]
    }
    else {
      return [
          status: HttpStatus.NOT_FOUND.value(),
          title : 'No such document',
          detail: "Metadata with ID ${fileIdentifier} does not exist" as String
      ]
    }
  }

  public Map deleteMetadata(String fileIdentifier, String type) {
    def bulkRequest = client.prepareBulk()
    def data = [
        id        : fileIdentifier,
        type      : type,
        attributes: [
            successes: [],
            failures : []
        ]
    ]

    def removeCollectionGranules = false
    if (type == GRANULE_TYPE) {
      [STAGING_INDEX, SEARCH_INDEX].each { index ->
        bulkRequest.add(client.prepareDelete(index, type, fileIdentifier))
      }
    }

    else if (type == COLLECTION_TYPE) {
      removeCollectionGranules = true
      [STAGING_INDEX, SEARCH_INDEX].each { index ->
        bulkRequest.add(client.prepareDelete(index, COLLECTION_TYPE, fileIdentifier))
      }
    }

    else {
      def docs = client.prepareMultiGet()
          .add(SEARCH_INDEX, COLLECTION_TYPE, fileIdentifier)
          .add(SEARCH_INDEX, GRANULE_TYPE, fileIdentifier)
          .get().responses
      def foundDocs = docs.count { it.response.exists }
      if (foundDocs == 2) {
        if (docs.any { it.response.source.parentIdentifier == fileIdentifier }) {
          // No-granule collection
          data.type = COLLECTION_TYPE
          bulkRequest.add(client.prepareDelete(STAGING_INDEX, COLLECTION_TYPE, fileIdentifier))
          [COLLECTION_TYPE, GRANULE_TYPE].each { t ->
            bulkRequest.add((client.prepareDelete(SEARCH_INDEX, t, fileIdentifier)))
          }
        }
        else {
          // Collection & unrelated granule
          return [
              errors: [
                  id    : fileIdentifier,
                  status: HttpStatus.CONFLICT.value(),
                  title : 'Ambiguous delete request received',
                  detail: "Collection and granule metadata found with ID ${fileIdentifier}. Try request again with 'type' request param specified." as String
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
          bulkRequest.add(client.prepareDelete(index, docs[0].type, fileIdentifier))
        }
      }

      else if (!foundDocs) {
        return [
            errors: [
                id    : fileIdentifier,
                status: HttpStatus.NOT_FOUND.value(),
                title : 'No such document',
                detail: "Metadata with ID ${fileIdentifier} does not exist" as String
            ]
        ]
      }
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
      DeleteByQueryResponse deleteResponse = new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE)
          .setIndices([STAGING_INDEX, SEARCH_INDEX])
          .setTypes(GRANULE_TYPE)
          .setQuery(QueryBuilders.termQuery('parentIdentifer', fileIdentifier))
          .execute().actionGet()
      data.attributes.stagingGranulesFound = deleteResponse.getIndex(STAGING_INDEX).found
      data.attributes.stagingGranulesDeleted = deleteResponse.getIndex(STAGING_INDEX).deleted
      data.attributes.searchGranulesFound = deleteResponse.getIndex(SEARCH_INDEX).found
      data.attributes.searchGranulesDeleted = deleteResponse.getIndex(SEARCH_INDEX).deleted
    }

    indexAdminService.refresh(STAGING_INDEX, SEARCH_INDEX)
    return data
  }

  void refresh() {
    indexAdminService.refresh(STAGING_INDEX)
  }

  void drop() {
    indexAdminService.drop(STAGING_INDEX)
  }

  @PostConstruct
  public void ensure() {
    def storageExists = client.admin().indices().prepareAliasesExist(STAGING_INDEX).execute().actionGet().exists
    if (!storageExists) {
      def realName = indexAdminService.create(STAGING_INDEX, [COLLECTION_TYPE, GRANULE_TYPE])
      client.admin().indices().prepareAliases().addAlias(realName, STAGING_INDEX).execute().actionGet()
    }
  }

  public void recreate() {
    drop()
    ensure()
  }

}
