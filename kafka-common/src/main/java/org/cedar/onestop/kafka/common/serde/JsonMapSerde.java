package org.cedar.onestop.kafka.common.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;


final class JsonMapSerde extends Serdes.WrapperSerde<Map<String, Object>> {
  JsonMapSerde() {
    super(new JsonMapSerializer(), new JsonMapDeserializer());
  }

  static final class JsonMapSerializer implements Serializer<Map<String, Object>> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
      // nothing to do
    }

    @Override
    public byte[] serialize(String topic, Map data) {
      try {
        return mapper.writeValueAsBytes(data);
      } catch (IOException e) {
        throw new RuntimeException("Failed to serialize Map to json", e);
      }
    }

    @Override
    public void close() {
      // nothing to do
    }
  }

  static final class JsonMapDeserializer implements Deserializer<Map<String, Object>> {
    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
      // nothing to do
    }

    @Override
    public Map deserialize(String topic, byte[] data) {
      try {
        return mapper.readValue(data, Map.class);
      } catch (IOException e) {
        throw new RuntimeException("Failed to deserialize Map to json", e);
      }
    }

    @Override
    public void close() {
      // nothing to do
    }
  }
}
