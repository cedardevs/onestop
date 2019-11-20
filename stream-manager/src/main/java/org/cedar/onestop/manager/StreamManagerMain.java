package org.cedar.onestop.manager;

import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.manager.stream.StreamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamManagerMain {
  private static final Logger log = LoggerFactory.getLogger(StreamManagerMain.class);

  public static void main(String[] args) {
    AppConfig appConfig;
    if (args.length == 1) {
      appConfig = new AppConfig(args[0]);
    }
    else if (args.length > 1) {
      log.error("Application received more than one arg for config file path. Using defaults and/or system/environment variables.");
      appConfig = new AppConfig();
    }
    else if (System.getenv().containsKey("CONFIG_LOCATION")) {
      appConfig = new AppConfig(System.getenv().get("CONFIG_LOCATION"));
    }
    else {
      appConfig = new AppConfig();
    }

    log.info("Starting stream with bootstrap servers {}", appConfig.get("kafka.bootstrap.servers"));
    var streamsApp = StreamManager.buildStreamsApp(appConfig);
    Runtime.getRuntime().addShutdownHook(new Thread(streamsApp::close));
    streamsApp.start();
  }
}
