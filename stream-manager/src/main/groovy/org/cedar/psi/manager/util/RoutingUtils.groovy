package org.cedar.psi.manager.util

import groovy.transform.CompileStatic
import org.apache.kafka.streams.kstream.Predicate
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.ParsedRecord


@CompileStatic
class RoutingUtils {

  // SME Splitting Info
  static final String SPLIT_FIELD = 'source'
  static final List<String> SPLIT_VALUES = ['common-ingest']

  static final Predicate<String, Input> isSME = new Predicate<String, Input>() {
    @Override boolean test(String key, Input value) {
      SPLIT_FIELD in value.schema.fields*.name() && value.get(SPLIT_FIELD) in SPLIT_VALUES
    }
  }

  static final Predicate<String, Input> isNotSME = new Predicate<String, Input>() {
    @Override boolean test(String key, Input value) {
      !isSME.test(key, value)
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
