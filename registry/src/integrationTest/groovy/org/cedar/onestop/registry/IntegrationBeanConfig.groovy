package org.cedar.onestop.registry

import io.confluent.kafka.schemaregistry.RestApp
import org.apache.kafka.clients.admin.AdminClient
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
  org.cedar.onestop.registry.stream.TopicInitializer topicInitializer(AdminClient adminClient) {
    new org.cedar.onestop.registry.stream.TopicInitializer(adminClient, 1, 1 as short)
  }
}
