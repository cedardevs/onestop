package org.cedar.psi.manager.util

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.cedar.schemas.avro.psi.*

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQueries
import java.time.temporal.TemporalQuery

import static org.cedar.schemas.avro.psi.TimeRangeDescriptor.*

@Slf4j
class Analyzers {

  static final DateTimeFormatter PARSE_DATE_FORMATTER = new DateTimeFormatterBuilder()
      .appendOptional(DateTimeFormatter.ISO_ZONED_DATE_TIME)  // e.g.  2010-12-30T00:00:00Z
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)  // e.g.  2010-12-30T00:00:00
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)       // e.g.  2010-12-30
      .appendOptional(new DateTimeFormatterBuilder()
        .appendValue(ChronoField.YEAR)
        .appendPattern("-MM-dd").toFormatter())        // e.g. -200-01-01
      .appendOptional(new DateTimeFormatterBuilder()
        .appendValue(ChronoField.YEAR).toFormatter())         // e.g. -200
      .toFormatter()
      .withResolverStyle(ResolverStyle.STRICT)

  static ParsedRecord addAnalysis(ParsedRecord record) {
    if (record == null) {
      return null // pass through
    }
    try {
      def builder = ParsedRecord.newBuilder(record)
      builder.analysis = analyze(record?.discovery)
      return builder.build()
    }
    catch (e) {
      log.error("An error occurred during analysis", e)
      def errors = record.errors ?: []
      def error = ErrorEvent.newBuilder()
          .setTitle("Analysis failed")
          .setDetail(ExceptionUtils.getRootCauseMessage(e).trim())
          .build()
      errors.add(error)
      return ParsedRecord.newBuilder(record).setErrors(errors).build()
    }
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

  static IdentificationAnalysis analyzeIdentifiers(Discovery metadata) {
    def fileIdInfo = stringInfo(metadata?.fileIdentifier)
    def doiInfo = stringInfo(metadata?.doi)
    def parentIdInfo = stringInfo(metadata?.parentIdentifier)
    def hierarchyInfo = stringInfo(metadata?.hierarchyLevelName)

    def builder = IdentificationAnalysis.newBuilder()
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
    // Gather info
    def beginInfo = dateInfo(metadata?.temporalBounding?.beginDate, true)
    def endInfo = dateInfo(metadata?.temporalBounding?.endDate, false)
    def instantInfo = dateInfo(metadata?.temporalBounding?.instant, true)
    def rangeDescriptor = rangeDescriptor(beginInfo, endInfo, instantInfo)

    // Build
    def builder = TemporalBoundingAnalysis.newBuilder()

    builder.beginDescriptor = beginInfo.descriptor
    builder.beginPrecision = beginInfo.precision
    builder.beginIndexable = beginInfo.indexable
    builder.beginZoneSpecified = beginInfo.zoneSpecified
    builder.beginUtcDateTimeString = beginInfo.utcDateTimeString

    builder.endDescriptor = endInfo.descriptor
    builder.endPrecision = endInfo.precision
    builder.endIndexable = endInfo.indexable
    builder.endZoneSpecified = endInfo.zoneSpecified
    builder.endUtcDateTimeString = endInfo.utcDateTimeString

    builder.instantDescriptor = instantInfo.descriptor
    builder.instantPrecision = instantInfo.precision
    builder.instantIndexable = instantInfo.indexable
    builder.instantZoneSpecified = instantInfo.zoneSpecified
    builder.instantUtcDateTimeString = instantInfo.utcDateTimeString

    builder.rangeDescriptor = rangeDescriptor

    return builder.build()
  }

  static SpatialBoundingAnalysis analyzeSpatialBounding(Discovery metadata) {
    def builder = SpatialBoundingAnalysis.newBuilder()

    builder.spatialBoundingExists = metadata?.spatialBounding != null
    if (builder.spatialBoundingExists){
      def validateGeometry = ValidateGeometry.validateGeometry(metadata)
      builder.isValid  = validateGeometry.isValid
      builder.validationError = validateGeometry.error
    } else {
      builder.isValid  = false
      builder.validationError = "Missing geographic bounding box"
    }

    return builder.build()
  }

  static TitleAnalysis analyzeTitles(Discovery metadata) {
    def titleAnalysis = stringInfo(metadata?.title)
    def altAnalysis = stringInfo(metadata?.alternateTitle)

    def builder = TitleAnalysis.newBuilder()
    builder.titleExists = titleAnalysis.exists
    builder.titleCharacters = titleAnalysis.characters
    builder.alternateTitleExists = altAnalysis.exists
    builder.alternateTitleCharacters = altAnalysis.characters
    builder.titleFleschReadingEaseScore = titleAnalysis.readingEase
    builder.titleFleschKincaidReadingGradeLevel = titleAnalysis.gradeLevel
    builder.alternateTitleFleschReadingEaseScore = altAnalysis.readingEase
    builder.alternateTitleFleschKincaidReadingGradeLevel = altAnalysis.gradeLevel

    return builder.build()
  }

  static DescriptionAnalysis analyzeDescription(Discovery metadata) {
    def analysis = stringInfo(metadata?.description)
    def builder = DescriptionAnalysis.newBuilder()
    builder.descriptionExists = analysis.exists
    builder.descriptionCharacters = analysis.characters
    builder.descriptionFleschReadingEaseScore = analysis.readingEase
    builder.descriptionFleschKincaidReadingGradeLevel = analysis.gradeLevel
    return builder.build()
  }

  static ThumbnailAnalysis analyzeThumbnail(Discovery metadata) {
    def builder = ThumbnailAnalysis.newBuilder()
    builder.thumbnailExists = metadata?.thumbnail != null
    return builder.build()
  }

  static DataAccessAnalysis analyzeDataAccess(Discovery metadata) {
    def builder = DataAccessAnalysis.newBuilder()
    builder.dataAccessExists = metadata?.links?.size() > 0
    return builder.build()
  }

  //-- Helpers

  static Map stringInfo(String input) {
    return [
        value     : input,
        exists    : input != null && input.length() > 0,
        characters: input?.length() ?: 0,
        readingEase: input? ReadingLevel.FleschReadingEaseScore(input):null,
        gradeLevel: input? ReadingLevel.FleschKincaidReadingGradeLevel(input):null,
    ]
  }

  static Map dateInfo(String dateString, boolean start) {
    if (!dateString) {
      return [
          descriptor       : ValidDescriptor.UNDEFINED,
          precision        : null,
          indexable        : true,
          zoneSpecified    : null,
          utcDateTimeString: null
      ]
    }

    try {
      def parsedDate = dateString.isLong() && !indexable(dateString.toLong()) ? dateString.toLong() :
          PARSE_DATE_FORMATTER.parseBest(
              dateString,
              ZonedDateTime.&from as TemporalQuery,
              LocalDateTime.&from as TemporalQuery,
              LocalDate.&from as TemporalQuery,
              Year.&from as TemporalQuery)

      return [
          descriptor       : ValidDescriptor.VALID,
          precision        : precision(parsedDate),
          indexable        : indexable(parsedDate),
          zoneSpecified    : timezone(parsedDate),
          utcDateTimeString: utcDateTimeString(parsedDate, start)
      ]
    }
    catch (DateTimeParseException e) {
      return [
          descriptor       : ValidDescriptor.INVALID,
          precision        : null,
          indexable        : false,
          zoneSpecified    : null,
          utcDateTimeString: null
      ]
    }
  }

  static boolean indexable(Long year) {
    // Year must be in the range [-292_275_055, 292_278_994] in order to be parsed as a date by ES (Joda time magic number). However,
    // this number is a bit arbitrary, and prone to change when ES switches to the Java time library (minimum supported year
    // being -999,999,999). We will limit the year ourselves instead to -100,000,000 -- since this is a fairly safe bet for
    // supportability across many date libraries if the utcDateTime ends up used as is by a downstream app.
    return year >= -100_000_000L
  }

  static boolean indexable(TemporalAccessor date) {
    return true // if it's a parsable accessor, it's indexable
  }

  static String precision(Long year) {
    return ChronoUnit.YEARS.toString()
  }

  static String precision(TemporalAccessor date) {
    return date?.query(TemporalQueries.precision())?.toString()
  }

  static String timezone(date) {
    return date instanceof ZonedDateTime ? date.offset.toString() : null
  }

  static String utcDateTimeString(TemporalAccessor parsedDate, boolean start) {
    def modifiedDate
    switch (parsedDate) {
      case Year:
        modifiedDate = start ? parsedDate.atMonth(1).atDay(1).atStartOfDay() : parsedDate.atMonth(12).atEndOfMonth().atTime(23, 59, 59)
        modifiedDate = modifiedDate.atZone(ZoneOffset.UTC)
        break

      case LocalDate:
        modifiedDate = start ? parsedDate.atStartOfDay() : parsedDate.atTime(23, 59, 59)
        modifiedDate = modifiedDate.atZone(ZoneOffset.UTC)
        break

      case LocalDateTime:
        modifiedDate = parsedDate.atZone(ZoneOffset.UTC)
        break

      case ZonedDateTime:
        modifiedDate = parsedDate.withZoneSameInstant(ZoneOffset.UTC)
        break

      default:
        return null
    }
    return DateTimeFormatter.ISO_ZONED_DATE_TIME.format(modifiedDate)
  }

  static String utcDateTimeString(Long year, boolean start) {
    return start ? "${year}-01-01T00:00:00Z" : "${year}-12-31T23:59:59Z"
  }

  static TimeRangeDescriptor rangeDescriptor(Map beginInfo, Map endInfo, Map instantInfo) {
    def begin = beginInfo.descriptor
    def end = endInfo.descriptor
    def instant = instantInfo.descriptor

    if (begin == ValidDescriptor.VALID &&
        end == ValidDescriptor.VALID &&
        instant == ValidDescriptor.UNDEFINED) {
      switch (beginLTEEnd(beginInfo, endInfo)) {
        case true:
          return BOUNDED
        case false:
          return BACKWARDS
        case null:
          return INVALID
      }
    }
    if (begin == ValidDescriptor.VALID &&
        end == ValidDescriptor.UNDEFINED &&
        instant == ValidDescriptor.UNDEFINED  ) {
      return ONGOING
    }
    if (begin == ValidDescriptor.UNDEFINED &&
        end == ValidDescriptor.UNDEFINED &&
        instant == ValidDescriptor.VALID) {
      return INSTANT
    }
    if (begin == ValidDescriptor.UNDEFINED &&
        end == ValidDescriptor.UNDEFINED &&
        instant == ValidDescriptor.UNDEFINED) {
      return UNDEFINED
    }

    return INVALID
  }

  static Boolean beginLTEEnd(Map beginInfo, Map endInfo) {
    def beginIndexable = beginInfo.indexable == true
    def endIndexable = endInfo.indexable == true
    def beginIsYears = beginInfo.precision == ChronoUnit.YEARS.toString()
    def endIsYears = endInfo.precision == ChronoUnit.YEARS.toString()

    if (beginIndexable && endIndexable) {
      // Compare actual dates with UTC string
      def beginDate = ZonedDateTime.parse(beginInfo.utcDateTimeString)
      def endDate = ZonedDateTime.parse(endInfo.utcDateTimeString)
      return beginDate.isBefore(endDate) || beginDate.isEqual(endDate)
    }
    else if ((beginIsYears && endIsYears) || (beginIsYears && endIndexable) || (beginIndexable && endIsYears)) {
      // Compare years only as longs; parse both as string objects since both may not be just a long.
      // Watch out for negative years...
      def beginYearText = beginInfo.utcDateTimeString.substring(0, beginInfo.utcDateTimeString.indexOf('-', 1))
      def endYearText = endInfo.utcDateTimeString.substring(0, endInfo.utcDateTimeString.indexOf('-', 1))
      def beginYear = Long.parseLong(beginYearText)
      def endYear = Long.parseLong(endYearText)
      return beginYear <= endYear
    }
    else {
      // One or both has an INVALID search format that is not just due to a paleo year
      return null
    }
  }

}
