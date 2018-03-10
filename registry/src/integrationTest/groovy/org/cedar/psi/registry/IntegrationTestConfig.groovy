package org.cedar.psi.registry

import groovy.util.logging.Slf4j
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.StreamsConfig
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

  @Bean // Override stream config bean to reference embedded broker
  StreamsConfig metadataConfig(KafkaContainer kafkaContainer) {
    return new StreamsConfig([
        (StreamsConfig.APPLICATION_ID_CONFIG)           : 'registry-integration-spec',
        (StreamsConfig.BOOTSTRAP_SERVERS_CONFIG)        : kafkaContainer.bootstrapServers,
        (StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
        (StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG): Serdes.String().class.name,
        (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)       : 'earliest',
        (ConsumerConfig.METADATA_MAX_AGE_CONFIG)        : 1000
    ])
  }

  @Bean
  AdminClient adminClient(KafkaContainer kafkaContainer) {
    return AdminClient.create([
        (ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG): kafkaContainer.bootstrapServers
    ])
  }

}
