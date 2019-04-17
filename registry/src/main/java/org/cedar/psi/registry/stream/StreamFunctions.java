package org.cedar.psi.registry.stream;

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import org.apache.kafka.streams.kstream.Reducer;
import org.cedar.schemas.avro.psi.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static org.cedar.schemas.avro.psi.Method.DELETE;

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

  public static Reducer<Input> mergeInputContent = (aggregate, nextValue) -> {
    log.debug("Merging new input {} into existing aggregate {}", nextValue, aggregate);

    Input.Builder resultBuilder = Input.newBuilder(aggregate);
    if (nextValue.getContentType() != null) {
      resultBuilder.setContentType(nextValue.getContentType());
    }
    if (nextValue.getMethod() != null) {
      resultBuilder.setMethod(nextValue.getMethod());
    }
    if (nextValue.getContent() != null) {
      resultBuilder.setContent(mergeJsonMapStrings(aggregate.getContent(), nextValue.getContent()));
    }

    return resultBuilder.build();
  };

  public static Reducer<Input> replaceInputMethod = (aggregate, nextValue) -> {
    log.debug("{} existing aggregate {}", nextValue.getMethod() == DELETE ? "Deleting" : "Resurrecting", aggregate);

    Input.Builder resultBuilder = Input.newBuilder(aggregate);
    if (nextValue.getMethod() != null) {
      resultBuilder.setMethod(nextValue.getMethod());
    }

    return resultBuilder.build();
  };

  public static Reducer<Input> reduceInputs = (aggregate, nextValue) -> {
    log.debug("Reducing inputs with method {}", nextValue.getMethod());

    switch (nextValue.getMethod()) {
      case PATCH:
        return mergeInputContent.apply(aggregate, nextValue);

      case DELETE:
      case GET:
        return replaceInputMethod.apply(aggregate, nextValue);

      case PUT:
      case POST:
      default:
        return nextValue;
    }
  };

  private static String mergeJsonMapStrings(String a, String b) {
    JsonSlurper slurper = new JsonSlurper();
    Map first = (Map) slurper.parseText(a);
    Map second = (Map) slurper.parseText(b);
    second.forEach((k, v) -> first.merge(k, v, (v1, v2) -> v2));
    return JsonOutput.toJson(first);
  }
}
