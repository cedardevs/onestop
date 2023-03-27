package org.cedar.onestop.parsalyzer.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.cedar.onestop.data.util.MapUtils;
import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.kafka.common.conf.KafkaConfigNames;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.util.DataUtils;
import org.cedar.onestop.kafka.common.util.LogAndContinueExceptionHandler;
import org.cedar.onestop.kafka.common.util.IgnoreRecordTooLargeHandler;
import org.cedar.onestop.parsalyzer.util.RecordParser;
import org.cedar.onestop.parsalyzer.util.RoutingUtils;
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

public class StreamParsalyzer {
  private static final Logger log = LoggerFactory.getLogger(StreamParsalyzer.class);

  public static KafkaStreams buildStreamsApp(AppConfig config) {
    var topology = buildTopology();
    var streamsConfig = streamsConfig(StreamsApps.PARSALYZER_ID, config);
    var streamsApp = new KafkaStreams(topology, streamsConfig);
    return streamsApp;
  }

  static Topology buildTopology() {
    var builder = new StreamsBuilder();

    Arrays.stream(RecordType.values()).forEach((it) -> {
      addTopologyForType(builder, it);
    });

    return builder.build();
  }

  private static void addTopologyForType(StreamsBuilder builder, RecordType type) {
    var inputStream = builder.<String, AggregatedInput>stream(inputChangelogTopicCombined(StreamsApps.REGISTRY_ID, type));
    inputStream
        .filterNot(RoutingUtils::hasErrors)
        .filter(RoutingUtils::requiresExtraction)
        .mapValues(v -> AvroUtils.avroToMap(v))
        .mapValues(StreamParsalyzer::toJsonOrNull)
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
    var kafkaConfigs = MapUtils.trimMapKeys("kafka.", config.getCurrentConfigMap());
    var filteredConfigs = MapUtils.filterMapKeys(KafkaConfigNames.streams, kafkaConfigs);

    log.info("Building kafka streams appConfig for {}", appId);
    Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(APPLICATION_ID_CONFIG, appId);
    streamsConfiguration.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    streamsConfiguration.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
    streamsConfiguration.put(DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class.getName());
    streamsConfiguration.put(DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG, IgnoreRecordTooLargeHandler.class.getName());
    streamsConfiguration.putAll(filteredConfigs);
    return streamsConfiguration;
  }
}
