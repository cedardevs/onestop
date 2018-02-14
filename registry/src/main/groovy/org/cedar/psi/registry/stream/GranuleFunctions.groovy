package org.cedar.psi.registry.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class GranuleFunctions {

  static mergeGranules = { String aggregate, String newValue ->
    log.debug("Merging new value $newValue into existing aggregate ${aggregate}")
    def slurper = new JsonSlurper()
    def slurpedAggregate = aggregate ? slurper.parseText(aggregate as String) : [:]
    def slurpedNewValue = slurper.parseText(newValue as String)
    def result = slurpedAggregate + slurpedNewValue
    return JsonOutput.toJson(result)
  }

}
