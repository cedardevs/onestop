package org.cedar.psi.registry.service;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.RecordType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;

import static org.cedar.psi.common.constants.Topics.inputStore;
import static org.cedar.psi.common.constants.Topics.parsedStore;

@Service
public class MetadataStore {
  private static final Logger log = LoggerFactory.getLogger(org.cedar.psi.registry.service.MetadataStore.class);

  private StreamsStateService streamsStateService;
  private HostInfo hostInfo;
  private int port;
  private WebClient webClient;
  private DisposableServer server;
  private SpecificAvroSerde<SpecificRecord> serde;

  @Autowired
  public MetadataStore(final StreamsStateService streamsStateService,
                       final HostInfo hostInfo,
                       final @Value("${db.server.port:9090}") int port,
                       final @Value("${kafka.schema.registry.url}") String schemaRegistryUrl) {
    this.streamsStateService = streamsStateService;
    this.hostInfo = hostInfo;
    this.port = port;
    this.webClient = WebClient.create();
    this.serde = buildSerde(schemaRegistryUrl);
  }

  @PostConstruct
  public void start() {
    this.server = buildServer().bindNow();
  }

  @PreDestroy
  public void stop() {
    this.server.disposeNow();
  }

  public ParsedRecord retrieveParsed(RecordType type, String source, String id) throws IOException {
    String storeName = type != null ? parsedStore(type) : null;
    return getRecordFromTable(storeName, id, ParsedRecord.class);
  }

  public AggregatedInput retrieveInput(RecordType type, String source, String id) throws IOException {
    String storeName = type != null && id != null ? inputStore(type, source) : null;
    return getRecordFromTable(storeName, id, AggregatedInput.class);
  }

  private <T extends SpecificRecord> T getRecordFromTable(String table, String key, Class<T> type) throws IOException {
    if (table == null || key == null) {
      return null;
    }
    try {
      var metadata = streamsStateService.metadataForStoreAndKey(table, key, Serdes.String().serializer());
      if (thisHost(metadata)) {
        var store = streamsStateService.getAvroStore(table);
        return store != null ? (T) store.get(key) : null;
      }
      else {
        return (T) getRemoteStoreState(metadata, table, key, AggregatedInput.getClassSchema());
      }
    }
    catch (Exception e) {
      log.error("Failed to retrieve record from table [" + table + "] with key [" + key + "]", e);
      throw e;
    }
  }

  private HttpServer buildServer() {
    // TODO - we could support https by taking a config value and using .secure() here
    ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(buildRoutes());
    return HttpServer.create().host("0.0.0.0").port(port).handle(adapter);
  }

  private HttpHandler buildRoutes() {
    return RouterFunctions.toHttpHandler(
        RouterFunctions
            .route(RequestPredicates.GET("/db/{table}/{key}"), this::remoteRecordHandler));
  }

  private Mono<ServerResponse> remoteRecordHandler(ServerRequest request) {
    try {
      var table = request.pathVariable("table");
      var key = request.pathVariable("key");
      var record = getRecordFromTable(table, key, SpecificRecord.class);
      var bytes = record != null ? serde.serializer().serialize(null, record) : null;
      return bytes != null ?
          ServerResponse.ok().contentLength(bytes.length).syncBody(bytes) :
          ServerResponse.notFound().build();
    }
    catch(Exception e) {
      return ServerResponse.status(500).build();
    }
  }

  private <T extends SpecificRecord> T getRemoteStoreState(StreamsMetadata metadata, String store, String id, Schema schema) throws IOException {
    String url = "http://" + metadata.host() + ":" + metadata.port() + "/db/" + store + '/' + id;
    log.debug("getting remote avro from: " + url);
    try {
      var bytes = webClient.get().uri(url).retrieve().bodyToMono(byte[].class).block();
      return (T) serde.deserializer().deserialize(null, bytes);
    }
    catch (WebClientResponseException.NotFound e) {
      return null;
    }
  }

  private boolean thisHost(final StreamsMetadata metadata) {
    log.debug("checking if " + metadata.hostInfo() + " is the local host: " + hostInfo);
    return hostInfo.equals(metadata != null ? metadata.hostInfo() : null);
  }

  private SpecificAvroSerde<SpecificRecord> buildSerde(String schemaRegistryUrl) {
    var config = Map.of("schema.registry.url", schemaRegistryUrl,
        // since there is no topic name to use in this context, name the registered schemas after the record types
        "value.subject.name.strategy", "io.confluent.kafka.serializers.subject.RecordNameStrategy");
    var serde = new SpecificAvroSerde<SpecificRecord>();
    serde.configure(config, false);
    return serde;
  }

}
