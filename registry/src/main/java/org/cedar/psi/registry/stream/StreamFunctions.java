package org.cedar.psi.registry.stream;

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Reducer;
import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.Input;
import org.cedar.schemas.avro.psi.InputEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.cedar.schemas.avro.psi.Method.*;

public class StreamFunctions {
  private static final Logger log = LoggerFactory.getLogger(StreamFunctions.class);

  public static Reducer identityReducer = (aggregate, nextValue) -> nextValue;

  public static Reducer<Set> setReducer = (aggregate, nextValue) -> {
    if (nextValue == null) {
      return null; // if we get a tombstone, tombstone the whole set
    }
    else {
      aggregate.addAll(nextValue);
      return aggregate;
    }
  };

  public static Initializer<AggregatedInput> aggregatedInputInitializer = () -> AggregatedInput.newBuilder().build();

  public static Aggregator<String, Input, AggregatedInput> inputAggregator = (key, input, aggregate) -> {
    log.debug("Aggregating input for key {} with method {}", key, input.getMethod());
    if (input == null) {
      // TODO - ??
    }

    var method = input.getMethod();
    if (method == DELETE || method == GET) {
      System.out.println("updating deleted");
      return updateDeleted(input, aggregate);
    }

    var builder = method == PATCH ? AggregatedInput.newBuilder(aggregate) : AggregatedInput.newBuilder();

    var contentType = input.getContentType().strip().toLowerCase();
    if (contentType.equals("application/json") || contentType.equals("text/json")) {
      builder.setRawJson(mergeJsonMapStrings(builder.getRawJson(), input.getContent()));
    }
    if (contentType.equals("application/xml") || contentType.equals("text/xml")) {
      builder.setRawXml(input.getContent());
    }
    if (builder.getInitialSource() == null) {
      builder.setInitialSource(input.getSource());
    }
    if (builder.getType() == null) {
      builder.setType(input.getType());
    }
    if (builder.getType() != input.getType()) {
      // TODO - what do we do if types don't match??
    }

    // note: we always preserve existing events, hence aggregate.getEvents() instead of builder.getEvents()
    builder.setEvents(addEventRecord(input, aggregate.getEvents()));
    return builder.build();
  };

  public static AggregatedInput updateDeleted(Input input, AggregatedInput aggregate) {
    return AggregatedInput.newBuilder(aggregate)
        .setDeleted(input.getMethod() == DELETE)
        .setEvents(addEventRecord(input, aggregate.getEvents()))
        .build();
  }

  public static List<InputEvent> addEventRecord(Input input, List<InputEvent> events) {
    var list = events == null ? new ArrayList<InputEvent>() : events;
    if (input != null) {
      list.add(buildEventRecord(input));
    }
    return list;
  }

  public static InputEvent buildEventRecord(Input input) {
    return InputEvent.newBuilder()
        .setMethod(input.getMethod())
        .setOperation(input.getOperation())
        .setSource(input.getSource())
//        .setTimestamp() TODO - how do we do this?
        .build();
  }

  public static String mergeJsonMapStrings(String a, String b) {
    var first = parseJsonMap(a);
    var second = parseJsonMap(b);
    second.forEach((k, v) -> first.merge(k, v, (v1, v2) -> v2));
    return JsonOutput.toJson(first);
  }

  private static Map parseJsonMap(String json) {
    if (json == null || json == "") {
      return new LinkedHashMap();
    }
    else {
      JsonSlurper slurper = new JsonSlurper();
      return (Map) slurper.parseText(json);
    }
  }
}
