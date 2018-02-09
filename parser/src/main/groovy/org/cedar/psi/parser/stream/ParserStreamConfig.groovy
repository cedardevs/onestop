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
class ParserStreamConfig {

  @Value('${kafka.topics.input}')
  String inputTopic

  @Value('${kafka.topics.output}')
  String outputTopic

  static final String id = "parsed-granules"

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
  Topology parserTopology() {
    def builder = new StreamsBuilder()
    KStream inputStream = builder.stream(inputTopic)
    KStream outputStream = inputStream.mapValues { msg ->
      Pattern filenamePattern = ~/(oe|ot|ie|it)_([a-zA-Z0-9]+)_([a-zA-Z0-9]+)_s(\d{14})_e(\d{14})_p(\d{14})_(pub|emb)\.nc\.gz/
      def msgMap = new JsonSlurper().parseText(msg)
      Matcher matcher = filenamePattern.matcher( msgMap.filepath as String)
      if ( ! matcher.matches() ) {
        log.error "filenamePattern ${filenamePattern} did not match granule file name ${msgMap.filename}"
        throw new RuntimeException( "file name does not contain necessary attributes" )
      }
      Map parsedAttributes = [
          processingEnvironment: matcher[0][1],
          dataType: matcher[0][2],
          satellite: matcher[0][3],
          startDate: matcher[0][4],
          endDate: matcher[0][5] ,
          processDate: matcher[0][6] ,
          publish: matcher[0][7] == 'pub'
      ]
      msgMap += parsedAttributes
      JsonOutput.toJson(msgMap) as String
    }
    outputStream.to(outputTopic)
    return builder.build()
  }

  @Bean(initMethod = 'start', destroyMethod = 'close')
  KafkaStreams parserStream(Topology parserTopology, StreamsConfig streamConfig) {
    return new KafkaStreams(parserTopology, streamConfig)
  }

}


