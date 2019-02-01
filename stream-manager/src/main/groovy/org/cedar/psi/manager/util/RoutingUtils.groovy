package org.cedar.psi.manager.util

import groovy.transform.CompileStatic
import org.apache.kafka.streams.kstream.Predicate
import org.cedar.schemas.avro.psi.Input
import org.cedar.schemas.avro.psi.RecordType


@CompileStatic
class RoutingUtils {

  // for each input type, a list of input sources which should be routed to the extractors
  static final Map<RecordType, List<String>> extractableInputSources = [
      (RecordType.granule): ['common-ingest']
  ]

  static final Predicate<String, Input> requiresExtraction = new Predicate<String, Input>() {
    @Override boolean test(String key, Input value) {
      value.source in extractableInputSources[value.type] ?: []
    }
  }

  static final Predicate not(Predicate orig) {
    return new Predicate() {
      @Override
      boolean test(Object key, Object value) {
        !orig.test(key, value)
      }
    }
  }

}
