package org.cedar.psi.manager;

import org.cedar.psi.manager.config.ManagerConfig;
import org.cedar.psi.manager.stream.StreamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamManagerMain {
  private static final Logger log = LoggerFactory.getLogger(StreamManagerMain.class);

  public static void main(String[] args) {
    ManagerConfig appConfig;
    if (args.length == 1) {
      appConfig = new ManagerConfig(args[0]);
    }
    else if (args.length > 1) {
      log.error("Application received more than one arg for config file path. Using defaults and/or system/environment variables.");
      appConfig = new ManagerConfig();
    }
    else if (System.getenv().containsKey("CONFIG_LOCATION")) {
      appConfig = new ManagerConfig(System.getenv().get("CONFIG_LOCATION"));
    }
    else {
      appConfig = new ManagerConfig();
    }

    log.info("Starting stream with bootstrap servers {}", appConfig.bootstrapServers());
    var streamsApp = StreamManager.buildStreamsApp(appConfig);
    Runtime.getRuntime().addShutdownHook(new Thread(streamsApp::close));
    streamsApp.start();
  }
}
