package org.cedar.onestop.indexer;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Materialized;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.indexer.util.IndexingHelpers;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.constants.Topics;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.RecordType;
import org.cedar.schemas.avro.psi.Relationship;
import org.cedar.schemas.avro.psi.RelationshipType;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class SearchIndexTopology {
  private static final Logger log = LoggerFactory.getLogger(SearchIndexTopology.class);

  Topology buildSearchIndexTopology(StreamsBuilder streamsBuilder, RestHighLevelClient client, ElasticsearchConfig config) {
    var bulkProcessor = buildBulkProcessor(client);

    streamsBuilder.<String, ParsedRecord>stream(Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.collection))
        .mapValues((readOnlyKey, value) -> buildRequest(config.COLLECTION_SEARCH_INDEX_ALIAS, readOnlyKey, value))
        .filter((key, value) -> value != null)
        .foreach((key, value) -> bulkProcessor.add(value));

    var collectionTable = streamsBuilder.<String, ParsedRecord>globalTable(Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.collection));
    var granuleStream = streamsBuilder.<String, ParsedRecord>stream(Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.granule));
    granuleStream
        .mapValues((readOnlyKey, value) -> buildRequest(config.GRANULE_SEARCH_INDEX_ALIAS, readOnlyKey, value))
        .filter((key, value) -> value != null)
        .foreach((key, value) -> bulkProcessor.add(value));
    granuleStream
        .join(collectionTable,
            (key, value) -> value.getRelationships().stream()
                .filter(rel -> rel.getType() == RelationshipType.COLLECTION)
                .findFirst()
                .map(Relationship::getId)
                .orElse(null),
            IndexingHelpers::flattenRecords)
        .mapValues((readOnlyKey, value) -> buildRequest(config.FLAT_GRANULE_SEARCH_INDEX_ALIAS, readOnlyKey, value))
        .filter((key, value) -> value != null)
        .foreach((key, value) -> bulkProcessor.add(value));

    // TODO - consider offset commit behavior with foreach... we don't commit offsets unless we know they're indexed

    // TODO - consider global ktable for streaming flattening vs. reindexing w/ script inside ES

    return streamsBuilder.build();
  }

  private static BulkProcessor buildBulkProcessor(RestHighLevelClient client) {
    final BiConsumer<BulkRequest, ActionListener<BulkResponse>> consumer = (request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);
    final BulkProcessor.Listener listener = new BulkProcessor.Listener() {
      @Override
      public void beforeBulk(long executionId, BulkRequest request) {
        log.debug("Submitting bulk request wth id [" + executionId + "] and [" + request.numberOfActions() + "] actions");
      }
      @Override
      public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
        log.info("Completed bulk request wth id [" + executionId + "] and [" + request.numberOfActions() + "] actions in [ " + response.getTook() + "]");
        response.iterator().forEachRemaining(item -> {
          if (item.isFailed()) {
            log.warn(item.getOpType() + " record with key [" + item.getId() + "] and index [" + item.getIndex() + "] failed: "
                + item.getFailureMessage(), item.getFailure().getCause());
          }
          else {
            log.info(item.getOpType() + " record with key [" + item.getId() + "] and index [" + item.getIndex() + "] succeeded");
          }
        });
      }
      @Override
      public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
        log.error("Failed to execute bulk request wth id [" + executionId + "] and [" + request.numberOfActions() + "] actions", failure);
      }
    };
    return BulkProcessor.builder(consumer, listener)
        .setBulkActions(1000)
        .setBulkSize(new ByteSizeValue(5 * 1024 * 1024)) // 5MiB
        .setFlushInterval(TimeValue.timeValueMillis(5 * 1000)) // 5s
        .setConcurrentRequests(1)
        .build();
  }

  private static DocWriteRequest buildRequest(String index, String key, ParsedRecord value) {
    if (value == null || (value.getPublishing() != null && value.getPublishing().getIsPrivate())) {
      return new DeleteRequest(index).id(key);
    }
    try {
      var formattedRecord = IndexingHelpers.reformatMessageForSearch(value);
      return new IndexRequest(index).id(key).source(formattedRecord);
    } catch (ElasticsearchGenerationException e) {
      log.error("Failed to serialize record with key [" + key + "] to json", e);
      return null;
    }
  }

}
