package org.cedar.psi.splitter.util

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.kstream.Predicate

@Slf4j
class SplitterUtil {

  static Map<String, Predicate> predicatesByOutput(List<Map> datastreams) {
    Map<String, Predicate> predicatesByOutput = [:]
    datastreams.each { stream ->
      stream.each { streamName, streamConfig ->
        predicatesByOutput.put(streamConfig.output.topic as String,
            { key, value ->
              log.debug "Running message $value against predicate ${streamConfig}"
              def msg = new JsonSlurper().parseText(value as String) as Map
              log.debug "Predicate: ${msg?.get(streamConfig.get('key')) == streamConfig.get('value')}"
              return msg?.get(streamConfig.get('key')) == streamConfig.get('value')
            } as Predicate
        )
      }
    }
    return predicatesByOutput
  }
}
