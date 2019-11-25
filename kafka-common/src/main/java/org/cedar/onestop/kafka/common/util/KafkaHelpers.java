package org.cedar.onestop.kafka.common.util;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static org.apache.kafka.streams.KafkaStreams.State.ERROR;
import static org.apache.kafka.streams.KafkaStreams.State.NOT_RUNNING;

public class KafkaHelpers {
  private static final Logger log = LoggerFactory.getLogger(KafkaHelpers.class);

  public static KafkaStreams buildStreamsAppWithKillSwitch(Topology topology, Properties streamsConfig) {
    var killSwitch = new CompletableFuture<KafkaStreams.State>();
    killSwitch.thenAcceptAsync((state) -> {
      log.error("kill switch triggered with state " + state + "exiting...");
      System.exit(1);
    });
    KafkaStreams.StateListener killSwitchListener = (newState, oldState) -> {
      if (!killSwitch.isDone() && (newState == ERROR || newState == NOT_RUNNING)) {
        log.error("app entered bad state " + newState + ", executing kill switch");
        killSwitch.complete(newState);
      }
    };

    var app = new KafkaStreams(topology, streamsConfig);
    app.setStateListener(killSwitchListener);
    return app;
  }

}
