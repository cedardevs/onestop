package org.cedar.psi.wrapper.stream

import groovy.util.logging.Slf4j
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.cedar.psi.wrapper.util.ConfigUtil
import org.cedar.psi.wrapper.util.TopologyUtil

@Slf4j
class ScriptWrapperStreamMain {

  static void main(final String[] args) throws Exception {
    // config file can be set on
    Map config = ConfigUtil.getConfig("script-wrapper")
    Map kafkaConfig = config?.kafka ? ConfigUtil.validateKafkaConfig(config.kafka as Map) : null
    Map topologyConfig = config?.stream ? ConfigUtil.validateTopologyConfig(config.stream as Map) : null
    if (kafkaConfig && topologyConfig) {
      def streams = buildStreamsApp(kafkaConfig, topologyConfig)
      log.info("Starting stream ...")
      streams.start()
      // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
      Runtime.getRuntime().addShutdownHook(new Thread({ streams.close() }))
    }
    else {
      log.error "Config not found"
    }
  }

  static KafkaStreams buildStreamsApp(Map kafkaConfig, Map topologyConfig) {
    StreamsBuilder builder = new StreamsBuilder()
    Topology topology = TopologyUtil.scriptWrapperStreamInstance(builder, topologyConfig)
    Properties streamConfig = ConfigUtil.streamsConfig(kafkaConfig)
    return new KafkaStreams(topology, streamConfig)
  }

}
