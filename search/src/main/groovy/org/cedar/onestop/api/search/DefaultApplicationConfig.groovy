package org.cedar.onestop.api.search

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.http.HttpEntity
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.elasticsearch.Version
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment

@Slf4j
@Configuration
class DefaultApplicationConfig {

  @Value('${elasticsearch.port}')
  Integer elasticPort

  @Value('#{\'${elasticsearch.host}\'.split(\',\')}')
  List<String> elasticHost

  @Value('${elasticsearch.ssl.enabled:}')
  Boolean sslEnabled

  @Value('${elasticsearch.ro.user:}')
  String roUser

  @Value('${elasticsearch.ro.pass:}')
  String roPassword

  @Bean(name = 'elasticsearchVersion')
  @DependsOn('restClient')
  Version elasticsearchVersion(RestClient restClient) throws IOException {
    Request versionRequest = new Request('GET', '/')
    Response versionResponse = restClient.performRequest(versionRequest)
    HttpEntity responseEntity = versionResponse.entity
    InputStream responseContent = responseEntity.content
    Map content = new JsonSlurper().parse(responseContent) as Map
    Map versionInfo = content.version as Map
    String versionNumber = versionInfo.number as String
    final Version version = Version.fromString(versionNumber)
    if(version == null) {
      throw new RuntimeException("Elasticsearch version not found in the response")
    }
    return version
  }

  // we dont' want this bean to be created when the tests use test containers,
  // but in our CI environment, we have a separate Elasticsearch and so we can leverage this normal RestClient again
  @Profile(["!integration", "ci"])
  @Bean(name = 'restClient', destroyMethod = 'close')
  RestClient restClient() {
    def hosts = []
    elasticHost.each { host ->
      hosts.add(new HttpHost(host, elasticPort, sslEnabled ? 'https' : 'http'))
    }

    def builder = RestClient.builder(hosts as HttpHost[])
    builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
      @Override
      HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
        if (roUser && roPassword) {
          final credentials = new BasicCredentialsProvider()
          credentials.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(roUser, roPassword))
          httpClientBuilder = httpClientBuilder.setDefaultCredentialsProvider(credentials)
        }

        // causes the builder to take system properties into account when building the
        // default ssl context, e.g. javax.net.ssl.trustStore, etc.
        httpClientBuilder = httpClientBuilder.useSystemProperties()
        return httpClientBuilder
      }
    })

    return builder.build()
  }

  // default: null
  @Value('${elasticsearch.index.prefix:}')
  String PREFIX
  // default: 10
  @Value('${elasticsearch.max-tasks:10}')
  Integer MAX_TASKS
  // default: null
  @Value('${elasticsearch.requests-per-second:}')
  Integer REQUESTS_PER_SECOND
  // optional: feature toggled by 'sitemap' profile, default: empty
  @Value('${etl.sitemap.scroll-size:}')
  Integer SITEMAP_SCROLL_SIZE
  // optional: feature toggled by 'sitemap' profile, default: empty
  @Value('${etl.sitemap.collections-per-submap:}')
  Integer SITEMAP_COLLECTIONS_PER_SUBMAP

  @Autowired
  Environment environment

  @Bean
  ElasticsearchConfig elasticsearchConfig(Version elasticsearchVersion) {
    def sitemapEnabled = environment.activeProfiles.contains('sitemap')
    return new ElasticsearchConfig(PREFIX, MAX_TASKS, REQUESTS_PER_SECOND, SITEMAP_SCROLL_SIZE, SITEMAP_COLLECTIONS_PER_SUBMAP, sitemapEnabled, elasticsearchVersion)
  }
}
