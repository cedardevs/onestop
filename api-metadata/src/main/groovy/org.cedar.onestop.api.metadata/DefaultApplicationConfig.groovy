package org.cedar.onestop.api.metadata

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

import java.security.KeyStore

@Configuration
@Profile("default")
class DefaultApplicationConfig {

  @Value('${elasticsearch.port}')
  Integer elasticPort

  @Value('#{\'${elasticsearch.host}\'.split(\',\')}')
  List<String> elasticHost

  @Value('${elasticsearch.ssl.keystore.path:}')
  String keystorePath

  @Value('${elasticsearch.ssl.keystore.password:}')
  String keystorePassword

  @Value('${elasticsearch.rw.user:}')
  String rwUser

  @Value('${elasticsearch.rw.pass:}')
  String rwPassword

  @Bean(destroyMethod = 'close')
  RestClient restClient() {

    def hosts = []
    elasticHost.each { host ->
      hosts.add(new HttpHost(host, elasticPort))
    }

    def builder = RestClient.builder(hosts as HttpHost[])

    if (keystorePath && keystorePassword) {
      // FIXME: Not sure what we need for prod security setup... this code is missing a setup SSLContext
//      KeyStore keystore = KeyStore.getInstance("jks")
//      try (InputStream is = Files.newInputStream(keystorePath)) {
//        keystore.load(is, keystorePassword.toCharArray())
//      }
//      builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
//        @Override
//        HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
//          return httpClientBuilder.setSSLContext(sslcontext)
//        }
//      })
    }

    if (rwUser && rwPassword) {
      final credentials = new BasicCredentialsProvider()
      credentials.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(rwUser, rwPassword))
      // FIXME: Below should be setup outside of SSL enabling & credentials, otherwise we're overriding params
      builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
        @Override
        HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
          return httpClientBuilder.setDefaultCredentialsProvider(credentials)
        }
      })
    }



    return builder.build()
  }
}
