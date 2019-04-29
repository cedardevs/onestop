package org.cedar.psi.manager;

import org.cedar.psi.manager.config.ManagerConfig;
import org.cedar.psi.manager.stream.StreamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class StreamManagerMain {
  private static final Logger log = LoggerFactory.getLogger(StreamManagerMain.class);


  public static void main(String[] args) {
    var combinedProps = new LinkedHashMap<String, Object>();
    getEnv().forEach((k, v) -> combinedProps.merge(k, v, (v1, v2) -> v2));
    System.getProperties().forEach((k, v) -> combinedProps.merge(k.toString(), v, (v1, v2) -> v2));

    var config = new ManagerConfig(combinedProps);
    log.info("Starting stream with bootstrap servers {}", config.bootstrapServers());
    var streams = StreamManager.buildStreamsApp(config);
    streams.start();

    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
    Runtime.getRuntime().addShutdownHook(new Thread(() -> streams.close()));
  }

  private static Map<String, String> getEnv() {
    try {
      return System.getenv();
    }
    catch(SecurityException e) {
      log.error("Application does not have permission to read environment variables. Using defaults.");
      return Collections.EMPTY_MAP;
    }
  }

}
