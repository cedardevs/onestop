package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.cedar.psi.registry.stream.TopicInitializer
import org.cedar.psi.registry.stream.TopologyBuilders
import org.cedar.schemas.avro.psi.Input
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import static org.apache.kafka.streams.StreamsConfig.*
import static org.cedar.psi.common.constants.StreamsApps.REGISTRY_ID

@Slf4j
@CompileStatic
@Configuration
class KafkaBeanConfig {

  @Value('${kafka.bootstrap.servers}')
  private String bootstrapServers

  @Value('${kafka.compression.type}')
  private String compressionType

  // default: 100 MiB
  @Value('${kafka.cache.max.bytes.buffering:104857600}')
  private Long cacheMaxBytes

  @Value('${kafka.commit.interval.ms:}')
  private Long commitIntervalMs

  @Value('${schema.registry.url}')
  private String schemaRegistryUrl

  @Value('${publishing.interval.ms:300000}')
  private long publishInterval

  @Value('${state.dir:}')
  private String stateDir


  @Bean(destroyMethod = 'close')
  AdminClient adminClient() {
    Map<String, Object> config = new HashMap<>()
    config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000)
    return AdminClient.create(config)
  }

  @Profile('!integration')
  @Bean(initMethod = 'initialize')
  TopicInitializer topicInitializer(AdminClient adminClient) {
    new TopicInitializer(adminClient)
  }

  @Bean
  Producer<String, Input> kafkaProducer() {
    Map<String, Object> configProps = new HashMap<>()
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    configProps.put(ProducerConfig.CLIENT_ID_CONFIG, 'api_publisher')
    configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType)
    configProps.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SpecificAvroSerializer.class.getName())
    return new KafkaProducer<>(configProps)
  }

  @Bean
  StreamsConfig streamsConfig() {
    def props = [
        (APPLICATION_ID_CONFIG)              : REGISTRY_ID,
        (BOOTSTRAP_SERVERS_CONFIG)           : bootstrapServers,
        (SCHEMA_REGISTRY_URL_CONFIG)         : schemaRegistryUrl,
        (DEFAULT_KEY_SERDE_CLASS_CONFIG)     : Serdes.String().class.name,
        (DEFAULT_VALUE_SERDE_CLASS_CONFIG)   : SpecificAvroSerde.class.name,
        (AUTO_OFFSET_RESET_CONFIG)           : 'earliest',
        (CACHE_MAX_BYTES_BUFFERING_CONFIG)   : cacheMaxBytes,
        (TopicConfig.COMPRESSION_TYPE_CONFIG): compressionType
    ]
    if (stateDir) {
      props.put(StreamsConfig.STATE_DIR_CONFIG, stateDir)
    }
    if (commitIntervalMs) {
      props.put(COMMIT_INTERVAL_MS_CONFIG, commitIntervalMs)
    }

    def streamsConfig = new StreamsConfig(props)
  }

  @Bean(destroyMethod = 'close')
  KafkaStreams streamsApp(StreamsConfig streamsConfig, TopicInitializer topicInitializer) {
    def streamsTopology = TopologyBuilders.buildTopology(publishInterval)
    def app = new KafkaStreams(streamsTopology, streamsConfig)

    topicInitializer.initialize()
    app.start()

    return app
  }

}
