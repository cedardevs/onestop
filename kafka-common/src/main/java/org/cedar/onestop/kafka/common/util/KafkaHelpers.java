package org.cedar.onestop.kafka.common.util;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static org.apache.kafka.streams.KafkaStreams.State.ERROR;
import static org.apache.kafka.streams.KafkaStreams.State.NOT_RUNNING;

public class KafkaHelpers {

  public static KafkaStreams buildStreamsAppWithKillSwitch(Topology topology, Properties streamsConfig) {
    var killSwitch = new CompletableFuture<KafkaStreams.State>();
    killSwitch.thenAcceptAsync((state) -> {
      throw new IllegalStateException("KafkaStreams app entered bad state: " + state);
    });
    KafkaStreams.StateListener killSwitchListener = (newState, oldState) -> {
      if (!killSwitch.isDone() && (newState == ERROR || newState == NOT_RUNNING)) {
        killSwitch.complete(newState);
      }
    };

    var app = new KafkaStreams(topology, streamsConfig);
    app.setStateListener(killSwitchListener);
    return app;
  }

}
