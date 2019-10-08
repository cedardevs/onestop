package org.cedar.psi.registry.service;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.RecordType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.cedar.psi.common.constants.Topics.inputStore;
import static org.cedar.psi.common.constants.Topics.parsedStore;

@Service
public class MetadataService {
  private static final Logger log = LoggerFactory.getLogger(org.cedar.psi.registry.service.MetadataService.class);

  private final KafkaStreams streamsApp;

  @Autowired
  public MetadataService(final KafkaStreams streamsApp) {
    this.streamsApp = streamsApp;
  }

  /**
   * Find the metadata for the given store and key if it exists.
   * @param store   Store to find
   * @param key     The key to find
   * @return {@link StreamsMetadata}
   */
  public <K> StreamsMetadata streamsMetadataForStoreAndKey(final String store, final K key,
                                                         final Serializer<K> serializer) {
    final StreamsMetadata metadata = streamsApp.metadataForKey(store, key, serializer);
    if (metadata == null) {
      throw new RuntimeException("Unable to retrieve metadata for store [" + store + "] and key [" + key +"]");
    }

    return metadata;
  }

  /**
   * query Aggregated input StateStore
   * @param type record type of the store
   * @param source metadata source
   * @return  Aggregated input of the (type and source) store
   */
  public ReadOnlyKeyValueStore<String, AggregatedInput> getInputStore(RecordType type, String source) {
   return streamsApp.store(inputStore(type, source), QueryableStoreTypes.keyValueStore());
  }

  /**
   * query Parsed record StateStore
   * @param type record type of the store
   * @return Parsed record of the source
   */
  public ReadOnlyKeyValueStore<String, ParsedRecord> getParsedStore(RecordType type) {
    return streamsApp.store(parsedStore(type), QueryableStoreTypes.keyValueStore());
  }

  public ReadOnlyKeyValueStore<String, SpecificRecord> getAvroStore(String storeName) {
    return streamsApp.store(storeName, QueryableStoreTypes.keyValueStore());
  }

}

