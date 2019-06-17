package org.cedar.psi.common.util;

import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.ErrorEvent;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.util.AvroUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class DataUtils {

  public static <T> List<T> addOrInit(List<T> list, T item) {
    var result = new ArrayList<T>();
    if (list != null && list.size() > 0) {
      result.addAll(list);
    }
    if (item != null) {
      result.add(item);
    }
    return result;
  }

  public static Map<String, Object> parseJsonMap(String json) throws IOException {
    if (json == null || json == "") {
      return new LinkedHashMap();
    }
    else {
      return new ObjectMapper().readValue(json, Map.class);
    }
  }

  // TODO - This needs to be MUCH more thorough to handle PATCHing of deeply nested ojects and lists...
  public static Map<String, Object> mergeMaps(Map<String, Object> first, Map<String, Object> second) {
    if (first == null && second == null) {
      return null;
    }
    Map left = first != null ? first : Map.of();
    Map right = second != null ? second : Map.of();
    Map result = new LinkedHashMap();
    left.forEach((k, v) -> result.merge(k, v, (v1, v2) -> v2));
    right.forEach((k, v) -> result.merge(k, v, (v1, v2) -> v2));
    return result;
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

  public static void updateDerivedFields(ParsedRecord.Builder builder, Map<String, Object> fieldData) {
    // 1. filter the merged map so we don't overwrite the entire ParsedRecord
    var fieldsToParse = List.of("discovery", "fileInformation", "fileLocations", "publishing", "relationships", "errors");
    var schema = ParsedRecord.getClassSchema();
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

  public static <T extends Object> T setValueOnPojo(T pojo, String fieldName, Object value) {
    try {
      var setter = findSetterForValue(pojo, fieldName, value);
      if (setter.isPresent()) {
        setter.get().invoke(pojo, value);
        return pojo;
      }
      else {
        throw new UnsupportedOperationException("Unable to find a setter for field [" + fieldName +
            "] on builder [" + pojo + "] accepting parameter type [" + value.getClass() + "]");
      }
    } catch (Exception e) {
      throw new UnsupportedOperationException("Unable to set value for field [" + fieldName +
          "] on builder [" + pojo + "]", e);
    }
  }

  // TODO - this method is a BEGGING to be memoized
  private static Optional<Method> findSetterForValue(Object pojo, String fieldName, Object value) {
    var setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    return Arrays.stream(pojo.getClass().getMethods())
        .filter(m -> m.getName().equals(setterName))
        .filter(m -> m.getParameterCount() == 1)
        .filter(m -> m.getParameterTypes()[0].isAssignableFrom(value.getClass()))
        .findFirst();
  }

  // TODO - this method is a BEGGING to be memoized
  private static Optional<Method> findGetterForType(Object pojo, String fieldName, Class clazz) {
    var getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    return Arrays.stream(pojo.getClass().getMethods())
        .filter(m -> m.getName().equals(getterName))
        .filter(m -> m.getParameterCount() == 0)
        .filter(m -> m.getReturnType().isAssignableFrom(clazz))
        .findFirst();
  }

}
