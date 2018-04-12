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
import org.cedar.psi.registry.stream.StreamFunctions
import org.springframework.beans.factory.annotation.Autowired
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

  private final AdminClient adminClient
  private KafkaStreams streamsApp

  MetadataStreamService(@Autowired AdminClient adminClient) {
    this.adminClient = adminClient
    declareTopics(adminClient)
    this.streamsApp = buildStreamsApp(adminClient)
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
      this.streamsApp = buildStreamsApp(this.adminClient)
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

  private static KafkaStreams buildStreamsApp(AdminClient adminClient) {
    def kafkaNodes = adminClient.describeCluster().nodes().get()
    def bootstrapServers = kafkaNodes.take(3).collect({ it.host() + ':' + it.port() })

    def streamsConfig = new StreamsConfig([
        (StreamsConfig.APPLICATION_ID_CONFIG)           : APP_ID,
        (StreamsConfig.BOOTSTRAP_SERVERS_CONFIG)        : bootstrapServers.join(','),
        (StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
        (StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG): Serdes.String().class.name,
        (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)       : 'earliest'
    ])
    def streamsTopology = buildTopology()
    return new KafkaStreams(streamsTopology, streamsConfig)
  }

  private static Topology buildTopology() {
    def builder = new StreamsBuilder()

    KStream rawGranules = builder.stream(RAW_GRANULE_TOPIC)
    KGroupedStream groupedGranules = rawGranules.groupByKey()
    KTable rawGranuleTable = groupedGranules.reduce(StreamFunctions.reduceJsonStrings, Materialized.as(RAW_GRANULE_STORE))

    KStream rawCollections = builder.stream(RAW_COLLECTION_TOPIC)
    KGroupedStream groupedCollections = rawCollections.groupByKey()
    KTable rawCollectionTable = groupedCollections.reduce(StreamFunctions.reduceJsonStrings, Materialized.as(RAW_COLLECTION_STORE))

    KTable parsedGranuleTable = builder.table(PARSED_GRANULE_TOPIC, Materialized.as(PARSED_GRANULE_STORE).withLoggingEnabled([:]))
    KTable parsedCollectionTable = builder.table(PARSED_COLLECTION_TOPIC, Materialized.as(PARSED_COLLECTION_STORE).withLoggingEnabled([:]))

    rawGranuleTable
        .outerJoin(parsedGranuleTable, StreamFunctions.buildKeyedJsonJoiner('raw', 'parsed'))
        .toStream()
        .to(COMBINED_GRANULE_TOPIC)

    rawCollectionTable
        .outerJoin(parsedCollectionTable, StreamFunctions.buildKeyedJsonJoiner('raw', 'parsed'))
        .toStream()
        .to(COMBINED_COLLECTION_TOPIC)

    return builder.build()
  }

}
