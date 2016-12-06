package ncei.onestop.api.service

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
    ensureStaging()
    ensureSearch()
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

  public void ensureStaging() {
    def storageExists = checkAliasExists(STAGING_INDEX)
    if (!storageExists) {
      def realName = create(STAGING_INDEX, [COLLECTION_TYPE, GRANULE_TYPE])
      adminClient.admin().indices().prepareAliases().addAlias(realName, STAGING_INDEX).execute().actionGet()
    }
  }

  public void ensureSearch() {
    def searchExists = checkAliasExists(SEARCH_INDEX)
    if (!searchExists) {
      def realName = create(SEARCH_INDEX, [GRANULE_TYPE, COLLECTION_TYPE])
      adminClient.admin().indices().prepareAliases().addAlias(realName, SEARCH_INDEX).execute().actionGet()
    }
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
