package org.cedar.onestop.manager;

import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.manager.stream.StreamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamManagerMain {
  private static final Logger log = LoggerFactory.getLogger(StreamManagerMain.class);

  public static void main(String[] args) {
    AppConfig appConfig = new AppConfig();
    log.info("Starting stream with bootstrap servers {}", appConfig.get("kafka.bootstrap.servers"));
    var streamsApp = StreamManager.buildStreamsApp(appConfig);
    Runtime.getRuntime().addShutdownHook(new Thread(streamsApp::close));
    streamsApp.start();
  }
}
