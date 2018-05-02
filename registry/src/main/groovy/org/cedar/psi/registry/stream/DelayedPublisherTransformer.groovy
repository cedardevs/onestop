package org.cedar.psi.registry.stream

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.KeyValueStore
import org.cedar.psi.registry.util.TimeFormatUtils

@Slf4j
class DelayedPublisherTransformer implements Transformer<String, String, KeyValue<String, String>> {
  private ProcessorContext context
  private KeyValueStore<Long, String> triggerStore
  private KeyValueStore<String, String> lookupStore

  private String triggerStoreName
  private String lookupStoreName
  private long interval


  DelayedPublisherTransformer(String triggerStoreName, String lookupStoreName, long interval) {
    this.triggerStoreName = triggerStoreName
    this.lookupStoreName = lookupStoreName
    this.interval = interval
  }

  @Override
  void init(ProcessorContext context) {
    this.context = context

    triggerStore = (KeyValueStore<Long, String>) this.context.getStateStore(triggerStoreName)
    lookupStore = (KeyValueStore<String, String>) this.context.getStateStore(lookupStoreName)

    this.context.schedule(interval, PunctuationType.WALL_CLOCK_TIME, this.&publishUpTo)
  }

  @Override
  KeyValue<String, String> transform(String key, String value) {
    log.debug("transforming value for key ${key}")
    def now = context.timestamp()
    def slurper = new JsonSlurper()
    def valueMap = slurper.parseText(value) as Map
    def publishingInfo = valueMap.publishing as Map ?: [:]
    def publishDate = publishingInfo.until ?: null
    def incomingPublishTime = TimeFormatUtils.parseTimestamp(publishDate as String)
    def storedPublishTime = lookupCurrentPublishTimestamp(key)

    log.debug("transforming value with private ${publishingInfo?.private} and publish date ${publishDate}")
    if (publishingInfo?.private == true) {
      if (incomingPublishTime && incomingPublishTime > now) {
        log.debug("incoming publish time is in the future => store the publish time")
        triggerStore.put(incomingPublishTime, key)
      }
      else if (storedPublishTime) {
        log.debug("removing existing publish time for ${key}")
        triggerStore.delete(storedPublishTime)
      }
    }
    else if (storedPublishTime) {
      log.debug("incoming value is not private => removing existing publish time and lookup value for ${key}")
      triggerStore.delete(storedPublishTime)
    }
    return null
  }

  @Override
  @Deprecated
  KeyValue<String, String> punctuate(long timestamp) {
    // do nothing
  }

  @Override
  void close() {
    triggerStore.flush()
    triggerStore.close()
    lookupStore.flush()
    lookupStore.close()
  }

  void publishUpTo(long timestamp) {
    log.debug("publishing up to ${timestamp}")
    def slurper = new JsonSlurper()
    def iterator = this.triggerStore.all()
    while (iterator.hasNext() && iterator.peekNextKey() <= timestamp) {
      def keyValue = iterator.next()
      def lookupId = keyValue.value as String
      log.debug("found publish event for ${keyValue}")
      def lookupValue = lookupStore.get(lookupId)
      if (lookupValue) {
        log.debug("looked up existing state for ${lookupId}: ${lookupValue}")
        def valueMap = slurper.parseText(lookupValue) as Map
        def publishingInfo = valueMap.publishing as Map ?: [:]
        def publishDate = publishingInfo?.until ?: null
        def publishTimestamp = TimeFormatUtils.parseTimestamp(publishDate as String)
        if (publishTimestamp && publishTimestamp <= timestamp) {
          log.debug("current publish date for ${lookupId} has passed => publish")
          context.forward(lookupId, lookupValue)
        }
      }
      log.debug("removing publishing event")
      triggerStore.delete(keyValue.key as Long)
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
      def publishDate = publishingInfo?.until ?: null
      if (publishDate) {
        return TimeFormatUtils.parseTimestamp(publishDate as String)
      }
    }
    return null
  }

}
