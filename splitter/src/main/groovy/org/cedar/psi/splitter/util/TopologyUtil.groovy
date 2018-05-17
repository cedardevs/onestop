package org.cedar.psi.splitter.util

import groovy.util.logging.Slf4j
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Predicate

@Slf4j
class TopologyUtil {

  static Topology splitterStreamInstance(StreamsBuilder builder, Map topologyConfig) {
    log.info "Building splitter topology with config $topologyConfig"
    String inputTopic = topologyConfig.input.topic
    List<Map> datastreams = topologyConfig.split
    Topology topology = builder.build()

//    Predicate[] predicates = SplitterUtil.buildPredicateList(datastreams)
//    List<String> outputTopics = SplitterUtil.getOutputTopicList(datastreams)

    Map<String, Predicate> predicatesByOutput = SplitterUtil.predicateByOutput(datastreams)
    Predicate[] predicates = predicatesByOutput.values() as Predicate[]
    List<String> outputTopics = predicatesByOutput.keySet() as List

    KStream inputStream = builder.stream(inputTopic)

    KStream[] outputStreams = inputStream.branch(predicates as Predicate[])

    log.info "outputstreams ${outputStreams.size()}"
    outputStreams.eachWithIndex{ KStream entry, int i ->
      log.info "Sending message to ${outputTopics[i]}"
      log.info "Entry ${entry.each{it.toString()}}"
      entry.to(outputTopics[i])
    }

    return topology
  }

}

