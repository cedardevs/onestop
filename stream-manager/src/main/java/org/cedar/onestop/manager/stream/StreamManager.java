package org.cedar.onestop.manager.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.kafka.common.conf.KafkaConfigNames;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.util.DataUtils;
import org.cedar.onestop.kafka.common.util.KafkaHelpers;
import org.cedar.onestop.manager.util.RecordParser;
import org.cedar.onestop.manager.util.RoutingUtils;
import org.cedar.schemas.analyze.Analyzers;
import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.RecordType;
import org.cedar.schemas.avro.util.AvroUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.*;
import static org.cedar.onestop.kafka.common.constants.Topics.*;

public class StreamManager {
  private static final Logger log = LoggerFactory.getLogger(StreamManager.class);

  public static KafkaStreams buildStreamsApp(AppConfig config) {
    var topology = buildTopology();
    var streamsConfig = streamsConfig(StreamsApps.MANAGER_ID, config);
    return KafkaHelpers.buildStreamsAppWithKillSwitch(topology, streamsConfig);
  }

  static Topology buildTopology() {
    var builder = new StreamsBuilder();

    Arrays.stream(RecordType.values()).forEach((it) -> {
      addTopologyForType(builder, it);
    });

    return builder.build();
  }

  private static void addTopologyForType(StreamsBuilder builder, RecordType type) {
    var inputStream = builder.<String, AggregatedInput>stream(inputChangelogTopics(StreamsApps.REGISTRY_ID, type));

    inputStream
        .filterNot(RoutingUtils::hasErrors)
        .filter(RoutingUtils::requiresExtraction)
        .mapValues(v -> AvroUtils.avroToMap(v))
        .mapValues(StreamManager::toJsonOrNull)
        .filterNot(RoutingUtils::isNull)
        .to(toExtractorTopic(type), Produced.with(Serdes.String(), Serdes.String()));

    var fromExtractorsStream = builder.
        <String, String>stream(fromExtractorTopic(type), Consumed.with(Serdes.String(), Serdes.String()))
        .mapValues(v -> RecordParser.parseRaw(v, type));

    inputStream
        .filterNot(RoutingUtils::hasErrors)
        .mapValues(RecordParser::parseInput)
        .merge(fromExtractorsStream)
        .mapValues(Analyzers::addAnalysis)
        .to(parsedTopic(type));

  }

  private static String toJsonOrNull(Object o) {
    try {
      return new ObjectMapper().writeValueAsString(o);
    }
    catch (Exception e) {
      log.error("Exception while serializing " + o.getClass() + " to json", e);
      return null;
    }
  }

  static Properties streamsConfig(String appId, AppConfig config) {
    // Filter to only valid config values -- Streams config + possible internal Producer & Consumer config
    var kafkaConfigs = DataUtils.trimMapKeys("kafka.", config.getCurrentConfigMap());
    var filteredConfigs = DataUtils.filterMapKeys(KafkaConfigNames.streams, kafkaConfigs);

    log.info("Building kafka streams appConfig for {}", appId);
    Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(APPLICATION_ID_CONFIG, appId);
    streamsConfiguration.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    streamsConfiguration.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
    streamsConfiguration.putAll(filteredConfigs);
    return streamsConfiguration;
  }
}
