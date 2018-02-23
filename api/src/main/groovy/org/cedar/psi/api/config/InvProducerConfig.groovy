package org.cedar.psi.api.config

import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class InvProducerConfig {

    @Value('${kafka.bootstrap.servers}')
    private String bootstrapServer

    @Value('${kafka.schema.registry}')
    private String schemaUrl

    @Bean
    Producer<String, GenericRecord> createProducer() {
        Map<String, Object> configProps = new HashMap<>()
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, "granuleProducer")
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer)

        // Configure the KafkaAvroSerializer.
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer)

        // Schema Registry location.
        configProps.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaUrl)

        return new KafkaProducer<>(configProps)
    }
}
