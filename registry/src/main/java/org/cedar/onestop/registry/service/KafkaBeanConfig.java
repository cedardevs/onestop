package org.cedar.onestop.registry.service;

import groovy.util.logging.Slf4j;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.HostInfo;
import org.cedar.onestop.kafka.common.conf.KafkaConfigNames;
import org.cedar.onestop.kafka.common.util.DataUtils;
import org.cedar.onestop.kafka.common.util.KafkaHelpers;
import org.cedar.onestop.registry.stream.TopicInitializer;
import org.cedar.onestop.registry.stream.TopologyBuilders;
import org.cedar.schemas.avro.psi.Input;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.apache.kafka.streams.StreamsConfig.*;
import static org.cedar.onestop.kafka.common.constants.StreamsApps.REGISTRY_ID;

@Slf4j
@Configuration
public class KafkaBeanConfig {

  private static final Map<String, Object> defaults = new LinkedHashMap<>();

  @Value("${publishing.interval.ms:300000}")
  private long publishInterval;

  @Value("${publishing.message.request.size:3000000}")
  private int MaxRequestSize;

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
    var props = DataUtils.filterProperties(kafkaProps, KafkaConfigNames.streams);
    props.put(APPLICATION_ID_CONFIG, REGISTRY_ID);
    props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
    props.put("max.request.size", MaxRequestSize);
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
  KafkaStreams streamsApp(
      Properties streamsConfig,
      TopicInitializer topicInitializer,
      CompletableFuture<Object> streamsErrorFuture,
      AdminClient adminClient) throws InterruptedException, ExecutionException
  {
    topicInitializer.initialize();
    var streamsTopology = TopologyBuilders.buildTopology(publishInterval, adminClient);
    var app = new KafkaStreams(streamsTopology, streamsConfig);
    KafkaHelpers.onError(app).thenAcceptAsync(o -> streamsErrorFuture.complete(0));
    return app;
  }

  @Bean
  CompletableFuture<Object> streamsErrorFuture() {
    return new CompletableFuture<>();
  }

  @Bean
  Properties adminConfig(Map kafkaProps) {
    return DataUtils.filterProperties(kafkaProps, KafkaConfigNames.admin);
  }

  @Bean(destroyMethod = "close")
  AdminClient adminClient(Properties adminConfig) {
    return AdminClient.create(adminConfig);
  }

  @Profile("!integration")
  @Bean
  TopicInitializer topicInitializer(AdminClient adminClient) {
    return new TopicInitializer(adminClient, topicsConfigurationProps());
  }

  @Bean
  Properties producerConfig(Map kafkaProps) {
    return DataUtils.filterProperties(kafkaProps, KafkaConfigNames.producer);
  }

  @Bean
  Producer<String, Input> kafkaProducer(Properties producerConfig) {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.CLIENT_ID_CONFIG, "api_publisher");
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SpecificAvroSerializer.class.getName());
    configProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, MaxRequestSize);
    producerConfig.forEach( (k, v) -> {
      configProps.put((String) k, v);
    });
    return new KafkaProducer<>(configProps);
  }

}
