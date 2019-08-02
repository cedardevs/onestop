package org.cedar.psi.registry.service;

import groovy.transform.CompileStatic;
import groovy.util.logging.Slf4j;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.cedar.psi.registry.stream.TopicInitializer;
import org.cedar.psi.registry.stream.TopologyBuilders;
import org.cedar.schemas.avro.psi.Input;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.*;
import java.util.stream.StreamSupport;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.*;
import static org.cedar.psi.common.constants.StreamsApps.REGISTRY_ID;

@Slf4j
@Configuration
public class KafkaBeanConfig {

  private static final Map<String, Object> defaults = new LinkedHashMap<>();

  static {
    defaults.put(BOOTSTRAP_SERVERS_CONFIG, "http://localhost:9092");
    defaults.put(SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
    defaults.put(TopicConfig.COMPRESSION_TYPE_CONFIG, "gzip");
    defaults.put(CACHE_MAX_BYTES_BUFFERING_CONFIG, 104857600L); // 100 MiB
    defaults.put(COMMIT_INTERVAL_MS_CONFIG, 30000L); // 30 sec
    defaults.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
    defaults.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
  }

  @Autowired
  Environment environment;

  @Value("${publishing.interval.ms:300000}")
  private long publishInterval;

  @ConfigurationProperties(prefix = "kafka")
  @Bean
  Properties kafkaProperties() {
    return new Properties();
  }

  @Bean
  Map<String, Object> kafkaProps(Properties kafkaProperties) {
    Map<String, Object> kafkaPropsMap = new LinkedHashMap<>();
    kafkaPropsMap.putAll(defaults);
    kafkaProperties.forEach((k, v) -> {
      kafkaPropsMap.put(k.toString(), v);
    });
    return kafkaPropsMap;
  }

  @Bean(destroyMethod = "close")
  AdminClient adminClient(Map kafkaProps) {
    Map<String, Object> config = new HashMap<>();
    config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProps.get(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG));
    config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaProps.get(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG));
    return AdminClient.create(config);
  }

  @Profile("!integration")
  @Bean(initMethod = "initialize")
  TopicInitializer topicInitializer(AdminClient adminClient) {
    return new TopicInitializer(adminClient);
  }

  @Bean
  Producer<String, Input> kafkaProducer(Map kafkaProps) {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.CLIENT_ID_CONFIG, "api_publisher");
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SpecificAvroSerializer.class.getName());
    kafkaProps.forEach((k, v) -> configProps.put((String) k, v) ); // fixme putall
    return new KafkaProducer<>(configProps);
  }

  @Bean
  StreamsConfig streamsConfig(Map kafkaProps) {
    var props = new HashMap<String, Object>();
    props.put(APPLICATION_ID_CONFIG, REGISTRY_ID);
    props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
    kafkaProps.forEach((k, v) -> props.put((String) k, v) ); //fixme putall

    return new StreamsConfig(props);
  }

  @Bean(destroyMethod = "close")
  KafkaStreams streamsApp(StreamsConfig streamsConfig, TopicInitializer topicInitializer) {
    var streamsTopology = TopologyBuilders.buildTopology(publishInterval);
    var app = new KafkaStreams(streamsTopology, streamsConfig);

    topicInitializer.initialize();
    app.start();

    return app;
  }

}
