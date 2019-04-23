package org.cedar.psi.registry.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.state.Stores;
import org.cedar.psi.common.constants.Topics;
import org.cedar.schemas.avro.psi.Input;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.RecordType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.stream.Collectors;

public class TopologyBuilders {
  private static final Logger log = LoggerFactory.getLogger(TopologyBuilders.class);

  public static Topology buildTopology(long publishInterval) {
    var builder = new StreamsBuilder();
    Topics.inputTypes().forEach(type -> addTopologyForType(builder, type, publishInterval));
    return builder.build();
  }

  public static StreamsBuilder addTopologyForType(StreamsBuilder builder, RecordType type, Long publishInterval) {
    var inputTopics = Topics.inputSources(type).stream()
        .map((s) -> Topics.inputTopic(type, s))
        .collect(Collectors.toList());

    // TODO - aggregate items from all input topics into a single KTable

    // build input table for each source
    var inputTables = Topics.inputSources(type).stream()
        .collect(Collectors.toMap(
            source -> source,
            source -> {
              return builder.<String, Input>stream(Topics.inputTopic(type, source))
                  .groupByKey()
                  .aggregate(
                      StreamFunctions.aggregatedInputInitializer,
                      StreamFunctions.inputAggregator,
                      Materialized.as(Topics.inputStore(type, source)));
            }
    ));

    // build parsed table
    KTable<String, ParsedRecord> parsedTable = builder
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
        .transformValues(() -> new PublishingAwareTransformer())
        .to(Topics.publishedTopic(type));

    return builder;
  }
}
