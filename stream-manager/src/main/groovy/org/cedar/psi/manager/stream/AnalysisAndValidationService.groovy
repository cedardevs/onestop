package org.cedar.psi.manager.stream

import groovy.util.logging.Slf4j

@Slf4j
class AnalysisAndValidationService {

  static Map analyzeParsedMetadata(Map msgMap) {
    log.debug("Received $msgMap")
    def analysisMap = [:]
    msgMap.put('analysis', analysisMap)

    // TODO Insert analysis functions...

    return msgMap
  }
}
