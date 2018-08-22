package org.cedar.psi.manager.stream

import groovy.util.logging.Slf4j

@Slf4j
class AnalysisAndValidationService {

  static Map analyzeParsedMetadata(Map msgMap) {
    log.debug("Received $msgMap")

    def analysisMap = [
        identification: analyzeIdentifiers(msgMap),
        temporalBoudning: analyzeTemporalBounding(msgMap),
        spatialBounding: analyzeSpatialBounding(msgMap),
        titles: analyzeTitles(msgMap),
        description: analyzeDescription(msgMap),
        thumbnail: analyzeThumbnail(msgMap),
        dataAccess: analyzeDataAccess(msgMap)
    ]

    msgMap.put('analysis', analysisMap)
    return msgMap
  }


  static Map analyzeIdentifiers(Map msgMap) {}

  static Map analyzeTemporalBounding(Map msgMap) {}

  static Map analyzeSpatialBounding(Map msgMap) {}

  static Map analyzeTitles(Map msgMap) {}

  static Map analyzeDescription(Map msgMap) {}

  static Map analyzeThumbnail(Map msgMap) {}

  static Map analyzeDataAccess(Map msgMap) {}
}
