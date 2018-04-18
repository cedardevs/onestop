package org.cedar.psi.registry.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.KeyValueStore

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.temporal.TemporalQuery


@Slf4j
class DelayedPublisherTransformer implements Transformer<String, String, KeyValue<String, String>> {
  // handle 3 optional date formats in priority of full-parse option to minimal-parse options
  private static final DateTimeFormatter PARSE_DATE_FORMATTER = new DateTimeFormatterBuilder()
      .appendOptional(DateTimeFormatter.ISO_ZONED_DATE_TIME)  // e.g. - 2010-12-30T00:00:00Z
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)  // e.g. - 2010-12-30T00:00:00
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)       // e.g. - 2010-12-30
      .toFormatter()
      .withResolverStyle(ResolverStyle.STRICT)

  private static final ZoneId UTC_ID = ZoneId.of('UTC')

  private ProcessorContext context
  private KeyValueStore<Long, String> publishTimestampStore
  private KeyValueStore<String, String> lookupStore

  private String publishTimestampStoreName
  private String lookupStoreName
  private long interval

  DelayedPublisherTransformer(String publishTimestampStoreName, String lookupStoreName, long interval) {
    this.publishTimestampStoreName = publishTimestampStoreName
    this.lookupStoreName = lookupStoreName
    this.interval = interval
  }

  @Override
  void init(ProcessorContext context) {
    this.context = context
    publishTimestampStore = (KeyValueStore<Long, String>) this.context.getStateStore(publishTimestampStoreName)
    lookupStore = (KeyValueStore<String, String>) this.context.getStateStore(lookupStoreName)
    this.context.schedule(interval, PunctuationType.WALL_CLOCK_TIME, this.&publishUpTo)
  }

  @Override
  KeyValue<String, String> transform(String key, String value) {
    def now = System.currentTimeMillis()
    def slurper = new JsonSlurper()

    def valueMap = slurper.parseText(value) as Map
    def publishingInfo = valueMap.publishing as Map ?: [:]
    def publishDate = publishingInfo.date ?: null
    if (publishDate) {
      def publishTimestamp = parseTimestamp(publishDate as String)
      if (publishTimestamp > now) {
        // incoming value has future publish time => set private false and store the publish time
        valueMap.publishing = publishingInfo + [private: true]
        publishTimestampStore.put(publishTimestamp, key)

        def lookupValue = lookupStore.get(key)
        if (lookupValue) {
          def lookupMap = slurper.parseText(lookupValue) as Map
          def lookupPublishingInfo = lookupMap.publishing as Map ?: [:]
          def lookupPublishDate = lookupPublishingInfo?.date ?: null
          def lookupPublishTimestamp = parseTimestamp(lookupPublishDate as String)
          if (lookupPublishTimestamp <= now) {
            // current stored value may already have been published => emit a null to delete it downstream
            this.context.forward(key, null)
          }
        }
      }
      else {
        // incoming value has publish time in past => set private true and ensure publish time is not stored
        valueMap.publishing = publishingInfo + [private: false]
        publishTimestampStore.delete(publishTimestamp)
      }
    }
    else {
      valueMap.publishing = publishingInfo + [private: false]
    }

    return KeyValue.pair(key, JsonOutput.toJson(valueMap))
  }

  @Override
  @Deprecated
  KeyValue<String, String> punctuate(long timestamp) {
    // do nothing
  }

  @Override
  void close() {
    publishTimestampStore.flush()
    publishTimestampStore.close()
    lookupStore.flush()
    lookupStore.close()
  }

  void publishUpTo(long timestamp) {
    log.debug("publishing up to ${timestamp}")
    def slurper = new JsonSlurper()
    def iterator = this.publishTimestampStore.all()
    while (iterator.hasNext() && iterator.peekNextKey() <= timestamp) {
      def keyValue = iterator.next()
      def lookupId = keyValue.value as String
      log.debug("found publish event for ${keyValue}")
      def lookupValue = lookupStore.get(lookupId)
      if (lookupValue) {
        log.debug("looked up existing state for ${lookupId}: ${lookupValue}")
        def valueMap = slurper.parseText(lookupValue) as Map
        def publishingInfo = valueMap.publishing as Map ?: [:]
        def publishDate = publishingInfo?.date ?: null
        def publishTimestamp = parseTimestamp(publishDate as String)
        if (publishTimestamp <= timestamp) {
          log.debug("current publish date for ${lookupId} has passed, publishing")
          valueMap.publishing = publishingInfo + [private: false]
          this.context.forward(lookupId, JsonOutput.toJson(valueMap))
        }
      }
      log.debug("removing publishing event")
      publishTimestampStore.delete(keyValue.key as Long)
    }
  }

  Long lookupCurrentPublishTimestamp(String key) {
    return extractTimestamp(lookupStore.get(key))
  }

  Long extractTimestamp(String json) {
    if (json) {
      def slurper = new JsonSlurper()
      def valueMap = slurper.parseText(json) as Map
      def publishingInfo = valueMap.publishing as Map ?: [:]
      def publishDate = publishingInfo?.date ?: null
      if (publishDate) {
        return parseTimestamp(publishDate as String)
      }
    }
    return null
  }

  Long parseTimestamp(String timeString) {
    log.debug("parsing time string: $timeString")
    if (!timeString) {
      return 0
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
}
