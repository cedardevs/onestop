package ncei.onestop.api

import org.elasticsearch.cluster.metadata.IndexMetaData
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.util.concurrent.EsExecutors
import org.elasticsearch.node.Node
import org.elasticsearch.client.Client
import org.elasticsearch.node.NodeBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile


@Profile("integration")
@Configuration
class IntegrationTestConfig {

    // Config constants:
    static final String CLUSTER_NAME = 'integrationTest'
    static final String INDEX = "metadata_v1"
    static final String TYPE = "item"


    ////////////////////////////////////////////

    // Bean definitions:

    @Bean(name = "dataDirectory")
    public File dataDirectory() {
        def tmpDir = File.createTempDir()
        return tmpDir
    }

    @Bean(name = "node", destroyMethod = "close")
    @DependsOn("dataDirectory")
    public Node node() {
        Settings settings = Settings.builder()
                .put("path.home", dataDirectory().toString())
                .put("cluster.name", CLUSTER_NAME)
                .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
                .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0)
                .put(EsExecutors.PROCESSORS, 1)
                .build()

        def node = NodeBuilder.nodeBuilder().local(true).settings(settings).build()
        return node
    }


    @Bean(destroyMethod = "close")
    @DependsOn("node")
    public Client client() {
        def node = node()
        def client = node.client()
        node.start()

        // Initialize index:
        def cl = ClassLoader.systemClassLoader
        def indexSettings = cl.getResourceAsStream("config/index-settings.json").text
        client.admin().indices().prepareCreate(INDEX).setSettings(indexSettings).execute().actionGet()
        client.admin().cluster().prepareHealth(INDEX).setWaitForActiveShards(1).execute().actionGet()

        // Initialize mapping & load data:
        def mapping = cl.getResourceAsStream("config/item-mapping.json").text
        client.admin().indices().preparePutMapping(INDEX).setSource(mapping).setType(TYPE).execute().actionGet()

        return client
    }

}
