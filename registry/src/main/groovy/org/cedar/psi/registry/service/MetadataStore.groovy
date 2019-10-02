package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.HostInfo
import org.apache.kafka.streams.state.StreamsMetadata
import org.cedar.schemas.avro.psi.AggregatedInput
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Slf4j
@Service
@CompileStatic
class MetadataStore {

  private KafkaStreams streamsApp
  private MetadataService metadataService
  private HostInfo hostInfo
  private RestTemplate restTemplate

  @Autowired
  MetadataStore(KafkaStreams streamsApp, final HostInfo hostInfo) {
    this.streamsApp = streamsApp
    this.metadataService = new MetadataService(streamsApp)
    this.hostInfo = hostInfo
    this.restTemplate = new RestTemplate()
  }

  ParsedRecord retrieveLocalParsed(RecordType type, String source, String id) {
    try {
      def parsedValue = metadataService.getParsedStore(type)?.get(id)
      return parsedValue ?: null
    }
    catch (Exception e) {
      log.error("Failed to retrieve [${type}] parsed value from source [${source}] with id [${id}]", e)
      throw e
    }
  }

  AggregatedInput retrieveLocalInput(RecordType type, String source, String id) {
    try {
      def inputValue = metadataService.getInputStore(type, source)?.get(id)

      return inputValue ?: null
    }
    catch (Exception e) {
      log.error("Failed to retrieve [${type}] input value from source [${source}] with id [${id}]", e)
      throw e
    }
  }

  Map getRemoteStoreState(StreamsMetadata metadata, String path, String id) {
    log.info("get remote instance value: " + metadata)
    ResponseEntity<Map> responseEntity
    String url = "http://" + metadata.host() + ":" + metadata.port() + path
    try {
      responseEntity = restTemplate.getForEntity(url, Map.class)
      if (responseEntity.getStatusCode().value() != 200) {
        log.error("error service failed with status code " + responseEntity.getStatusCode().value())
        return [
            links : url,
            errors: [
                [
                    status: responseEntity.getStatusCodeValue(),
                    title : responseEntity.notFound().toString(),
                    detail: "No parsed values exist for id [${id}]]" as String
                ]
            ]
        ]
      }
      if (log.isTraceEnabled()) {
        log.trace("services details: " + responseEntity.getBody())
      }
      return responseEntity.getBody()
    } catch (RestClientException e) {
      log.error("error in calling service details: ", e)
      return [
          links : url,
          errors: [
              [
                  detail: "error in calling service details:[${e.message}]]" as String
              ]
          ]
      ]
    }
  }

  boolean thisHost(final StreamsMetadata metadata) {
    return metadata.host().equals(hostInfo.host()) &&
        metadata.port() == hostInfo.port()
  }

}
