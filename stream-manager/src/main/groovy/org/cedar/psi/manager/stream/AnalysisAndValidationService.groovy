package org.cedar.psi.manager.stream

import groovy.util.logging.Slf4j

@Slf4j
class AnalysisAndValidationService {

  static Map analyzeParsedMetadata(Map msgMap) {
    log.debug("Received $msgMap")

    def analysisMap = [
        identification: analyzeIdentifiers(msgMap),
        temporalBounding: analyzeTemporalBounding(msgMap),
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

  static Map analyzeTemporalBounding(Map msgMap) {

    def invalidDates = msgMap.temporalBounding.invalidDates

    return [
        beginDate: [
            exists: msgMap.temporalBounding.beginDate ? true : false,
            valid: invalidDates ? !invalidDates.begin : true
        ],
        endDate: [
            exists: msgMap.temporalBounding.endDate ? true : false,
            valid: invalidDates ? !invalidDates.end : true
        ],
        instant: [
            exists: msgMap.temporalBounding.instant ? true : false,
            valid: invalidDates ? !invalidDates.instant : true
        ]
    ]
  }

  static Map analyzeSpatialBounding(Map msgMap) {}

  static Map analyzeTitles(Map msgMap) {}

  static Map analyzeDescription(Map msgMap) {
    String description = msgMap.description

    return [
        exists: description ? true : false,
        characters: description ? description.length() : 0
    ]
  }

  static Map analyzeThumbnail(Map msgMap) {}

  static Map analyzeDataAccess(Map msgMap) {}
}
