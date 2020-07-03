package org.cedar.onestop.registry.stream;

import groovy.json.JsonOutput;
import org.apache.avro.specific.SpecificData;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Reducer;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.util.DataUtils;
import org.cedar.schemas.avro.psi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.cedar.schemas.avro.psi.Method.*;

public class StreamFunctions {
  private static final Logger log = LoggerFactory.getLogger(StreamFunctions.class);

  private static final int eventListLimit = 10;

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

  public static Aggregator<String, ValueAndTimestamp<Input>, AggregatedInput> inputAggregator = (key, timestampedInput, currentState) -> {
    var input = ValueAndTimestamp.getValueOrNull(timestampedInput);

    if (input == null) {
      return null; // Tombstone
    }

    log.debug("Aggregating input for key {} with method {}", key, input.getMethod());

    var method = input.getMethod();
    if (method == DELETE || method == GET) {
      return updateDeleted(timestampedInput, currentState);
    }

    // if we're PATCHing then build on top of the existing state, else create a new state
    var builder = method == PATCH && currentState instanceof AggregatedInput ?
        AggregatedInput.newBuilder(currentState) :
        AggregatedInput.newBuilder();

    if(builder.getType() != null && builder.getType() != input.getType()) {
      // Don't accept attempts to change the type of a record (and don't put the record in an error state based on
      // this attempt) but log an error if it happens
      log.error("Input attempted to change the type of an entity from ["
          + builder.getType() + "] to [" + input.getType() + "]");
      return builder.build();
    }

    // Reset the errors on the builder so they're always current
    builder.clearErrors();

    if (builder.getType() == null) {
      builder.setType(input.getType());
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

    var errors = builder.getErrors();
    var failedState = errors != null && !errors.isEmpty();

    // Note: we always preserve existing events, hence currentState.getEvents() instead of builder.getEvents()
    var currentEvents = currentState != null ? currentState.getEvents() : null;
    var mergedEvents = DataUtils.addOrInit(currentEvents, buildEventRecord(timestampedInput, failedState));
    builder.setEvents(DataUtils.truncateList(mergedEvents, eventListLimit, true));
    return builder.build();
  };

  public static AggregatedInput updateDeleted(ValueAndTimestamp<Input> input, AggregatedInput currentState) {
    if (currentState == null) {
      // Don't "update" a record that doesn't exist
      return null;
    }
    var data = input != null ? input.value() : null;
    var method = data != null ? input.value().getMethod() : null;
    var deleted = method != null && method.equals(DELETE);

    if (currentState.getDeleted() == deleted) {
      // Don't append to the events list if nothing is changing
      return currentState;
    }
    else {
      assert input != null;
      var mergedEvents = DataUtils.addOrInit(currentState.getEvents(), buildEventRecord(input, false));
      return AggregatedInput.newBuilder(currentState)
          .setDeleted(deleted)
          .setEvents(DataUtils.truncateList(mergedEvents, eventListLimit, true))
          .build();
    }
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
          .setSource(StreamsApps.REGISTRY_ID)
          .build();
      builder.setErrors(DataUtils.addOrInit(builder.getErrors(), error));
      return null;
    }
  }

  public static InputEvent buildEventRecord(ValueAndTimestamp<Input> input, boolean failedState) {
    return InputEvent.newBuilder()
        .setMethod(input.value().getMethod())
        .setOperation(input.value().getOperation())
        .setSource(input.value().getSource())
        .setTimestamp(input.timestamp())
        .setFailedState(failedState)
        .build();
  }

  /**
   * A value joiner to combine two parsed records by flattening/denormalizing their Discovery values
   *
   * NOTE: This is a simple field-by-field override process, i.e. if a child discovery field has been set
   * to any non-default value then it will fully replace the value from the parent. This is designed to mimic
   * the behavior implemented downstream via Elasticsearch update_by_query requests, which are still in use
   * for bulk updates when a parent collection changes.
   *
   * TODO: Revisit this logic if/when we have another approach for bulk flattening on collection changes or
   * if we rethink our flattening approach in its entirety.
   */
  public static ValueJoiner<? super ParsedRecord, ? super ParsedRecord, ParsedRecord> flattenRecords = (child, parent) -> {
    var discoverySchema = Discovery.getClassSchema();
    // defensive copy of the parent discovery object
    var copyForOverrides = SpecificData.getForClass(Discovery.class).deepCopy(discoverySchema, parent.getDiscovery());
    discoverySchema.getFields().forEach(f -> {
      var childVal = child.getDiscovery().get(f.name());
      if (childVal != null && !childVal.equals(f.defaultVal())) {
        // override the parent value if a child value exists
        copyForOverrides.put(f.pos(), childVal);
      }
    });
    // build a new record with the overridden discovery copy
    var flattenedRecordBuilder = ParsedRecord.newBuilder(child);
    flattenedRecordBuilder.setDiscovery(copyForOverrides);
    return flattenedRecordBuilder.build();
  };

}
