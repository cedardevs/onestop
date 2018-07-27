package org.cedar.psi.manager.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class AnalysisAndValidationService {

  static String analyzeParsedMetadata(String msg) {
    def msgMap = new JsonSlurper().parseText(msg) as Map
    def analysisMap = [:]
    def responseMap = msgMap.put('analysis', analysisMap)

    // TODO Insert analysis functions...

    return JsonOutput.toJson(responseMap)
  }
}
