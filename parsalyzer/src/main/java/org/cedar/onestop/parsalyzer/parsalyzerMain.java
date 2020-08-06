package org.cedar.onestop.parsalyzer;

import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.kafka.common.util.KafkaHealthProbeServer;
import org.cedar.onestop.kafka.common.util.KafkaHelpers;
import org.cedar.onestop.parsalyzer.stream.StreamParsalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class ParsalyzerMain {
  private static final Logger log = LoggerFactory.getLogger(ParsalyzerMain.class);

  public static void main(String[] args) {
    AppConfig appConfig = new AppConfig();
    log.info("Starting stream with bootstrap servers {}", appConfig.get("kafka.bootstrap.servers"));
    var streamsApp = StreamParsalyzer.buildStreamsApp(appConfig);
    var probeServer = new KafkaHealthProbeServer(streamsApp);

    Runtime.getRuntime().addShutdownHook(new Thread(streamsApp::close));
    Runtime.getRuntime().addShutdownHook(new Thread(probeServer::stop));
    KafkaHelpers.onError(streamsApp).thenAcceptAsync(o -> {
      log.error("Application encountered an error. Shutting down.");
      streamsApp.close(Duration.ofSeconds(5));
      probeServer.stop(Duration.ofSeconds(5));
    });

    streamsApp.start();
    probeServer.start();
  }
}
