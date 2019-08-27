package org.cedar.psi.registry.stream;

import groovy.json.JsonOutput;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Reducer;
import org.cedar.psi.common.util.DataUtils;
import org.cedar.psi.common.util.TimestampedValue;
import org.cedar.schemas.avro.psi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
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

  public static Initializer<AggregatedInput> aggregatedInputInitializer = () -> null;

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
    var builder = method == PATCH && aggregate instanceof AggregatedInput ?
        AggregatedInput.newBuilder(aggregate) :
        AggregatedInput.newBuilder();

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
      var operation = input.getOperation();
      var mergedMap = updateRawJson(builder, input, operation);
      if (mergedMap != null) {
        // filter the merged map so we don't overwrite the entire AggregatedInput
        var fieldsToParse = List.of("fileInformation", "fileLocations", "publishing", "relationships");
        DataUtils.updateDerivedFields(builder, mergedMap, fieldsToParse);
      }
    }
    if (contentType.equals("application/xml") || contentType.equals("text/xml")) {
      builder.setRawXml(input.getContent());
    }

    // note: we always preserve existing events, hence aggregate.getEvents() instead of builder.getEvents()
    var currentEvents = aggregate != null ? aggregate.getEvents() : null;
    builder.setEvents(DataUtils.addOrInit(currentEvents, buildEventRecord(timestampedInput)));
    return builder.build();
  };

  public static AggregatedInput updateDeleted(TimestampedValue<Input> input, AggregatedInput aggregate) {
    if (aggregate == null) {
      return null;
    }
    var data = input != null ? input.data : null;
    var method = data != null ? input.data.getMethod() : null;
    var deleted = method != null && method.equals(DELETE);
    return AggregatedInput.newBuilder(aggregate)
        .setDeleted(deleted)
        .setEvents(DataUtils.addOrInit(aggregate.getEvents(), buildEventRecord(input)))
        .build();
  }

  public static Map updateRawJson(AggregatedInput.Builder builder, Input input, OperationType operationType) {
    try {
      var currentMap = DataUtils.parseJsonMap(builder.getRawJson());
      var inputMap = DataUtils.parseJsonMap(input.getContent());
      Map mergedMap = new HashMap();
      if(operationType == OperationType.REMOVE) {
        mergedMap = DataUtils.removeFromMap(currentMap, inputMap);
      }
      else {
        mergedMap = DataUtils.mergeMaps(currentMap, inputMap);
      }
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

  public static InputEvent buildEventRecord(TimestampedValue<Input> input) {
    return InputEvent.newBuilder()
        .setMethod(input.data.getMethod())
        .setOperation(input.data.getOperation())
        .setSource(input.data.getSource())
        .setTimestamp(input.timestampMs)
        .build();
  }

}
