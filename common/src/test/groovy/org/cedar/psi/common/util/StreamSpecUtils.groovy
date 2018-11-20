package org.cedar.psi.common.util

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TopologyTestDriver
import org.cedar.psi.common.serde.JsonSerdes


class StreamSpecUtils {

  static final STRING_SERIALIZER = Serdes.String().serializer()
  static final STRING_DESERIALIZER = Serdes.String().deserializer()
  static final JSON_SERIALIZER = JsonSerdes.Map().serializer()
  static final JSON_DESERIALIZER = JsonSerdes.Map().deserializer()

  static List<ProducerRecord> readAllOutput(
      TopologyTestDriver driver, String topic,
      Deserializer keyDeserializer = STRING_DESERIALIZER, Deserializer valueDeserializer = JSON_DESERIALIZER) {
    def curr
    def output = []
    while (curr = driver.readOutput(topic, keyDeserializer, valueDeserializer)) {
      output << curr
    }
    return output
  }

}
