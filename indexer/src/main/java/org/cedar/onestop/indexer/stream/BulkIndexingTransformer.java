package org.cedar.onestop.indexer.stream;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.cedar.onestop.indexer.util.BulkIndexingConfig;
import org.cedar.onestop.indexer.util.ElasticsearchService;
import org.cedar.onestop.indexer.util.IndexingHelpers;
import org.cedar.onestop.indexer.util.IndexingOutput;
import org.cedar.schemas.analyze.Analyzers;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.parse.DefaultParser;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.elasticsearch.action.DocWriteRequest.OpType.*;

public class BulkIndexingTransformer implements Transformer<String, ValueAndTimestamp<ParsedRecord>, KeyValue<String, IndexingOutput>> {
  private static final Logger log = LoggerFactory.getLogger(BulkIndexingTransformer.class);

  private final String storeName;
  private final ElasticsearchService client;
  private final BulkIndexingConfig config;

  private KeyValueStore<String, ValueAndTimestamp<ParsedRecord>> store;
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
    this.store = (KeyValueStore<String, ValueAndTimestamp<ParsedRecord>>) this.context.getStateStore(storeName);
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
    var requests = mapRecordToRequests(context.topic(), key, record);
    requests.forEach(request::add);
    if (request.estimatedSizeInBytes() >= config.getMaxPublishBytes()) {
      log.debug("flushing request due to size: {} >= {}", request.estimatedSizeInBytes(), config.getMaxPublishBytes());
      flushRequest();
    }
    return null; // outputs are forwarded later when bulk request is flushed
  }

  private List<DocWriteRequest> mapRecordToRequests(String topic, String id, ValueAndTimestamp<ParsedRecord> value) {
    try {
      var record = ValueAndTimestamp.getValueOrNull(value);
      var operation = (IndexingHelpers.isTombstone(record) || IndexingHelpers.isPrivate(record)) ? DELETE : INDEX;
      var indices = config.getTargetIndices(topic, operation);
      return indices.stream()
          .map(indexName -> buildWriteRequest(indexName, operation, id, value))
          .collect(Collectors.toList());
    } catch (ElasticsearchGenerationException e) {
      log.error("failed to serialize record with key [" + id + "] to json", e);
      return new ArrayList<>();
    }
  }

  private DocWriteRequest buildWriteRequest(String indexName, DocWriteRequest.OpType opType, String id, ValueAndTimestamp<ParsedRecord> record) {
    if (opType == DELETE) {
      return new DeleteRequest(indexName).id(id);
    }

    var formattedRecord = IndexingHelpers.reformatMessageForSearch(record.value());
    formattedRecord.put("stagedDate", record.timestamp());

    if (opType == INDEX || opType == CREATE) {
      return new IndexRequest(indexName).opType(opType).id(id).source(formattedRecord);
    }
    if (opType == UPDATE) {
      return new UpdateRequest(indexName, id).doc(formattedRecord);
    }
    throw new UnsupportedOperationException("unsupported elasticsearch OpType: " + opType);
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
