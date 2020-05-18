package org.cedar.onestop.kafka.common.util;

import org.apache.kafka.streams.KafkaStreams;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE;
import static org.cedar.onestop.kafka.common.util.KafkaHealthUtils.isStreamsAppAlive;
import static org.cedar.onestop.kafka.common.util.KafkaHealthUtils.isStreamsAppReady;

public class KafkaHealthProbeServer {

  private final Integer port;
  private final KafkaStreams streamsApp;

  private DisposableServer server;

  public KafkaHealthProbeServer(KafkaStreams streamsApp) {
    this(streamsApp, 8080);
  }

  public KafkaHealthProbeServer(KafkaStreams streamsApp, Integer port) {
    this.streamsApp = streamsApp;
    this.port = port;
  }

  public void start() {
    this.server = buildServer();
  }

  public void stop() throws IllegalStateException {
    stop(Duration.ofSeconds(5));
  }

  public void stop(Duration duration) throws IllegalStateException {
    if (this.server != null) {
      this.server.disposeNow(duration);
    }
  }

  private DisposableServer buildServer() {
    return HttpServer.create()
        .host("0.0.0.0")
        .port(port)
        .route(routes -> routes
            .get("/health/liveness", (request, response) ->
              response.status(isStreamsAppAlive(streamsApp) ? OK : SERVICE_UNAVAILABLE))
            .get("/health/readiness", (request, response) ->
                response.status(isStreamsAppReady(streamsApp) ? OK : SERVICE_UNAVAILABLE)))
        .bindNow();
  }

}
