package org.cedar.onestop.indexer;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.apache.kafka.streams.state.internals.ValueAndTimestampSerde;
import org.cedar.onestop.elastic.common.FileUtil;
import org.cedar.onestop.indexer.stream.BulkIndexingTransformer;
import org.cedar.onestop.indexer.stream.ElasticsearchRequestMapper;
import org.cedar.onestop.indexer.stream.FlatteningTriggerTransformer;
import org.cedar.onestop.indexer.stream.SitemapIndexer;
import org.cedar.onestop.indexer.util.ElasticsearchService;
import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.constants.Topics;
import org.cedar.onestop.kafka.common.util.Timestamper;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.RecordType;
import org.cedar.schemas.avro.psi.Relationship;
import org.cedar.schemas.avro.psi.RelationshipType;
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

  public static Topology buildSearchIndexTopology(ElasticsearchService esService, AppConfig appConfig) throws IOException {
    var streamsBuilder = new StreamsBuilder();
    long bulkIntervalMillis = Long.parseLong(appConfig.get("elasticsearch.bulk.interval.ms").toString());
    long bulkMaxBytes = Long.parseLong(appConfig.get("elasticsearch.bulk.max.bytes").toString());
    var flatteningScript = FileUtil.textFromClasspathFile(FLATTEN_SCRIPT_PATH);
    if (flatteningScript == null || flatteningScript.isEmpty()) {
      throw new IllegalStateException("Unable to load required flattening script from [" + FLATTEN_SCRIPT_PATH + "]");
    }

    // transform collection messages into elasticsearch delete/index requests
    var collectionIndex = esService.getConfig().COLLECTION_SEARCH_INDEX_ALIAS;
    var collectionTopic = Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.collection);
    var inputCollections = streamsBuilder.<String, ParsedRecord>stream(collectionTopic);
    var timestampedInputCollections = inputCollections.transformValues(Timestamper<ParsedRecord>::new);
    var collectionRequests = timestampedInputCollections.mapValues(new ElasticsearchRequestMapper(collectionIndex));

    // transform granule messages into elasticsearch delete/index requests
    var granuleIndex = esService.getConfig().GRANULE_SEARCH_INDEX_ALIAS;
    var granuleTopic = Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.granule);
    var inputGranules = streamsBuilder.<String, ParsedRecord>stream(granuleTopic);
    var timestampedInputGranules = inputGranules.transformValues(Timestamper<ParsedRecord>::new);
    var granuleRequests = timestampedInputGranules.mapValues(new ElasticsearchRequestMapper(granuleIndex));

    // merge delete/index requests and send them to ES in bulk
    var indexResults = collectionRequests.merge(granuleRequests)
        .filter((key, value) -> value != null)
        .transform(() -> new BulkIndexingTransformer(esService, Duration.ofMillis(bulkIntervalMillis), bulkMaxBytes));

    var flatteningTriggersFromGranules = timestampedInputGranules
        .flatMap((granuleId, granuleAndTimestamp) -> getParentIds(granuleAndTimestamp.value())
            .map(parentId -> new KeyValue<>(parentId, granuleAndTimestamp.timestamp()))
            .collect(Collectors.toList())); // stream of collectionId => timestamp of granule to flatten from

    var flatteningTriggersFromCollections = timestampedInputCollections
        .mapValues(bulkItemResponse -> 0L); // stream of collectionId => 0, since all granules should be flattened

    var flatteningTopicName = appConfig.get("flattening.topic.name").toString();
    long flatteningIntervalMillis = Long.parseLong(appConfig.get("flattening.interval.ms").toString());
    flatteningTriggersFromCollections.merge(flatteningTriggersFromGranules)
        .through(flatteningTopicName, Produced.with(Serdes.String(), Serdes.Long())) // re-partition so all triggers for a given collection go to the same consumer
        .groupByKey()
        .windowedBy(TimeWindows.of(Duration.ofMillis(flatteningIntervalMillis)))
        // TODO - disable ktable logging on these triggers?
        .reduce(Math::min) // each trigger uses the *earliest* time value in the window so all related granules are flattened
        .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
        .toStream()
        .transform(() -> new FlatteningTriggerTransformer(esService, flatteningScript))
        .filter((k, v) -> !v.successful)
        .mapValues(v -> v.timestamp)
        .to(flatteningTopicName, Produced.with(Serdes.String(), Serdes.Long()));

    var sitemapTopicName = appConfig.get("sitemap.topic.name").toString();
    long sitemapIntervalMillis = Long.parseLong(appConfig.get("sitemap.interval.ms").toString());
    timestampedInputCollections
        .map((k, v) -> new KeyValue<>("ALL", v.timestamp())) // group all collection changes under one key so ETL is run once per window at most
        .through(sitemapTopicName, Produced.with(Serdes.String(), Serdes.Long())) // re-partition so all triggers for a given collection go to the same consumer
        .groupByKey()
        .windowedBy(TimeWindows.of(Duration.ofMillis(sitemapIntervalMillis)))
        // TODO - disable ktable logging on these triggers?
        .reduce(Math::max) // uses the *latest* time value in the window so sitemap reflects most recent update
        .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
        .toStream()
        .foreach((k, v) -> SitemapIndexer.buildSitemap(esService, v));

    return streamsBuilder.build();
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
