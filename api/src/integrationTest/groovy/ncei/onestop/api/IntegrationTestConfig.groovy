package ncei.onestop.api

import org.elasticsearch.Version
import org.elasticsearch.cluster.metadata.IndexMetaData
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.util.concurrent.EsExecutors
import org.elasticsearch.node.Node
import org.elasticsearch.client.Client
import org.elasticsearch.node.NodeBuilder
import org.elasticsearch.env.Environment
import org.elasticsearch.node.internal.InternalSettingsPreparer
import org.elasticsearch.plugins.Plugin
import org.elasticsearch.script.expression.ExpressionPlugin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile


@Profile("integration")
@Configuration
class IntegrationTestConfig {

  // Config constants:
  static final String CLUSTER_NAME = 'integrationTest'

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
        .put("discovery.zen.ping.multicast", false)
        .put("node.local", true)
        .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
        .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0)
        .put(EsExecutors.PROCESSORS, 1)
        .build()
    Collection<Class<? extends Plugin>> classpathPlugins = Collections.singletonList(ExpressionPlugin.class)

    def node = new IntegrationTestNode(InternalSettingsPreparer.prepareEnvironment(settings, null),
        Version.CURRENT, classpathPlugins)
    return node
  }


  @Bean(destroyMethod = "close")
  @DependsOn("node")
  public Client client() {
    def node = node()
    def client = node.client()
    node.start()
    return client
  }

}
