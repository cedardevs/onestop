package org.cedar.psi.manager.util

import groovy.transform.CompileStatic
import org.apache.kafka.streams.kstream.Predicate
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.ParsedRecord
import org.cedar.psi.common.avro.RecordType


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

  static final Predicate<String, Input> isNotSME = new Predicate<String, Input>() {
    @Override boolean test(String key, Input value) {
      !requiresExtraction.test(key, value)
    }
  }

  static final Predicate<String, ParsedRecord> hasErrors = new Predicate<String, ParsedRecord>() {
    @Override boolean test(String key, ParsedRecord value) {
      value?.errors?.size() > 0
    }
  }

  static final Predicate<Object, Object> isDefault = new Predicate<Object, Object>() {
    @Override boolean test(Object key, Object value) {
      true
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
