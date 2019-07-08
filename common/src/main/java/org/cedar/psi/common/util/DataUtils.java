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

  /**
   * Returns a merged Map of the original and toAdd Maps. Deep merges of nested Maps and Lists are performed and
   * explicit duplicates (exact matches for all fields) are avoided.
   * @param original Base Map to which elements will be merged from toAdd
   * @param toAdd Map of elements to add to the original Map
   * @return An updated original Map where new elements from toAdd have been merged. Returns empty Map if
   * original and toAdd are empty or null.
   */
  public static Map<String, Object> mergeMaps(Map<String, Object> original, Map<String, Object> toAdd) {
    Map mergedMap = original == null ? new LinkedHashMap() : new LinkedHashMap(original);
    if (toAdd != null) {
      toAdd.forEach( (k, v) -> {
        var originalValue = mergedMap.get(k);
        if (originalValue == null) {
          mergedMap.put(k, v);
        }
        else if (v instanceof Map && originalValue instanceof Map) {
          mergedMap.put(k, mergeMaps((Map) originalValue, (Map) v));
        }
        else if (v instanceof List && originalValue instanceof List) {
          var mergedList = new HashSet((List) originalValue);
          mergedList.addAll((List) v);
          mergedMap.put(k, mergedList);
        }
        else {
        /* This overwrites simple values but also mismatched object types. Accepting that "risk" here since
        useful errors are generated downstream for objects being cast to avro pojos but also because unknown JSON is
        allowed to pass through later parsing/analysis steps untouched (either type change could be erroneous but
        there's no way to know which) */
          mergedMap.put(k, v);
        }
      });
    }
    return mergedMap;
  }

  /**
   * Returns a new Map of the original with elements in toRemove discarded. Elements in toRemove must match those in
   * original exactly, or they will not be removed. Handles nested Maps and Lists.
   * @param original Base Map from which elements in toRemove will be removed
   * @param toRemove Map of elements to remove from the original Map
   * @return An updated original Map where matching elements from toRemove have been discarded. Returns empty Map if
   * original is empty or null.
   */
  public static Map<String, Object> removeFromMap(Map<String, Object> original, Map<String, Object> toRemove) {
    Map mergedMap = original == null ? new LinkedHashMap() : new LinkedHashMap(original);
    if (toRemove != null) {
      toRemove.forEach( (k, v) -> {
        var originalValue = mergedMap.get(k);
        if (v instanceof Map && originalValue instanceof Map) {
          mergedMap.put(k, removeFromMap((Map) originalValue, (Map) v));
        }
        else if (v instanceof List && originalValue instanceof List) {
          var mergedList = new HashSet((List) originalValue);
          mergedList.removeAll((List) v);
          mergedMap.put(k, mergedList);
        }
        else if ((v == null && originalValue == null) || v.equals(originalValue)) {
          mergedMap.remove(k);
        }
      });
    }
    return mergedMap;
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
                .setTitle("Failed to parse field [" + e.getKey() + "]")
                .setDetail(ex.getMessage())
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
                .setTitle("Failed to parse field [" + e.getKey() + "]")
                .setDetail(ex.getMessage())
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
