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
    KStream inputStream = builder.stream(topologyConfig.topics.input as String)
    KStream scriptOutputStream = inputStream.mapValues({ msg -> ScriptWrapperFunctions.scriptCaller(msg, command, timeout) })

    //split script output into 2 streams, one for good and one for bad
    KStream[] goodAndBadStreams = scriptOutputStream.branch(isValid, isNotValid)
    KStream goodStream = goodAndBadStreams[0]
    KStream badStream = goodAndBadStreams.size() > 1 ? goodAndBadStreams[1] : null

    //send error to errorout topic and good msgs through parser
    if(badStream){badStream.to(errorOut)}
    KStream parsedStream = goodStream.mapValues({ msg -> ScriptWrapperFunctions.parseOutput(msg as String) })

    //split the parsed stream into valid and invalid streams
    KStream[] parsedGoodAndBadStreams = parsedStream.branch(isValid, isNotValid)
    KStream goodParsedStream = parsedGoodAndBadStreams[0]
    KStream badParsedStream = parsedGoodAndBadStreams.size() > 1 ? parsedGoodAndBadStreams[1] : null

    //send the error msgs to the error topic, otherwise
    if(badParsedStream){badParsedStream.to(errorOut)}
    goodParsedStream.to(outputTopic)

    return topology
  }

  // filter incoming stream
  static Predicate isValid = { k, v -> !v.toString().startsWith('ERROR') }
  static Predicate isNotValid = { k, v -> v.toString().startsWith('ERROR') }

}
