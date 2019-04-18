package org.cedar.onestop.api.metadata

import groovy.util.logging.Slf4j
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.client.config.RequestConfig
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Slf4j
@Configuration
@Profile("!integration")
class DefaultApplicationConfig {

  @Value('${elasticsearch.port}')
  Integer elasticPort

  @Value('#{\'${elasticsearch.host}\'.split(\',\')}')
  List<String> elasticHost

  @Value('${elasticsearch.ssl.enabled:}')
  Boolean sslEnabled

  @Value('${elasticsearch.rw.user:}')
  String rwUser

  @Value('${elasticsearch.rw.pass:}')
  String rwPassword

  @Bean(destroyMethod = 'close')
  RestClient restClient() {
    def hosts = []
    elasticHost.each { host ->
      hosts.add(new HttpHost(host, elasticPort, sslEnabled ? 'https' : 'http'))
    }

    RestClient restClient = RestClient.builder(hosts as HttpHost[])
        .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
          @Override
          RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
            // Set connect timeout to 1 minute and socket timeout to 5 minutes
            return requestConfigBuilder.setConnectTimeout(60000).setSocketTimeout(300000)
          }
        })
        .setMaxRetryTimeoutMillis(300000)
        .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
          @Override
          HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
            if (rwUser && rwPassword) {
              final credentials = new BasicCredentialsProvider()
              credentials.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(rwUser, rwPassword))
              httpClientBuilder = httpClientBuilder.setDefaultCredentialsProvider(credentials)
            }
            // causes the builder to take system properties into account when building the
            // default ssl context, e.g. javax.net.ssl.trustStore, etc.
            httpClientBuilder = httpClientBuilder.useSystemProperties()
            return httpClientBuilder
          }
        }).build()

    return restClient
  }
}
