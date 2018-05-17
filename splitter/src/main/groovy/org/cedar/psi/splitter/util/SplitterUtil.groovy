package org.cedar.psi.splitter.util

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.kstream.Predicate

@Slf4j
class SplitterUtil {
  static Predicate[] buildPredicateList(List<Map> datastreams){
    List<Predicate> predicates = []
    datastreams.each{ stream ->
      stream.each{ streamName, streamConfig ->
        predicates.add(
            {k, m ->
              log.info "Running predicate against $m"
              Map msg = new JsonSlurper().parseText(m)
              log.info "Predicate: ${msg?.get(streamConfig.get('key')) == streamConfig.get('value')}"
              msg?.get(streamConfig.get('key')) == streamConfig.get('value')
            } as Predicate
        )
      }
    }
    predicates
  }

  static List<String> getOutputTopicList(List<Map> datastreams){
    List<String> outputTopics = []
    datastreams.each{ stream ->
      stream.each{ name, config ->
        log.info "name: $name , config: $config"
        outputTopics.add(config.output.topic as String)
      }
    }
    outputTopics
  }
}
