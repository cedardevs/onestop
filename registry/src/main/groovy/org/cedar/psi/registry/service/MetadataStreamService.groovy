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
import org.apache.kafka.streams.kstream.KGroupedStream
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.ValueTransformerSupplier
import org.apache.kafka.streams.state.Stores
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

  static final String RAW_GRANULE_TOPIC = 'raw-granule-events'
  static final String RAW_COLLECTION_TOPIC = 'raw-collection-events'
  static final String PARSED_GRANULE_TOPIC = 'parsed-granules'
  static final String PARSED_COLLECTION_TOPIC = 'parsed-collections'
  static final String COMBINED_GRANULE_TOPIC = 'combined-granules'
  static final String COMBINED_COLLECTION_TOPIC = 'combined-collections'

  static final String RAW_GRANULE_STORE = 'raw-granules'
  static final String RAW_COLLECTION_STORE = 'raw-collections'
  static final String PARSED_GRANULE_STORE = 'parsed-granules'
  static final String PARSED_COLLECTION_STORE = 'parsed-collections'

  static final String GRANULE_PUBLISH_TIMES = 'granule-publish-times'
  static final String GRANULE_LOOKUP_VALUES = 'granule-lookup-values'
  static final String COLLECTION_PUBLISH_TIMES = 'collection-publish-times'
  static final String COLLECTION_LOOKUP_VALUES = 'collection-lookup-values'

  private final AdminClient adminClient
  private final long publishInterval
  private KafkaStreams streamsApp


  MetadataStreamService(@Autowired AdminClient adminClient, @Value('${publishing.interval.ms:300000}') long publishInterval) {
    this.adminClient = adminClient
    this.publishInterval = publishInterval
    declareTopics(adminClient)
    this.streamsApp = buildStreamsApp(adminClient, publishInterval)
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
      this.streamsApp = buildStreamsApp(this.adminClient, this.publishInterval)
      this.streamsApp.start()
    }
  }

  static int DEFAULT_NUM_PARTITIONS = 1
  static short DEFAULT_REPLICATION_FACTOR = 1
  static Map<String, Map> topicConfigs = [
      (RAW_GRANULE_TOPIC): null,
      (RAW_COLLECTION_TOPIC): null,
      (PARSED_GRANULE_TOPIC): null,
      (PARSED_COLLECTION_TOPIC): null,
      (COMBINED_GRANULE_TOPIC): null,
      (COMBINED_COLLECTION_TOPIC): null,
  ] as Map<String, Map>

  private static void declareTopics(AdminClient adminClient) {
    def currentTopics = adminClient.listTopics().names().get()
    def missingTopics = topicConfigs.findAll({ !currentTopics.contains(it.key) })
    def newTopics = missingTopics.collect { name, config ->
      return new NewTopic(name, DEFAULT_NUM_PARTITIONS, DEFAULT_REPLICATION_FACTOR).configs(config)
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

  static KafkaStreams buildStreamsApp(AdminClient adminClient, long publishInterval) {
    def kafkaNodes = adminClient.describeCluster().nodes().get()
    def bootstrapServers = kafkaNodes.take(3).collect({ it.host() + ':' + it.port() })

    def streamsConfig = new StreamsConfig([
        (StreamsConfig.APPLICATION_ID_CONFIG)           : APP_ID,
        (StreamsConfig.BOOTSTRAP_SERVERS_CONFIG)        : bootstrapServers.join(','),
        (StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
        (StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG): Serdes.String().class.name,
        (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)       : 'earliest'
    ])
    def streamsTopology = buildTopology(publishInterval)
    return new KafkaStreams(streamsTopology, streamsConfig)
  }

  static Topology buildTopology(long publishInterval) {
    def builder = new StreamsBuilder()

    KStream rawGranules = builder.stream(RAW_GRANULE_TOPIC)
    KGroupedStream groupedGranules = rawGranules.groupByKey()
    KTable rawGranuleTable = groupedGranules.reduce(StreamFunctions.mergeJsonStrings, Materialized.as(RAW_GRANULE_STORE))

    KStream rawCollections = builder.stream(RAW_COLLECTION_TOPIC)
    KGroupedStream groupedCollections = rawCollections.groupByKey()
    KTable rawCollectionTable = groupedCollections.reduce(StreamFunctions.mergeJsonStrings, Materialized.as(RAW_COLLECTION_STORE))

    KTable parsedGranuleTable = builder
        .stream(PARSED_GRANULE_TOPIC)
        .mapValues(StreamFunctions.parsedInfoNormalizer)
        .groupByKey()
        .reduce(StreamFunctions.identityReducer, Materialized.as(PARSED_GRANULE_STORE))
    KTable parsedCollectionTable = builder
        .stream(PARSED_COLLECTION_TOPIC)
        .mapValues(StreamFunctions.parsedInfoNormalizer)
        .groupByKey()
        .reduce(StreamFunctions.identityReducer, Materialized.as(PARSED_COLLECTION_STORE))

    builder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(GRANULE_PUBLISH_TIMES), Serdes.Long(), Serdes.String()).withLoggingEnabled([:]))
    builder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(COLLECTION_PUBLISH_TIMES), Serdes.Long(), Serdes.String()).withLoggingEnabled([:]))

    parsedGranuleTable
        .toStream()
        .transform({->
      new DelayedPublisherTransformer(GRANULE_PUBLISH_TIMES, PARSED_GRANULE_STORE, publishInterval)
    }, GRANULE_PUBLISH_TIMES, PARSED_GRANULE_STORE)
        .to(PARSED_GRANULE_TOPIC)
    parsedCollectionTable
        .toStream()
        .mapValues(StreamFunctions.parsedInfoNormalizer)
        .transform({->
      new DelayedPublisherTransformer(COLLECTION_PUBLISH_TIMES, PARSED_COLLECTION_STORE, publishInterval)
    }, COLLECTION_PUBLISH_TIMES, PARSED_COLLECTION_STORE)
        .to(PARSED_COLLECTION_TOPIC)

    rawGranuleTable
        .outerJoin(parsedGranuleTable, StreamFunctions.buildKeyedJsonJoiner('raw'))
        .toStream()
        .transformValues({-> new PublishingAwareTransformer()} as ValueTransformerSupplier)
        .to(COMBINED_GRANULE_TOPIC)
    rawCollectionTable
        .outerJoin(parsedCollectionTable, StreamFunctions.buildKeyedJsonJoiner('raw'))
        .toStream()
        .transformValues({-> new PublishingAwareTransformer()} as ValueTransformerSupplier)
        .to(COMBINED_COLLECTION_TOPIC)

    return builder.build()
  }

}
