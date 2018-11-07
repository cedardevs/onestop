package org.cedar.onestop.api.metadata


import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

import java.time.Duration

@Profile("integration")
@TestConfiguration
class IntegrationTestConfig {

  // the default startup timeout is 60s, but we tend to run into issues when resources are dragging
  // upping this to 120s as the first safeguard against failed integration tests
  static final long SECONDS_TO_WAIT_FOR_CONTAINER_TO_STARTUP = 120

  @Value('${elasticsearch.version}')
  String esVersion

  @Bean(name = 'esContainer', initMethod = 'start', destroyMethod = 'stop')
  GenericContainer esContainer() {
    String dockerImageName = "docker.elastic.co/elasticsearch/elasticsearch:${esVersion}"
    GenericContainer esContainer = new GenericContainer(dockerImageName)
        .withStartupTimeout(Duration.ofSeconds(SECONDS_TO_WAIT_FOR_CONTAINER_TO_STARTUP))
        .withExposedPorts(9200)
        // adding success status code to the root elasticsearch endpoint wait conditions
        .waitingFor(Wait.forHttp('/').forStatusCode(200))
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
