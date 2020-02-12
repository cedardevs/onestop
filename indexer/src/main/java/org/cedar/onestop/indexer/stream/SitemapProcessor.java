package org.cedar.onestop.indexer.stream;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;
import org.cedar.onestop.indexer.util.ElasticsearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class SitemapProcessor implements Processor<String, Long> {
  private static final Logger log = LoggerFactory.getLogger(SitemapProcessor.class);
  private static final String constantKey = "update_until";

  private final ElasticsearchService service;
  private final String storeName;
  private final Duration interval;

  private KeyValueStore<String, Long> store;

  public SitemapProcessor(ElasticsearchService service, SitemapConfig config) {
    this.storeName = config.getStoreName();
    this.service = service;
    this.interval = config.getInterval();
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
    var storeState = store.get(constantKey);
    var currentValue = storeState != null ? storeState : 0L;
    var incomingValue = value != null ? value : currentValue + 1;
    var nextValue = Math.max(currentValue, incomingValue);
    log.debug("updating stored timestamp to [" + nextValue + "]");
    store.put(constantKey, nextValue);
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
