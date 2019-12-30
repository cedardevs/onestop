package org.cedar.onestop.registry;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.cedar.schemas.avro.psi.Input;
import org.cedar.schemas.avro.psi.Method;
import org.cedar.schemas.avro.psi.OperationType;
import org.cedar.schemas.avro.psi.RecordType;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

class KafkaSampleTest {
  // broker config values and topic name
  private final static String TOPIC_NAME = "test-topic";
  private final static String BOOTSTRAP_SERVERS = "http://localhost:9092";
  private final static String SCHEMA_REGISTRY_URL = "http://localhost:8081";
  private final static String CLIENT_ID = "test-client";

  // constants needed by the avro schema (input schema)
  private final static String SOURCE = "record-source";             // record original source
  private final static String CONTENT_TYPE = "application/json";    // or application/json

  public static void main(String[] args) {
    String key = "11111111-1111-1111-11111111" ;
    // Creating an empty Map
    Map<String, String> map = new HashMap<>();
    // Mapping string values to keys
    map.put("trackingId" , "tracking123");
    map.put("dataStream", "stream");
    map.put("fileSize" , "111");
    map.put("lastUpdated", "2018-01-25T18:33:28Z");

    // create producer
    Producer<String, Input> producer = createProducer();

    try {
      Input message = buildInputTopicMessage(map);
      var record = new ProducerRecord<>(TOPIC_NAME, key, message);
      producer.send(record).get();
    }
    catch (Exception e) {
      // failed to publish messages to kafka
      System.out.println("Exception handling code " + e.getMessage());
    }
    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Producer
    Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
  }
  // initializing kafka producer
  private static Producer<String, Input> createProducer() {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,                 BOOTSTRAP_SERVERS);
    props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
    props.put(ProducerConfig.CLIENT_ID_CONFIG,                         CLIENT_ID);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,              StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,            KafkaAvroSerializer.class.getName());

    return new KafkaProducer<>(props);
  }

  // avro data foramt
  private static Input buildInputTopicMessage(Map info) {
    Input.Builder builder = Input.newBuilder();
    builder.setType(RecordType.collection);
    builder.setMethod(Method.PUT);
    builder.setContent(String.valueOf(info));
    builder.setContentType(CONTENT_TYPE);
    builder.setSource(SOURCE);
    builder.setOperation(OperationType.NO_OP);

    return builder.build();
  }
}