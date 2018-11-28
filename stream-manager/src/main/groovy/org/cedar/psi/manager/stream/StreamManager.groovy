package org.cedar.psi.manager.stream

import groovy.util.logging.Slf4j
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.ValueMapper
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.ParsedRecord
import org.cedar.psi.common.avro.RecordType
import org.cedar.psi.common.serde.JsonSerdes
import org.cedar.psi.common.util.AvroUtils
import org.cedar.psi.manager.config.ManagerConfig
import org.cedar.psi.manager.util.Analyzers
import org.cedar.psi.manager.util.RecordParser

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import static org.apache.kafka.streams.StreamsConfig.*
import static org.cedar.psi.common.constants.StreamsApps.MANAGER_ID
import static org.cedar.psi.common.constants.StreamsApps.REGISTRY_ID
import static org.cedar.psi.common.constants.Topics.*
import static org.cedar.psi.manager.util.RoutingUtils.requiresExtraction

@Slf4j
class StreamManager {

  static KafkaStreams buildStreamsApp(ManagerConfig config) {
    def topology = buildTopology()
    def streamsConfig = streamsConfig(MANAGER_ID, config)
    return new KafkaStreams(topology, streamsConfig)
  }

  static Topology buildTopology() {
    def builder = new StreamsBuilder()

    RecordType.values().each {
      addTopologyForType(builder, it)
    }

    return builder.build()
  }

  static StreamsBuilder addTopologyForType(StreamsBuilder builder, RecordType type) {
    def inputStream = builder.stream(inputChangelogTopics(REGISTRY_ID, type))
    def fromExtractorsStream = builder.stream(fromExtractorTopic(type), Consumed.with(Serdes.String(), JsonSerdes.Map()))

    inputStream
        .filter(requiresExtraction)
        .mapValues({ v -> v.content } as ValueMapper<Input, String>)
        .to(toExtractorTopic(type), Produced.with(Serdes.String(), Serdes.String()))

    inputStream
        .filterNot(requiresExtraction)
        .mapValues(AvroUtils.&avroToMap as ValueMapper<Input, Map>)
        .merge(fromExtractorsStream)
        .mapValues({ RecordParser.parse(it, type) } as ValueMapper<Map, ParsedRecord>)
        .mapValues(Analyzers.&addAnalysis as ValueMapper)
        .to(parsedTopic(type))

    return builder
  }

  static Properties streamsConfig(String appId, ManagerConfig config) {
    log.info "Building kafka streams appConfig for $appId"
    Properties streamsConfiguration = new Properties()
    streamsConfiguration.put(APPLICATION_ID_CONFIG, appId)
    streamsConfiguration.put(BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServers())
    streamsConfiguration.put(SCHEMA_REGISTRY_URL_CONFIG, config.schemaRegistryUrl())
    streamsConfiguration.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().class.name)
    streamsConfiguration.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.name)
    streamsConfiguration.put(COMMIT_INTERVAL_MS_CONFIG, 500)
    streamsConfiguration.put(AUTO_OFFSET_RESET_CONFIG, "earliest")
    return streamsConfiguration
  }
}
