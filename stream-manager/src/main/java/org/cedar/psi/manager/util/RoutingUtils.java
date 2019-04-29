package org.cedar.psi.manager.util;

import org.apache.kafka.streams.kstream.Predicate;
import org.cedar.schemas.avro.psi.Input;
import org.cedar.schemas.avro.psi.RecordType;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RoutingUtils {

  // for each input type, a list of input sources which should be routed to the extractors
  public static final Map<RecordType, List<String>> extractableInputSources = new LinkedHashMap<>();

  static {
    extractableInputSources.put(RecordType.granule, Arrays.asList("common-ingest"));
  }

  public static boolean requiresExtraction(String key, Input value) {
    return extractableInputSources.get(value.getType()).contains(value.getSource());
  };

  static final Predicate not(Predicate orig) {
    return new Predicate() {
      @Override
      public boolean test(Object key, Object value) {
        return !orig.test(key, value);
      }
    };
  }

}
