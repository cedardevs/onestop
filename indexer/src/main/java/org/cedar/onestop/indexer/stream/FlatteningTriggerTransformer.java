package org.cedar.onestop.indexer.stream;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.indexer.util.ElasticsearchService;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

public class FlatteningTriggerTransformer implements Transformer<Windowed<String>, Long, KeyValue<String, FlatteningTriggerTransformer.FlatteningTriggerResult>> {
  private static final Logger log = LoggerFactory.getLogger(FlatteningTriggerTransformer.class);

  private final ElasticsearchService service;
  private final ElasticsearchConfig config;
  private final String flatteningScript;

  private ProcessorContext context;

  public FlatteningTriggerTransformer(ElasticsearchService esService, String flatteningScript) {
    this.service = esService;
    this.config = esService.getConfig();
    this.flatteningScript = flatteningScript;
  }

  @Override
  public void init(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public KeyValue<String, FlatteningTriggerResult> transform(Windowed<String> collectionWindow, Long timeToFlattenFrom) {
    var collectionId = collectionWindow.key();
    try {
      triggerFlattening(collectionId, timeToFlattenFrom);
      return null; // produces results asynchronously when flattening job completes
    }
    catch(IOException e) {
      throw new IllegalStateException("Elasticsearch request failed while triggering flattening for collection " + collectionId, e);
    }
  }

  private void triggerFlattening(String collectionId, Long timeToFlattenFrom) throws IOException {
    var collectionResponse = service.get(config.COLLECTION_SEARCH_INDEX_ALIAS, collectionId); // TODO -- uuuggghh this sucks!
    var collectionBody = collectionResponse.getSourceAsMap();

    var params = new HashMap<String, Object>();
    params.put("defaults", collectionBody);
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
    service.reindexAsync(request, new ActionListener<>() {
      @Override
      public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
        log.debug("successfully flattened granules from collection [" + collectionId + "] updated since [" + timeToFlattenFrom + "]");
        context.forward(collectionId, new FlatteningTriggerResult(true, timeToFlattenFrom));
      }
      @Override
      public void onFailure(Exception e) {
        log.error("failed to flatten granules from collection [" + collectionId + "] updated since [" + timeToFlattenFrom + "]", e);
        context.forward(collectionId, new FlatteningTriggerResult(false, timeToFlattenFrom));
      }
    });
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
