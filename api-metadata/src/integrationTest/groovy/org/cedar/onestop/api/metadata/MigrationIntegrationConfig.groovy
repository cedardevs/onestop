package org.cedar.onestop.api.metadata

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import io.confluent.kafka.schemaregistry.RestApp
import org.springframework.beans.factory.annotation.Value

// NOTE: We need a different profile for this because KafkaIngestIntegrationSpec and
// MigrationIntegrationTest and cannot share a schemaRegistryRestApp
@Profile('migration-ingest')
@Configuration
class MigrationIntegrationConfig {

  @Value('${schema-registry.testPort:8082}')
  int port

  @Value('${spring.embedded.zookeeper.connect:}')
  String zkConnect

  @Bean(initMethod = 'start')
  RestApp schemaRegistryRestApp() {
    new RestApp(port, zkConnect, '_schemas')
  }

}
