package org.cedar.psi.manager

import groovy.util.logging.Slf4j
import org.cedar.psi.manager.config.ManagerConfig
import org.cedar.psi.manager.stream.StreamManager

@Slf4j
class StreamManagerMain {

  static void main(String[] args) {
    def config = new ManagerConfig(getEnv() + System.properties as Map)
    log.info("Starting stream with bootstrap servers ${config.bootstrapServers()}")
    def streams = StreamManager.buildStreamsApp(config)
    streams.start()

    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
    Runtime.getRuntime().addShutdownHook(new Thread({ streams.close() }))
  }

  static Map getEnv() {
    try {
      return System.getenv()
    }
    catch(SecurityException e) {
      log.error 'Application does not have permission to read environment variables. Using defaults.'
      return [:]
    }
  }

}
