package org.cedar.psi.api.config

import groovy.transform.CompileStatic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
class KafkaConfig {

  @Value('${kafka.bootstrap.servers}')
  private String bootstrapServer

  @Bean
  Producer<String, String> kafkaProducer() {
    Map<String, Object> configProps = new HashMap<>()
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
    configProps.put(ProducerConfig.CLIENT_ID_CONFIG, 'api_publisher')
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
    return new KafkaProducer<>(configProps)
  }

}
