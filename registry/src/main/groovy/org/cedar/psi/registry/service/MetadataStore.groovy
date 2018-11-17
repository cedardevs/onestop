package org.cedar.psi.registry.service

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.ParsedRecord
import org.cedar.psi.common.util.AvroUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static org.cedar.psi.common.constants.Topics.inputStore
import static org.cedar.psi.common.constants.Topics.parsedStore

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

  Map retrieveEntity(String type, String source, String id) {
    try {
      def inputValue = getInputStore(type, source)?.get(id)
      def parsedValue = getParsedStore(type)?.get(id)

      if (!inputValue && !parsedValue) {
        return null
      }

      if (parsedValue?.errors?.size() > 0) {
        return [
            errors: parsedValue.errors
        ]
      }

      def inputMap = inputValue ? [input: inputValue] : [:]
      def parsedMap = parsedValue ? AvroUtils.avroToMap(parsedValue) : [:]
      return [
          data: [
              id        : id,
              type      : type,
              attributes: mergeAttributes(inputMap, parsedMap)
          ]
      ]
    }
    catch (Exception e) {
      log.error("Failed to retrieve [${type}] value from source [${source}] with id [${id}]", e)
      throw e
    }
  }

  ReadOnlyKeyValueStore<String, Input> getInputStore(String type, String source) {
    metadataStreamService?.streamsApp?.store(inputStore(type, source), QueryableStoreTypes.keyValueStore())
  }

  ReadOnlyKeyValueStore<String, ParsedRecord> getParsedStore(String type) {
    metadataStreamService?.streamsApp?.store(parsedStore(type), QueryableStoreTypes.keyValueStore())
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
