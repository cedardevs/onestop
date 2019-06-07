package org.cedar.psi.registry.stream;

import groovy.json.JsonOutput;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Reducer;
import org.cedar.psi.common.util.DataUtils;
import org.cedar.psi.common.util.TimestampedValue;
import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.ErrorEvent;
import org.cedar.schemas.avro.psi.Input;
import org.cedar.schemas.avro.psi.InputEvent;
import org.cedar.schemas.avro.util.AvroUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  public static Aggregator<String, TimestampedValue<Input>, AggregatedInput> inputAggregator = (key, timestampedInput, aggregate) -> {
    var input = timestampedInput.data;
    log.debug("Aggregating input for key {} with method {}", key, input.getMethod());
    if (input == null) {
      // TODO - ??
    }

    var method = input.getMethod();
    if (method == DELETE || method == GET) {
      return updateDeleted(timestampedInput, aggregate);
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
      return builder.setErrors(DataUtils.addOrInit(builder.getErrors(), error)).build();
    }

    if (builder.getInitialSource() == null) {
      builder.setInitialSource(input.getSource());
    }

    var contentType = input.getContentType().strip().toLowerCase();
    if (contentType.equals("application/json") || contentType.equals("text/json")) {
      var mergedMap = updateRawJson(builder, input);
      if (mergedMap != null) {
        updateDerivedFields(builder, mergedMap);
      }
    }
    if (contentType.equals("application/xml") || contentType.equals("text/xml")) {
      builder.setRawXml(input.getContent());
    }

    // note: we always preserve existing events, hence aggregate.getEvents() instead of builder.getEvents()
    builder.setEvents(DataUtils.addOrInit(aggregate.getEvents(), buildEventRecord(timestampedInput)));
    return builder.build();
  };

  public static AggregatedInput updateDeleted(TimestampedValue<Input> input, AggregatedInput aggregate) {
    return AggregatedInput.newBuilder(aggregate)
        .setDeleted(input.data.getMethod().equals(DELETE))
        .setEvents(DataUtils.addOrInit(aggregate.getEvents(), buildEventRecord(input)))
        .build();
  }

  public static Map updateRawJson(AggregatedInput.Builder builder, Input input) {
    try {
      var currentMap = DataUtils.parseJsonMap(builder.getRawJson());
      var inputMap = DataUtils.parseJsonMap(input.getContent());
      var mergedMap = DataUtils.mergeMaps(currentMap, inputMap);
      builder.setRawJson(JsonOutput.toJson(mergedMap));
      return mergedMap;
    }
    catch (IOException e) {
      var error = ErrorEvent.newBuilder()
          .setTitle("Unable to parse json")
          .setDetail("Failed to parsed json: " + e.getMessage())
          .build();
      builder.setErrors(DataUtils.addOrInit(builder.getErrors(), error));
      return null;
    }
  }

  public static void updateDerivedFields(AggregatedInput.Builder builder, Map<String, Object> fieldData) {
    // 1. filter the merged map so we don't overwrite the entire AggregatedInput
    var fieldsToParse = List.of("fileInformation", "fileLocations", "publishing", "relationships");
    var schema = AggregatedInput.getClassSchema();
    fieldData
        .entrySet()
        .stream()
        .filter(e -> fieldsToParse.contains(e.getKey()))
        .forEach(e -> {
          try {
            // 2. coerce the value to its AggregatedInput field value and apply it to the builder
            var field = schema.getField(e.getKey());
            var coerced = AvroUtils.coerceValueForSchema(e.getValue(), field.schema());
            DataUtils.setValueOnPojo(builder, field.name(), coerced);
          }
          catch (Exception ex) {
            // 3. if it fails attach an error to the AggregatedInput, but continue with remaining fields
            var error = ErrorEvent.newBuilder()
                .setTitle("Failed to parse field")
                .setDetail("Failed to parse field [" + e.getKey() + "] with value [" + e.getValue() + "]")
                .build();
            builder.setErrors(DataUtils.addOrInit(builder.getErrors(), error));
          }
        });
  }

  public static InputEvent buildEventRecord(TimestampedValue<Input> input) {
    return InputEvent.newBuilder()
        .setMethod(input.data.getMethod())
        .setOperation(input.data.getOperation())
        .setSource(input.data.getSource())
        .setTimestamp(input.timestampMs)
        .build();
  }

}
