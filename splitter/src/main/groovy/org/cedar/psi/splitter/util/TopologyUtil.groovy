package org.cedar.psi.splitter.util

import groovy.util.logging.Slf4j
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Predicate

@Slf4j
class TopologyUtil {

  static Topology splitterStreamInstance(Map topologyConfig) {
    log.info "Building splitter topology with config $topologyConfig"
    String inputTopic = topologyConfig.input.topic
    List<Map> datastreams = topologyConfig.split
    StreamsBuilder builder = new StreamsBuilder()
    Topology topology = builder.build()

    Map<String, Predicate> predicatesByOutput = SplitterUtil.predicatesByOutput(datastreams)
    Predicate[] predicates = predicatesByOutput.values() as Predicate[]
    List<String> outputTopics = predicatesByOutput.keySet() as List

    KStream inputStream = builder.stream(inputTopic)

    KStream[] outputStreams = inputStream.branch(predicates as Predicate[])

    outputStreams.eachWithIndex{ KStream entry, int i ->
      entry.to(outputTopics[i])
    }

    return topology
  }

}

