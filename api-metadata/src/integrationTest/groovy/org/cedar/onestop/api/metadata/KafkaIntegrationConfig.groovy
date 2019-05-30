package org.cedar.onestop.api.metadata

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.beans.factory.annotation.Value
import io.confluent.kafka.schemaregistry.RestApp

@Configuration
@Profile('kafka-ingest')
class KafkaIntegrationConfig {
  @Value('${spring.embedded.zookeeper.connect:}')
  String zkConnect

  @Bean(initMethod = 'start')
  RestApp schemaRegistryRestApp() {
    new RestApp(8081, zkConnect, '_schemas')
  }
}