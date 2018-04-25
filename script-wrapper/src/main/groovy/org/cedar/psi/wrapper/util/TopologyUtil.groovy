package org.cedar.psi.wrapper.util

import groovy.util.logging.Slf4j
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Predicate
import org.cedar.psi.wrapper.stream.ScriptWrapperFunctions

@Slf4j
class TopologyUtil {

  static Topology scriptWrapperStreamInstance(StreamsBuilder builder, Map topologyConfig) {
    log.info "Building script wrapper topology with config $topologyConfig"
    def timeout = topologyConfig.timeout.toString().toLong()
    def command = topologyConfig.command as String
    def outputTopic = topologyConfig.topics.output as String
    def errorOut = topologyConfig.topics.errorout as String

    Topology topology = builder.build()
    KStream inputMsg = builder.stream(topologyConfig.topics.input as String)
    KStream scriptMsg = inputMsg.mapValues({ msg -> ScriptWrapperFunctions.scriptCaller(msg, command, timeout) })

    Predicate validParsedRecord = ({ key, msg ->
      if(!msg.toString().startsWith('ERROR: ')){
        println('validParsedRecord : ' + msg)
        return true
      }
    })

    Predicate inValidParsedRecord = ({ key, msg ->
      if(msg.toString().startsWith('ERROR: ')){
        println('inValidParsedRecord : ' + msg)
        return true
      }
    })

    KStream[] scriptPartitioned = scriptMsg.branch(validParsedRecord, inValidParsedRecord)

    KStream parsedMsg = scriptPartitioned[0].mapValues({ msg -> ScriptWrapperFunctions.parseOutput(msg as String) })
    scriptPartitioned[1]
        .to(errorOut)

    KStream[] parsedPartitioned = parsedMsg.branch(validParsedRecord, inValidParsedRecord)
    parsedPartitioned[0]
      .to(outputTopic)

    parsedPartitioned[1]
        .to(errorOut)

//    builder.stream(topologyConfig.topics.input as String)
//        .mapValues({ msg -> ScriptWrapperFunctions.scriptCaller(msg, command, timeout) })
//        .filterNot({ key, msg -> msg.toString().startsWith('ERROR') })
//        .mapValues({ msg -> ScriptWrapperFunctions.parseOutput(msg as String) })
//        .filterNot({key, msg -> msg.toString().startsWith('ERROR')})
//        inputMsg.to(outputTopic)

    return topology
  }

}
