package ncei.onestop.api

import org.elasticsearch.common.settings.Settings
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.client.Client
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.springframework.context.annotation.Profile

@Configuration
@Profile("default")
class DefaultApplicationConfig {

  @Value('${elasticsearch.cluster.name}')
  String clusterName

  @Value('${elasticsearch.port}')
  Integer elasticPort

  @Value('#{\'${elasticsearch.host}\'.split(\',\')}')
  List<String> elasticHost

  @Value('${elasticsearch.ssl.enabled:}')
  Boolean sslEnabled

  @Value('${elasticsearch.ssl.keystore.path:}')
  String keystorePath

  @Value('${elasticsearch.ssl.keystore.password:}')
  String keystorePassword

  @Value('${elasticsearch.ro.user:}')
  String roUser

  @Value('${elasticsearch.ro.pass:}')
  String roPassword

  @Value('${elasticsearch.rw.user:}')
  String rwUser

  @Value('${elasticsearch.rw.pass:}')
  String rwPassword

  @Bean(destroyMethod = 'close')
  Client searchClient() {
    def settingsBuilder = Settings.builder()

    settingsBuilder.put('cluster.name', clusterName)

    if (roUser && roPassword) {
      // FIXME That's nice, you'd like security! What once was Shield is now something else though....
    }
    if (sslEnabled) {
      settingsBuilder.put('shield.transport.ssl', 'true')
    }
    if (keystorePath) {
      settingsBuilder.put('shield.ssl.keystore.path', keystorePath)
    }
    if (keystorePassword) {
      settingsBuilder.put('shield.ssl.keystore.password', keystorePassword)
    }

    def client = new PreBuiltTransportClient(settingsBuilder.build())
    elasticHost.each { host ->
      client = client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), elasticPort))
    }
    return client
  }

  @Bean(destroyMethod = 'close')
  Client adminClient() {

    def settingsBuilder = Settings.builder()

    settingsBuilder.put('cluster.name', clusterName)

    if (rwUser && rwPassword) {
      // FIXME That's nice, you'd like security! What once was Shield is now something else though....
    }
    if (sslEnabled) {
      settingsBuilder.put('shield.transport.ssl', 'true')
    }
    if (keystorePath) {
      settingsBuilder.put('shield.ssl.keystore.path', keystorePath)
    }
    if (keystorePassword) {
      settingsBuilder.put('shield.ssl.keystore.password', keystorePassword)
    }

    def client = new PreBuiltTransportClient(settingsBuilder.build())
    elasticHost.each { host ->
      client = client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), elasticPort))
    }
    return client
  }


}
