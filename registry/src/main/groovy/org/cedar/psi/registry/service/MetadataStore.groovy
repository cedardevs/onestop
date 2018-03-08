package org.cedar.psi.registry.service

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Slf4j
@Service
@CompileStatic
class MetadataStore {

  @Value('${kafka.stores.raw.granule}')
  String RAW_GRANULE_STORE

  @Value('${kafka.stores.raw.collection}')
  String RAW_COLLECTION_STORE

  private KafkaStreams metadataStream
  private JsonSlurper slurper

  @Autowired
  MetadataStore(KafkaStreams metadataStream) {
    this.metadataStream = metadataStream
    this.slurper = new JsonSlurper()
  }

  Map retrieveFromStore(String type, String id) {
    def storeName = lookupStoreName(type)
    if (!storeName) { return null }
    try {
      def store = metadataStream.store(storeName, QueryableStoreTypes.keyValueStore())
      if (!store) { return null }
      def value = store.get(id)
      if (!value) { return null }
      def attributes = slurper.parseText(value as String) as Map
      return [id: id, type: type, attributes: attributes]
    }
    catch (Exception e) {
      log.error("Failed to retrieve [${type}] with id [${id}] from state store [${storeName}]", e)
      throw e
    }
  }

  private String lookupStoreName(String type) {
    return type == 'granule' ? RAW_GRANULE_STORE :
        type == 'collection' ? RAW_COLLECTION_STORE : null
  }

}
