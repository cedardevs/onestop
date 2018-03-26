package org.cedar.psi.wrapper.util

import groovy.util.logging.Slf4j
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.cedar.psi.wrapper.stream.ScriptWrapperFunctions

@Slf4j
class TopologyUtil {

  static Topology scriptWrapperStreamInstance(StreamsBuilder builder, Map topologyConfig) {
    log.info "Building script wrapper topology with config $topologyConfig"
    def timeout = topologyConfig.timeout.toString().toLong()
    def command = topologyConfig.command as String
    Topology topology = builder.build()
    builder.stream(topologyConfig.topics.input as String)
        .mapValues({ msg -> ScriptWrapperFunctions.scriptCaller(msg, command, timeout) })
        .filterNot({ key, msg -> msg.toString().startsWith('ERROR') })
        .mapValues({ msg ->
          topologyConfig.doIsoConversion ? IsoConversionUtil.parseXMLMetadata(msg as String) : msg
        })
        .filterNot({key, msg -> msg.toString().startsWith('ERROR')})
        .to(topologyConfig.topics.output)

    return topology
  }

}
