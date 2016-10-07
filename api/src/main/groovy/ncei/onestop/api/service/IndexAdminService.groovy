package ncei.onestop.api.service

import groovy.util.logging.Slf4j
import org.elasticsearch.client.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class IndexAdminService {

  private Client client

  @Autowired
  IndexAdminService(Client client) {
    this.client = client
  }

  public String create(String baseName, List typeNames) {
    def indexName = "${baseName}-${System.currentTimeMillis()}"

    // Initialize index:
    def cl = Thread.currentThread().contextClassLoader
    def indexSettings = cl.getResourceAsStream("config/${baseName}-settings.json").text
    client.admin().indices().prepareCreate(indexName).setSettings(indexSettings).execute().actionGet()
    client.admin().cluster().prepareHealth(indexName).setWaitForActiveShards(1).execute().actionGet()

    // Initialize mappings:
    typeNames.each { type ->
      def mapping = cl.getResourceAsStream("config/${baseName}-mapping-${type}.json").text
      client.admin().indices().preparePutMapping(indexName).setSource(mapping).setType(type).execute().actionGet()
    }

    log.debug "created new index [${indexName}]"
    return indexName
  }

  public void refresh(String... indices) {
    client.admin().indices().prepareRefresh(indices).execute().actionGet()
  }

  public void drop(String indexName) {
    client.admin().indices().prepareDelete(indexName).execute().actionGet()
    log.debug "dropped index [${indexName}]"
  }

}
