package org.cedar.psi.registry.service

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static org.cedar.psi.common.constants.Topics.RAW_GRANULE_STORE
import static org.cedar.psi.common.constants.Topics.RAW_COLLECTION_STORE
import static org.cedar.psi.common.constants.Topics.PARSED_GRANULE_STORE
import static org.cedar.psi.common.constants.Topics.PARSED_COLLECTION_STORE


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
    Map rawValue = rawStore ? getValueFromStore(rawStore, id) : null
    Map parsedValue = parsedStore ? getValueFromStore(parsedStore, id) : null
    if (!rawValue && !parsedValue) { return null }
    return [id: id, type: type, attributes: mergeAttributes(rawValue, parsedValue)]
  }

  private Map getValueFromStore(String storeName, String id) {
    try {
      def store = metadataStreamService.streamsApp.store(storeName, QueryableStoreTypes.keyValueStore())
      if (!store) { return null }
      return store.get(id) as Map
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

  /**
   * this is not a full recursive merge
   * it combines maps and lists with the same key in each input map
   */
  private Map mergeAttributes(Map raw, Map parsed) {
    def result = [:]
    if (raw) {
      result.putAll(raw)
    }
    if (parsed) {
      parsed.each { k, v ->
        if (result[k] instanceof Map && v instanceof Map) {
          result[k] = (result[k] as Map) + (v as Map)
        }
        else if (result[k] instanceof List && v instanceof List) {
          result[k] = (result[k] as List) + (v as List)
        }
        else {
          result[k] = v
        }
      }
    }
    return result
  }

}
