package org.cedar.onestop.api.metadata

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer

@EnableKafka
@Configuration
class KafkaConsumerConfig {
  
  @Value('${kafka.bootstrap.servers}')
  private String bootstrapServers
  
  @Bean
  Map<String, Object> consumerConfigs() {
    Map<String, Object> configProps = new HashMap<>()
    configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
    configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
    configProps.put(ConsumerConfig.CLIENT_ID_CONFIG, 'api-Consumer')
    configProps.put(ConsumerConfig.GROUP_ID_CONFIG, 'api-metadata')
    configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    
    return configProps
  }
  
  @Bean
  ConsumerFactory<String, String> consumerFactory() {
    return new DefaultKafkaConsumerFactory<>(consumerConfigs())
  }
  
  @Bean
  KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>()
    factory.setConsumerFactory(consumerFactory())
    return factory
  }
  
}