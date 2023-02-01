package org.cedar.onestop.registry.stream;

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.TimestampedKeyValueStore;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.Duration;

@SuppressWarnings("unchecked")
public class DelayedPublisherTransformer implements Transformer<String, ParsedRecord, KeyValue<String, ParsedRecord>> {
  private static final Logger log = LoggerFactory.getLogger(DelayedPublisherTransformer.class);

  private ProcessorContext context;
  private KeyValueStore<Long, String> triggerTimesStore;
  private KeyValueStore<String, Long> triggerKeysStore;
  private TimestampedKeyValueStore<String, ParsedRecord> lookupStore;

  private String triggerTimesStoreName;
  private String triggerKeysStoreName;
  private String lookupStoreName;
  private long interval;


  DelayedPublisherTransformer(String triggerTimesStoreName, String triggerKeysStoreName, String lookupStoreName, long interval) {
    this.triggerTimesStoreName = triggerTimesStoreName;
    this.triggerKeysStoreName = triggerKeysStoreName;
    this.lookupStoreName = lookupStoreName;
    this.interval = interval;
  }

  @Override
  public void init(ProcessorContext context) {
    this.context = context;

    triggerTimesStore = (KeyValueStore<Long, String>) this.context.getStateStore(triggerTimesStoreName);
    triggerKeysStore = (KeyValueStore<String, Long>) this.context.getStateStore(triggerKeysStoreName);
    lookupStore = (TimestampedKeyValueStore<String, ParsedRecord>) this.context.getStateStore(lookupStoreName);

    this.context.schedule(Duration.ofMillis(interval), PunctuationType.WALL_CLOCK_TIME, this::publishUpTo);
  }

  @Override
  public KeyValue<String, ParsedRecord> transform(String key, ParsedRecord value) {
    log.debug("transforming value for key {}", key);
    var now = context.timestamp();
    var publishingInfo = value != null ? value.getPublishing() : null;
    var incomingPublishTime = publishingInfo != null ? publishingInfo.getUntil() : null;
    var storedPublishTime = triggerKeysStore.get(key);
    boolean isPrivate = publishingInfo != null ? publishingInfo.getIsPrivate() : false;

    log.debug("transforming value with private {} and publish date {}", isPrivate, incomingPublishTime);
    if (isPrivate) {
      if (incomingPublishTime != null && incomingPublishTime > now) {
        log.debug("incoming publish time is in the future => store the publish time");
        addTrigger(incomingPublishTime, key);
        triggerKeysStore.put(key, incomingPublishTime);
      }
      else if (storedPublishTime != null) {
        log.debug("removing existing publish time for {}", key);
        removeTrigger(storedPublishTime, key);
        triggerKeysStore.delete(key);
      }
    }
    else if (storedPublishTime != null) {
      log.debug("incoming value is not private => removing existing publish time and lookup value for {}", key);
      removeTrigger(storedPublishTime, key);
      triggerKeysStore.delete(key);
    }
    return null;
  }

  @Override
  public void close() {
    triggerKeysStore.flush();
    triggerTimesStore.flush();
    lookupStore.flush();
  }

  void publishUpTo(long timestamp) {
    log.debug("publishing up to {}", timestamp);
    var iterator = this.triggerTimesStore.all();
    while (iterator.hasNext() && iterator.peekNextKey() <= timestamp) {
      var keyValue = iterator.next();
      var triggerTime = keyValue.key;
      var triggerKeys = deserializeList(keyValue.value);
      triggerKeys.forEach( triggerKey -> {
        log.debug("found publish event for {}", keyValue);
        var value = lookupStore.get(triggerKey).value();
        if (value != null) {
          log.debug("looked up existing state for {}: {}", triggerKey, value);
          var publishingInfo = value != null ? value.getPublishing() : null;
          var publishTimestamp = publishingInfo != null ? publishingInfo.getUntil() : null;
          if (publishTimestamp != null && publishTimestamp <= timestamp) {
            log.debug("current publish date for {} has passed => publish", triggerKey);
            context.forward(triggerKey, value);
          }
        }
        log.debug("removing publishing event");
        removeTrigger(triggerTime, triggerKey);
        if (triggerKeysStore.get(triggerKey) == triggerTime) {
          triggerKeysStore.delete(triggerKey);
        }
      });
    }
  }

  static List<String> deserializeList(String value) {
    return value != null ? (List<String>) new JsonSlurper().parseText(value) : new ArrayList<>();
  }

  List<String> getTrigger(Long key) {
    return deserializeList(triggerTimesStore.get(key));
  }

  void addTrigger(Long key, String value) {
    var currentList = getTrigger(key);
    currentList.add(value);
    currentList.sort(Comparator.naturalOrder());
    var newString = JsonOutput.toJson(currentList);
    triggerTimesStore.put(key, newString);
  }

  void removeTrigger(Long key, String value) {
    var currentList = getTrigger(key);
    if (currentList == null) { return; }
    currentList.remove(value);
    currentList.sort(Comparator.naturalOrder());
    if (currentList.isEmpty()) {
      triggerTimesStore.delete(key);
    }
    else {
      var newString = JsonOutput.toJson(currentList);
      triggerTimesStore.put(key, newString);
    }
  }

}
