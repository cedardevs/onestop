package org.cedar.psi.manager.util;

import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.RecordType;

import java.util.List;
import java.util.Map;


public class RoutingUtils {

  // for each input type, a list of input sources which should be routed to the extractors
  public static final Map<RecordType, List<String>> extractableInputSources = Map.of(
      RecordType.granule, List.of("common-ingest"));

  public static boolean requiresExtraction(String key, AggregatedInput value) {
    return extractableInputSources.get(value.getType()).contains(value.getInitialSource());
  };

  public static boolean hasErrors(String key, AggregatedInput value) {
    return value.getErrors() != null && value.getErrors().size() > 0;
  }

  public static boolean isNull(String key, Object value) {
    return value == null;
  }

}
