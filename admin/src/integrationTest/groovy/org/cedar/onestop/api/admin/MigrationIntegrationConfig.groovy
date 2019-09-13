package org.cedar.onestop.api.admin

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

  // 8082 is used for adjacent Spring integration tests (with separate contexts) which utilize schema registry
  // when the last test hasn't yet let go of port 8081, the tests can easily fail with a port in use error
  @Value('${schema-registry.testPort:8082}')
  int port

  @Value('${spring.embedded.zookeeper.connect:}')
  String zkConnect

  @Bean(initMethod = 'start')
  RestApp schemaRegistryRestApp() {
    new RestApp(port, zkConnect, '_schemas')
  }

}
