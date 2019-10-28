package org.cedar.onestop.registry.service;

import groovy.util.logging.Slf4j;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.HostInfo;
import org.cedar.onestop.kafka.common.util.DataUtils;
import org.cedar.onestop.registry.stream.TopicInitializer;
import org.cedar.onestop.registry.stream.TopologyBuilders;
import org.cedar.schemas.avro.psi.Input;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.apache.kafka.streams.KafkaStreams.State.ERROR;
import static org.apache.kafka.streams.KafkaStreams.State.NOT_RUNNING;
import static org.apache.kafka.streams.StreamsConfig.*;
import static org.cedar.onestop.kafka.common.constants.StreamsApps.REGISTRY_ID;

@Slf4j
@Configuration
public class KafkaBeanConfig {

  private static final Map<String, Object> defaults = new LinkedHashMap<>();

  @Value("${publishing.interval.ms:300000}")
  private long publishInterval;

  @ConfigurationProperties(prefix = "kafka")
  @Bean
  Properties kafkaProps() {
    return new Properties();
  }

  @ConfigurationProperties(prefix = "topics")
  @Bean
  TopicsConfigurationProps topicsConfigurationProps(){return new TopicsConfigurationProps();}

  @Bean
  Properties streamsConfig(Map kafkaProps) {
    var validConfigNames = new HashSet<>(StreamsConfig.configDef().names());
    validConfigNames.addAll(ProducerConfig.configNames());
    validConfigNames.addAll(ConsumerConfig.configNames());
    var props = DataUtils.filterProperties(kafkaProps, validConfigNames);
    props.put(APPLICATION_ID_CONFIG, REGISTRY_ID);
    props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
    props.putAll(kafkaProps);
    return props;
  }

  @Bean
  HostInfo hostInfo(Properties streamsConfig){
    var appServerConfigParts = streamsConfig.getProperty(APPLICATION_SERVER_CONFIG).split(":");
    var host = appServerConfigParts[0];
    var port = Integer.parseInt(appServerConfigParts[1]);
    return new HostInfo(host, port);
  }

  @Bean(initMethod = "start", destroyMethod = "close")
  KafkaStreams streamsApp(Properties streamsConfig, TopicInitializer topicInitializer) throws InterruptedException, ExecutionException {
    topicInitializer.initialize();

    var killSwitch = new CompletableFuture<KafkaStreams.State>();
    killSwitch.thenAcceptAsync((state) -> {
      throw new IllegalStateException("KafkaStreams app entered bad state: " + state);
    });
    KafkaStreams.StateListener killSwitchListener = (newState, oldState) -> {
      if (!killSwitch.isDone() && (newState == ERROR || newState == NOT_RUNNING)) {
        killSwitch.complete(newState);
      }
    };

    var streamsTopology = TopologyBuilders.buildTopology(publishInterval);
    var app = new KafkaStreams(streamsTopology, streamsConfig);
    app.setStateListener(killSwitchListener);
    return app;
  }

  @Bean
  Properties adminConfig(Map kafkaProps) {
    return DataUtils.filterProperties(kafkaProps, AdminClientConfig.configNames());
  }

  @Bean(destroyMethod = "close")
  AdminClient adminClient(Properties adminConfig) {
    return AdminClient.create(adminConfig);
  }

  @Profile("!integration")
  @Bean(initMethod = "initialize")
  TopicInitializer topicInitializer(AdminClient adminClient) {
    return new TopicInitializer(adminClient, topicsConfigurationProps());
  }

  @Bean
  Properties producerConfig(Map kafkaProps) {
    var names = new HashSet<>(ProducerConfig.configNames());
    names.add(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG);
    return DataUtils.filterProperties(kafkaProps, names);
  }

  @Bean
  Producer<String, Input> kafkaProducer(Properties producerConfig) {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.CLIENT_ID_CONFIG, "api_publisher");
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SpecificAvroSerializer.class.getName());
    producerConfig.forEach( (k, v) -> {
      configProps.put((String) k, v);
    });
    return new KafkaProducer<>(configProps);
  }

}
