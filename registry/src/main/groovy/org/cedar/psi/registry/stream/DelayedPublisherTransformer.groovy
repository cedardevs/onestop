package org.cedar.psi.registry.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.KeyValueStore
import org.cedar.psi.registry.util.TimeFormatUtils

@Slf4j
@CompileStatic
class DelayedPublisherTransformer implements Transformer<String, String, KeyValue<String, String>> {
  private ProcessorContext context
  private KeyValueStore<Long, String> triggerTimesStore
  private KeyValueStore<String, Long> triggerKeysStore
  private KeyValueStore<String, String> lookupStore

  private String triggerTimesStoreName
  private String triggerKeysStoreName
  private String lookupStoreName
  private long interval


  DelayedPublisherTransformer(String triggerTimesStoreName, String triggerKeysStoreName, String lookupStoreName, long interval) {
    this.triggerTimesStoreName = triggerTimesStoreName
    this.triggerKeysStoreName = triggerKeysStoreName
    this.lookupStoreName = lookupStoreName
    this.interval = interval
  }

  @Override
  void init(ProcessorContext context) {
    this.context = context

    triggerTimesStore = (KeyValueStore<Long, String>) this.context.getStateStore(triggerTimesStoreName)
    triggerKeysStore = (KeyValueStore<String, Long>) this.context.getStateStore(triggerKeysStoreName)
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
    def storedPublishTime = triggerKeysStore.get(key)

    log.debug("transforming value with private ${publishingInfo?.private} and publish date ${publishDate}")
    if (publishingInfo?.private == true) {
      if (incomingPublishTime && incomingPublishTime > now) {
        log.debug("incoming publish time is in the future => store the publish time")
        addTrigger(incomingPublishTime, key)
        triggerKeysStore.put(key, incomingPublishTime)
      }
      else if (storedPublishTime) {
        log.debug("removing existing publish time for ${key}")
        removeTrigger(storedPublishTime, key)
        triggerKeysStore.delete(key)
      }
    }
    else if (storedPublishTime) {
      log.debug("incoming value is not private => removing existing publish time and lookup value for ${key}")
      removeTrigger(storedPublishTime, key)
      triggerKeysStore.delete(key)
    }
    return null
  }

  @Override
  void close() {
    triggerKeysStore.flush()
    triggerKeysStore.close()
    triggerTimesStore.flush()
    triggerTimesStore.close()
    lookupStore.flush()
    lookupStore.close()
  }

  void publishUpTo(long timestamp) {
    log.debug("publishing up to ${timestamp}")
    def slurper = new JsonSlurper()
    def iterator = this.triggerTimesStore.all()
    while (iterator.hasNext() && iterator.peekNextKey() <= timestamp) {
      def keyValue = iterator.next()
      def triggerTime = keyValue.key as Long
      def triggerKeys = deserializeList(keyValue.value as String)
      triggerKeys?.each { String triggerKey ->
        log.debug("found publish event for ${keyValue}")
        def lookupValue = lookupStore.get(triggerKey)
        if (lookupValue) {
          log.debug("looked up existing state for ${triggerKey}: ${lookupValue}")
          def valueMap = slurper.parseText(lookupValue) as Map
          def publishingInfo = valueMap.publishing as Map ?: [:]
          def publishDate = publishingInfo?.until ?: null
          def publishTimestamp = TimeFormatUtils.parseTimestamp(publishDate as String)
          if (publishTimestamp && publishTimestamp <= timestamp) {
            log.debug("current publish date for ${triggerKey} has passed => publish")
            context.forward(triggerKey, lookupValue)
          }
        }
        log.debug("removing publishing event")
        removeTrigger(triggerTime, triggerKey)
        if (triggerKeysStore.get(triggerKey) == triggerTime) {
          triggerKeysStore.delete(triggerKey)
        }
      }
    }
  }

  static List<String> deserializeList(String value) {
    return value ? new JsonSlurper().parseText(value) as List : null
  }

  List<String> getTrigger(Long key) {
    deserializeList(triggerTimesStore.get(key))
   }

  void addTrigger(Long key, String value) {
    def currentList = getTrigger(key) ?: []
    currentList.add(value)
    currentList.sort()
    def newString = JsonOutput.toJson(currentList)
    triggerTimesStore.put(key, newString)
  }

  void removeTrigger(Long key, String value) {
    def currentList = getTrigger(key)
    if (currentList == null) { return }
    currentList.remove(value)
    currentList.sort()
    if (currentList.isEmpty()) {
      triggerTimesStore.delete(key)
    }
    else {
      def newString = JsonOutput.toJson(currentList)
      triggerTimesStore.put(key, newString)
    }
  }

}
