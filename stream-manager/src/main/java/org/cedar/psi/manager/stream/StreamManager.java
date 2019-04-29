package org.cedar.psi.manager.stream;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.cedar.psi.common.constants.StreamsApps;
import org.cedar.psi.common.serde.JsonSerdes;
import org.cedar.psi.manager.config.ManagerConfig;
import org.cedar.psi.manager.util.Analyzers;
import org.cedar.psi.manager.util.RecordParser;
import org.cedar.psi.manager.util.RoutingUtils;
import org.cedar.schemas.avro.psi.Input;
import org.cedar.schemas.avro.psi.RecordType;
import org.cedar.schemas.avro.util.AvroUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.*;
import static org.cedar.psi.common.constants.Topics.*;


public class StreamManager {
  private static final Logger log = LoggerFactory.getLogger(StreamManager.class);


  public static KafkaStreams buildStreamsApp(ManagerConfig config) {
    var topology = buildTopology();
    var streamsConfig = streamsConfig(StreamsApps.MANAGER_ID, config);
    return new KafkaStreams(topology, streamsConfig);
  }

  static Topology buildTopology() {
    var builder = new StreamsBuilder();

    Arrays.stream(RecordType.values()).forEach((it) -> {
      addTopologyForType(builder, it);
    });

    return builder.build();
  };

  static StreamsBuilder addTopologyForType(StreamsBuilder builder, RecordType type) {
    var inputStream = builder.<String, Input>stream(inputChangelogTopics(StreamsApps.REGISTRY_ID, type));
    var fromExtractorsStream = builder.<String, Map<String, Object>>stream(fromExtractorTopic(type), Consumed.with(Serdes.String(), JsonSerdes.Map()));

    inputStream
        .filter(RoutingUtils::requiresExtraction)
        .mapValues((v) -> v.getContent())
        .to(toExtractorTopic(type), Produced.with(Serdes.String(), Serdes.String()));

    inputStream
        .filterNot(RoutingUtils::requiresExtraction)
        .mapValues(it -> AvroUtils.avroToMap(it))
        .merge(fromExtractorsStream)
        .mapValues(it -> RecordParser.parse(it, type))
        .mapValues(Analyzers::addAnalysis)
        .to(parsedTopic(type));

    return builder;
  }

  static Properties streamsConfig(String appId, ManagerConfig config) {
    log.info("Building kafka streams appConfig for {}", appId);
    Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(APPLICATION_ID_CONFIG, appId);
    streamsConfiguration.put(BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServers());
    streamsConfiguration.put(SCHEMA_REGISTRY_URL_CONFIG, config.schemaRegistryUrl());
    streamsConfiguration.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    streamsConfiguration.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
    streamsConfiguration.put(COMMIT_INTERVAL_MS_CONFIG, 500);
    streamsConfiguration.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
    streamsConfiguration.put(TopicConfig.COMPRESSION_TYPE_CONFIG, config.compressionType());
    return streamsConfiguration;
  }
}
