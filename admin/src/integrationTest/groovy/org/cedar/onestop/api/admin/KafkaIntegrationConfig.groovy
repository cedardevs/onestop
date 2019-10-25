package org.cedar.onestop.api.admin

import io.confluent.kafka.schemaregistry.RestApp
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile('kafka-ingest & integration')
class KafkaIntegrationConfig {

  @Value('${kafka.schema.registry.port:8081}')
  int port

  @Value('${spring.embedded.zookeeper.connect:}')
  String zkConnect

  @Bean(initMethod = 'start', destroyMethod = 'stop')
  RestApp schemaRegistryRestApp() {
    new RestApp(port, zkConnect, '_schemas')
  }

}
