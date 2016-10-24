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

  @Value('${elasticsearch.port}')
  Integer elasticPort

  @Value('${elasticsearch.host}')
  String elasticHost

  @Value('${ro.user:}')
  String roUser

  @Value('${ro.pass:}')
  String roPassword

  @Value('${rw.user:}')
  String rwUser

  @Value('${rw.pass:}')
  String rwPassword

  @Bean(destroyMethod = 'close')
  public Client searchClient() {
    def builder = TransportClient.builder()

    if (roUser && roPassword) {
      builder.addPlugin(ShieldPlugin)
      builder.settings(Settings.builder().put('shield.user', "${roUser}:${roPassword}"))
    }

    def client = builder.build()
    client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticHost), elasticPort))
  }

  @Bean(destroyMethod = 'close')
  public Client adminClient() {
    def builder = TransportClient.builder()
    builder.addPlugin(DeleteByQueryPlugin)

    if (rwUser && rwPassword) {
      builder.addPlugin(ShieldPlugin)
      builder.settings(Settings.builder().put('shield.user', "${rwUser}:${rwPassword}"))
    }

    def client = builder.build()
    client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticHost), elasticPort))
  }

}