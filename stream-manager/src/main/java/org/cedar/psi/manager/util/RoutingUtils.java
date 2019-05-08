package org.cedar.psi.manager.util;

import org.cedar.schemas.avro.psi.Input;
import org.cedar.schemas.avro.psi.RecordType;

import java.util.List;
import java.util.Map;


public class RoutingUtils {

  // for each input type, a list of input sources which should be routed to the extractors
  public static final Map<RecordType, List<String>> extractableInputSources = Map.of(
      RecordType.granule, List.of("common-ingest"));

  public static boolean requiresExtraction(String key, Input value) {
    return extractableInputSources.get(value.getType()).contains(value.getSource());
  };

}
