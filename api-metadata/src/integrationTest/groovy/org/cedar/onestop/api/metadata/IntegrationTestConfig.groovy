package org.cedar.onestop.api.metadata

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile
import org.testcontainers.elasticsearch.ElasticsearchContainer

@Profile("integration")
@TestConfiguration
class IntegrationTestConfig {

  @Value('${elasticsearch.version}')
  String esVersion

  // This elasticsearch specific module from org.testcontainers is now available in their 1.10.0 version
  // but it must be brought in as a separate dependency org.testcontainers:elasticsearch:1.10.0
  // There are some differences from the `GenericContainer` we were previously using:
  // - longer startup timeout: 2m > 1m
  // - waits for an OK (200) or UNAUTHORIZED (401) response
  // - default elasticsearch port built in
  // - sets network alias
  // - sets single-node discovery type env var
  // https://github.com/testcontainers/testcontainers-java/blob/46b6865b258f67ec4a3f24a7d9bf0777d27e3329/docs/usage/elasticsearch_container.md
  @Bean(name = 'esContainer', initMethod = 'start', destroyMethod = 'stop')
  ElasticsearchContainer esContainer() {
    String dockerImageName = "docker.elastic.co/elasticsearch/elasticsearch:${esVersion}"
    ElasticsearchContainer container = new ElasticsearchContainer(dockerImageName)
    return container
  }

  @Bean(destroyMethod = 'close')
  @DependsOn('esContainer')
  RestClient restClient() {
    ElasticsearchContainer container = esContainer()

    // Not using `getHost()` convenience method due to issue I raised:
    // https://github.com/testcontainers/testcontainers-java/issues/962
    HttpHost host = new HttpHost(container.getContainerIpAddress(), container.getMappedPort(9200))

    // Using the default credentials in our ES version `5.5.3` should suffice for tests until a newer ES is used
    // They claimed newer versions removed this default password at ElasticCon
    // This gets us around having to set env var "xpack.security.enabled" to "false"
    // Some other things we were setting on the test container previously may not be necessary now:
    // - .withEnv("transport.host", "127.0.0.1")
    // - .withEnv("http.host", "0.0.0.0")
    // - .withEnv("ES_JAVA_OPTS", "-Xms1g -Xmx1g")
    // Whether we need to re-implement these env vars in the container bean is TBD based on continous testing
    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider()
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "changeme"))
    RestClientBuilder.HttpClientConfigCallback callback = new RestClientBuilder.HttpClientConfigCallback() {
      @Override
      HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
      }
    }

    RestClient restClient = RestClient.builder(host)
            .setHttpClientConfigCallback(callback)
            .build()
    return restClient
  }

}
