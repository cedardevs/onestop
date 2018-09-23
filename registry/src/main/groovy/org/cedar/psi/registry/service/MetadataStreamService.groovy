package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.ValueTransformerSupplier
import org.apache.kafka.streams.state.Stores
import org.cedar.psi.common.serde.JsonSerdes
import org.cedar.psi.common.constants.Topics
import org.cedar.psi.registry.stream.DelayedPublisherTransformer
import org.cedar.psi.registry.stream.PublishingAwareTransformer
import org.cedar.psi.registry.stream.StreamFunctions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Slf4j
@Service
@CompileStatic
class MetadataStreamService {

  static final String APP_ID = 'metadata-aggregator'

  private final AdminClient adminClient
  private final long publishInterval
  private final String stateDir
  private KafkaStreams streamsApp


  MetadataStreamService(
      @Autowired AdminClient adminClient,
      @Value('${publishing.interval.ms:300000}') long publishInterval,
      @Value('${state.dir:}') String stateDir) {
    this.adminClient = adminClient
    this.publishInterval = publishInterval
    this.stateDir = stateDir
    declareTopics(adminClient)
    this.streamsApp = buildStreamsApp(adminClient, publishInterval, stateDir)
  }

  @PostConstruct
  void start() {
    this.streamsApp?.start()
  }

  @PreDestroy
  void stop() {
    this.streamsApp?.close()
  }

  KafkaStreams getStreamsApp() {
    return this.streamsApp
  }

  synchronized void recreate() {
    if (this.streamsApp) {
      this.streamsApp.close()
      this.streamsApp.cleanUp()
      this.streamsApp = buildStreamsApp(this.adminClient, this.publishInterval, this.stateDir)
      this.streamsApp.start()
    }
  }

  static Map<String, Map> topicConfigs = [
      (Topics.inputTopic('granule'))      : null,
      (Topics.inputTopic('collection'))   : null,
      (Topics.parsedTopic('granule'))     : null,
      (Topics.parsedTopic('collection'))  : null,
      (Topics.combinedTopic('granule'))   : null,
      (Topics.combinedTopic('collection')): null,
      (Topics.errorTopic())               : null,
      (Topics.smeTopic('granule'))        : null,
      (Topics.unparsedTopic('granule'))   : null,
  ] as Map<String, Map>

  private static void declareTopics(AdminClient adminClient) {
    def currentTopics = adminClient.listTopics().names().get()
    def missingTopics = topicConfigs.findAll({ !currentTopics.contains(it.key) })
    def newTopics = missingTopics.collect { name, config ->
      return new NewTopic(name, Topics.DEFAULT_NUM_PARTITIONS, Topics.DEFAULT_REPLICATION_FACTOR).configs(config)
    }
    def result = adminClient.createTopics(newTopics)
    result.all().get()
  }

  private static Map createChangelogTopicConfig(Map additionalConfig = [:]) {
    def config = [
        (TopicConfig.CLEANUP_POLICY_CONFIG): TopicConfig.CLEANUP_POLICY_COMPACT,
        (TopicConfig.RETENTION_MS_CONFIG)  : '-1'
    ]
    return config + additionalConfig
  }

  static KafkaStreams buildStreamsApp(AdminClient adminClient, long publishInterval, String stateDir) {
    def kafkaNodes = adminClient.describeCluster().nodes().get()
    def bootstrapServers = kafkaNodes.take(3).collect({ it.host() + ':' + it.port() })

    def props = [
        (StreamsConfig.APPLICATION_ID_CONFIG)           : APP_ID,
        (StreamsConfig.BOOTSTRAP_SERVERS_CONFIG)        : bootstrapServers.join(','),
        (StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
        (StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG): JsonSerdes.Map().class.name,
        (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)       : 'earliest'
    ]
    if (stateDir) {
      props.put(StreamsConfig.STATE_DIR_CONFIG, stateDir)
    }

    def streamsConfig = new StreamsConfig(props)
    def streamsTopology = buildTopology(publishInterval)
    return new KafkaStreams(streamsTopology, streamsConfig)
  }

  static Topology buildTopology(long publishInterval) {
    def builder = new StreamsBuilder()

    Topics.inputTypes().each { type ->
      addTopologyForType(builder, type, publishInterval)
    }

    // TODO this table is unused, plus should we really store every error forever?
    KTable<String, Map> errorHandlerTable = builder.table(
        Topics.errorTopic(),
        Consumed.with(Serdes.String(), JsonSerdes.Map()),
        Materialized.as(Topics.errorStore())
            .withLoggingEnabled([:])
            .withKeySerde(Serdes.String())
            .withValueSerde(JsonSerdes.Map())
    )

    return builder.build()
  }

  static StreamsBuilder addTopologyForType(StreamsBuilder builder, String type, Long publishInterval = null) {
    // build input table
    KTable inputTable = builder
        .stream(Topics.inputTopic(type))
        .groupByKey()
        .reduce(StreamFunctions.mergeContentMaps, Materialized.as(Topics.inputStore(type)).withValueSerde(JsonSerdes.Map()))

    // build parsed table
    KTable parsedTable = builder
        .stream(Topics.parsedTopic(type), Consumed.with(Serdes.String(), JsonSerdes.Map()))
        .mapValues(StreamFunctions.parsedInfoNormalizer)
        .groupByKey()
        .reduce(StreamFunctions.identityReducer, Materialized.as(Topics.parsedStore(type)).withValueSerde(JsonSerdes.Map()))

    // add publisher
    if (publishInterval) {
      builder.addStateStore(Stores.keyValueStoreBuilder(
          Stores.persistentKeyValueStore(
              Topics.publishTimeStore(type)), Serdes.Long(), Serdes.String()).withLoggingEnabled([:]))
      builder.addStateStore(Stores.keyValueStoreBuilder(
          Stores.persistentKeyValueStore(
              Topics.publishKeyStore(type)), Serdes.String(), Serdes.Long()).withLoggingEnabled([:]))

      def granulePublisher = new DelayedPublisherTransformer(Topics.publishTimeStore(type), Topics.publishKeyStore(type), Topics.parsedStore(type), publishInterval)
      parsedTable
          .toStream()
          .transform({ -> granulePublisher }, Topics.publishTimeStore(type), Topics.publishKeyStore(type), Topics.parsedStore(type))
          .to(Topics.parsedTopic(type))
    }

    // build combined topic
    inputTable
        .outerJoin(parsedTable, StreamFunctions.buildKeyedMapJoiner('input'))
        .toStream()
        .transformValues({ -> new PublishingAwareTransformer() } as ValueTransformerSupplier)
        .to(Topics.combinedTopic(type))

    return builder
  }

}
