package org.cedar.psi.manager.util

import groovy.transform.CompileStatic
import org.apache.kafka.streams.kstream.Predicate
import org.cedar.psi.common.avro.Input


@CompileStatic
class RoutingUtils {

  // SME Splitting Info
  static final String SPLIT_FIELD = 'source'
  static final List<String> SPLIT_VALUES = ['common-ingest']

  static Predicate<String, Input> isSME = new Predicate<String, Input>() {
    @Override boolean test(String key, Input value) {
      SPLIT_FIELD in value.schema.fields*.name() && value.get(SPLIT_FIELD) in SPLIT_VALUES
    }
  }

  static Predicate<String, Input> isNotSME = new Predicate<String, Input>() {
    @Override boolean test(String key, Input value) {
      !isSME.test(key, value)
    }
  }

}
