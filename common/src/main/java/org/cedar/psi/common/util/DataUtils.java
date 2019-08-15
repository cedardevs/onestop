package org.cedar.psi.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.ErrorEvent;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.util.AvroUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("unchecked")
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
    Map<String, Object> mergedMap = original == null ? new LinkedHashMap<>() : new LinkedHashMap<>(original);
    if (original == null && toAdd == null) {
      return Collections.emptyMap();
    }
    if (original == null || original.size() == 0) {
      return toAdd;
    }
    if (toAdd == null || toAdd.size() == 0) {
      return original;
    }

    toAdd.forEach((k, v) -> {
      var originalValue = mergedMap.get(k);
      if (v instanceof Map && originalValue instanceof Map) {
        mergedMap.put(k, mergeMaps((Map) originalValue, (Map) v));
      }
      else if (v instanceof List && originalValue instanceof List) {
        var mergedList = new HashSet((List) originalValue);
        mergedList.addAll((List) v);
        mergedMap.put(k, new ArrayList(mergedList));
      }
      else {
        /* This overwrites simple values but also mismatched object types. Accepting that "risk" here since
        useful errors are generated downstream for objects being cast to avro pojos but also because unknown JSON is
        allowed to pass through later parsing/analysis steps untouched (either type change could be erroneous but
        there's no way to know which) */
        mergedMap.put(k, v);
      }
    });

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
    Map mergedMap = original == null ? new LinkedHashMap<>() : new LinkedHashMap<>(original);
    if (original == null && toRemove == null) {
      return Collections.emptyMap();
    }
    if (original == null || original.size() == 0) {
      return Collections.emptyMap();
    }
    if (toRemove == null || toRemove.size() == 0) {
      return original;
    }

    toRemove.forEach((k, v) -> {
      var originalValue = mergedMap.get(k);
      if (v instanceof Map && originalValue instanceof Map) {
        mergedMap.put(k, removeFromMap((Map) originalValue, (Map) v));
      }
      else if (v instanceof List && originalValue instanceof List) {
        var mergedList = new HashSet<>((List) originalValue);
        mergedList.removeAll((List) v);
        mergedMap.put(k, mergedList);
      }
      else if ((v == null && originalValue == null) || v.equals(originalValue)) {
        mergedMap.remove(k);
      }
    });

    return mergedMap;
  }

  /**
   * @param builderType   type of schema builder either ParsedRecord or AggregatedInput, otherwise error out
   * @param fieldData     parsed or input metadata values
   * @param fieldsToParse list of schema fields that only support merged map
   */
  public static void updateDerivedFields(Object builderType, Map<String, Object> fieldData, List fieldsToParse) {
    // 1. identify the builder type
    var AggregatedSchemaType = builderType instanceof AggregatedInput.Builder;
    var parsedSchemaType = builderType instanceof ParsedRecord.Builder;
    var schema = AggregatedSchemaType ? AggregatedInput.getClassSchema() : ParsedRecord.getClassSchema();
    var builder = AggregatedSchemaType ? (AggregatedInput.Builder) builderType : (ParsedRecord.Builder) builderType;

    fieldData
        .entrySet()
        .stream()
        .filter(e -> fieldsToParse.contains(e.getKey()))
        .forEach(e -> {
          try {
            // 2. coerce the value to its AggregatedInput field value and apply it to the builder
            var field = schema.getField(e.getKey());
            var coerced = AvroUtils.coerceValueForSchema(e.getValue(), field.schema());
            setValueOnPojo(builder, field.name(), coerced);
          }
          catch (Exception ex) {
            // 3. if it fails attach an error to the AggregatedInput, but continue with remaining fields
            var error = ErrorEvent.newBuilder()
                .setTitle("Failed to parse field [" + e.getKey() + "]")
                .setDetail(ex.getMessage())
                .build();
            if (AggregatedSchemaType) {
              var aggregatedInputBuilder = (AggregatedInput.Builder) builder;
              aggregatedInputBuilder.setErrors(addOrInit(aggregatedInputBuilder.getErrors(), error));
            }
            else if(parsedSchemaType) {
              var parsedBuilder = (ParsedRecord.Builder) builder;
              parsedBuilder.setErrors(addOrInit(parsedBuilder.getErrors(), error));
            }
            else {
              throw new UnsupportedOperationException("Unable to set error value [" + error +
                  "] on builder [" + builder + "]");
            }
          }
        });
  }

  /**
   *
   * @param pojo schema builder type
   * @param fieldName schema field name
   * @param value metadata values that need to be updated
   * @param <T> type of pojo
   * @return it returns the new updated metadata value otherwise, throws unsupported operation
   */
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

  /**
   *
   * @param pojo schema builder type
   * @param fieldName schema field name
   * @param value metadata values that need to be updated
   * @return returns the builder setter name
   */
  // TODO - this method is a BEGGING to be memoized
  private static Optional<Method> findSetterForValue(Object pojo, String fieldName, Object value) {
    var setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    return Arrays.stream(pojo.getClass().getMethods())
        .filter(m -> m.getName().equals(setterName))
        .filter(m -> m.getParameterCount() == 1)
        .filter(m -> m.getParameterTypes()[0].isAssignableFrom(value.getClass()))
        .findFirst();
  }

  /**
   *
   * @param pojo schema builder type
   * @param fieldName schema field name
   * @param clazz schema class name
   * @return returns the builder getter name
   */
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
