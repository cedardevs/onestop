package ncei.onestop.api.etl.service

import groovy.util.logging.Slf4j
import org.elasticsearch.client.Client
import org.elasticsearch.index.IndexNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

@Slf4j
@Service
class IndexAdminService {

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.name}')
  String STAGING_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.name}')
  String SEARCH_INDEX

  @Value('${elasticsearch.index.staging.collectionType}')
  String COLLECTION_TYPE

  @Value('${elasticsearch.index.staging.granuleType}')
  String GRANULE_TYPE

  @Value('${elasticsearch.index.prefix:}')
  String PREFIX

  private Client adminClient

  @Autowired
  IndexAdminService(Client adminClient) {
    this.adminClient =  adminClient
  }

  @PostConstruct
  public void ensure() {
    ensure(STAGING_INDEX)
    ensure(SEARCH_INDEX)
  }

  public String create(String baseName, List typeNames) {
    def indexName = "${baseName}-${System.currentTimeMillis()}"

    // Initialize index:
    def cl = Thread.currentThread().contextClassLoader
    def indexSettings = cl.getResourceAsStream("config/${baseName - PREFIX}-settings.json").text
    adminClient.admin().indices().prepareCreate(indexName).setSettings(indexSettings).execute().actionGet()

    try {
      adminClient.admin().cluster().prepareHealth(indexName).setWaitForActiveShards(1).execute().actionGet()
    }
    catch (e) {
      log.warn('Unable to use cluster health api to wait for active shards', e)
    }

    // Initialize mappings:
    typeNames.each { type ->
      def mapping = cl.getResourceAsStream("config/${baseName - PREFIX}-mapping-${type}.json").text
      adminClient.admin().indices().preparePutMapping(indexName).setSource(mapping).setType(type).execute().actionGet()
    }

    log.debug "created new index [${indexName}]"
    return indexName
  }

  public void refresh(String... indices) {
    adminClient.admin().indices().prepareRefresh(indices).execute().actionGet()
  }

  public void drop(String indexName) {
    adminClient.admin().indices().prepareDelete(indexName).execute().actionGet()
    log.debug "dropped index [${indexName}]"
  }

  public void ensure(String indexName) {
    def exists = checkAliasExists(indexName)
    if (!exists) {
      def realName = create(indexName, [COLLECTION_TYPE, GRANULE_TYPE])
      adminClient.admin().indices().prepareAliases().addAlias(realName, indexName).execute().actionGet()
    }
  }

  public void recreate(String indexName) {
    drop(indexName)
    ensure(indexName)
  }

  private Boolean checkAliasExists(String name) {
    def result
    try {
      result = adminClient.admin().indices().prepareAliasesExist(name).execute().actionGet().exists
    }
    catch (IndexNotFoundException e) {
      result = false
    }
    return result
  }

}
