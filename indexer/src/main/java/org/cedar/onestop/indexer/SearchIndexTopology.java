package org.cedar.onestop.indexer;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.TimestampedKeyValueStore;
import org.cedar.onestop.indexer.stream.FlatteningConfig;
import org.cedar.onestop.indexer.stream.SitemapConfig;
import org.cedar.onestop.indexer.stream.SitemapProcessor;
import org.cedar.onestop.indexer.stream.BulkIndexingConfig;
import org.cedar.onestop.indexer.util.ElasticsearchService;
import org.cedar.onestop.indexer.util.ValidationUtils;
import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.constants.Topics;
import org.cedar.onestop.kafka.common.util.KafkaHelpers;
import org.cedar.onestop.kafka.common.util.Timestamper;
import org.cedar.onestop.kafka.common.util.TopicIdentifier;
import org.cedar.schemas.analyze.Analyzers;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.Relationship;
import org.cedar.schemas.avro.psi.RelationshipType;
import org.cedar.schemas.parse.DefaultParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchIndexTopology {
  private static final Logger log = LoggerFactory.getLogger(SearchIndexTopology.class);
  private static final String FLATTEN_SCRIPT_PATH = "scripts/flattenGranules.painless";
  private static final String INDEXER_STORE_NAME = "bulk-indexer-buffer";
  public static final String FLATTENING_STORE_NAME = "flattening-trigger-store";
  public static final String SITEMAP_STORE_NAME = "sitemap-trigger-store";

  public static Topology buildSearchIndexTopology(ElasticsearchService esService, AppConfig appConfig) {
    var collectionIndex = esService.getConfig().COLLECTION_SEARCH_INDEX_ALIAS;
    var granuleIndex = esService.getConfig().GRANULE_SEARCH_INDEX_ALIAS;

    var streamsBuilder = new StreamsBuilder();

    //----- Indexing Setup --------
    var bulkConfig = indexingConfig(appConfig);
    var indexerStoreBuilder = indexerStoreBuilder(appConfig, bulkConfig);
    streamsBuilder.addStateStore(indexerStoreBuilder);

    //----- Indexing Stream --------
    var inputTopics = new ArrayList<>(Topics.parsedChangelogTopics(StreamsApps.REGISTRY_ID));
    inputTopics.add(Topics.flattenedGranuleTopic());
    var inputRecords = streamsBuilder.<String, ParsedRecord>stream(inputTopics);
    var filledInRecords = inputRecords.mapValues(DefaultParser::fillInDefaults);
    var analyzedRecords = filledInRecords.mapValues(Analyzers::addAnalysis);
    var recordsWithTopic = analyzedRecords.transformValues(TopicIdentifier<ParsedRecord>::new);
    var validatedRecords = recordsWithTopic.mapValues(ValidationUtils::addValidationErrors);

    var bulkResults = validatedRecords
        .peek((k, v) -> log.debug("submitting [{} => {}] to bulk indexer", k, v))
        .transformValues(Timestamper<ParsedRecord>::new)
        .transform(() -> esService.buildBulkIndexingTransformer(bulkConfig), bulkConfig.getStoreName());
    var successfulBulkResults = bulkResults.filter((k, v) -> v != null && v.isSuccessful());

    //----- Flattening Setup --------
    var flatteningConfig = flatteningConfig(appConfig);
    if (flatteningConfig.getEnabled()) {
      var flatteningStoreBuilder = flatteningStoreBuilder(flatteningConfig);
      streamsBuilder.addStateStore(flatteningStoreBuilder);
      var flatteningTopicName = appConfig.get("flattening.topic.name").toString();

      //----- Flattening Stream --------
      successfulBulkResults
          .filter((k, v) -> v.getIndex() != null && v.getIndex().startsWith(collectionIndex))
          .mapValues(bulkItemResponse -> 0L) // stream of collectionId => 0, since all granules should be flattened
          .peek((k, v) -> log.debug("producing flattening trigger [{} => {}]", k, v))
          // re-partition so all triggers for a given collection go to the same consumer
          .through(flatteningTopicName, Produced.with(Serdes.String(), Serdes.Long()))
          .peek((k, v) -> log.debug("consuming flattening trigger [{} => {}]", k, v))
          .transform(() -> esService.buildFlatteningTransformer(flatteningConfig), flatteningConfig.getStoreName())
          // cycle failed flattening requests back into the queue so nothing is lost
          .filter((k, v) -> !v.successful)
          .mapValues(v -> v.timestamp)
          .to(flatteningTopicName, Produced.with(Serdes.String(), Serdes.Long()));
    }

    //----- Sitemap Setup --------
    var sitemapConfig = sitemapConfig(appConfig);
    if (sitemapConfig.getEnabled()) {
      var sitemapStoreBuilder = sitemapStoreBuilder(sitemapConfig);
      streamsBuilder.addStateStore(sitemapStoreBuilder);
      var sitemapTopicName = appConfig.get("sitemap.topic.name").toString();

      //----- Sitemap Stream --------
      successfulBulkResults
          .filter((k, v) -> v.getIndex() != null && v.getIndex().startsWith(collectionIndex))
          // group all collection changes under one key/partition so sitemap is generated once per window at most
          .map((k, v) -> new KeyValue<>("ALL", v == null ? null : v.getTimestamp()))
          // re-partition so all sitemap triggers go to the same consumer
          .through(sitemapTopicName, Produced.with(Serdes.String(), Serdes.Long()))
          .process(() -> new SitemapProcessor(esService, sitemapConfig), sitemapConfig.getStoreName());
    }

    return streamsBuilder.build();
  }

  private static BulkIndexingConfig indexingConfig(AppConfig appConfig) {
    long bulkIntervalMillis = Long.parseLong(appConfig.get("elasticsearch.bulk.interval.ms").toString());
    long bulkMaxBytes = Long.parseLong(appConfig.get("elasticsearch.bulk.max.bytes").toString());
    int bulkMaxActions = Integer.parseInt(appConfig.get("elasticsearch.bulk.max.actions").toString());

    return BulkIndexingConfig.newBuilder()
        .withStoreName(INDEXER_STORE_NAME)
        .withMaxPublishBytes(bulkMaxBytes)
        .withMaxPublishInterval(Duration.ofMillis(bulkIntervalMillis))
        .withMaxPublishActions(bulkMaxActions)
        .build();
  }

  private static StoreBuilder<TimestampedKeyValueStore<String, ParsedRecord>> indexerStoreBuilder(AppConfig appConfig, BulkIndexingConfig indexingConfig) {
    var parseRecordSerde = new SpecificAvroSerde<ParsedRecord>();
    parseRecordSerde.configure(KafkaHelpers.buildAvroSerdeConfig(appConfig), false);
    var bulkIndexerStoreSupplier = Stores.persistentTimestampedKeyValueStore(indexingConfig.getStoreName());
    return Stores.timestampedKeyValueStoreBuilder(bulkIndexerStoreSupplier, Serdes.String(), parseRecordSerde);
  }

  private static FlatteningConfig flatteningConfig(AppConfig appConfig) {
    Boolean flatteningEnabled = Boolean.parseBoolean(appConfig.get("flattening.enabled").toString());
    long flatteningIntervalMillis = Long.parseLong(appConfig.get("flattening.interval.ms").toString());
    var flatteningInterval = Duration.ofMillis(flatteningIntervalMillis);
    return FlatteningConfig.newBuilder()
        .withEnabled(flatteningEnabled)
        .withStoreName(FLATTENING_STORE_NAME)
        .withScriptPath(FLATTEN_SCRIPT_PATH)
        .withInterval(flatteningInterval)
        .build();
  }

  private static StoreBuilder<KeyValueStore<String, Long>> flatteningStoreBuilder(FlatteningConfig config) {
    return Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(config.getStoreName()), Serdes.String(), Serdes.Long()).withLoggingDisabled();
  }

  private static SitemapConfig sitemapConfig(AppConfig appConfig) {
    Boolean sitemapEnabled = Boolean.parseBoolean(appConfig.get("sitemap.enabled").toString());
    long sitemapIntervalMillis = Long.parseLong(appConfig.get("sitemap.interval.ms").toString());
    var sitemapInterval = Duration.ofMillis(sitemapIntervalMillis);
    return SitemapConfig.newBuilder()
        .withEnabled(sitemapEnabled)
        .withStoreName(SITEMAP_STORE_NAME)
        .withInterval(sitemapInterval)
        .build();
  }

  private static StoreBuilder<KeyValueStore<String, Long>> sitemapStoreBuilder(SitemapConfig config) {
    return Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(config.getStoreName()), Serdes.String(), Serdes.Long()).withLoggingDisabled();
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
