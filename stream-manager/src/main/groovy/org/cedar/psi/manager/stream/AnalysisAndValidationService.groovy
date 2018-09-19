package org.cedar.psi.manager.stream

import groovy.util.logging.Slf4j

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery

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

  // handle 3 optional date formats in priority of full-parse option to minimal-parse options
  static final DateTimeFormatter PARSE_DATE_FORMATTER = new DateTimeFormatterBuilder()
      .appendOptional(DateTimeFormatter.ISO_ZONED_DATE_TIME)  // e.g. - 2010-12-30T00:00:00Z
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)  // e.g. - 2010-12-30T00:00:00
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)       // e.g. - 2010-12-30
      .toFormatter()
      .withResolverStyle(ResolverStyle.STRICT)

  // use custom formatter for when time zone information is not supplied in a LocalDateTime format for ES's happiness
  static final DateTimeFormatter ELASTIC_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

//  static Map elasticDateInfo(String date) {
//
//    def dateInvalid = false
//
//    // don't bother parsing if there's nothing here
//    if(!date) {
//      return null
//    }
//
//    // default to null
//    String elasticDate = null
//    TemporalAccessor parsedDate = null
//    Long year = null
//
//    // paleo dates can be longs
//    if(date.isLong()) {
//      elasticDate = date
//      year = Long.parseLong(date)
//      // we only care about the year if outside of limits
//      if(year < MIN_DATE_LONG || year > MAX_DATE_LONG) {
//        elasticDate = null
//      }
//    }
//    else {
//      try {
//        // the "::" operator in Java8 is ".&" in groovy until groovy fully adopts "::"
//        parsedDate = PARSE_DATE_FORMATTER.parseBest(date, ZonedDateTime.&from as TemporalQuery, LocalDateTime.&from as TemporalQuery, LocalDate.&from as TemporalQuery)
//        year = parsedDate.get(ChronoField.YEAR)
//
//        // date is in format like: 2010-12-30T00:00:00Z
//        if(parsedDate instanceof ZonedDateTime) {
//          elasticDate = date
//        }
//        // date is in format like: 2010-12-30T00:00:00
//        else if(parsedDate instanceof LocalDateTime) {
//          // assume UTC
//          ZonedDateTime parsedDateUTC = parsedDate.atZone(ZoneId.of("UTC"))
//          elasticDate = parsedDateUTC.format(ELASTIC_DATE_FORMATTER)
//          // re-evaluate year in off-chance year was affected by zone id
//          year = parsedDateUTC.get(ChronoField.YEAR)
//        }
//        // date is in format like: 2010-12-30
//        else if(parsedDate instanceof LocalDate) {
//          elasticDate = date
//        }
//      }

  static Map dateInfo(String dateString) {

    def DateTimeFormatter DATE_OPT_TIME_PARSER = DateTimeFormatter.ofPattern("yyyy['-'MM['-'dd]]['T'HH[':'mm[':'ss]]][X]")

    def exists = dateString ? true : false
    def yearOnly = exists ? dateString.isLong() : 'NA'

    def parsedDate, validFormat
    if(exists) {
      if(yearOnly) {
        // Year must be in the range [-292275055,292278994] in order to be parsed as date by ES (Joda time magic number)
        def MIN_YEAR_LONG = -292275055L
        def MAX_YEAR_LONG = 292278994L
        def year = Long.parseLong(dateString)
        validFormat = year < MIN_YEAR_LONG || year > MAX_YEAR_LONG ? false : true
        parsedDate = validFormat ? DATE_OPT_TIME_PARSER.parse(dateString) : null
      }
      else {
        try {
          parsedDate = DATE_OPT_TIME_PARSER.parse(dateString)
          validFormat = true
        }
        catch(DateTimeParseException e) {
          validFormat = false
          parsedDate = null
        }
      }
    }
    else {
      validFormat = 'NA'
      parsedDate = null
    }

    return [
        exists: exists,
        yearOnly: yearOnly,
        validFormat: validFormat,
        dateObject: parsedDate
    ]
  }

  static Map analyzeTemporalBounding(Map metadata) {

    def beginExists, beginValidFormat, beginYearOnly, endExists, endValidFormat, endYearOnly, beginLTEEnd

    def beginInfo = dateInfo(metadata.temporalBounding.beginDate)
    def endInfo = dateInfo(metadata.temporalBounding.endDate)

    // Compare parsed dates, if they both exist
    if(beginInfo.exists && endInfo.exists) {
      // FIXME
      // Compare when both year only
      // Compare when one is year only and other is not
      // Compare parsed date objects -- they have to be the same format. Should dateInfo put everything into dateTimeZ for this?

    }
    else {
      beginLTEEnd = 'NA'
    }

    return [
        beginExists     : beginInfo.exists,
        beginYearOnly   : beginInfo.yearOnly,
        beginValidFormat: beginInfo.validFormat,
        endExists       : endInfo.exists,
        endYearOnly     : endInfo.yearOnly,
        endValidFormat  : endInfo.validFormat,
        beginLTEEnd     : beginLTEEnd
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
