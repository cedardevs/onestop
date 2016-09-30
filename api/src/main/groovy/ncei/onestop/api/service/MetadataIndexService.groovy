package ncei.onestop.api.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.elasticsearch.action.WriteConsistencyLevel
import org.elasticsearch.client.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import java.util.regex.Pattern

@Slf4j
@Service
class MetadataIndexService {

  @Value('${elasticsearch.index.staging.name}')
  String STAGING_INDEX

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
      if(type == COLLECTION_TYPE) {
        source.isoXml = document
      }
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
              id: response.id,
              type: response.type,
              attributes: [
                  source: response.source
              ]
          ]
      ]
    }
    else {
      return [
          status: 404,
          title: 'No such document',
          detail: "Metadata with ID ${fileIdentifier} does not exist" as String
      ]
    }
  }

  public Map deleteMetadata(String fileIdentifier) {
    // delete requires explicit type, so we have to try deleting from both types
    def responses = [COLLECTION_TYPE, GRANULE_TYPE].collect {
      client.prepareDelete(STAGING_INDEX, it, fileIdentifier)
          .setConsistencyLevel(WriteConsistencyLevel.QUORUM)
          .execute().actionGet()
    }
    def success = responses.find { it.found }
    if (success) {
      return [
          data: [
              id: success.id,
              type: success.type,
          ],
          meta: [
              deleted: true,
              message: "Deleted metadata with ID ${success.id}" as String
          ]
      ]
    }
    else {
      return [
          status: 404,
          title: 'No such document',
          detail: "Metadata with ID ${fileIdentifier} does not exist" as String
      ]
    }
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
