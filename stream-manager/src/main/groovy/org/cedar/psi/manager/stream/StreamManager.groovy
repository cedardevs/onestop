package org.cedar.psi.manager.stream

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Predicate
import org.apache.kafka.streams.kstream.ValueMapper
import org.cedar.psi.manager.config.Constants


@Slf4j
class StreamManager {

  static KafkaStreams buildStreamsApp(String bootstrapServers) {
    def topology = buildTopology()
    def streamsConfig = streamsConfig(Constants.APP_ID, bootstrapServers)
    return new KafkaStreams(topology, streamsConfig)
  }

  static Topology buildTopology() {
    def builder = new StreamsBuilder()

    // Send messages directly to parser or to topic for SME functions to process
    Predicate toSMETopic = { key, value ->
      return isForSME(value.toString(), Constants.SPLIT_FIELD, Constants.SPLIT_VALUES)
    }

    Predicate toParsing = { key, value ->
      return !isForSME(value.toString(), Constants.SPLIT_FIELD, Constants.SPLIT_VALUES)
    }

    //stream incoming granule and collection messages
    KStream granuleInputStream = builder.stream(Constants.RAW_GRANULES_TOPIC)
    KStream collectionInputStream = builder.stream(Constants.RAW_COLLECTIONS_TOPIC)

    //split script output into 2 streams, one for good and one for bad
    KStream[] smeBranches = granuleInputStream.branch(toParsing, toSMETopic)
    KStream toSmeFunction = smeBranches[1]
    KStream toParsingFunction = smeBranches[0]

    // To SME functions:
    toSmeFunction.to(Constants.SME_TOPIC)
    // Merge straight-to-parsing stream with topic SME granules write to:
    KStream unparsedGranules = builder.stream(Constants.UNPARSED_TOPIC)
    KStream parsedNotAnalyzedGranules = toParsingFunction.merge(unparsedGranules)
        .mapValues({ value ->
      return MetadataParsingService.parseToInternalFormat(value as String)
    } as ValueMapper<String, String>)

    // Branch again, sending errors to separate topic
    KStream[] parsedStreams = parsedNotAnalyzedGranules.branch(isValid, isNotValid)
    KStream goodParsedStream = parsedStreams[0]
    KStream badParsedStream = parsedStreams[1]
    //send the bad stream off to the error topic
    badParsedStream.to(Constants.ERROR_TOPIC)
    // TODO Create intermediary topic between parsing & analysis for KafkaStreams tasking
    //      parallelization, or at least compare with and without topic in load testing?

    // Send valid messages to analysis & send final output to topic
    goodParsedStream.mapValues({ value ->
      return AnalysisAndValidationService.analyzeParsedMetadata(value as String)
    } as ValueMapper<String, String>).to(Constants.PARSED_TOPIC)

    // parsing collection:
    KStream parsedNotAnalyzedCollection = collectionInputStream
        .mapValues({ value ->
      return MetadataParsingService.parseToInternalFormat(value as String)
    } as ValueMapper<String, String>)

    // Branch again, sending errors to separate topic
    KStream[] parsedCollection = parsedNotAnalyzedCollection.branch(isValid, isNotValid)
    KStream goodParsedCollection = parsedCollection[0]
    KStream badParsedCollection = parsedCollection[1]
    //send the bad stream off to the error topic
    badParsedCollection.to(Constants.ERROR_TOPIC)
    // TODO Create intermediary topic between parsing & analysis for KafkaStreams tasking
    //      parallelization, or at least compare with and without topic in load testing?

    // Send valid messages to analysis & send final output to topic
    goodParsedCollection.mapValues({ value ->
      return AnalysisAndValidationService.analyzeParsedMetadata(value as String)
    } as ValueMapper<String, String>).to(Constants.PARSED_COLLECTIONS_TOPIC)

    return builder.build()
  }

  static boolean isForSME(String value, String splitField, List<String> splitValues) {
    def msg = new JsonSlurper().parseText(value) as Map
    return splitValues.contains(msg[splitField])
  }

  static Predicate isValid = { k, v -> !new JsonSlurper().parseText(v as String).containsKey('error') }
  static Predicate isNotValid = { k, v -> new JsonSlurper().parseText(v as String).containsKey('error') }

  static Properties streamsConfig(String appId, String bootstrapServers) {
    log.info "Building kafka streams appConfig for $appId"
    Properties streamsConfiguration = new Properties()
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, appId)
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().class.name)
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().class.name)
    streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 500)
    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    return streamsConfiguration
  }
}
