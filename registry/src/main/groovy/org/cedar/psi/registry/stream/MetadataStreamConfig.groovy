package org.cedar.psi.registry.stream

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KGroupedStream
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Slf4j
@CompileStatic
@Configuration
class MetadataStreamConfig {

  static final String APP_ID = "metadata-aggregator"

  @Value('${kafka.bootstrap.servers}')
  String bootstrapServers

  @Value('${kafka.topics.raw.granule}')
  String RAW_GRANULE_TOPIC

  @Value('${kafka.stores.raw.granule}')
  String RAW_GRANULE_STORE

  @Value('${kafka.topics.raw.collection}')
  String RAW_COLLECTION_TOPIC

  @Value('${kafka.stores.raw.collection}')
  String RAW_COLLECTION_STORE

  @Bean
  StreamsConfig metadataConfig() {
    return new StreamsConfig([
        (StreamsConfig.APPLICATION_ID_CONFIG)           : APP_ID,
        (StreamsConfig.BOOTSTRAP_SERVERS_CONFIG)        : bootstrapServers,
        (StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
        (StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG): Serdes.String().class.name,
        (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)       : 'earliest'
    ])
  }

  @Bean
  Topology metadataTopology() {
    def builder = new StreamsBuilder()

    KStream rawGranules = builder.stream(RAW_GRANULE_TOPIC)
    KGroupedStream groupedGranules = rawGranules.groupByKey()
    groupedGranules.reduce(StreamFunctions.mergeJsonStrings, Materialized.as(RAW_GRANULE_STORE))

    KStream rawStream = builder.stream(RAW_COLLECTION_TOPIC)
    KGroupedStream groupedCollections = rawStream.groupByKey()
    groupedCollections.reduce(StreamFunctions.mergeJsonStrings, Materialized.as(RAW_COLLECTION_STORE))

    return builder.build()
  }


  @Bean(initMethod = 'start', destroyMethod = 'close')
  KafkaStreams metadataStream(Topology rawGranuleTopology, StreamsConfig rawGranuleConfig) {
    return new KafkaStreams(rawGranuleTopology, rawGranuleConfig)
  }

}
