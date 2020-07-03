package org.cedar.onestop.registry.stream;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.state.Stores;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.constants.Topics;
import org.cedar.onestop.kafka.common.util.Timestamper;
import org.cedar.onestop.registry.util.UUIDValidator;
import org.cedar.schemas.avro.psi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TopologyBuilders {
  private static final Logger log = LoggerFactory.getLogger(TopologyBuilders.class);

  public static Topology buildTopology(long publishInterval, AdminClient adminClient) throws ExecutionException, InterruptedException {
    var builder = new StreamsBuilder();
    var collectionTopology = addTopologyForType(builder, RecordType.collection, publishInterval);
    var granuleTopology = addTopologyForType(builder, RecordType.granule, publishInterval);

    // add flattening topology
    var granulesByCollectionId = granuleTopology.parsedTable
        .toStream()
        .flatMap((granuleId, granuleRecord) ->
            granuleRecord.getRelationships().stream()
                .filter(relationship -> relationship.getType().equals(RelationshipType.COLLECTION))
                .map((collection -> new KeyValue<>(collection.getId(), granuleRecord)))
                .collect(Collectors.toList()))
        .through(Topics.granulesByCollectionId()); // go through kafka to co-partition granules with their collections

    var flattenedGranuleStream = granulesByCollectionId.join(
        collectionTopology.parsedTable,
        StreamFunctions.flattenRecords);
    flattenedGranuleStream.to(Topics.flattenedGranuleTopic());

    // pipe legacy changelogs into combined ones
    var existingTopics = adminClient.listTopics().names().get();
    consumeLegacyChangelogs(builder, RecordType.collection, existingTopics);
    consumeLegacyChangelogs(builder, RecordType.granule, existingTopics);

    return builder.build();
  }

  public static TopologyWrapperForType addTopologyForType(StreamsBuilder builder, RecordType type, Long publishInterval) {
    return new TopologyWrapperForType(builder, type, publishInterval);
  }

  public static void consumeLegacyChangelogs(StreamsBuilder builder, RecordType type, Collection<String> existingTopics) {
    // filter legacy changelogs to those that actually exist
    var legacyChangelogs = Topics.inputChangelogTopicsSplit(StreamsApps.REGISTRY_ID, type)
        .stream()
        .filter(existingTopics::contains)
        .collect(Collectors.toList());
    // pipe them into the new, combined input changelog
    if (legacyChangelogs.size() > 0) {
      builder.stream(legacyChangelogs)
          .to(Topics.inputChangelogTopicCombined(StreamsApps.REGISTRY_ID, type));
    }
  }

  public static class TopologyWrapperForType {
    public final StreamsBuilder builder;
    public final RecordType type;

    public final Long publishInterval;
    public final List<String> inputTopicNames;
    public final KStream<String, Input> inputStream;
    public final KTable<String, AggregatedInput> inputTable;
    public final KTable<String, ParsedRecord> parsedTable;

    TopologyWrapperForType(StreamsBuilder builder, RecordType type, Long publishInterval) {
      this.builder = builder;
      this.type = type;
      this.publishInterval = publishInterval;

      inputTopicNames = Topics.inputSources(type).stream()
          .map((s) -> Topics.inputTopic(type, s))
          .collect(Collectors.toList());

      // build input table for each source
      inputStream = builder.stream(inputTopicNames);
      inputTable = inputStream
          .filter((k, v) -> UUIDValidator.isValid(k))
          .transformValues(Timestamper<Input>::new)
          .groupByKey()
          .aggregate(
              StreamFunctions.aggregatedInputInitializer,
              StreamFunctions.inputAggregator,
              Materialized.as(Topics.inputStoreCombined(type)));

      // build parsed table
      parsedTable = builder
          .table(Topics.parsedTopic(type), Materialized.as(Topics.parsedStore(type)));

      // add delayed publisher
      if (publishInterval != null && publishInterval > 0) {
        builder.addStateStore(Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(
                Topics.publishTimeStore(type)), Serdes.Long(), Serdes.String()).withLoggingEnabled(Collections.EMPTY_MAP));
        builder.addStateStore(Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(
                Topics.publishKeyStore(type)), Serdes.String(), Serdes.Long()).withLoggingEnabled(Collections.EMPTY_MAP));

        // re-published items go back through the parsed topic
        TransformerSupplier publisherSupplier = () -> new DelayedPublisherTransformer(
            Topics.publishTimeStore(type),
            Topics.publishKeyStore(type),
            Topics.parsedStore(type),
            publishInterval
        );

        parsedTable
            .toStream()
            .transform(publisherSupplier, Topics.publishTimeStore(type), Topics.publishKeyStore(type), Topics.parsedStore(type))
            .to(Topics.parsedTopic(type));
      }

      // build published topic
      parsedTable
          .toStream()
          .transformValues(PublishingAwareTransformer::new)
          .to(Topics.publishedTopic(type));
    }

  }
}
