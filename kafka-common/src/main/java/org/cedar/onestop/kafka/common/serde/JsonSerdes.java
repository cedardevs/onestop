package org.cedar.onestop.kafka.common.serde;

import org.apache.kafka.common.serialization.Serde;

import java.util.Map;

public class JsonSerdes {

  public static Serde<Map<String, Object>> Map() {
    return new JsonMapSerde();
  }

}
