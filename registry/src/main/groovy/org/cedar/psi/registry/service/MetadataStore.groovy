package org.cedar.psi.registry.service

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static org.cedar.psi.registry.service.MetadataStreamService.RAW_GRANULE_STORE
import static org.cedar.psi.registry.service.MetadataStreamService.RAW_COLLECTION_STORE
import static org.cedar.psi.registry.service.MetadataStreamService.PARSED_GRANULE_STORE
import static org.cedar.psi.registry.service.MetadataStreamService.PARSED_COLLECTION_STORE

@Slf4j
@Service
@CompileStatic
class MetadataStore {

  private MetadataStreamService metadataStreamService
  private JsonSlurper slurper

  @Autowired
  MetadataStore(MetadataStreamService metadataStreamService) {
    this.metadataStreamService = metadataStreamService
    this.slurper = new JsonSlurper()
  }

  Map retrieveEntity(String type, String id) {
    def rawStore = lookupRawStoreName(type)
    def parsedStore = lookupParsedStoreName(type)
    if (!rawStore && !parsedStore) { return null }
    def rawValue = rawStore ? getValueFromStore(rawStore, id) : null
    def parsedValue = parsedStore ? getValueFromStore(parsedStore, id) : null
    if (!rawValue && !parsedValue) { return null }
    return [id: id, type: type, attributes: [raw: rawValue, parsed: parsedValue]]
  }

  private getValueFromStore(String storeName, String id) {
    try {
      def store = metadataStreamService.streamsApp.store(storeName, QueryableStoreTypes.keyValueStore())
      if (!store) { return null }
      return store.get(id)
    }
    catch (Exception e) {
      log.error("Failed to retrieve value with id [${id}] from state store [${storeName}]", e)
      throw e
    }
  }

  private static String lookupRawStoreName(String type) {
    return type == 'granule' ? RAW_GRANULE_STORE :
        type == 'collection' ? RAW_COLLECTION_STORE : null
  }

  private static String lookupParsedStoreName(String type) {
    return type == 'granule' ? PARSED_GRANULE_STORE :
        type == 'collection' ? PARSED_COLLECTION_STORE : null
  }

}
