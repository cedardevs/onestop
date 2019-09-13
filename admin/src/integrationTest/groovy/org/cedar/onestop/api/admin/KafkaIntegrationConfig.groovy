package org.cedar.onestop.api.admin

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import io.confluent.kafka.schemaregistry.RestApp
import org.springframework.beans.factory.annotation.Value

@Configuration
@Profile('kafka-ingest')
class KafkaIntegrationConfig {

  @Value('${schema-registry.testPort:8081}')
  int port

  @Value('${spring.embedded.zookeeper.connect:}')
  String zkConnect

  @Bean(initMethod = 'start')
  RestApp schemaRegistryRestApp() {
    new RestApp(port, zkConnect, '_schemas')
  }
}
