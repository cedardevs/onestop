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
import org.apache.kafka.streams.kstream.KGroupedStream
import org.apache.kafka.streams.kstream.KStream
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
      (Topics.RAW_GRANULE_TOPIC)        : null,
      (Topics.RAW_COLLECTION_TOPIC)     : null,
      (Topics.PARSED_GRANULE_TOPIC)     : null,
      (Topics.PARSED_COLLECTION_TOPIC)  : null,
      (Topics.COMBINED_GRANULE_TOPIC)   : null,
      (Topics.COMBINED_COLLECTION_TOPIC): null,
      (Topics.ERROR_HANDLER_TOPIC)      : null,
      (Topics.SME_GRANULE_TOPIC)        : null,
      (Topics.UNPARSED_GRANULE_TOPIC)   : null,
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

    KStream<String, Map> rawGranules = builder.stream(Topics.RAW_GRANULE_TOPIC)
    KGroupedStream groupedGranules = rawGranules.groupByKey()
    KTable rawGranuleTable = groupedGranules.reduce(StreamFunctions.mergeMaps, Materialized.as(Topics.RAW_GRANULE_STORE).withValueSerde(JsonSerdes.Map()))

    KStream<String, Map> rawCollections = builder.stream(Topics.RAW_COLLECTION_TOPIC)
    KGroupedStream groupedCollections = rawCollections.groupByKey()
    KTable rawCollectionTable = groupedCollections.reduce(StreamFunctions.mergeMaps, Materialized.as(Topics.RAW_COLLECTION_STORE).withValueSerde(JsonSerdes.Map()))

    KTable parsedGranuleTable = builder
        .stream(Topics.PARSED_GRANULE_TOPIC, Consumed.with(Serdes.String(), JsonSerdes.Map()))
        .mapValues(StreamFunctions.parsedInfoNormalizer)
        .groupByKey()
        .reduce(StreamFunctions.identityReducer, Materialized.as(Topics.PARSED_GRANULE_STORE).withValueSerde(JsonSerdes.Map()))
    KTable parsedCollectionTable = builder
        .stream(Topics.PARSED_COLLECTION_TOPIC, Consumed.with(Serdes.String(), JsonSerdes.Map()))
        .mapValues(StreamFunctions.parsedInfoNormalizer)
        .groupByKey()
        .reduce(StreamFunctions.identityReducer, Materialized.as(Topics.PARSED_COLLECTION_STORE).withValueSerde(JsonSerdes.Map()))

    builder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(Topics.GRANULE_PUBLISH_TIMES), Serdes.Long(), Serdes.String()).withLoggingEnabled([:]))
    builder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(Topics.GRANULE_PUBLISH_KEYS), Serdes.String(), Serdes.Long()).withLoggingEnabled([:]))
    builder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(Topics.COLLECTION_PUBLISH_TIMES), Serdes.Long(), Serdes.String()).withLoggingEnabled([:]))
    builder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(Topics.COLLECTION_PUBLISH_KEYS), Serdes.String(), Serdes.Long()).withLoggingEnabled([:]))

    def granulePublisher = new DelayedPublisherTransformer(Topics.GRANULE_PUBLISH_TIMES, Topics.GRANULE_PUBLISH_KEYS, Topics.PARSED_GRANULE_STORE, publishInterval)
    def collectionPublisher = new DelayedPublisherTransformer(Topics.COLLECTION_PUBLISH_TIMES,Topics. COLLECTION_PUBLISH_KEYS, Topics.PARSED_COLLECTION_STORE, publishInterval)

    parsedGranuleTable
        .toStream()
        .transform({ -> granulePublisher }, Topics.GRANULE_PUBLISH_TIMES, Topics.GRANULE_PUBLISH_KEYS, Topics.PARSED_GRANULE_STORE)
        .to(Topics.PARSED_GRANULE_TOPIC)
    parsedCollectionTable
        .toStream()
        .mapValues(StreamFunctions.parsedInfoNormalizer)
        .transform({ -> collectionPublisher }, Topics.COLLECTION_PUBLISH_TIMES, Topics.COLLECTION_PUBLISH_KEYS, Topics.PARSED_COLLECTION_STORE)
        .to(Topics.PARSED_COLLECTION_TOPIC)
    // TODO check with team if we need to create store for the following topics

    KTable<String, Map> errorHandlerTable = builder.table(
        Topics.ERROR_HANDLER_TOPIC,
        Consumed.with(Serdes.String(), JsonSerdes.Map()),
        Materialized.as(Topics.ERROR_HANDLER_STORE)
            .withLoggingEnabled([:])
            .withKeySerde(Serdes.String())
            .withValueSerde(JsonSerdes.Map())
    )

    rawGranuleTable
        .outerJoin(parsedGranuleTable, StreamFunctions.buildKeyedMapJoiner('raw'))
        .toStream()
        .transformValues({ -> new PublishingAwareTransformer() } as ValueTransformerSupplier)
        .to(Topics.COMBINED_GRANULE_TOPIC)
    rawCollectionTable
        .outerJoin(parsedCollectionTable, StreamFunctions.buildKeyedMapJoiner('raw'))
        .toStream()
        .transformValues({ -> new PublishingAwareTransformer() } as ValueTransformerSupplier)
        .to(Topics.COMBINED_COLLECTION_TOPIC)

    return builder.build()
  }

}
