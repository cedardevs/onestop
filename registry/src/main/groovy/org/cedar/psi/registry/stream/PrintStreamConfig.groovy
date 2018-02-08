package org.cedar.psi.registry.stream

import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Printed
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Slf4j
@Configuration
class PrintStreamConfig {

  @Value('${kafka.topic}')
  String topic

  static final String id = "raw-granule-printer"

  @Value('${kafka.bootstrap.servers}')
  String bootstrapServers

  @Bean
  StreamsConfig printerStreamConfig() {
    return new StreamsConfig([
        (StreamsConfig.APPLICATION_ID_CONFIG): id,
        (StreamsConfig.BOOTSTRAP_SERVERS_CONFIG): bootstrapServers,
        (StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG): Serdes.String().class.name,
        (StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG): Serdes.String().class.name,
        (StreamsConfig.COMMIT_INTERVAL_MS_CONFIG): 500,
        (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG): "earliest"
    ])
  }

  @Bean
  Topology printerTopology() {
    def builder = new StreamsBuilder()
    builder
        .stream(topic)
        .print(Printed.toSysOut().withLabel(topic))
    return builder.build()
  }


  @Bean(initMethod = 'start', destroyMethod = 'close')
  KafkaStreams printerStream(Topology printerTopology, StreamsConfig printerStreamConfig) {
    return new KafkaStreams(printerTopology, printerStreamConfig)
  }

}
