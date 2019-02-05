package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.cedar.schemas.avro.psi.Input
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.avro.util.AvroUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static org.cedar.psi.common.constants.Topics.inputStore
import static org.cedar.psi.common.constants.Topics.parsedStore

@Slf4j
@Service
@CompileStatic
class MetadataStore {

  private KafkaStreams streamsApp

  @Autowired
  MetadataStore(KafkaStreams streamsApp) {
    this.streamsApp = streamsApp
  }

  Map retrieveParsed(RecordType type, String source, String id) {
    try {
      def parsedValue = getParsedStore(type)?.get(id)
      if (parsedValue) {
        return AvroUtils.avroToMap(parsedValue)
      }
      else {
        return null
      }
    }
    catch (Exception e) {
      log.error("Failed to retrieve [${type}] parsed value from source [${source}] with id [${id}]", e)
      throw e
    }
  }

  Map retrieveInput(RecordType type, String source, String id) {
    try {
      def inputValue = getInputStore(type, source)?.get(id)
      if (!inputValue) {
        return null
      }
      else {
        return AvroUtils.avroToMap(inputValue)
      }
    }
    catch (Exception e) {
      log.error("Failed to retrieve [${type}] input value from source [${source}] with id [${id}]", e)
      throw e
    }
  }

  ReadOnlyKeyValueStore<String, Input> getInputStore(RecordType type, String source) {
    streamsApp?.store(inputStore(type, source), QueryableStoreTypes.keyValueStore())
  }

  ReadOnlyKeyValueStore<String, ParsedRecord> getParsedStore(RecordType type) {
    streamsApp?.store(parsedStore(type), QueryableStoreTypes.keyValueStore())
  }

}
