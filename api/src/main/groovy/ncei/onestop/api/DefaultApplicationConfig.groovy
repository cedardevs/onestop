package ncei.onestop.api

import org.elasticsearch.common.settings.Settings
import org.elasticsearch.shield.ShieldPlugin
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.plugin.deletebyquery.DeleteByQueryPlugin
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
    def builder = TransportClient.builder()
    def settingsBuilder = Settings.builder()

    settingsBuilder.put('cluster.name', clusterName)

    if (roUser && roPassword) {
      builder.addPlugin(ShieldPlugin)
      settingsBuilder.put('shield.user', "${roUser}:${roPassword}")
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

    def client = builder.settings(settingsBuilder.build()).build()
    elasticHost.each { host ->
      client = client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), elasticPort))
    }
    return client
  }

  @Bean(destroyMethod = 'close')
  Client adminClient() {
    def builder = TransportClient.builder()
    def settingsBuilder = Settings.builder()

    builder.addPlugin(DeleteByQueryPlugin)
    settingsBuilder.put('cluster.name', clusterName)

    if (rwUser && rwPassword) {
      builder.addPlugin(ShieldPlugin)
      settingsBuilder.put('shield.user', "${rwUser}:${rwPassword}")
    }
    if (sslEnabled == 'true') {
      settingsBuilder.put('shield.transport.ssl', 'true')
    }
    if (keystorePath) {
      settingsBuilder.put('shield.ssl.keystore.path', keystorePath)
    }
    if (keystorePassword) {
      settingsBuilder.put('shield.ssl.keystore.password', keystorePassword)
    }

    def client = builder.settings(settingsBuilder.build()).build()
    elasticHost.each { host ->
      client = client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), elasticPort))
    }
    return client
  }


}
