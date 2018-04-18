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
    log.debug("transforming value for key ${key}")
    def now = System.currentTimeMillis()
    def slurper = new JsonSlurper()
    def valueMap = slurper.parseText(value) as Map
    def publishingInfo = valueMap.publishing as Map ?: [:]
    def publishDate = publishingInfo.date ?: null
    def incomingPublishTime = parseTimestamp(publishDate as String)
    def storedPublishTime = lookupCurrentPublishTimestamp(key)

    if (publishDate) {
      log.debug("transforming value with publish date ${publishDate}")
      if (incomingPublishTime > now) {
        log.debug("incoming publish time is in the future => set private to true and store the publish time")
        valueMap.publishing = publishingInfo + [private: true]
        publishTimestampStore.put(incomingPublishTime, key)

        if (storedPublishTime && storedPublishTime <= now) {
          log.debug("current stored value for ${key} may have already been published => emit a tombstone")
          context.forward(key, null)
        }
      }
      else {
        log.debug("incoming publish time is in the past => set private false")
        valueMap.publishing = publishingInfo + [private: false]
        if (storedPublishTime) {
          log.debug("removing existing publish time for ${key}")
          publishTimestampStore.delete(storedPublishTime)
        }
      }
    }
    else {
      if (publishingInfo?.containsKey('private')) {
        if (publishingInfo.private == false && storedPublishTime) {
          log.debug("incoming value is not private => removing existing publish time for ${key}")
          publishTimestampStore.delete(storedPublishTime)
        }
      }
      else {
        log.debug("incoming value has no publishing info => default to private: false")
        valueMap.publishing = publishingInfo + [private: false]
      }
    }

    def finalValue = JsonOutput.toJson(valueMap)
    lookupStore.put(key, finalValue)
    return KeyValue.pair(key, finalValue)
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
          log.debug("current publish date for ${lookupId} has passed => update store and publish")
          valueMap.publishing = publishingInfo + [private: false]
          def newValue = JsonOutput.toJson(valueMap)
          lookupStore.put(lookupId, newValue)
          context.forward(lookupId, newValue)
        }
      }
      log.debug("removing publishing event")
      publishTimestampStore.delete(keyValue.key as Long)
    }
  }

  Long lookupCurrentPublishTimestamp(String key) {
    return extractTimestamp(lookupStore.get(key))
  }

  static Long extractTimestamp(String json) {
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

  static Long parseTimestamp(String timeString) {
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
