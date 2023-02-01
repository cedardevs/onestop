package org.cedar.onestop.kafka.common.util;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.StreamsMetadata;

import java.util.Collection;

public class KafkaHealthUtils {

  public static Boolean isStreamsAppAlive(KafkaStreams streamsApp) {
    return streamsApp.state().isRunningOrRebalancing();
  }

  public static Boolean isStreamsAppReady(KafkaStreams streamsApp) {
    var state = streamsApp.state();

    // short-circuit further checks if not running
    if (!state.isRunningOrRebalancing()) {
      return false;
    }

    var hasStores = streamsApp.allMetadata().stream()
        .map(StreamsMetadata::stateStoreNames)
        .mapToLong(Collection::size)
        .sum() > 0;
    if (hasStores && state.equals(KafkaStreams.State.REBALANCING)) {
      // TODO - in Kafka Streams 2.5 it's possible to serve stale data from existing instances
      // while rebalancing is in progress. If we upgrade the library and then MetadataStore logic
      // such that data can be served while rebalancing is underway then each instance should
      // continue to be READY while in the rebalancing state
      return false;
    }

    return true;
  }

}
