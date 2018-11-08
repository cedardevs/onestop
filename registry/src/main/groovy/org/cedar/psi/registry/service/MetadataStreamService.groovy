package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.Stores
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.serde.JsonSerdes
import org.cedar.psi.registry.stream.DelayedPublisherTransformer
import org.cedar.psi.registry.stream.PublishingAwareTransformer
import org.cedar.psi.registry.stream.StreamFunctions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import static org.apache.kafka.streams.StreamsConfig.*
import static org.cedar.psi.common.constants.StreamsApps.REGISTRY_ID
import static org.cedar.psi.common.constants.Topics.*

@Slf4j
@Service
@CompileStatic
class MetadataStreamService {

  private final AdminClient adminClient
  private final String schemaRegistryUrl
  private final long publishInterval
  private final String stateDir
  private KafkaStreams streamsApp


  MetadataStreamService(
      @Autowired AdminClient adminClient,
      @Value('${schema.registry.url}') String schemaRegistryUrl,
      @Value('${publishing.interval.ms:300000}') long publishInterval,
      @Value('${state.dir:}') String stateDir) {
    this.adminClient = adminClient
    this.schemaRegistryUrl = schemaRegistryUrl
    this.publishInterval = publishInterval
    this.stateDir = stateDir
    declareTopics(adminClient)
    this.streamsApp = buildStreamsApp(adminClient, schemaRegistryUrl, publishInterval, stateDir)
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
      this.streamsApp = buildStreamsApp(this.adminClient, this.schemaRegistryUrl, this.publishInterval, this.stateDir)
      this.streamsApp.start()
    }
  }

  // add custom config by topic name here
  static Map<String, Map> topicConfigs = [:] as Map<String, Map>

  private static void declareTopics(AdminClient adminClient) {
    def currentTopics = adminClient.listTopics().names().get()
    def declaredTopics = inputTopics() + parsedTopics() + unparsedTopics() + smeTopics() + publishedTopics()
    def missingTopics = declaredTopics.findAll({ !currentTopics.contains(it) })
    def newTopics = missingTopics.collect { name ->
      return new NewTopic(name, DEFAULT_NUM_PARTITIONS, DEFAULT_REPLICATION_FACTOR)
          .configs(topicConfigs[name] ?: [:])
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

  static KafkaStreams buildStreamsApp(AdminClient adminClient, String schemaRegistryUrl, long publishInterval, String stateDir) {
    def kafkaNodes = adminClient.describeCluster().nodes().get()
    def bootstrapServers = kafkaNodes.take(3).collect({ it.host() + ':' + it.port() })

    def props = [
        (APPLICATION_ID_CONFIG)           : REGISTRY_ID,
        (BOOTSTRAP_SERVERS_CONFIG)        : bootstrapServers.join(','),
        (SCHEMA_REGISTRY_URL_CONFIG)      : schemaRegistryUrl,
        (DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
        (DEFAULT_VALUE_SERDE_CLASS_CONFIG): SpecificAvroSerde.class.name,
        (AUTO_OFFSET_RESET_CONFIG)        : 'earliest'
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

    inputTypes().each { type ->
      addTopologyForType(builder, type, publishInterval)
    }

    // TODO this table is unused, plus should we really store every error forever?
    KTable<String, Map> errorHandlerTable = builder.table(
        errorTopic(),
        Consumed.with(Serdes.String(), JsonSerdes.Map()),
        Materialized.as(errorStore())
            .withLoggingEnabled([:])
            .withKeySerde(Serdes.String())
            .withValueSerde(JsonSerdes.Map())
    )

    return builder.build()
  }

  static StreamsBuilder addTopologyForType(StreamsBuilder builder, String type, Long publishInterval = null) {
    // build input table for each source
    Map<String, KTable> inputTables = inputSources(type).collectEntries { source ->
      KStream<String, Input> inputStream = builder.stream(inputTopic(type, source))
      KTable<String, Input> inputTable = inputStream
          .groupByKey()
          .reduce(StreamFunctions.mergeInputs, Materialized.as(inputStore(type, source)))
      return [(source): inputTable]
    }

    // build parsed table
    KTable<String, Map> parsedTable = builder
        .stream(parsedTopic(type), Consumed.with(Serdes.String(), JsonSerdes.Map()))
        .mapValues(StreamFunctions.parsedInfoNormalizer)
        .groupByKey()
        .reduce(StreamFunctions.identityReducer, Materialized.as(parsedStore(type)).withValueSerde(JsonSerdes.Map()))

    // add delayed publisher
    if (publishInterval) {
      builder.addStateStore(Stores.keyValueStoreBuilder(
          Stores.persistentKeyValueStore(
              publishTimeStore(type)), Serdes.Long(), Serdes.String()).withLoggingEnabled([:]))
      builder.addStateStore(Stores.keyValueStoreBuilder(
          Stores.persistentKeyValueStore(
              publishKeyStore(type)), Serdes.String(), Serdes.Long()).withLoggingEnabled([:]))

      // re-published items go back through the parsed topic
      def publisher = new DelayedPublisherTransformer(publishTimeStore(type), publishKeyStore(type), parsedStore(type), publishInterval)
      parsedTable
          .toStream()
          .transform({ -> publisher }, publishTimeStore(type), publishKeyStore(type), parsedStore(type))
          .to(parsedTopic(type), Produced.with(Serdes.String(), JsonSerdes.Map()))
    }

    // build published topic
    parsedTable
        .toStream()
        .transformValues({ -> new PublishingAwareTransformer() } as ValueTransformerSupplier<Map, Map>)
        .to(publishedTopic(type), Produced.with(Serdes.String(), JsonSerdes.Map()))

    return builder
  }

}
