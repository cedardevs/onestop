package org.cedar.onestop.indexer;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.indexer.util.BulkIndexingTransformer;
import org.cedar.onestop.indexer.util.IndexingHelpers;
import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.constants.Topics;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.RecordType;
import org.cedar.schemas.avro.psi.Relationship;
import org.cedar.schemas.avro.psi.RelationshipType;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SearchIndexTopology {
  private static final Logger log = LoggerFactory.getLogger(SearchIndexTopology.class);

  public static Topology buildSearchIndexTopology(RestHighLevelClient client, ElasticsearchConfig esConfig, AppConfig appConfig) {
    var streamsBuilder = new StreamsBuilder();
    long bulkIntervalMillis = Long.parseLong(appConfig.getOrDefault("elasticsearch.bulk.interval.ms", "10000").toString());
    long bulkMaxBytes = Long.parseLong(appConfig.getOrDefault("elasticsearch.bulk.max.bytes", "10000000").toString());

    var collectionIndex = esConfig.COLLECTION_SEARCH_INDEX_ALIAS;
    var collectionTopic = Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.collection);
    var inputCollections = streamsBuilder.<String, ParsedRecord>stream(collectionTopic);
    var collectionRequests = inputCollections.mapValues((readOnlyKey, value) -> buildRequest(esConfig, collectionIndex, readOnlyKey, value));

    var granuleIndex = esConfig.GRANULE_SEARCH_INDEX_ALIAS;
    var granuleTopic = Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.granule);
    var inputGranules = streamsBuilder.<String, ParsedRecord>stream(granuleTopic);
    var granuleRequests = inputGranules.mapValues((readOnlyKey, value) -> buildRequest(esConfig, granuleIndex, readOnlyKey, value));

    var indexResults = collectionRequests.merge(granuleRequests)
        .filter((key, value) -> value != null)
        .transform(() -> new BulkIndexingTransformer(client, Duration.ofMillis(bulkIntervalMillis), bulkMaxBytes));

    var successfullyIndexed = indexResults.filter((key, value) -> value != null && !value.isFailed());
    var successfulCollections = successfullyIndexed.filter((key, value) -> value.getIndex().equals(collectionIndex));
    var successfulGranules = successfullyIndexed.filter((key, value) -> value.getIndex().equals(granuleIndex));

    // stream of granule_id => [related_collection_ids]
//    var collectionsByGranule = inputGranules.mapValues(SearchIndexTopology::getParentIds);
//    collectionsByGranule.join(
//        successfulGranules,
//        (collectionIds, bulkItemResponse) -> collectionIds,
//        JoinWindows.of(Duration.ofMillis(bulkTimeoutMillis * 2))
//    ).flatMapValues((readOnlyKey, value) -> value)
//        .selectKey((key, value) -> value)
//        .merge(successfulCollections.mapValues((readOnlyKey, value) -> readOnlyKey))
//        .groupByKey()
//        .windowedBy(TimeWindows.of(Duration.ofMillis(bulkTimeoutMillis)))
//        .count()
//        .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
//        .




    // potential example of streaming flattening:
    // creates a global ktable of collections and joins granules with it based on the relationship id
//    var collectionTable = streamsBuilder.<String, ParsedRecord>globalTable(Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.collection));
//    var granuleStream = streamsBuilder.<String, ParsedRecord>stream(Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.granule));
//    granuleStream
//        .mapValues((readOnlyKey, value) -> buildRequest(config.GRANULE_SEARCH_INDEX_ALIAS, readOnlyKey, value))
//        .filter((key, value) -> value != null)
//        .foreach((key, value) -> bulkProcessor.add(value));
//    granuleStream
//        .join(collectionTable,
//            (key, value) -> value.getRelationships().stream()
//                .filter(rel -> rel.getType() == RelationshipType.COLLECTION)
//                .findFirst()
//                .map(Relationship::getId)
//                .orElse(null),
//            IndexingHelpers::flattenRecords)
//        .mapValues((readOnlyKey, value) -> buildRequest(config.FLAT_GRANULE_SEARCH_INDEX_ALIAS, readOnlyKey, value))
//        .filter((key, value) -> value != null)
//        .foreach((key, value) -> bulkProcessor.add(value));

    return streamsBuilder.build();
  }

  private static DocWriteRequest buildRequest(ElasticsearchConfig config, String index, String key, ParsedRecord value) {
    if (value == null || (value.getPublishing() != null && value.getPublishing().getIsPrivate())) {
      return new DeleteRequest(index).id(key);
    }
    try {
      var formattedRecord = IndexingHelpers.reformatMessageForSearch(value);
      return new IndexRequest(index).id(key).source(formattedRecord).type(config.TYPE);
    } catch (ElasticsearchGenerationException e) {
      log.error("Failed to serialize record with key [" + key + "] to json", e);
      return null;
    }
  }

  private static List<String> getParentIds(ParsedRecord value) {
    return Optional.ofNullable(value)
        .map(ParsedRecord::getRelationships)
        .orElse(Collections.emptyList())
        .stream()
        .filter(rel -> rel.getType() == RelationshipType.COLLECTION)
        .map(Relationship::getId)
        .collect(Collectors.toList());
  }

}
