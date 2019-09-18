package org.cedar.onestop.api.admin

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.BatchLoggingErrorHandler
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer

@EnableKafka
@Configuration
@Profile(["kafka-ingest", 'migration-ingest'])
class KafkaConsumerConfig {
  
  @Value('${kafka.bootstrap.servers}')
  private String bootstrapServers
  
  @Value('${kafka.bootstrap.max_wait_ms}')
  private String maxWaitMs
  
  @Value('${kafka.bootstrap.max_poll_records}')
  private String maxPollRecords

  @Value('${schema-registry.url}')
  private String schemaRegistryUrl
  
  @Bean
  Map<String, Object> consumerConfigs() {
    Map<String, Object> configProps = new HashMap<>()
    configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    configProps.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
    configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
    configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SpecificAvroDeserializer.class)
    configProps.put(ConsumerConfig.CLIENT_ID_CONFIG, 'metadata-consumer')
    configProps.put(ConsumerConfig.GROUP_ID_CONFIG, 'onestop-admin')
    configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, maxWaitMs)
    configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords)
    
    return configProps
  }
  
  @Bean
  ConsumerFactory<String, SpecificRecord> consumerFactory() {
    return new DefaultKafkaConsumerFactory<>(consumerConfigs())
  }
  
  @Bean
  KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, SpecificRecord>> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, SpecificRecord> factory = new ConcurrentKafkaListenerContainerFactory<>()
    factory.setConsumerFactory(consumerFactory())
    //set batch listener to true
    factory.setBatchListener(true)
    factory.setBatchErrorHandler(new BatchLoggingErrorHandler())
    
    return factory
  }
  
}
