package org.cedar.onestop.indexer;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.TimestampedKeyValueStore;
import org.cedar.onestop.elastic.common.FileUtil;
import org.cedar.onestop.indexer.stream.SitemapTriggerProcessor;
import org.cedar.onestop.indexer.stream.BulkIndexingConfig;
import org.cedar.onestop.indexer.util.ElasticsearchService;
import org.cedar.onestop.indexer.util.IndexingHelpers;
import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.constants.Topics;
import org.cedar.onestop.kafka.common.util.KafkaHelpers;
import org.cedar.onestop.kafka.common.util.Timestamper;
import org.cedar.schemas.analyze.Analyzers;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.RecordType;
import org.cedar.schemas.avro.psi.Relationship;
import org.cedar.schemas.avro.psi.RelationshipType;
import org.cedar.schemas.parse.DefaultParser;
import org.elasticsearch.action.DocWriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchIndexTopology {
  private static final Logger log = LoggerFactory.getLogger(SearchIndexTopology.class);
  private static final String FLATTEN_SCRIPT_PATH = "scripts/flattenGranules.painless";
  private static final String INDEXER_STORE_NAME = "bulk-indexer-buffer";

  public static Topology buildSearchIndexTopology(ElasticsearchService esService, AppConfig appConfig) throws IOException {
    var streamsBuilder = new StreamsBuilder();
    var flatteningScript = FileUtil.textFromClasspathFile(FLATTEN_SCRIPT_PATH);
    if (flatteningScript == null || flatteningScript.isEmpty()) {
      throw new IllegalStateException("Unable to load required flattening script from [" + FLATTEN_SCRIPT_PATH + "]");
    }
    var collectionIndex = esService.getConfig().COLLECTION_SEARCH_INDEX_ALIAS;
    var granuleIndex = esService.getConfig().GRANULE_SEARCH_INDEX_ALIAS;

    var bulkConfig = indexingConfig(esService, appConfig);
    var indexerStoreBuilder = indexerStoreBuilder(appConfig, bulkConfig);
    streamsBuilder.addStateStore(indexerStoreBuilder);

    // validate collection/granule records and send them to ES in bulk
    var inputRecords = streamsBuilder.<String, ParsedRecord>stream(Topics.parsedChangelogTopics(StreamsApps.REGISTRY_ID));
    var filledInRecords = inputRecords.mapValues(DefaultParser::fillInDefaults);
    var analyzedRecords = filledInRecords.mapValues(Analyzers::addAnalysis);
    var validRecords = analyzedRecords.filter((k, v) -> v == null || (Boolean) IndexingHelpers.validateMessage(k, v).get("valid"));
    var bulkResults = validRecords
        .peek((k, v) -> log.debug("submitting [{} => {}] to bulk indexer", k, v))
        .transformValues(Timestamper<ParsedRecord>::new)
        .transform(() -> esService.buildBulkIndexingTransformer(bulkConfig), bulkConfig.getStoreName());
    var successfulBulkResults = bulkResults.filter((k, v) -> v != null && v.isSuccessful());

    var flatteningTriggersFromGranules = successfulBulkResults
        .filter((k, v) -> v.getIndex() != null && v.getIndex().startsWith(granuleIndex))
        .flatMap((k, v) ->
            getParentIds(v.getRecord())
                .map(parentId -> new KeyValue<>(parentId, v.getTimestamp()))
                .collect(Collectors.toList())); // stream of collectionId => timestamp of granule to flatten from
    var flatteningTriggersFromCollections = successfulBulkResults
        .filter((k, v) -> v.getIndex() != null && v.getIndex().startsWith(collectionIndex))
        .mapValues(bulkItemResponse -> 0L); // stream of collectionId => 0, since all granules should be flattened

    var flatteningStoreName = "flattening-trigger-store";
    streamsBuilder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(flatteningStoreName), Serdes.String(), Serdes.Long()).withLoggingDisabled());
    var flatteningTopicName = appConfig.get("flattening.topic.name").toString();
    long flatteningIntervalMillis = Long.parseLong(appConfig.get("flattening.interval.ms").toString());
    var flatteningInterval = Duration.ofMillis(flatteningIntervalMillis);
    flatteningTriggersFromCollections.merge(flatteningTriggersFromGranules)
        .peek((k, v) -> log.debug("producing flattening trigger [{} => {}]", k, v))
        // re-partition so all triggers for a given collection go to the same consumer
        .through(flatteningTopicName, Produced.with(Serdes.String(), Serdes.Long()))
        .peek((k, v) -> log.debug("consuming flattening trigger [{} => {}]", k, v))
        .transform(
            () -> esService.buildFlatteningTriggerTransformer(flatteningStoreName, flatteningScript, flatteningInterval),
            flatteningStoreName)
        // cycle failed flattening requests back into the queue so nothing is lost
        .filter((k, v) -> !v.successful)
        .mapValues(v -> v.timestamp)
        .to(flatteningTopicName, Produced.with(Serdes.String(), Serdes.Long()));

    var sitemapTriggerStoreName = "sitemap-trigger-store";
    streamsBuilder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(sitemapTriggerStoreName), Serdes.String(), Serdes.Long()).withLoggingDisabled());
    var sitemapTopicName = appConfig.get("sitemap.topic.name").toString();
    long sitemapIntervalMillis = Long.parseLong(appConfig.get("sitemap.interval.ms").toString());
    var sitemapInterval = Duration.ofMillis(sitemapIntervalMillis);
    successfulBulkResults
        .filter((k, v) -> v.getIndex() != null && v.getIndex().startsWith(collectionIndex))
        // group all collection changes under one key/partition so sitemap is generated once per window at most
        .map((k, v) -> new KeyValue<>("ALL", v == null ? null : v.getTimestamp()))
        // re-partition so all sitemap triggers go to the same consumer
        .through(sitemapTopicName, Produced.with(Serdes.String(), Serdes.Long()))
        .process(
            () -> new SitemapTriggerProcessor(sitemapTriggerStoreName, esService, sitemapInterval),
            sitemapTriggerStoreName);

    return streamsBuilder.build();
  }

  private static BulkIndexingConfig indexingConfig(ElasticsearchService esService, AppConfig appConfig) {
    var collectionIndex = esService.getConfig().COLLECTION_SEARCH_INDEX_ALIAS;
    var collectionTopic = Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.collection);
    var granuleIndex = esService.getConfig().GRANULE_SEARCH_INDEX_ALIAS;
    var flattenedIndex = esService.getConfig().FLAT_GRANULE_SEARCH_INDEX_ALIAS;
    var granuleTopic = Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.granule);
    long bulkIntervalMillis = Long.parseLong(appConfig.get("elasticsearch.bulk.interval.ms").toString());
    long bulkMaxBytes = Long.parseLong(appConfig.get("elasticsearch.bulk.max.bytes").toString());

    return BulkIndexingConfig.newBuilder()
        .withStoreName(INDEXER_STORE_NAME)
        .withMaxPublishBytes(bulkMaxBytes)
        .withMaxPublishInterval(Duration.ofMillis(bulkIntervalMillis))
        .addIndexMapping(collectionTopic, DocWriteRequest.OpType.INDEX, collectionIndex)
        .addIndexMapping(collectionTopic, DocWriteRequest.OpType.DELETE, collectionIndex)
        .addIndexMapping(granuleTopic, DocWriteRequest.OpType.INDEX, granuleIndex)
        .addIndexMapping(granuleTopic, DocWriteRequest.OpType.DELETE, granuleIndex)
        .addIndexMapping(granuleTopic, DocWriteRequest.OpType.DELETE, flattenedIndex)
        .build();
  }

  private static StoreBuilder<TimestampedKeyValueStore<String, ParsedRecord>> indexerStoreBuilder(AppConfig appConfig, BulkIndexingConfig indexingConfig) {
    var parseRecordSerde = new SpecificAvroSerde<ParsedRecord>();
    parseRecordSerde.configure(KafkaHelpers.buildAvroSerdeConfig(appConfig), false);
    var bulkIndexerStoreSupplier = Stores.persistentTimestampedKeyValueStore(indexingConfig.getStoreName());
    return Stores.timestampedKeyValueStoreBuilder(bulkIndexerStoreSupplier, Serdes.String(), parseRecordSerde);
  }

  private static Stream<String> getParentIds(ParsedRecord value) {
    return Optional.ofNullable(value)
        .map(ParsedRecord::getRelationships)
        .orElse(Collections.emptyList())
        .stream()
        .filter(rel -> rel.getType() == RelationshipType.COLLECTION)
        .map(Relationship::getId);
  }

}
