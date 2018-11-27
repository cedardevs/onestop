package org.cedar.psi.registry.stream

import groovy.util.logging.Slf4j
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.ValueTransformerSupplier
import org.apache.kafka.streams.state.Stores
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.ParsedRecord
import org.cedar.psi.common.avro.RecordType
import org.cedar.psi.common.constants.Topics


@Slf4j
class TopologyBuilders {

  static Topology buildTopology(long publishInterval) {
    def builder = new StreamsBuilder()

    Topics.inputTypes().each { type ->
      addTopologyForType(builder, type, publishInterval)
    }

    return builder.build()
  }

  static StreamsBuilder addTopologyForType(StreamsBuilder builder, RecordType type, Long publishInterval) {
    // build input table for each source
    Map<String, KTable> inputTables = Topics.inputSources(type).collectEntries { source ->
      KStream<String, Input> inputStream = builder.stream(Topics.inputTopic(type, source))
      KTable<String, Input> inputTable = inputStream
          .groupByKey()
          .reduce(StreamFunctions.mergeInputs, Materialized.as(Topics.inputStore(type, source)))
      return [(source): inputTable]
    }

    // build parsed table
    KTable<String, ParsedRecord> parsedTable = builder
        .stream(Topics.parsedTopic(type))
        .groupByKey()
        .reduce(StreamFunctions.identityReducer, Materialized.as(Topics.parsedStore(type)))

    // add delayed publisher
    if (publishInterval) {
      builder.addStateStore(Stores.keyValueStoreBuilder(
          Stores.persistentKeyValueStore(
              Topics.publishTimeStore(type)), Serdes.Long(), Serdes.String()).withLoggingEnabled([:]))
      builder.addStateStore(Stores.keyValueStoreBuilder(
          Stores.persistentKeyValueStore(
              Topics.publishKeyStore(type)), Serdes.String(), Serdes.Long()).withLoggingEnabled([:]))

      // re-published items go back through the parsed topic
      def publisher = new DelayedPublisherTransformer(Topics.publishTimeStore(type), Topics.publishKeyStore(type), Topics.parsedStore(type), publishInterval)
      parsedTable
          .toStream()
          .transform({ -> publisher }, Topics.publishTimeStore(type), Topics.publishKeyStore(type), Topics.parsedStore(type))
          .to(Topics.parsedTopic(type))
    }

    // build published topic
    parsedTable
        .toStream()
        .transformValues({ -> new PublishingAwareTransformer() } as ValueTransformerSupplier<ParsedRecord, ParsedRecord>)
        .to(Topics.publishedTopic(type))

    return builder
  }
}
