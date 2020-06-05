package org.cedar.onestop.kafka.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.*;
import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.ErrorEvent;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.util.AvroUtils;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class DataUtils {

  private static final ObjectMapper mapper = new ObjectMapper();

  public static <T> List<T> addOrInit(List<T> list, T item) {
    var result = new ArrayList<T>();
    if (list != null && !list.isEmpty()) {
      result.addAll(list);
    }
    if (item != null) {
      result.add(item);
    }
    return result;
  }

  /**
   *
   * @param list list to truncate
   * @param maxListSize list size limit
   * @param mostRecentAdditions if true, returned list reflects end of original list as opposed to start
   * @param <T> list object type
   * @return truncated list of T objects
   * @throws IllegalArgumentException if maxListSize is less than or equal to 0
   */
  public static <T> List<T> truncateList(List<T> list, int maxListSize, boolean mostRecentAdditions) {
    if (maxListSize <= 0) {
      throw new IllegalArgumentException("Attempted to make a list of size [ " + maxListSize + " ]. " +
          "Expected a size limit greater than 0.");
    }

    var result = new ArrayList<T>();
    if (list != null && !list.isEmpty()) {
      var size = list.size();
      if(size <= maxListSize) {
        result.addAll(list);
      }
      else {
        var fromIndex = mostRecentAdditions ? size - maxListSize : 0;
        var toIndex = mostRecentAdditions ? size : maxListSize;
        result.addAll(list.subList(fromIndex, toIndex));
      }
    }
    return result;
  }

  public static Map<String, Object> parseJsonMap(String json) throws IOException {
    if (json == null || json == "") {
      return new LinkedHashMap();
    }
    else {
      return mapper.readValue(json, Map.class);
    }
  }

  /**
   * Compares a source map to a target map and returns a diff list POJO of JSON PATCHes to go from the source to the
   * target. Maps are assumed to represent JSON objects. Input objects are alphabetically sorted for the user to return
   * an accurate diff list.
   * @param sourceJson
   * @param targetJson
   * @return JSON PATCH diff list for converting sourceJson to targetJson
   * @throws IOException
   */
  public static List<Map<String, Object>> getJsonDiffList(Map sourceJson, Map targetJson) throws IOException {
    var sortedSource = getJsonObject(mapper.writeValueAsString(sortMapByKeys(sourceJson)));
    var sortedTarget = getJsonObject(mapper.writeValueAsString(sortMapByKeys(targetJson)));

    return getJsonDiffList(sortedSource, sortedTarget);
  }

  /**
   * Compares a source JSON string to a target JSON string and returns a diff list POJO of JSON PATCHes to go from
   * the source to the target. Input JSON objects are alphabetically sorted for the user to return an accurate diff list.
   * @param sourceJson
   * @param targetJson
   * @return JSON PATCH diff list for converting sourceJson to targetJson
   * @throws IOException
   */
  public static List<Map<String, Object>> getJsonDiffList(String sourceJson, String targetJson) throws IOException {
    var sourceMap = parseJsonMap(sourceJson);
    var targetMap = parseJsonMap(targetJson);
    return getJsonDiffList(sourceMap, targetMap);
  }

  /**
   * Private method working directly with JsonObjects to create POJO of JSON PATCH diff list from source to target.
   * IMPORTANT: Unlike the public overloaded methods, this method assumes input JsonObjects are already sorted alphabetically.
   *
   * @param sourceJson
   * @param targetJson
   * @return JSON PATCH diff list for converting sourceJson to targetJson
   */
  private static List<Map<String, Object>> getJsonDiffList(JsonObject sourceJson, JsonObject targetJson) {
    JsonPatch diff = Json.createDiff(sourceJson, targetJson);
    List<Map<String, Object>> finalList = new ArrayList<>();
    List<JsonObject> joList = diff.toJsonArray().getValuesAs(JsonObject.class);
    joList.forEach(jo -> {
      var keys = jo.keySet();
      Map<String, Object> nestedMap = new HashMap<>();
      keys.forEach(k -> {
        JsonValue v = jo.get(k);
        var type = v.getValueType();
        Object value;
        switch (type) {
          case TRUE: case FALSE:
            value = Boolean.parseBoolean(v.toString());
            break;
          case NUMBER:
            value = ((JsonNumber) v).numberValue();
            break;
          case NULL:
            value = null;
          default:
            value = v.toString();
            break;
        }
        nestedMap.put(k, value);
      });
      finalList.add(nestedMap);
    });

    return finalList;
  }

  public static JsonObject getJsonObject(String jsonString) throws IOException {
    JsonReader reader = Json.createReader(new StringReader(jsonString));
    try {
      var object = reader.readObject();
      reader.close();
      return object;
    }
    catch (Exception e) {
      throw new IOException("Cannot parse text [ " + jsonString + " ] as JSON object. Details: [ " + e.getMessage() + " ]");
    }
  }

  /**
   * Sorts a given Map by its keys. Recurses both singular nested maps and arrays of maps (but does not change the array order).
   * @param unsortedMap
   * @return A new sorted LinkedHashMap
   */
  public static LinkedHashMap<String, Object> sortMapByKeys(Map<String, Object> unsortedMap) {
    var sortedMap = new LinkedHashMap<String, Object>();

    unsortedMap.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(e -> sortedMap.put(e.getKey(), e.getValue()));

    sortedMap.forEach((k, v) -> {
      if (v instanceof Map) {
        sortedMap.put(k, sortMapByKeys((Map) v));
      }
      else if (v instanceof List && !((List) v).isEmpty() && ((List) v).get(0) instanceof Map) {
        var sortedList = ((List) v).stream()
            .map(e -> sortMapByKeys((Map) e))
            .collect(Collectors.toList());
        sortedMap.put(k, sortedList);
      }
    });

    return sortedMap;
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
   * Turns a nested map into a flat map with nested keys appended together with the delimiter
   * @param parentKey Prefix that all flattened keys start with. Null, empty, or whitespace-only value results in no prefix
   * @param delimiter String to delimit between each nested key. Defaults to "." if null or empty
   * @param originalMap Nested-key map to be flattened
   * @return Single-level map with flattened keys
   */
  public static Map<String, Object> consolidateNestedKeysInMap(String parentKey, String delimiter, Map<String, Object> originalMap) {
    var parent = (parentKey == null || parentKey.isBlank()) ? new String() : parentKey;
    var delimiterString = (delimiter == null || delimiter.isEmpty()) ? "." : delimiter;
    var newMap = new HashMap<String, Object>();

    if(originalMap != null && !originalMap.isEmpty()) {
      originalMap.forEach((k, v) -> {
        String newKey = parent.isEmpty() ? k : parent + delimiterString + k;
        if(v instanceof Map) {
          newMap.putAll(consolidateNestedKeysInMap(newKey, delimiterString, (Map<String, Object>) v));
        }
        else {
          newMap.put(newKey, v);
        }
      });
    }
    return newMap;
  }

  /**
   * Removes the given trimString from any keys in originalMap that match. For example a trim string 'abc.' would turn
   * key 'abc.123' into key '123'.
   * @param trimString Case insensitive prefix to remove from keys in originalMap
   * @param originalMap
   * @return New map with modified keys
   */
  public static Map<String, Object> trimMapKeys(String trimString, Map<String, Object> originalMap) {
    Map<String, Object> trimmedKeysMap = new LinkedHashMap<>();
    originalMap.forEach((k, v) -> {
      String trimmedKey = k.toLowerCase().startsWith(trimString.toLowerCase()) ? k.substring(trimString.length()) : k;
      trimmedKeysMap.put(trimmedKey, v);
    });
    return trimmedKeysMap;
  }

  /**
   * Returns an map with all keys not contained in the given collection removed
   * @param keysToKeep A collection of the keys to preserve in the filtered output; all others will be removed
   * @return The filtered map
   */
  public static Map<String, Object> filterMapKeys(Collection<String> keysToKeep, Map<String, Object> originalMap) {
    if (keysToKeep == null || keysToKeep.size() == 0) {
      return new HashMap<>();
    }
    return originalMap.entrySet().stream()
        .filter(e -> keysToKeep.contains(e.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * @param builderType   type of schema builder either ParsedRecord or AggregatedInput, otherwise error out
   * @param fieldData     parsed or input metadata values
   * @param fieldsToParse list of schema fields that only support merged map
   *
   * @throws ClassCastException if builderType is not of ParsedRecord.Builder or AggregatedInput.Builder
   */
  public static void updateDerivedFields(Object builderType, Map<String, Object> fieldData, List fieldsToParse) {
    // 1. identify the builder type
    var aggregatedSchemaType = builderType instanceof AggregatedInput.Builder;
    var schema = aggregatedSchemaType ? AggregatedInput.getClassSchema() : ParsedRecord.getClassSchema();
    var builder = aggregatedSchemaType ? (AggregatedInput.Builder) builderType : (ParsedRecord.Builder) builderType;

    fieldData
        .entrySet()
        .stream()
        .filter(e -> fieldsToParse.contains(e.getKey()))
        .forEach(e -> {
          try {
            // 2. coerce the value to its field value and apply it to the builder
            var field = schema.getField(e.getKey());
            var coerced = AvroUtils.coerceValueForSchema(e.getValue(), field.schema());
            setValueOnPojo(builder, field.name(), coerced);
          }
          catch (Exception ex) {
            // 3. if it fails attach an error to the builder, but continue with remaining fields
            var error = ErrorEvent.newBuilder()
                .setTitle("Failed to parse field [" + e.getKey() + "]")
                .setDetail(ex.getMessage())
                .build();
            if (aggregatedSchemaType) {
              var aggregatedInputBuilder = (AggregatedInput.Builder) builder;
              aggregatedInputBuilder.setErrors(addOrInit(aggregatedInputBuilder.getErrors(), error));
            }
            else {
              var parsedBuilder = (ParsedRecord.Builder) builder;
              parsedBuilder.setErrors(addOrInit(parsedBuilder.getErrors(), error));
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
  private static Optional<Method> findGetterForType(Object pojo, String fieldName, Class clazz) {
    var getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    return Arrays.stream(pojo.getClass().getMethods())
        .filter(m -> m.getName().equals(getterName))
        .filter(m -> m.getParameterCount() == 0)
        .filter(m -> m.getReturnType().isAssignableFrom(clazz))
        .findFirst();
  }

  // Helper functions:
  public static Properties filterProperties(Map kafkaConfigMap, Set<String> keySet) {
    var props = new Properties();
    kafkaConfigMap.forEach( (k, v) -> {
      if(keySet.contains(k)) {
        props.put(k, v);
      }
    });
    return props;
  }
}
