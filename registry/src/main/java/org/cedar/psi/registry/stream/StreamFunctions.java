package org.cedar.psi.registry.stream;

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Reducer;
import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.ErrorEvent;
import org.cedar.schemas.avro.psi.Input;
import org.cedar.schemas.avro.psi.InputEvent;
import org.cedar.schemas.avro.util.AvroUtils;
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
      return updateDeleted(input, aggregate);
    }

    // if we're PATCHing then build on top of the existing state, else create a new state
    var builder = method == PATCH ? AggregatedInput.newBuilder(aggregate) : AggregatedInput.newBuilder();

    if (builder.getType() == null) {
      builder.setType(input.getType());
    }
    else if (builder.getType() != input.getType()) {
      var error = ErrorEvent.newBuilder()
          .setTitle("Mismatched types")
          .setDetail("Input attempted to change the type of an entity from ["
              + builder.getType() + "] to [" + input.getType() + "]")
          .build();
      return builder.setErrors(addOrInit(builder.getErrors(), error)).build();
    }

    if (builder.getInitialSource() == null) {
      builder.setInitialSource(input.getSource());
    }

    var contentType = input.getContentType().strip().toLowerCase();
    if (contentType.equals("application/json") || contentType.equals("text/json")) {
      // 1. merge the existing json map with the new one
      var currentMap = parseJsonMap(builder.getRawJson());
      var inputMap = parseJsonMap(input.getContent());
      var mergedMap = mergeMaps(currentMap, inputMap);
      builder.setRawJson(JsonOutput.toJson(mergedMap));

      // 2. filter the merged map so we don't overwrite the entire AggregatedInput
      var fieldsToParse = List.of("fileInformation", "fileLocations", "publishing", "relationships");
      var schema = AggregatedInput.getClassSchema();
      mergedMap
          .entrySet()
          .stream()
          .filter(e -> fieldsToParse.contains(e.getKey()))
          .forEach(e -> {
            try {
              // 3. coerce the value to its AggregatedInput field value and apply it to the builder
              var field = schema.getField(e.getKey());
              var coerced = AvroUtils.coerceValueForSchema(e.getValue(), field.schema());
              setValueOnPojo(builder, field.name(), coerced);
            }
            catch (Exception ex) {
              // 4. if it fails attach an error to the AggregatedInput, but continue with remaining fields
              var error = ErrorEvent.newBuilder()
                  .setTitle("Failed to parse field")
                  .setDetail("Failed to parse field [" + e.getKey() + "] with value [" + e.getValue() + "]")
                  .build();
              builder.setErrors(addOrInit(builder.getErrors(), error));
            }
          });
    }
    if (contentType.equals("application/xml") || contentType.equals("text/xml")) {
      builder.setRawXml(input.getContent());
    }

    // note: we always preserve existing events, hence aggregate.getEvents() instead of builder.getEvents()
    builder.setEvents(addOrInit(aggregate.getEvents(), buildEventRecord(input)));
    return builder.build();
  };

  public static AggregatedInput updateDeleted(Input input, AggregatedInput aggregate) {
    return AggregatedInput.newBuilder(aggregate)
        .setDeleted(input.getMethod().equals(DELETE))
        .setEvents(addOrInit(aggregate.getEvents(), buildEventRecord(input)))
        .build();
  }

  public static <T> List<T> addOrInit(List<T> list, T item) {
    var result = list == null ? new ArrayList<T>() : list;
    if (item != null) {
      result.add(item);
    }
    return result;
  }

  public static InputEvent buildEventRecord(Input input) {
    return InputEvent.newBuilder()
        .setMethod(input.getMethod())
        .setOperation(input.getOperation())
        .setSource(input.getSource())
//        .setTimestamp() TODO - how do we do this?
        .build();
  }

  // TODO - This needs to be MUCH more thorough to handle PATCHing of deeply nested ojects and lists...
  public static Map<String, Object> mergeMaps(Map<String, Object> first, Map<String, Object> second) {
    Map result = new LinkedHashMap();
    first.forEach((k, v) -> result.merge(k, v, (v1, v2) -> v2));
    second.forEach((k, v) -> result.merge(k, v, (v1, v2) -> v2));
    return result;
  }

  private static Map<String, Object> parseJsonMap(String json) {
    if (json == null || json == "") {
      return new LinkedHashMap();
    }
    else {
      JsonSlurper slurper = new JsonSlurper();
      return (Map) slurper.parseText(json);
    }
  }

  public static <T extends Object> T setValueOnPojo(T pojo, String fieldName, Object value) {
    try {
      var setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
      var setter = Arrays.stream(pojo.getClass().getMethods())
          .filter(m -> m.getName().equals(setterName))
          .filter(m -> m.getParameterCount() == 1)
          .filter(m -> m.getParameterTypes()[0].isAssignableFrom(value.getClass()))
          .findFirst();

      if (setter.isEmpty()) {
        throw new UnsupportedOperationException("Unable to find a setter for field [" + fieldName +
            "] on builder [" + pojo + "] accepting parameter type [" + value.getClass() + "]");
      }
      else {
        setter.get().invoke(pojo, value);
        return pojo;
      }
    } catch (Exception e) {
      throw new UnsupportedOperationException("Unable to set value for field [" + fieldName +
          "] on builder [" + pojo + "]", e);
    }
  }

}
