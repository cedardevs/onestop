package org.cedar.onestop.kafka.common.util;

import org.apache.kafka.streams.KafkaStreams;

public class KafkaHealthUtils {

  public static Boolean isStreamsAppAlive(KafkaStreams streamsApp) {
    return streamsApp.state().isRunningOrRebalancing();
  }

  public static Boolean isStreamsAppReady(KafkaStreams streamsApp) {
    return streamsApp.state().isRunningOrRebalancing();
  }

}
