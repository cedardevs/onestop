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
@Profile("staging")
class ShieldApplicationConfig {

  @Value('${elasticsearch.port}')
  Integer elasticPort

  @Value('${elasticsearch.host}')
  String elasticHost

  @Value('${ro.user}')
  String roUser

  @Value('${ro.pass}')
  String roPassword

  @Value('${rw.user}')
  String rwUser

  @Value('${ro.pass}')
  String rwPassword

  @Bean(destroyMethod = 'close')
  public Client searchClient() {
    TransportClient.builder()
        .addPlugin(ShieldPlugin)
        .settings(Settings.builder()
          .put('shield.user', "${roUser}:${roPassword}"))
        .build()
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticHost), elasticPort))
  }

  @Bean(destroyMethod = 'close')
  public Client adminClient() {
    TransportClient.builder()
        .addPlugin(DeleteByQueryPlugin)
        .addPlugin(ShieldPlugin)
        .settings(Settings.builder()
        .put('shield.user', "${rwUser}:${rwPassword}"))
        .build()
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticHost), elasticPort))
  }
}