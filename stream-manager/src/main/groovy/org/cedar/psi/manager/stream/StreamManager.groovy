package org.cedar.psi.manager.stream

import groovy.util.logging.Slf4j
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.ValueMapper
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.ParsedRecord
import org.cedar.psi.common.serde.JsonSerdes
import org.cedar.psi.common.util.AvroUtils
import org.cedar.psi.manager.config.ManagerConfig
import org.cedar.psi.manager.util.Analyzers

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import static org.apache.kafka.streams.StreamsConfig.*
import static org.cedar.psi.common.constants.StreamsApps.MANAGER_ID
import static org.cedar.psi.common.constants.StreamsApps.REGISTRY_ID
import static org.cedar.psi.common.constants.Topics.*
import static org.cedar.psi.manager.util.RoutingUtils.isDefault
import static org.cedar.psi.manager.util.RoutingUtils.hasErrors
import static org.cedar.psi.manager.util.RoutingUtils.isSME
import static org.cedar.psi.manager.util.RoutingUtils.not

@Slf4j
class StreamManager {

  static KafkaStreams buildStreamsApp(ManagerConfig config) {
    def topology = buildTopology()
    def streamsConfig = streamsConfig(MANAGER_ID, config)
    return new KafkaStreams(topology, streamsConfig)
  }

  static Topology buildTopology() {
    def builder = new StreamsBuilder()

    //-- granules

    // Stream incoming granules
    KStream<String, Input> granuleInputStream = builder.stream(inputChangelogTopics(REGISTRY_ID, 'granule'))

    // Split granules to those that need SME processing and those ready to parse
    KStream<String, Input>[] smeBranches = granuleInputStream.branch(isSME, not(isSME))
    KStream toSmeFunction = smeBranches[0]
    KStream toParsingFunction = smeBranches[1]

    // To SME functions:
    toSmeFunction
        .mapValues({ v -> v.content } as ValueMapper<Input, String>)
        .to(toExtractorTopic('granule'), Produced.with(Serdes.String(), Serdes.String()))

    // Merge straight-to-parsing stream with topic SME granules write to:
    KStream<String, Map> unparsedGranules = builder.stream(fromExtractorTopic('granule'), Consumed.with(Serdes.String(), JsonSerdes.Map()))
    KStream<String, ParsedRecord> parsedNotAnalyzedGranules = toParsingFunction
        .mapValues(AvroUtils.&avroToMap as ValueMapper<Input, Map>)
        .merge(unparsedGranules)
        .mapValues({ v -> MetadataParsingService.parseToInternalFormat(v) } as ValueMapper<Map, ParsedRecord>)

    // Short circuit records with errors back to registry
    KStream<String, ParsedRecord>[] parsedGranules = parsedNotAnalyzedGranules.branch(hasErrors, isDefault)
    KStream badParsedStream = parsedGranules[0]
    badParsedStream.to(parsedTopic('granule'))

    // Analyze and send final output to parsed topic
    KStream goodParsedStream = parsedGranules[1]
    goodParsedStream
        .mapValues(Analyzers.&addAnalysis as ValueMapper<ParsedRecord, ParsedRecord>)
        .to(parsedTopic('granule'))


    //-- collections

    // Stream incoming collections
    KStream<String, Input> collectionInputStream = builder.stream(inputChangelogTopics(REGISTRY_ID, 'collection'))

    // parsing collection:
    KStream<String, ParsedRecord> parsedNotAnalyzedCollection = collectionInputStream
        .mapValues(AvroUtils.&avroToMap as ValueMapper<Input, Map>)
        .mapValues({ v -> MetadataParsingService.parseToInternalFormat(v) } as ValueMapper<Map, ParsedRecord>)

    // Short circuit records with errors back to registry
    KStream<String, Map>[] parsedCollection = parsedNotAnalyzedCollection.branch(hasErrors, isDefault)
    KStream badParsedCollection = parsedCollection[0]
    badParsedCollection.to(parsedTopic('collection'))

    // TODO Create intermediary topic between parsing & analysis for KafkaStreams tasking
    //      parallelization, or at least compare with and without topic in load testing?

    // Analyze and send final output to parsed topic
    KStream goodParsedCollection = parsedCollection[1]
    goodParsedCollection
        .mapValues(Analyzers.&addAnalysis as ValueMapper<ParsedRecord, ParsedRecord>)
        .to(parsedTopic('collection'))

    return builder.build()
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
