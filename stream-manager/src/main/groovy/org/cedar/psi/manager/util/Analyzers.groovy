package org.cedar.psi.manager.util

import groovy.util.logging.Slf4j
import org.cedar.psi.common.avro.*

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
class Analyzers {

  // Just to decrease chance of typos
  static final String UNDEFINED = 'UNDEFINED'
  static final String INVALID = 'INVALID'
  static final String VALID = 'VALID'

  static final DateTimeFormatter PARSE_DATE_FORMATTER = new DateTimeFormatterBuilder()
      .appendOptional(DateTimeFormatter.ISO_ZONED_DATE_TIME)  // e.g. - 2010-12-30T00:00:00Z
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)  // e.g. - 2010-12-30T00:00:00
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)       // e.g. - 2010-12-30
      .toFormatter()
      .withResolverStyle(ResolverStyle.STRICT)

  static ParsedRecord addAnalysis(ParsedRecord record) {
    def builder = ParsedRecord.newBuilder(record)
    builder.analysis = analyze(record?.discovery)
    return builder.build()
  }

  static Analysis analyze(Discovery discovery) {
    log.debug("Analyzing record: $discovery")
    if (discovery == null) {
      return null
    }
    def builder = Analysis.newBuilder()
    builder.identification = analyzeIdentifiers(discovery)
    builder.temporalBounding = analyzeTemporalBounding(discovery)
    builder.spatialBounding = analyzeSpatialBounding(discovery)
    builder.titles = analyzeTitles(discovery)
    builder.description = analyzeDescription(discovery)
    builder.thumbnail = analyzeThumbnail(discovery)
    builder.dataAccess = analyzeDataAccess(discovery)
    return builder.build()
  }

  static Identification analyzeIdentifiers(Discovery metadata) {
    def fileIdInfo = stringInfo(metadata?.fileIdentifier)
    def doiInfo = stringInfo(metadata?.doi)
    def parentIdInfo = stringInfo(metadata?.parentIdentifier)
    def hierarchyInfo = stringInfo(metadata?.hierarchyLevelName)

    def builder = Identification.newBuilder()
    builder.fileIdentifierExists = fileIdInfo.exists
    builder.fileIdentifierString = fileIdInfo.value
    builder.doiExists = doiInfo.exists
    builder.doiString = doiInfo.value
    builder.parentIdentifierExists = parentIdInfo.exists
    builder.parentIdentifierString = parentIdInfo.value
    builder.hierarchyLevelNameExists = hierarchyInfo.exists
    builder.matchesIdentifiers = (hierarchyInfo.value == 'granule' && parentIdInfo.exists) || !hierarchyInfo.exists
    return builder.build()
  }

  static TemporalBoundingAnalysis analyzeTemporalBounding(Discovery metadata) {

    // Gather info on individual dates:
    def beginInfo = dateInfo(metadata?.temporalBounding?.beginDate, true)
    def endInfo = dateInfo(metadata?.temporalBounding?.endDate, false)
    def instantInfo = dateInfo(metadata?.temporalBounding?.instant, true)

    // Determine the descriptor of the given time range:
    def descriptor
    if (beginInfo.exists) {
      // ( begin && end ) OR ( begin && !end )
      descriptor = endInfo.exists ? 'BOUNDED' : 'ONGOING'
    }
    else {
      // ( !begin && end ) OR ( !begin && !end )
      descriptor = endInfo.exists ? INVALID : UNDEFINED
    }

    // Update descriptor if !begin and !end but instant exists
    if (descriptor == UNDEFINED && instantInfo.exists) {
      // Instant describes range, but its validity still needs to be validated
      descriptor = (instantInfo.validSearchFormat || instantInfo.precision == ChronoUnit.YEARS.toString()) ? 'INSTANT' : INVALID
    }

    // Determine if the given time range is valid:
    def beginLTEEnd
    if (descriptor == INVALID || descriptor == UNDEFINED) {
      beginLTEEnd = UNDEFINED
    }
    else if (descriptor == 'ONGOING' || descriptor == 'INSTANT') {
      beginLTEEnd = true
    }
    else {
      if (beginInfo.validSearchFormat == true && endInfo.validSearchFormat == true) {
        // Compare actual dates with UTC string
        def beginDate = ZonedDateTime.parse(beginInfo.utcDateTimeString)
        def endDate = ZonedDateTime.parse(endInfo.utcDateTimeString)
        beginLTEEnd = beginDate.isBefore(endDate) || beginDate.isEqual(endDate)
      }
      else if ((beginInfo.precision == ChronoUnit.YEARS.toString() && endInfo.precision == ChronoUnit.YEARS.toString()) ||
          (beginInfo.precision == ChronoUnit.YEARS.toString() && endInfo.validSearchFormat == true) ||
          (beginInfo.validSearchFormat == true && endInfo.precision == ChronoUnit.YEARS.toString())) {
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
      if (beginLTEEnd == false) {
        descriptor = INVALID
      }
    }

    def builder = TemporalBoundingAnalysis.newBuilder()

    builder.beginexists = beginInfo.exists
    builder.beginprecision = beginInfo.precision
    builder.beginvalidSearchFormat = beginInfo.validSearchFormat
    builder.beginzoneSpecified = beginInfo.zoneSpecified
    builder.beginutcDateTimeString = beginInfo.utcDateTimeString

    builder.endexists = endInfo.exists
    builder.endprecision = endInfo.precision
    builder.endvalidSearchFormat = endInfo.validSearchFormat
    builder.endzoneSpecified = endInfo.zoneSpecified
    builder.endutcDateTimeString = endInfo.utcDateTimeString

    builder.instantexists = instantInfo.exists
    builder.instantprecision = instantInfo.precision
    builder.instantvalidSearchFormat = instantInfo.validSearchFormat
    builder.instantzoneSpecified = instantInfo.zoneSpecified
    builder.instantutcDateTimeString = instantInfo.utcDateTimeString

    builder.rangedescriptor = descriptor
    builder.rangebeginLTEEnd = beginLTEEnd

    return builder.build()
  }

  static SpatialBounding analyzeSpatialBounding(Discovery metadata) {
    def builder = SpatialBounding.newBuilder()
    builder.spatialBoundingExists = metadata?.spatialBounding != null
    return builder.build()
  }

  static Titles analyzeTitles(Discovery metadata) {
    def titleAnalysis = stringInfo(metadata?.title)
    def altAnalysis = stringInfo(metadata?.alternateTitle)

    def builder = Titles.newBuilder()
    builder.titleExists = titleAnalysis.exists
    builder.titleCharacters = titleAnalysis.characters
    builder.alternateTitleExists = altAnalysis.exists
    builder.alternateTitleCharacters = altAnalysis.characters
    return builder.build()
  }

  static Description analyzeDescription(Discovery metadata) {
    def analysis = stringInfo(metadata?.description)
    def builder = Description.newBuilder()
    builder.descriptionExists = analysis.exists
    builder.descriptionCharacters = analysis.characters
    return builder.build()
  }

  static Thumbnail analyzeThumbnail(Discovery metadata) {
    def builder = Thumbnail.newBuilder()
    builder.thumbnailExists = metadata?.thumbnail != null
    return builder.build()
  }

  static DataAccess analyzeDataAccess(Discovery metadata) {
    def builder = DataAccess.newBuilder()
    builder.dataAccessExists = metadata?.links?.size() > 0
    return builder.build()
  }

  //-- Helpers

  static Map stringInfo(String input) {
    return [
        value     : input,
        exists    : input != null && input.length() > 0,
        characters: input?.length() ?: 0
    ]
  }

  static Map dateInfo(String dateString, boolean start) {
    def exists = dateString ? true : false
    if (!exists) {
      return [
          exists           : exists,
          precision        : UNDEFINED,
          validSearchFormat: UNDEFINED,
          zoneSpecified    : UNDEFINED,
          utcDateTimeString: UNDEFINED
      ]
    }

    def yearOnly = dateString.isLong()

    def utcDateTimeString, validSearchFormat, precision, timezone
    if (yearOnly) {
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
      try {
        def parsedDate = PARSE_DATE_FORMATTER.parseBest(dateString, ZonedDateTime.&from as TemporalQuery,
            LocalDateTime.&from as TemporalQuery, LocalDate.&from as TemporalQuery)
        validSearchFormat = true
        precision = parsedDate.query(TemporalQueries.precision()).toString()
        if (parsedDate instanceof LocalDate) {
          parsedDate = start ? parsedDate.atTime(0, 0, 0) : parsedDate.atTime(23, 59, 59)
          parsedDate = parsedDate.atZone(ZoneOffset.UTC)
        }
        else if (parsedDate instanceof LocalDateTime) {
          parsedDate = parsedDate.atZone(ZoneOffset.UTC)
        }
        else if (parsedDate instanceof ZonedDateTime) {
          timezone = parsedDate.offset.toString()
          parsedDate = parsedDate.withZoneSameInstant(ZoneOffset.UTC)
        }
        utcDateTimeString = DateTimeFormatter.ISO_ZONED_DATE_TIME.format(parsedDate)
      }
      catch (DateTimeParseException e) {
        validSearchFormat = false
        precision = INVALID
        timezone = INVALID
        utcDateTimeString = INVALID
      }

    }

    return [
        exists           : exists,
        precision        : precision,
        validSearchFormat: validSearchFormat,
        zoneSpecified    : timezone ?: UNDEFINED,
        utcDateTimeString: utcDateTimeString as String
    ]
  }

  String isValidSearchFormat(String input) {
    if (input.isLong()) {
      // Year must be in the range [-292275055,292278994] in order to be parsed as a date by ES (Joda time magic number). However,
      // this number is a bit arbitrary, and prone to change when ES switches to the Java time library (minimum supported year
      // being -999999999). We will limit the year ourselves instead to -100,000,000 -- since this is a fairly safe bet for
      // supportability across many date libraries if the utcDateTime ends up used as is by a downstream app.
      return Long.parseLong(dateString) < -100000000L ? false : true
    }
    else {

    }
  }

  String utcString(String input) {
    def utcDateTimeString, validSearchFormat, precision, timezone
    if (dateString.isLong()) {
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
  }
}
