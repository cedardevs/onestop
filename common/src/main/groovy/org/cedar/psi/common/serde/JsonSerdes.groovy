package org.cedar.psi.common.serde

import groovy.transform.CompileStatic
import org.apache.kafka.common.serialization.Serde

@CompileStatic
class JsonSerdes {

  static Serde<Map<String, Object>> Map() {
    return new JsonMapSerde()
  }

}
