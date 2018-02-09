package org.cedar.psi.registry.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KGroupedStream
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Slf4j
@Configuration
class RawGranuleStreamConfig {

  static final String topic = 'granule'

  static final String id = "raw-granule-aggregator"

  static final String storeName = 'raw-granules'

  @Value('${kafka.bootstrap.servers}')
  String bootstrapServers

  @Bean
  StreamsConfig rawGranuleConfig() {
    return new StreamsConfig([
        (StreamsConfig.APPLICATION_ID_CONFIG)           : id,
        (StreamsConfig.BOOTSTRAP_SERVERS_CONFIG)        : bootstrapServers,
        (StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
        (StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG): Serdes.String().class.name,
        (StreamsConfig.COMMIT_INTERVAL_MS_CONFIG)       : 500,
        (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)       : "earliest"
    ])
  }

  @Bean
  Topology rawGranuleTopology() {
    def builder = new StreamsBuilder()

    KStream rawStream = builder.stream(topic)
    KGroupedStream groupedStream = rawStream.groupByKey()
    KTable mergedGranules = groupedStream.reduce(
        {aggregate, newValue ->
            log.debug("aggregate (${aggregate?.getClass()}): $aggregate")
            log.debug("newValue (${newValue?.getClass()}): $newValue")
            def slurper = new JsonSlurper()
            def slurpedAggregate = aggregate ? slurper.parseText(aggregate as String) : [:]
            def slurpedNewValue = slurper.parseText(newValue as String)
            def result = slurpedAggregate + slurpedNewValue
            return JsonOutput.toJson(result)
        },
        Materialized.as(storeName)
    )

    return builder.build()
  }


  @Bean(initMethod = 'start', destroyMethod = 'close')
  KafkaStreams rawGranuleStream(Topology rawGranuleTopology, StreamsConfig rawGranuleConfig) {
    return new KafkaStreams(rawGranuleTopology, rawGranuleConfig)
  }

}
