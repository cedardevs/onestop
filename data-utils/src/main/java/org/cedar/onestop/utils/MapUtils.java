package org.cedar.onestop.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MapUtils {

//  public static Map<String, Object> parseJsonMap(String json) throws IOException {
//    if (json == null || json == "") {
//      return new LinkedHashMap();
//    }
//    else {
//      return new ObjectMapper().readValue(json, Map.class);
//    }
//  }

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
}
