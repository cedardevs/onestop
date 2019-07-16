package org.cedar.psi.manager;

import org.cedar.psi.common.util.DataUtils;
import org.cedar.psi.manager.config.ManagerConfig;
import org.cedar.psi.manager.stream.StreamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class StreamManagerMain {
  private static final Logger log = LoggerFactory.getLogger(StreamManagerMain.class);


  public static void main(String[] args) {
    var combinedProps = new LinkedHashMap<String, Object>();
    getEnv().forEach((k, v) -> combinedProps.merge(k, v, (v1, v2) -> v2));
    System.getProperties().forEach((k, v) -> combinedProps.merge(k.toString(), v, (v1, v2) -> v2));
    if (args.length == 1) {
      Yaml yaml = new Yaml();
      try {
        InputStream inputStream = Files.newInputStream(Path.of(args[0]));
        Map<String, Object> configFileMap = yaml.load(inputStream);
        Map<String, Object> yamlKafkaConfigs =
            DataUtils.consolidateNestedKeysInMap(null, (Map<String, Object>) configFileMap.get("kafka"));

        // Any environment or system properties are prefixed with "kafka." so remove the prefix before merging (Kafka
        // config values do NOT have this prefix)
        Map<String, Object> trimmedProps = new LinkedHashMap<>();
        combinedProps.forEach((k, v) -> {
          String trimmedKey = k.toLowerCase().startsWith("kafka.") ? k.substring(6) : k;
          trimmedProps.put(trimmedKey, v);
        });
        yamlKafkaConfigs.forEach((k, v) -> combinedProps.merge(k, v, (v1, v2) -> v2));

        // Note -- if anything other than "kafka" config values are in the yaml file, we are currently dropping them.
      }
      catch (IOException e) {
        log.error("Cannot open config file path [ " + args[0] + " ]. Using defaults and/or system/environment variables.");
      }
    }
    else if(args.length > 1) {
      log.error("Application received more than one arg for config file path. Using defaults and/or system/environment variables.");
    }

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
