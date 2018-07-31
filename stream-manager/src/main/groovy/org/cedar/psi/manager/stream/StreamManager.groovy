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
import org.cedar.psi.manager.config.AppConfig
import org.cedar.psi.manager.config.KafkaConfig

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Slf4j
@Service
class StreamManager {

  private AppConfig appConfig
  private KafkaConfig kafkaConfig
  private KafkaStreams streamsApp

  @Autowired
  StreamManager(AppConfig appConfig, KafkaConfig kafkaConfig) {
    this.appConfig = appConfig
    this.kafkaConfig = kafkaConfig
    streamsApp = buildStreamsApp(appConfig, kafkaConfig)
  }

  @PostConstruct
  void start() {
    this.streamsApp?.start()
  }

  @PreDestroy
  void stop() {
    this.streamsApp?.close()
  }

  static KafkaStreams buildStreamsApp(AppConfig appConfig, KafkaConfig kafkaConfig) {
    def topology = buildTopology(appConfig)
    def streamsConfig = streamsConfig(kafkaConfig.application.id, kafkaConfig.bootstrapServers)
    return new KafkaStreams(topology, streamsConfig)
  }

  static Topology buildTopology(AppConfig config) {
    def builder = new StreamsBuilder()

    // Send messages directly to parser or to topic for SME functions to process
    Predicate toSMETopic = { key, value ->
      return isForSME(value.toString(), config.splitField, config.splitValues)
    }

    Predicate toParsing = { key, value ->
      return !isForSME(value.toString(), config.splitField, config.splitValues)
    }

    KStream[] smeBranches = builder.stream(config.topics.rawGranules)
        .branch(toParsing, toSMETopic)

    // To SME functions:
    smeBranches[1].to(config.topics.smeGranules)

    // Straight to parsing:
    KStream parsedNotAnalyzedGranules = smeBranches[0].mapValues({ value ->
      return MetadataParsingService.parseToInternalFormat(value as String)
    } as ValueMapper<String, String>)

    // Branch again, sending errors to separate topic
    KStream[] parsedStreams = parsedNotAnalyzedGranules.branch(isValid, isNotValid)
    parsedStreams[1].to(config.topics.errorGranules)

    // Send valid messages to analysis & send final output to topic
    parsedStreams[0].mapValues({ value ->
      return AnalysisAndValidationService.analyzeParsedMetadata(value as String)
    } as ValueMapper<String, String>).to(config.topics.parsedGranules)

    return builder.build()
  }

  static boolean isForSME(String value, String splitField, List<String> splitValues) {
    def msg = new JsonSlurper().parseText(value) as Map
    return splitValues.contains(msg[splitField])
  }

  static Predicate isValid = {k, v -> !new JsonSlurper().parseText(v as String).containsKey('error') }
  static Predicate isNotValid = {k, v -> new JsonSlurper().parseText(v as String).containsKey('error') }

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
