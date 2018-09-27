package org.cedar.psi.manager.stream

import groovy.util.logging.Slf4j

@Slf4j
class AnalysisAndValidationService {

  static Map analyzeParsedMetadata(Map msgMap) {
    log.info "Analyzing message with id: ${msgMap?.id}"
    log.debug("Received $msgMap")
    def parsedMetadata = msgMap.discovery

    def analysisMap = [
        identification  : analyzeIdentifiers(parsedMetadata),
        temporalBounding: analyzeTemporalBounding(parsedMetadata),
        spatialBounding : analyzeSpatialBounding(parsedMetadata),
        titles          : analyzeTitles(parsedMetadata),
        description     : analyzeDescription(parsedMetadata),
        thumbnail       : analyzeThumbnail(parsedMetadata),
        dataAccess      : analyzeDataAccess(parsedMetadata)
    ]

    msgMap.put('analysis', analysisMap)
    return msgMap
  }

  static Map analyzeIdentifiers(Map metadata) {
    String fileIdentifier = metadata.fileIdentifier
    String doi = metadata.doi
    String parentIdentifier = metadata.parentIdentifier
    String hierarchy = metadata.hierarchyLevelName

    def matchesIdentifiers = (hierarchy == 'granule' && parentIdentifier) || (hierarchy == null)

    return [
        fileIdentifier    : [
            exists: fileIdentifier ? true : false
        ],
        doi               : [
            exists: doi ? true : false
        ],
        parentIdentifier  : [
            exists: parentIdentifier ? true : false
        ],
        hierarchyLevelName: [
            exists            : hierarchy ? true : false,
            matchesIdentifiers: matchesIdentifiers
        ]
    ]
  }

  static Map analyzeTemporalBounding(Map metadata) {

    def invalidDates = metadata.temporalBounding.invalidDates

    return [
        beginDate: [
            exists: metadata.temporalBounding.beginDate ? true : false,
            valid : invalidDates ? !invalidDates.begin : true
        ],
        endDate  : [
            exists: metadata.temporalBounding.endDate ? true : false,
            valid : invalidDates ? !invalidDates.end : true
        ],
        instant  : [
            exists: metadata.temporalBounding.instant ? true : false,
            valid : invalidDates ? !invalidDates.instant : true
        ]
    ]
  }

  static Map analyzeSpatialBounding(Map metadata) {
    String spatialBounding = metadata.spatialBounding

    return [
        exists: spatialBounding ? true : false
    ]
  }

  static Map analyzeTitles(Map metadata) {
    String title = metadata.title
    String altTitle = metadata.alternateTitle

    return [
        title         : [
            exists    : title ? true : false,
            characters: title ? title.length() : 0
        ],
        alternateTitle: [
            exists    : altTitle ? true : false,
            characters: altTitle ? altTitle.length() : 0
        ]
    ]
  }

  static Map analyzeDescription(Map metadata) {
    String description = metadata.description

    return [
        exists    : description ? true : false,
        characters: description ? description.length() : 0
    ]
  }

  static Map analyzeThumbnail(Map metadata) {
    String thumbnail = metadata.thumbnail

    return [
        exists: thumbnail ? true : false,
    ]
  }

  static Map analyzeDataAccess(Map metadata) {
    return [
        exists: metadata.links ? true : false
    ]
  }
}
