package org.cedar.psi.common.util

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TopologyTestDriver

class StreamSpecUtils {

  static final STRING_SERIALIZER = Serdes.String().serializer()
  static final STRING_DESERIALIZER = Serdes.String().deserializer()
  static final JSON_SERIALIZER = new MockSchemaRegistrySerde().serializer()
  static final AVRO_DESERIALIZER =  new MockSchemaRegistrySerde().deserializer()

  static List<ProducerRecord> readAllOutput(
      TopologyTestDriver driver, String topic,
      Deserializer keyDeserializer = STRING_DESERIALIZER, Deserializer valueDeserializer = AVRO_DESERIALIZER) {
    def curr
    def output = []
    while (curr = driver.readOutput(topic, keyDeserializer, valueDeserializer)) {
      output << curr
    }
    return output
  }

}
