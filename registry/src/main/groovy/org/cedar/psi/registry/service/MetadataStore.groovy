package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.avro.Schema
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.HostInfo
import org.apache.kafka.streams.state.StreamsMetadata
import org.cedar.schemas.avro.psi.AggregatedInput
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

import static org.cedar.psi.common.constants.Topics.inputStore
import static org.cedar.psi.common.constants.Topics.parsedStore

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

  ParsedRecord retrieveParsed(RecordType type, String source, String id) {
    try {
      String storeName = parsedStore(type).toString()
      def metadata = metadataService.streamsMetadataForStoreAndKey(storeName, id, Serdes.String().serializer())
      if (thisHost(metadata)) {
        def parsedValue = metadataService.getParsedStore(type)?.get(id)
        return parsedValue ?: null
      }
      else {
        log.info("remote instance : " + metadata)
        return (ParsedRecord) getRemoteStoreState(metadata, storeName, id, ParsedRecord.getClassSchema())
      }
    }
    catch (Exception e) {
      log.error("Failed to retrieve [${type}] parsed value from source [${source}] with id [${id}]", e)
      throw e
    }
  }

  AggregatedInput retrieveInput(RecordType type, String source, String id) {
    try {
      String storeName = inputStore(type, source).toString()
      def metadata = metadataService.streamsMetadataForStoreAndKey(storeName, id, Serdes.String().serializer())
      if (thisHost(metadata)) {
        def result = metadataService.getInputStore(type, source)?.get(id)
        return result ?: null
      }
      else {
        log.info("remote instance : " + metadata)
        return (AggregatedInput) getRemoteStoreState(metadata, storeName, id, AggregatedInput.getClassSchema())
      }
    }
    catch (Exception e) {
      log.error("Failed to retrieve [${type}] input value from source [${source}] with id [${id}]", e)
      throw e
    }
  }

  public <T extends SpecificRecord> T getRemoteStoreState(StreamsMetadata metadata, String store, String id, Schema schema) {
    // TODO - get http vs https and context path from spring environment
    String url = "http://" + metadata.host() + ":" + metadata.port() + '/registry/db/' + store + '/' + id
    log.info("getting remote avro from: " + url)
    ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(url, byte[].class)
    if (responseEntity.getStatusCode().value() != 200) {
      return null
    }
    def decoder = DecoderFactory.get().binaryDecoder(responseEntity.getBody(), null)
    def reader = new SpecificDatumReader<T>(schema)
    def result = reader.read(null, decoder)
    return result
  }

  boolean thisHost(final StreamsMetadata metadata) {
    return metadata.host().equals(hostInfo.host()) &&
        metadata.port() == hostInfo.port()
  }

}
