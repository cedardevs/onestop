package org.cedar.psi.parser.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Printed
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.util.regex.Matcher
import java.util.regex.Pattern

@Slf4j
@Configuration
class ScriptWrapperStreamConfig {

  @Value('${kafka.topics.input}')
  String inputTopic

  @Value('${kafka.topics.output}')
  String outputTopic

  @Value('${alg.lang}')
  String lang

  @Value('${alg.absolutePath}')
  String absolutePath

  static final String id = "validated-granules"

  @Value('${kafka.bootstrap.servers}')
  String bootstrapServers

  @Bean
  StreamsConfig streamConfig() {
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
  Topology wrapperTopology() {
    def builder = new StreamsBuilder()
    KStream inputStream = builder.stream(inputTopic)
    KStream outputStream = inputStream.mapValues { msg ->
      def cmdArray = [lang, absolutePath, "$msg"]
      def cmd = cmdArray.execute()
      cmd.waitForOrKill(50000)
      String outputMessage = cmd.text
      outputMessage
    }
    outputStream.to(outputTopic)
    return builder.build()
  }

  @Bean(initMethod = 'start', destroyMethod = 'close')
  KafkaStreams parserStream(Topology wrapperTopology, StreamsConfig streamConfig) {
    return new KafkaStreams(wrapperTopology, streamConfig)
  }

}


