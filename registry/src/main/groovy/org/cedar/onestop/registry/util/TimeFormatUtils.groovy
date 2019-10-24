package org.cedar.onestop.registry.util

import groovy.util.logging.Slf4j

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.temporal.TemporalQuery

@Slf4j
class TimeFormatUtils {

  private static final ZoneId UTC_ID = ZoneId.of('UTC')

  // handle 3 optional date formats in priority of full-parse option to minimal-parse options
  private static final DateTimeFormatter PARSE_DATE_FORMATTER = new DateTimeFormatterBuilder()
      .appendOptional(DateTimeFormatter.ISO_ZONED_DATE_TIME)  // e.g. - 2010-12-30T00:00:00Z
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)  // e.g. - 2010-12-30T00:00:00
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)       // e.g. - 2010-12-30
      .toFormatter()
      .withResolverStyle(ResolverStyle.STRICT)

  static Long parseTimestamp(String timeString) {
    log.debug("parsing time string: $timeString")
    if (!timeString) {
      return null
    }

    // the "::" operator in Java 8 is ".&" in groovy 2
    def parsedDate = PARSE_DATE_FORMATTER.parseBest(timeString,
        ZonedDateTime.&from as TemporalQuery,
        LocalDateTime.&from as TemporalQuery,
        LocalDate.&from as TemporalQuery)

    if (parsedDate instanceof LocalDate) {
      parsedDate = parsedDate.atStartOfDay(UTC_ID)
    }
    if (parsedDate instanceof LocalDateTime) {
      parsedDate = parsedDate.atZone(UTC_ID)
    }

    def result = Instant.from(parsedDate).toEpochMilli()
    log.debug("parsed epoch millis: ${result}")
    return result
  }

  static String formatTimestamp(Long timestamp) {
    return PARSE_DATE_FORMATTER.format(Instant.ofEpochMilli(timestamp))
  }

}
