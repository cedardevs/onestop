package org.cedar.onestop.api.search

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.Wait

@Profile("integration")
@TestConfiguration
class IntegrationTestConfig {

  @Value('${elasticsearch.version}')
  String esVersion

  @Bean(name = 'esContainer', initMethod = 'start', destroyMethod = 'stop')
  GenericContainer esContainer() {
    String dockerImageName = "docker.elastic.co/elasticsearch/elasticsearch:${esVersion}"
    GenericContainer esContainer = new GenericContainer(dockerImageName)
        .withExposedPorts(9200)
        .waitingFor(Wait.forHttp('/'))
        .withEnv("xpack.security.enabled", "false")
        .withEnv("transport.host", "127.0.0.1")
        .withEnv("http.host", "0.0.0.0")
        .withEnv("ES_JAVA_OPTS", "-Xms1g -Xmx1g")
    return esContainer
  }

  @Bean(destroyMethod = 'close')
  @DependsOn('esContainer')
  RestClient restClient() {
    def esContainer = esContainer()
    def restClient = RestClient.builder(
        new HttpHost(esContainer.getContainerIpAddress(), esContainer.getMappedPort(9200))
    ).build()
    return restClient
  }
}
