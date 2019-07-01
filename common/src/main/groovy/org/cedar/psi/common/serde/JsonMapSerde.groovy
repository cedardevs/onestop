package org.cedar.psi.common.serde

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer


final class JsonMapSerde extends Serdes.WrapperSerde<Map<String, Object>> {
  JsonMapSerde() {
    super(new JsonMapSerializer(), new JsonMapDeserializer())
  }

  static final class JsonMapSerializer implements Serializer<Map> {
    private boolean isKey
    private String charset

    @Override
    void configure(Map<String, ?> configs, boolean isKey) {
      this.isKey = isKey
      this.charset = configs?.charset
    }

    @Override
    byte[] serialize(String topic, Map data) {
      JsonOutput.toJson(data).getBytes(charset ?: System.getProperty('file.encoding', 'UTF-8'))
    }

    @Override
    void close() {
      // nothing to do
    }
  }

  static final class JsonMapDeserializer implements Deserializer<Map> {
    private boolean isKey
    private String charset

    @Override
    void configure(Map<String, ?> configs, boolean isKey) {
      this.isKey = isKey
      this.charset = configs?.charset
    }

    @Override
    Map deserialize(String topic, byte[] data) {
      new JsonSlurper().parse(data, charset ?: System.getProperty('file.encoding', 'UTF-8')) as Map
    }

    @Override
    void close() {
      // nothing to do
    }
  }
}
