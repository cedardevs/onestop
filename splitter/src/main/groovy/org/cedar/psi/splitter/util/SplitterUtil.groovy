package org.cedar.psi.splitter.util

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.kstream.Predicate

@Slf4j
class SplitterUtil {

  static Map<String, Predicate> predicateByOutput(List<Map> datastreams){
    Map<String, Predicate> predicatesByOutput = [:]
    datastreams.each{ stream ->
      stream.each{ streamName, streamConfig ->
        predicatesByOutput.put(streamConfig.output.topic,
            {k, m ->
              log.info "Running predicate against $m"
              Map msg = new JsonSlurper().parseText(m)
              log.info "Predicate: ${msg?.get(streamConfig.get('key')) == streamConfig.get('value')}"
              msg?.get(streamConfig.get('key')) == streamConfig.get('value')
            } as Predicate
        )
      }
    }
    predicatesByOutput
  }
}
