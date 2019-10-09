package org.cedar.psi.registry.service;

import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.cedar.psi.registry.util.AvroTransformers;
import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.RecordType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.cedar.psi.common.constants.Topics.inputStore;
import static org.cedar.psi.common.constants.Topics.parsedStore;

@Service
public class MetadataStore {
  private static final Logger log = LoggerFactory.getLogger(org.cedar.psi.registry.service.MetadataStore.class);

  private StreamsStateService streamsStateService;
  private HostInfo hostInfo;
  private String contextPath;
  private RestTemplate restTemplate;

  @Autowired
  MetadataStore(final StreamsStateService streamsStateService, final HostInfo hostInfo,
      final @Value("server.servlet.context-path:") String contextPath) {
    this.streamsStateService = streamsStateService;
    this.hostInfo = hostInfo;
    this.contextPath = contextPath;
    this.restTemplate = new RestTemplate();
  }

  public ParsedRecord retrieveParsed(RecordType type, String source, String id) throws IOException {
    if (type != null) {
      String storeName = parsedStore(type);
      try {
        var metadata = streamsStateService.metadataForStoreAndKey(storeName, id, Serdes.String().serializer());
        if (thisHost(metadata)) {
          var store = streamsStateService.getParsedStore(type);
          return store != null ? store.get(id) : null;
        }
        else {
          log.info("remote instance: " + metadata);
          return (ParsedRecord) getRemoteStoreState(metadata, storeName, id, ParsedRecord.getClassSchema());
        }
      }
      catch (Exception e) {
        log.error("Failed to retrieve [" + type + "] parsed value from source [" + source + "] with id [" + id + "]", e);
        throw e;
      }
    }
    else {
      return null;
    }
  }

  public AggregatedInput retrieveInput(RecordType type, String source, String id) throws IOException {
    if (type != null) {
      String storeName = inputStore(type, source);
      try {
        var metadata = streamsStateService.metadataForStoreAndKey(storeName, id, Serdes.String().serializer());
        if (thisHost(metadata)) {
          var store = streamsStateService.getInputStore(type, source);
          return store != null ? store.get(id) : null;
        }
        else {
          log.info("remote instance : " + metadata);
          return (AggregatedInput) getRemoteStoreState(metadata, storeName, id, AggregatedInput.getClassSchema());
        }
      }
      catch (Exception e) {
        log.error("Failed to retrieve [" + type + "] input value from source [" + source + "] with id [" + id + "]", e);
        throw e;
      }
    }
    else {
      return null;
    }
  }

  private <T extends SpecificRecord> T getRemoteStoreState(StreamsMetadata metadata, String store, String id, Schema schema) throws IOException {
    // TODO - can we determine http vs https dynamically?
    String url = "http://" + metadata.host() + ":" + metadata.port() + contextPath + "/db/" + store + '/' + id;
    log.debug("getting remote avro from: " + url);
    try {
      ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(url, byte[].class);
      return AvroTransformers.bytesToAvro(responseEntity.getBody(), schema);
    }
    catch (HttpClientErrorException.NotFound e) {
      return null;
    }
  }

  private boolean thisHost(final StreamsMetadata metadata) {
    log.debug("checking if " + metadata.hostInfo() + " is the local host: " + hostInfo);
    return metadata.host().equals(hostInfo.host()) &&
        metadata.port() == hostInfo.port();
  }

}


