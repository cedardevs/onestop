package org.cedar.onestop.indexer.stream;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;
import org.cedar.onestop.indexer.util.ElasticsearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class SitemapTriggerProcessor implements Processor<String, Long> {
  private static final Logger log = LoggerFactory.getLogger(SitemapTriggerProcessor.class);
  private static final String constantKey = "update_until";

  private final ElasticsearchService service;
  private final String storeName;
  private final Duration interval;

  private KeyValueStore<String, Long> store;

  public SitemapTriggerProcessor(String storeName, ElasticsearchService service, Duration interval) {
    this.storeName = storeName;
    this.service = service;
    this.interval = interval;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void init(ProcessorContext context) {
    this.store = (KeyValueStore<String, Long>) context.getStateStore(storeName);
    context.schedule(interval, PunctuationType.WALL_CLOCK_TIME, timestamp -> triggerSitemap());
  }

  @Override
  public void process(String key, Long value) {
    log.debug("processing sitemap trigger with key [" + key + "] and value [" + value + "]");
    if (value == null) {
      log.debug("deleting stored timestamp");
      store.delete(constantKey);
      return;
    }
    var curr = store.get(constantKey);
    var next = curr == null ? value : Math.max(curr, value);
    log.debug("updating stored timestamp to [" + next + "]");
    store.put(constantKey, next);
  }

  private void triggerSitemap() {
    var timestamp = store.get(constantKey);
    if (timestamp != null) {
      service.buildSitemap(timestamp);
    }
  }

  @Override
  public void close() {
    // nothing to do
  }

}
