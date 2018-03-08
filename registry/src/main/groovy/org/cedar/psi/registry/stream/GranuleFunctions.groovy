package org.cedar.psi.registry.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.kstream.Reducer

@Slf4j
@CompileStatic
class GranuleFunctions {

  static Reducer<String> mergeGranules = new Reducer<String>() {
    @Override
    String apply(String aggregate, String newValue) {
      log.debug("Merging new value $newValue into existing aggregate ${aggregate}")
      def slurper = new JsonSlurper()
      def slurpedAggregate = aggregate ? slurper.parseText(aggregate as String) as Map : [:]
      def slurpedNewValue = slurper.parseText(newValue as String) as Map
      def result = slurpedAggregate + slurpedNewValue
      return JsonOutput.toJson(result)
    }
  }

}
