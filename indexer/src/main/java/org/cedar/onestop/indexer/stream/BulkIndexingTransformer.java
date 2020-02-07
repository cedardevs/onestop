package org.cedar.onestop.indexer.stream;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.TimestampedKeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.cedar.onestop.indexer.util.BulkIndexingConfig;
import org.cedar.onestop.indexer.util.ElasticsearchService;
import org.cedar.onestop.indexer.util.IndexingHelpers;
import org.cedar.onestop.indexer.util.IndexingOutput;
import org.cedar.schemas.analyze.Analyzers;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.parse.DefaultParser;
import org.elasticsearch.action.bulk.BulkRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BulkIndexingTransformer implements Transformer<String, ValueAndTimestamp<ParsedRecord>, KeyValue<String, IndexingOutput>> {
  private static final Logger log = LoggerFactory.getLogger(BulkIndexingTransformer.class);

  private final String storeName;
  private final ElasticsearchService client;
  private final BulkIndexingConfig config;

  private TimestampedKeyValueStore<String, ParsedRecord> store;
  private ProcessorContext context;
  private BulkRequest request;

  public BulkIndexingTransformer(String storeName, ElasticsearchService client, BulkIndexingConfig config) {
    this.storeName = storeName;
    this.client = client;
    this.config = config;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void init(ProcessorContext context) {
    this.context = context;
    this.store = (TimestampedKeyValueStore<String, ParsedRecord>) this.context.getStateStore(storeName);
    this.request = new BulkRequest();
    this.context.schedule(config.getMaxPublishInterval(), PunctuationType.WALL_CLOCK_TIME, timestamp -> flushRequest());
  }

  @Override
  public KeyValue<String, IndexingOutput> transform(String key, ValueAndTimestamp<ParsedRecord> record) {
    var filledInRecord = DefaultParser.fillInDefaults(record != null ? record.value() : null);
    var analyzedRecord = Analyzers.addAnalysis(filledInRecord);
    var validationResult = IndexingHelpers.validateMessage(key, analyzedRecord);
    if (analyzedRecord != null && !((Boolean) validationResult.get("valid"))) {
      var output = new IndexingOutput(false, record, null);
      return new KeyValue<>(key, output);
    }

    store.put(key, record);
    var requests = IndexingHelpers.mapRecordToRequests(context.topic(), key, record, config);
    requests.forEach(request::add);
    if (request.estimatedSizeInBytes() >= config.getMaxPublishBytes()) {
      log.debug("flushing request due to size: {} >= {}", request.estimatedSizeInBytes(), config.getMaxPublishBytes());
      flushRequest();
    }
    return null; // outputs are forwarded later when bulk request is flushed
  }

  private void flushRequest() {
    int numActions = request.numberOfActions();
    if (numActions == 0) {
      return;
    }
    try {
      log.info("submitting bulk request with [" + numActions + "] actions and approximately [" + request.estimatedSizeInBytes() + "] bytes");
      var response = client.bulk(request);
      log.info("completed bulk request with [" + numActions + "] actions in [" + response.getTook() + "]");
      response.iterator().forEachRemaining(item -> {
        var id = item.getId();
        if (item.isFailed()) {
          log.warn(item.getOpType() + " record with key [" + id + "] and index [" + item.getIndex() + "] failed: "
              + item.getFailureMessage(), item.getFailure().getCause());
        } else {
          log.debug(item.getOpType() + " record with key [" + id + "] and index [" + item.getIndex() + "] succeeded");
        }
        var record = store.get(id);
        var result = new IndexingOutput(true, record, item);
        context.forward(id, result);
        store.delete(id);
      });
      this.context.commit();
    } catch (IOException e) {
      log.error("failed to execute bulk request wth [" + numActions + "] actions", e);
      this.request.requests().forEach(docWriteRequest -> this.store.delete(docWriteRequest.id()));
    } finally {
      this.request = new BulkRequest();
    }
  }

  @Override
  public void close() {
    // nothing to do here
  }

}
