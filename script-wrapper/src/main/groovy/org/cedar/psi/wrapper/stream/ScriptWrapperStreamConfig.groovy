package org.cedar.psi.wrapper.stream

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.cedar.psi.wrapper.util.IsoConversionUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Slf4j
@Configuration
@CompileStatic
class ScriptWrapperStreamConfig {

  @Value('${stream.topics.input}')
  String inputTopic

  @Value('${stream.topics.output}')
  String outputTopic

  @Value('${stream.command}')
  String command

  @Value('${stream.convert.iso:false}')
  Boolean doIsoConversion

  @Value('${stream.command_timeout:5000}')
  long timeout

  @Value('${kafka.group.id}')
  String id

  @Value('${kafka.bootstrap.servers}')
  String bootstrapServers

  @Autowired
  IsoConversionUtil isoConversionUtil

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
    inputStream.mapValues ({ msg ->
      ScriptWrapperFunctions.scriptCaller(msg, command, timeout)
    })
    .filterNot({key, msg -> msg.toString().startsWith('ERROR')})
    .mapValues({msg -> doIsoConversion ? isoConversionUtil.parseXMLMetadata(msg as String)  : msg})
    .to(outputTopic)
    return builder.build()
  }

  @Bean(initMethod = 'start', destroyMethod = 'close')
  KafkaStreams parserStream(Topology wrapperTopology, StreamsConfig streamConfig) {
    return new KafkaStreams(wrapperTopology, streamConfig)
  }

}
