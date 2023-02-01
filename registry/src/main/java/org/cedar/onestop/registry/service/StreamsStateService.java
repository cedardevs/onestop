package org.cedar.onestop.registry.service;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StreamsStateService {
  private final KafkaStreams streamsApp;

  @Autowired
  public StreamsStateService(final KafkaStreams streamsApp) {
    this.streamsApp = streamsApp;
  }

  /**
   * Find the metadata for the given store and key if it exists.
   * @param store   Store to find
   * @param key     The key to find
   * @return {@link KeyQueryMetadata}
   */
  public <K> KeyQueryMetadata metadataForStoreAndKey(final String store, final K key,
                                                    final Serializer<K> serializer) {
    final KeyQueryMetadata metadata = streamsApp.queryMetadataForKey(store, key, serializer);
    if (metadata == null) {
      throw new RuntimeException("Unable to retrieve metadata for store [" + store + "] and key [" + key +"]");
    }
    return metadata;
  }

  /**
   * Retrieve a state store which holds Avro SpecificRecords from the KafkaStreams application
   * @param storeName The name of the store
   * @return The store
   */
  public ReadOnlyKeyValueStore<String, SpecificRecord> getAvroStore(String storeName) {
    return streamsApp.store(StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore()));
  }

}
