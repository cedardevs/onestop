package org.cedar.onestop.registry.service;

import org.apache.kafka.streams.KafkaStreams;
import org.cedar.onestop.kafka.common.util.KafkaHealthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class KafkaStreamsReadiness implements HealthIndicator {

  private final KafkaStreams streamsApp;

  @Autowired
  KafkaStreamsReadiness(KafkaStreams streamsApp) {
    this.streamsApp = streamsApp;
  }

  @Override
  public Health health() {
    if (KafkaHealthUtils.isStreamsAppReady(streamsApp)) {
      return Health.up().build();
    }
    else {
      return Health.down()
          .withDetail("Error State", "Kafka Streams application state is " + streamsApp.state())
          .build();
    }
  }

}
