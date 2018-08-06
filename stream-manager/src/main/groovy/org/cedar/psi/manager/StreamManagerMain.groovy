package org.cedar.psi.manager

import groovy.util.logging.Slf4j
import org.cedar.psi.manager.config.Constants
import org.cedar.psi.manager.stream.StreamManager

@Slf4j
class StreamManagerMain {

  static void main(String[] args) {
    def bootstrapServers

    // Check for supplied bootstrap servers before using default. Command-line
    // args supersede environment variable.
    if(args.length == 1) {
      bootstrapServers = args[0]
    }
    else {
      try {
        bootstrapServers = System.getenv('IM_BOOTSTRAP_SERVERS') ?: Constants.BOOTSTRAP_DEFAULT
      }
      catch(SecurityException e) {
        log.error 'Application does not have permission to read environment variables.\n' +
            'Defaulting to "localhost:9092" as bootstrap servers.'
        bootstrapServers = Constants.BOOTSTRAP_DEFAULT
      }
    }

    log.info("Starting stream with bootstrap servers ${bootstrapServers}")
    def streams = StreamManager.buildStreamsApp(bootstrapServers)
    streams.start()

    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
    Runtime.getRuntime().addShutdownHook(new Thread({ streams.close() }))
  }
}
