package org.cedar.psi.manager.stream

import groovy.util.logging.Slf4j

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalQueries
import java.time.temporal.TemporalQuery

@Slf4j
class AnalysisAndValidationService {

  // Just to decrease chance of typos
  static final String UNDEFINED = 'UNDEFINED'
  static final String INVALID = 'INVALID'

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

  static Map dateInfo(String dateString, boolean start) {

    final DateTimeFormatter PARSE_DATE_FORMATTER = new DateTimeFormatterBuilder()
        .appendOptional(DateTimeFormatter.ISO_ZONED_DATE_TIME)  // e.g. - 2010-12-30T00:00:00Z
        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)  // e.g. - 2010-12-30T00:00:00
        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)       // e.g. - 2010-12-30
        .toFormatter()
        .withResolverStyle(ResolverStyle.STRICT)

    def exists = dateString ? true : false
    if(!exists) {
      return [
          exists: exists,
          precision: UNDEFINED,
          validSearchFormat: UNDEFINED,
          zoneSpecified: UNDEFINED,
          utcDateTimeString: UNDEFINED
      ]
    }

    def yearOnly = dateString.isLong()

    def utcDateTimeString, validSearchFormat, precision, timezone
    if(yearOnly) {
      def year = Long.parseLong(dateString)
      // Year must be in the range [-292275055,292278994] in order to be parsed as a date by ES (Joda time magic number). However,
      // this number is a bit arbitrary, and prone to change when ES switches to the Java time library (minimum supported year
      // being -999999999). We will limit the year ourselves instead to -100,000,000 -- since this is a fairly safe bet for
      // supportability across many date libraries if the utcDateTime ends up used as is by a downstream app.
      validSearchFormat = year < -100000000L ? false : true
      precision = ChronoUnit.YEARS.toString()
      timezone = UNDEFINED
      utcDateTimeString = "${year}-01-01T00:00:00Z"
    }
    else {
      try{
        def parsedDate = PARSE_DATE_FORMATTER.parseBest(dateString, ZonedDateTime.&from as TemporalQuery,
            LocalDateTime.&from as TemporalQuery, LocalDate.&from as TemporalQuery)
        validSearchFormat = true
        precision = parsedDate.query(TemporalQueries.precision()).toString()
        if(parsedDate instanceof LocalDate) {
          parsedDate = start ? parsedDate.atTime(0, 0, 0) : parsedDate.atTime(23, 59, 59)
          parsedDate = parsedDate.atZone(ZoneOffset.UTC)
        }
        else if(parsedDate instanceof LocalDateTime) {
          parsedDate = parsedDate.atZone(ZoneOffset.UTC)
        }
        else if(parsedDate instanceof ZonedDateTime) {
          timezone = parsedDate.offset.toString()
          parsedDate = parsedDate.withZoneSameInstant(ZoneOffset.UTC).toInstant()
        }
        utcDateTimeString = parsedDate.toString()
      }
      catch(DateTimeParseException e) {
        validSearchFormat = false
        precision = INVALID
        timezone = INVALID
        utcDateTimeString = INVALID
      }

    }

    return [
        exists: exists,
        precision: precision,
        validSearchFormat: validSearchFormat,
        zoneSpecified: timezone ?: UNDEFINED,
        utcDateTimeString: utcDateTimeString as String
    ]
  }

  static Map analyzeTemporalBounding(Map metadata) {

    // Gather info on individual dates:
    def beginInfo = dateInfo(metadata.temporalBounding.beginDate, true)
    def endInfo = dateInfo(metadata.temporalBounding.endDate, false)
    def instantInfo = dateInfo(metadata.temporalBounding.instant, true)

    // Determine the descriptor of the given time range:
    def descriptor
    if(beginInfo.exists) {
      // ( begin && end ) OR ( begin && !end )
      descriptor = endInfo.exists ? 'BOUNDED' : 'ONGOING'
    }
    else {
      // ( !begin && end ) OR ( !begin && !end )
      descriptor = endInfo.exists ? INVALID : UNDEFINED
    }

    // Update descriptor if !begin and !end but instant exists
    if(instantInfo.exists) {

    }

    // Determine if the given time range is valid:
    def beginLTEEnd
    if(descriptor == INVALID || descriptor == UNDEFINED) {
      beginLTEEnd = UNDEFINED
    }
    else if(descriptor == 'ONGOING') {
      beginLTEEnd = true
    }
    else {
      if(beginInfo.validSearchFormat == true && endInfo.validSearchFormat == true) {
        // Compare actual dates with UTC string
        def beginDate = ZonedDateTime.parse(beginInfo.utcDateTimeString)
        def endDate = ZonedDateTime.parse(endInfo.utcDateTimeString)
        beginLTEEnd = beginDate.isBefore(endDate) || beginDate.isEqual(endDate)
      }
      else if( (beginInfo.precision == ChronoUnit.YEARS.toString() && endInfo.precision == ChronoUnit.YEARS.toString()) ||
          (beginInfo.precision == ChronoUnit.YEARS.toString() && endInfo.validSearchFormat == true) ||
          (beginInfo.validSearchFormat == true && endInfo.precision == ChronoUnit.YEARS.toString()) ) {
        // Compare years only as longs; parse both as string objects since both may not be just a long.
        // Watch out for negative years...
        def beginYearText = beginInfo.utcDateTimeString.substring(0, beginInfo.utcDateTimeString.indexOf('-', 1))
        def endYearText = endInfo.utcDateTimeString.substring(0, endInfo.utcDateTimeString.indexOf('-', 1))
        def beginYear = Long.parseLong(beginYearText)
        def endYear = Long.parseLong(endYearText)
        beginLTEEnd = beginYear <= endYear
      }
      else {
        // One or both has an INVALID search format that is not just due to a paleo year
        beginLTEEnd = UNDEFINED
      }

      // Update descriptor to INVALID if !beginLTEEnd since BOUNDED is no longer accurate
      if(beginLTEEnd == false) {
        descriptor = INVALID
      }
    }

    return [
        begin: beginInfo,
        end: endInfo,
        instant: instantInfo,
        range: [
            descriptor: descriptor,
            beginLTEEnd: beginLTEEnd
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
