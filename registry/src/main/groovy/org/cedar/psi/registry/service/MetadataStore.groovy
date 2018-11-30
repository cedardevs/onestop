package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.ParsedRecord
import org.cedar.psi.common.util.AvroUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder

import static org.cedar.psi.common.constants.Topics.inputStore
import static org.cedar.psi.common.constants.Topics.parsedStore

@Slf4j
@Service
@CompileStatic
class MetadataStore {

  private MetadataStreamService metadataStreamService

  @Autowired
  MetadataStore(MetadataStreamService metadataStreamService) {
    this.metadataStreamService = metadataStreamService
  }

  Map retrieveParsed(String type, String source, String id) {
    try {
      def parsedValue = getParsedStore(type)?.get(id)
      def inputValue = getInputStore(type, source)?.get(id)

      if (!inputValue && !parsedValue) {
        return [
            error  : "No such parsed record of ${type} with id ${id}"
        ]
      }

      if (inputValue && !parsedValue) {
        return [
            id        : id,
            type      : type,
            source    : inputValue.source,
            error     : "Input record didn't get parsed"
        ]
      }

      def parsedMap = parsedValue ? AvroUtils.avroToMap(parsedValue) : [:]

      return [
              id        : id,
              type      : type,
              source    : inputValue.source,
              attributes: parsedMap
          ]
    }
    catch (Exception e) {
      log.error("Failed to retrieve [${type}] value from source [${source}] with id [${id}]", e)
      throw e
    }
  }

  Map retrieveInput(String type, String source, String id) {
    try {
      def inputValue = getInputStore(type, source)?.get(id)

      if (!inputValue) {
        return [
            error  : "No such input record of ${type} with id ${id}"
        ]
      }

      def inputMap = inputValue ? [input: inputValue] : [:]

      return [
              id        : id,
              type      : type,
              attributes: inputMap
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

  String constructUri(String path) {
    def servletComponent = ServletUriComponentsBuilder.fromCurrentRequestUri().build()
    def host = servletComponent.host + ':' + servletComponent.port
    def scheme = servletComponent.scheme

    try{
      UriComponents uriComponents = UriComponentsBuilder.newInstance()
          .scheme(scheme).host(host).path(path).build()

      return uriComponents.toUriString()
    } catch (Exception e) {
      log.error("Failed to construct uri for ${path}", e)
      throw e
    }
  }

}
