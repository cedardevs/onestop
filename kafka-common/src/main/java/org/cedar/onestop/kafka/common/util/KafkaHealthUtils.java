package org.cedar.onestop.kafka.common.util;

import org.apache.kafka.streams.KafkaStreams;

public class KafkaHealthUtils {

  public static Boolean isStreamsAppAlive(KafkaStreams streamsApp) {
    return streamsApp.state().isRunning();
  }

  public static Boolean isStreamsAppReady(KafkaStreams streamsApp) {
    var state = streamsApp.state();
    // TODO - in Kafka Streams 2.5 it's possible to serve stale data from existing instances
    // while rebalancing is in progress. If we upgrade the library and then MetadataStore logic
    // such that data can be served while rebalancing is underway then each instance should
    // continue to be READY while in the reblancing state
    return state.isRunning() && !state.equals(KafkaStreams.State.REBALANCING);
  }

}
