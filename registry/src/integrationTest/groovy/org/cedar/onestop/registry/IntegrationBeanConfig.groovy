package org.cedar.onestop.registry

import io.confluent.kafka.schemaregistry.RestApp
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.streams.KafkaStreams
import org.cedar.onestop.registry.stream.TopicInitializer
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

  @Bean
  Boolean waitForStreamsToRun(KafkaStreams streamsApp) {
    println "waiting for streams app to be running"
    def tries = 0
    while (streamsApp.state() != KafkaStreams.State.RUNNING) {
      if (tries >= 5) {
        throw new RuntimeException("Streams app still in bad state ${streamsApp.state()} after ${tries} seconds")
      }
      sleep(1000)
      tries += 1
    }
    return true
  }
}
