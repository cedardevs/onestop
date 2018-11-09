package org.cedar.psi.manager.stream

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Predicate
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.ValueMapper
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.serde.JsonSerdes
import org.cedar.psi.manager.config.ManagerConfig

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import static org.apache.kafka.streams.StreamsConfig.*
import static org.cedar.psi.common.constants.StreamsApps.MANAGER_ID
import static org.cedar.psi.common.constants.StreamsApps.REGISTRY_ID
import static org.cedar.psi.common.constants.Topics.*
import static org.cedar.psi.manager.util.RoutingUtils.getIsNotSME
import static org.cedar.psi.manager.util.RoutingUtils.getIsSME

@Slf4j
class StreamManager {

  static KafkaStreams buildStreamsApp(ManagerConfig config) {
    def topology = buildTopology()
    def streamsConfig = streamsConfig(MANAGER_ID, config)
    return new KafkaStreams(topology, streamsConfig)
  }

  static Topology buildTopology() {
    def builder = new StreamsBuilder()

    //stream incoming granule and collection messages
    KStream<String, Input> granuleInputStream = builder.stream(inputChangelogTopics(REGISTRY_ID, 'granule'))
    KStream<String, Input> collectionInputStream = builder.stream(inputChangelogTopics(REGISTRY_ID, 'collection'))

    // Split granules to those that need SME processing and those ready to parse
    KStream<String, Input>[] smeBranches = granuleInputStream.branch(isSME, isNotSME)
    KStream toSmeFunction = smeBranches[0]
    KStream toParsingFunction = smeBranches[1]

    // To SME functions:
    toSmeFunction
        .mapValues({ v -> v.content } as ValueMapper<Input, String>)
        .to(smeTopic('granule'), Produced.with(Serdes.String(), Serdes.String()))

    // Merge straight-to-parsing stream with topic SME granules write to:
    KStream<String, Map> unparsedGranules = builder.stream(unparsedTopic('granule'), Consumed.with(Serdes.String(), JsonSerdes.Map()))
    KStream<String, Map> parsedNotAnalyzedGranules = toParsingFunction
        .mapValues({ v -> new JsonSlurper().parseText(v.toString()) as Map } as ValueMapper<Input, Map>)
        .merge(unparsedGranules)
        .mapValues({ v -> MetadataParsingService.parseToInternalFormat(v) } as ValueMapper<Map, Map>)

    // Branch again, sending errors to separate topic
    KStream<String, Map>[] parsedStreams = parsedNotAnalyzedGranules.branch(isValid, isNotValid)
    KStream goodParsedStream = parsedStreams[0]
    KStream badParsedStream = parsedStreams[1]
    //send the bad stream off to the error topic
    badParsedStream.to(errorTopic(), Produced.with(Serdes.String(), JsonSerdes.Map()))
    // TODO Create intermediary topic between parsing & analysis for KafkaStreams tasking
    //      parallelization, or at least compare with and without topic in load testing?

    // Send valid messages to analysis & send final output to topic
    goodParsedStream
        .mapValues({ v -> AnalysisAndValidationService.analyzeParsedMetadata(v) } as ValueMapper<Map, Map>)
        .to(parsedTopic('granule'), Produced.with(Serdes.String(), JsonSerdes.Map()))

    // parsing collection:
    KStream<String, Map> parsedNotAnalyzedCollection = collectionInputStream
        .mapValues({ v -> MetadataParsingService.parseToInternalFormat(v.input) } as ValueMapper<Map, Map>)

    // Branch again, sending errors to separate topic
    KStream<String, Map>[] parsedCollection = parsedNotAnalyzedCollection.branch(isValid, isNotValid)
    KStream goodParsedCollection = parsedCollection[0]
    KStream badParsedCollection = parsedCollection[1]
    //send the bad stream off to the error topic
    badParsedCollection.to(errorTopic(), Produced.with(Serdes.String(), JsonSerdes.Map()))
    // TODO Create intermediary topic between parsing & analysis for KafkaStreams tasking
    //      parallelization, or at least compare with and without topic in load testing?

    // Send valid messages to analysis & send final output to topic
    goodParsedCollection
        .mapValues({ v -> AnalysisAndValidationService.analyzeParsedMetadata(v) } as ValueMapper<Map, Map>)
        .to(parsedTopic('collection'), Produced.with(Serdes.String(), JsonSerdes.Map()))

    return builder.build()
  }

  static Predicate<String, Map> isValid = { String k, Map v -> !v.containsKey('error') }
  static Predicate<String, Map> isNotValid = { String k, Map v -> v.containsKey('error') }

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
