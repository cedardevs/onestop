package org.cedar.onestop.indexer.util;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

public class BulkIndexingTransformer implements Transformer<String, DocWriteRequest, KeyValue<String, BulkItemResponse>> {
  private static final Logger log = LoggerFactory.getLogger(BulkIndexingTransformer.class);

  private final RestHighLevelClient client;
  private final Duration maxPublishInterval;
  private final long maxPublishBytes;

  private ProcessorContext context;
  private BulkRequest request;

  public BulkIndexingTransformer(RestHighLevelClient client, Duration maxPublishInterval, long maxPublishBytes) {
    this.client = client;
    this.maxPublishInterval = maxPublishInterval;
    this.maxPublishBytes = maxPublishBytes;
  }

  @Override
  public void init(ProcessorContext context) {
    this.context = context;
    this.request = new BulkRequest();
    this.context.schedule(maxPublishInterval, PunctuationType.WALL_CLOCK_TIME, timestamp -> flushRequest());
  }

  @Override
  public KeyValue<String, BulkItemResponse> transform(String key, DocWriteRequest value) {
    request.add(value);
    if (request.estimatedSizeInBytes() >= maxPublishBytes) {
      flushRequest();
    }
    return null; // does not return new values directly, instead forwards results via context when a bulk request is flushed
  }

  private void flushRequest() {
    int numActions = request.numberOfActions();
    if (numActions == 0) {
      return;
    }
    try {
      log.debug("Submitting bulk request wth [" + numActions + "] actions");
      var response = client.bulk(request, RequestOptions.DEFAULT);
      log.info("Completed bulk request wth [" + numActions + "] actions in [ " + response.getTook() + "]");
      response.iterator().forEachRemaining(item -> {
        var id = item.getId();
        context.forward(id, item);
        if (item.isFailed()) {
          log.warn(item.getOpType() + " record with key [" + id + "] and index [" + item.getIndex() + "] failed: "
              + item.getFailureMessage(), item.getFailure().getCause());
        }
        else {
          log.info(item.getOpType() + " record with key [" + id + "] and index [" + item.getIndex() + "] succeeded");
        }
      });
      this.context.commit();
    }
    catch (IOException e) {
      log.error("Failed to execute bulk request wth [" + numActions + "] actions", e);
    }
    finally {
      this.request = new BulkRequest();
    }
  }

  @Override
  public void close() {
    // nothing to do here
  }
}
