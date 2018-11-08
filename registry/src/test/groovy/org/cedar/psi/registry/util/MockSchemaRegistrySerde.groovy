package org.cedar.psi.registry.util

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde


/**
 * Extension of SpecificAvroSerde to support testing via TopologyTestDriver.
 * All instances will use a singleton mock schema registry.
 */
class MockSchemaRegistrySerde extends SpecificAvroSerde {

  @Singleton
  static class MockSchemaRegistryClientSingleton implements SchemaRegistryClient {
    @Delegate SchemaRegistryClient client = new MockSchemaRegistryClient()
  }

  MockSchemaRegistrySerde() {
    super(MockSchemaRegistryClientSingleton.instance)
    def serdeProps = [
        (AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG): "http://localhost:8081",
        (KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG): "true",
        (KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS)        : "true"
    ]
    configure(serdeProps, false)
  }

}
