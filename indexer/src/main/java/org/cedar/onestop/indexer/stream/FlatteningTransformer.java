package org.cedar.onestop.indexer.stream;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.indexer.util.ElasticsearchService;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class FlatteningTransformer implements Transformer<String, Long, KeyValue<String, FlatteningTransformer.FlatteningTriggerResult>> {
  private static final Logger log = LoggerFactory.getLogger(FlatteningTransformer.class);

  private final String storeName;
  private final ElasticsearchService service;
  private final ElasticsearchConfig config;
  private final String flatteningScript;
  private final Duration interval;
  private final Map<String, Cancellable> requestsInFlight;

  private ProcessorContext context;
  private KeyValueStore<String, Long> store;

  public FlatteningTransformer(ElasticsearchService esService, FlatteningConfig config) {
    this.storeName = config.getStoreName();
    this.service = esService;
    this.config = esService.getConfig();
    this.flatteningScript = config.getScript();
    this.interval = config.getInterval();
    this.requestsInFlight = new HashMap<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void init(ProcessorContext context) {
    this.context = context;
    this.store = (KeyValueStore<String, Long>) this.context.getStateStore(storeName);
    this.context.schedule(interval, PunctuationType.WALL_CLOCK_TIME, timestamp -> triggerAll());
  }

  @Override
  public KeyValue<String, FlatteningTriggerResult> transform(String collectionId, Long timeToFlattenFrom) {
    if (collectionId == null) {
      return null;
    }
    if (timeToFlattenFrom == null) {
      store.delete(collectionId);
      return null;
    }
    var curr = store.get(collectionId);
    var next = curr == null ? timeToFlattenFrom : Math.min(curr, timeToFlattenFrom);
    store.put(collectionId, next);
    return null;
  }

  private void triggerAll() {
    store.all().forEachRemaining(kv -> {
      try {
        if (service.currentRunningTasks() < config.MAX_TASKS) {
          triggerFlattening(kv.key, kv.value);
          store.delete(kv.key);
        }
      }
      catch(IOException e) {
        log.error("Triggering flattening for collection ["+ kv.key + "] starting at [" + kv.value +"] failed", e);
        context.forward(kv.key, new FlatteningTriggerResult(false, kv.value));
      }
    });
  }

  private void triggerFlattening(String collectionId, Long timeToFlattenFrom) throws IOException {
    // If an existing request is in flight, cancel it and start a new one
    if (requestsInFlight.containsKey(collectionId)) {
      requestsInFlight.remove(collectionId).cancel();
    }

    // Unfortunately we have to retrieve the collection to provide the flattening script parameters
    var collectionResponse = service.get(config.COLLECTION_SEARCH_INDEX_ALIAS, collectionId);
    if (!collectionResponse.isExists()) {
      return; // no parent collection => no flattening
    }

    var params = new HashMap<String, Object>();
    params.put("defaults", collectionResponse.getSourceAsMap());
    params.put("stagedDate", timeToFlattenFrom);
    var script = new Script(ScriptType.INLINE, "painless", flatteningScript, params);

    var query = QueryBuilders.boolQuery()
        .filter(QueryBuilders.termQuery("internalParentIdentifier", collectionId))
        .filter(QueryBuilders.rangeQuery("stagedDate").gte(timeToFlattenFrom));

    var request = new ReindexRequest();
    request.setSourceIndices(config.GRANULE_SEARCH_INDEX_ALIAS);
    request.setSourceQuery(query);
    request.setDestIndex(config.FLAT_GRANULE_SEARCH_INDEX_ALIAS);
    request.setScript(script);
    request.setConflicts("proceed");
    if (config.REQUESTS_PER_SECOND != null) {
      request.setRequestsPerSecond(config.REQUESTS_PER_SECOND);
    }

    log.debug("starting flattening for granules from collection [" + collectionId + "] updated since [" + timeToFlattenFrom + "]");
    var reindexCancellable = service.reindexAsync(request, ActionListener.wrap(
        response -> {
          var numFailures = response.getBulkFailures().size();
          var successful = numFailures == 0;
          log.info("flattened granules from collection [" + collectionId + "] updated since [" + timeToFlattenFrom + "] with [" + numFailures + "] failures");
          context.forward(collectionId, new FlatteningTriggerResult(successful, timeToFlattenFrom));
        },
        error -> {
          log.error("failed to flatten granules from collection [" + collectionId + "] updated since [" + timeToFlattenFrom + "]", error);
        }));
    requestsInFlight.put(collectionId, reindexCancellable);
  }

  @Override
  public void close() {
    // nothing to do
  }

  public static class FlatteningTriggerResult {
    public final boolean successful;
    public final long timestamp;

    private FlatteningTriggerResult(boolean successful, long timestamp) {
      this.successful = successful;
      this.timestamp = timestamp;
    }
  }

}
