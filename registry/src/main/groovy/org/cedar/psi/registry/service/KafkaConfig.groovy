package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.cedar.psi.common.avro.Input
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Slf4j
@CompileStatic
@Configuration
class KafkaConfig {

  @Value('${kafka.bootstrap.servers}')
  private String bootstrapServers

  @Value('${schema.registry.url}')
  private String schemaRegistryUrl

  @Bean
  Producer<String, Input> kafkaProducer() {
    Map<String, Object> configProps = new HashMap<>()
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    configProps.put(ProducerConfig.CLIENT_ID_CONFIG, 'api_publisher')
    configProps.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SpecificAvroSerializer.class.getName())
    return new KafkaProducer<>(configProps)
  }

  @Bean(destroyMethod = 'close')
  AdminClient adminClient() {
    Map<String, Object> config = new HashMap<>()
    config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000)
    return AdminClient.create(config)
  }

}
