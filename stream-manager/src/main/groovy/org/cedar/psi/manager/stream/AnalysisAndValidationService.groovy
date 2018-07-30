package org.cedar.psi.manager.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class AnalysisAndValidationService {

  static String analyzeParsedMetadata(String msg) {
    log.debug("Received $msg")
    def msgMap = new JsonSlurper().parseText(msg) as Map
    def analysisMap = [:]
    msgMap.put('analysis', analysisMap)

    // TODO Insert analysis functions...

    return JsonOutput.toJson(msgMap)
  }
}
