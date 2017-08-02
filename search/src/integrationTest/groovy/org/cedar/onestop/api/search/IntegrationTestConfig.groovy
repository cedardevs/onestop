package org.cedar.onestop.api.search

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile
import org.elasticsearch.node.Node
import org.elasticsearch.client.Client
import org.elasticsearch.cluster.metadata.IndexMetaData
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.util.concurrent.EsExecutors
import org.elasticsearch.env.Environment

@Profile("integration")
@TestConfiguration
class IntegrationTestConfig {

  // Config constants:
  static final String CLUSTER_NAME = 'integrationTest'

  @Bean(name = "dataDirectory")
  File dataDirectory() {
    def tmpDir = File.createTempDir()
    return tmpDir
  }

  @Bean(name = "node", destroyMethod = "close")
  @DependsOn("dataDirectory")
  Node node() {
    println 'creating test node'

    Settings settings = Settings.builder()
        .put("path.home", dataDirectory().toString())
        .put("cluster.name", CLUSTER_NAME)
        .put("discovery.zen.ping.multicast", false)
        .put("node.local", true)
        .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
        .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0)
        .put(EsExecutors.PROCESSORS_SETTING, 1)
        .build()

    def node = new Node(new Environment(settings))
    return node
  }

  @Bean(destroyMethod = "close")
  @DependsOn("node")
  Client client() {
    def node = node()
    def client = node.client()
    node.start()
    return client
  }

  @Bean(destroyMethod = 'close')
  RestClient restClient() {
    def restClient = RestClient.builder(
        new HttpHost("localhost", 9200)
    ).build()
    return restClient
  }
}
