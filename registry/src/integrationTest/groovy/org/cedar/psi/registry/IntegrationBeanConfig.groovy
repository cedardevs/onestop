package org.cedar.psi.registry

import io.confluent.kafka.schemaregistry.RestApp
import org.apache.kafka.clients.admin.AdminClient
import org.cedar.psi.registry.stream.TopicInitializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class IntegrationBeanConfig {
  @Value('${spring.embedded.zookeeper.connect}')
  String zkConnect

  @Bean(initMethod = 'start', destroyMethod = 'stop')
  RestApp schemaRegistryRestApp() {
    new RestApp(8081, zkConnect, '_schemas')
  }

  @Bean
  TopicInitializer topicInitializer(AdminClient adminClient) {
    new TopicInitializer(adminClient, 1, 1 as short)
  }
}
