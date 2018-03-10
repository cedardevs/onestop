package org.cedar.psi.registry

import groovy.util.logging.Slf4j
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.KafkaContainer

@Slf4j
@Profile('integration')
@TestConfiguration
class IntegrationTestConfig {

  @Bean(initMethod = 'start', destroyMethod = 'stop')
  KafkaContainer kafkaContainer() {
    new KafkaContainer()
  }

  @Bean // Override producer config bean to reference embedded broker
  Producer<String, String> kafkaProducer(KafkaContainer kafkaContainer) {
    Map<String, Object> configProps = new HashMap<>()
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
    configProps.put(ProducerConfig.CLIENT_ID_CONFIG, 'api_publisher')
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
    return new KafkaProducer<>(configProps)
  }

  @Bean(destroyMethod = 'close')
  AdminClient adminClient(KafkaContainer kafkaContainer) {
    Map<String, Object> config = new HashMap<>()
    config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
    config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000)
    return AdminClient.create(config)
  }

}
