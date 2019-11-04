package org.cedar.onestop.api.admin

import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.cedar.onestop.kafka.common.util.DataUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.listener.BatchLoggingErrorHandler
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG

@EnableKafka
@Configuration
@Profile(["kafka-ingest"])
class KafkaConsumerConfig {

  /**
   * @deprecated Use kafka.schema.registry.url instead
   */
  @Deprecated
  @Value('${schema-registry.url:}')
  private String schemaRegistryUrl

  @ConfigurationProperties(prefix = "kafka")
  @Bean
  Properties kafkaProps() {
    return new Properties()
  }
  
  @Bean
  Properties consumerConfigs(Map kafkaProps) {
    def names = new HashSet<>(ProducerConfig.configNames())
    names.add(SCHEMA_REGISTRY_URL_CONFIG)
    def configProps = DataUtils.filterProperties(kafkaProps, names)
    configProps.put(ConsumerConfig.GROUP_ID_CONFIG, 'onestop-admin')
    configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
    configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SpecificAvroDeserializer.class)
    if (schemaRegistryUrl) {
      configProps.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
    }
    return configProps
  }

  @Bean
  ConsumerFactory<String, SpecificRecord> consumerFactory(Map consumerConfigs) {
    return new DefaultKafkaConsumerFactory<>(consumerConfigs)
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

  @Bean
  KafkaAdmin kafkaAdmin(Properties kafkaProps) {
    def adminProps = DataUtils.filterProperties(kafkaProps, AdminClientConfig.configNames())
    return new KafkaAdmin(adminProps as Map<String, Object>)
  }
  
}
