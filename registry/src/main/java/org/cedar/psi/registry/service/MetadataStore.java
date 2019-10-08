package org.cedar.psi.registry.service;

import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.RecordType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Objects;

import static org.cedar.psi.common.constants.Topics.inputStore;
import static org.cedar.psi.common.constants.Topics.parsedStore;

@Service
public class MetadataStore {
  private static final Logger log = LoggerFactory.getLogger(org.cedar.psi.registry.service.MetadataStore.class);

  private MetadataService metadataService;
  private HostInfo hostInfo;
  private RestTemplate restTemplate;

  @Autowired
  MetadataStore(KafkaStreams streamsApp, final HostInfo hostInfo) {
    this.metadataService = new MetadataService(streamsApp);
    this.hostInfo = hostInfo;
    this.restTemplate = new RestTemplate();
  }

  public ParsedRecord retrieveParsed(RecordType type, String source, String id) throws IOException {
    if (type != null) {
      String storeName = parsedStore(type);
      try {
        var metadata = metadataService.streamsMetadataForStoreAndKey(storeName, id, Serdes.String().serializer());
        if (thisHost(metadata)) {
          return metadataService.getParsedStore(type).get(id);
        }
        else {
          log.info("remote instance : " + metadata);
          return (ParsedRecord) getRemoteStoreState(metadata, storeName, id, ParsedRecord.getClassSchema());
        }
      }
      catch (Exception e) {
        log.error("Failed to retrieve [${type}] parsed value from source [${source}] with id [${id}]", e);
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
        var metadata = metadataService.streamsMetadataForStoreAndKey(storeName, id, Serdes.String().serializer());
        if (thisHost(metadata)) {
          return metadataService.getInputStore(type, source).get(id);
        }
        else {
          log.info("remote instance : " + metadata);
          return (AggregatedInput) getRemoteStoreState(metadata, storeName, id, AggregatedInput.getClassSchema());
        }
      }
      catch (Exception e) {
        log.error("Failed to retrieve [${type}] input value from source [${source}] with id [${id}]", e);
        throw e;
      }
    }
    else {
      return null;
    }
  }

  private <T extends SpecificRecord> T getRemoteStoreState(StreamsMetadata metadata, String store, String id, Schema schema) throws IOException {
    // TODO - get http vs https and context path from spring environment
    String url = "http://" + metadata.host() + ":" + metadata.port() + "/registry/db/" + store + '/' + id;
    log.info("getting remote avro from: " + url);
    ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(url, byte[].class);
    if (responseEntity.getStatusCode().value() != 200) {
      return null;
    }
    var decoder = DecoderFactory.get().binaryDecoder(Objects.requireNonNull(responseEntity.getBody()), null);
    var reader = new SpecificDatumReader<T>(schema);
    var result = reader.read(null, decoder);
    return result;
  }

  private boolean thisHost(final StreamsMetadata metadata) {
    return metadata.host().equals(hostInfo.host()) &&
        metadata.port() == hostInfo.port();
  }

}


