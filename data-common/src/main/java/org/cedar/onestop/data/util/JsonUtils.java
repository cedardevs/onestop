package org.cedar.onestop.data.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class JsonUtils {

  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * Method parses a JSON string into a plain Java Map object. Throws an exception if String is invalid.
   * @param json JSON String to convert
   * @return Map representation of input JSON string.
   * @throws IOException if input string cannot be parsed as JSON
   */
  public static Map<String, Object> parseJsonAsMapSafe(String json) throws IOException {
    if (json == null || json.equals("")) {
      return new LinkedHashMap<>();
    }
    else {
      return mapper.readValue(json, Map.class);
    }
  }

  /**
   * Method parses a JSON string into a plain Java Map object. Returns a null instead of an exception if String is invalid.
   * @param json JSON String to convert
   * @return Map representation of input JSON string. Returns null if input is invalid.
   */
  public static Map<String, Object> parseJsonAsMap(String json) {
    // FIXME null and empty string would return empty map without this code
    if (json == null || json.equals("")) {
      return new LinkedHashMap<>();
    }
    else {
      try {
        return mapper.readValue(json, Map.class);
      } catch (JsonProcessingException e) {
        return null;
      }
    }
  }

  /**
   * Converts input Object to JSON string and returns null if object cannot produce valid JSON
   * @param obj Object to convert to JSON String
   * @return JSON string representation of input object or null if map cannot be interpreted as JSON
   */
  public static String toJson(Object obj) {
    try {
      return obj == null ? null : mapper.writeValueAsString(obj);
    }
    catch (JsonProcessingException e) {
      return null;
    }
  }

  /**
   * Compares a source map to a target map and returns a diff list POJO of JSON PATCHes to go from the source to the
   * target. Maps are assumed to represent JSON objects. Input objects are alphabetically sorted for the user to return
   * an accurate diff list.
   * @param sourceJson source JSON as Java Map
   * @param targetJson target JSON as Java Map
   * @return JSON PATCH diff list for converting sourceJson to targetJson
   * @throws IOException if either Map cannot be parsed as JSON
   */
  public static List<Map<String, Object>> getJsonDiffList(Map<String, Object> sourceJson, Map<String, Object> targetJson) throws IOException {
    var sortedSource = getJsonObject(mapper.writeValueAsString(MapUtils.sortMapByKeys(sourceJson)));
    var sortedTarget = getJsonObject(mapper.writeValueAsString(MapUtils.sortMapByKeys(targetJson)));

    return getJsonDiffList(sortedSource, sortedTarget);
  }

  /**
   * Compares a source JSON string to a target JSON string and returns a diff list POJO of JSON PATCHes to go from
   * the source to the target. Input JSON objects are alphabetically sorted for the user to return an accurate diff list.
   * @param sourceJson source JSON as String
   * @param targetJson target JSON as String
   * @return JSON PATCH diff list for converting sourceJson to targetJson
   * @throws IOException if either String cannot be parsed as JSON
   */
  public static List<Map<String, Object>> getJsonDiffList(String sourceJson, String targetJson) throws IOException {
    var sourceMap = parseJsonAsMapSafe(sourceJson);
    var targetMap = parseJsonAsMapSafe(targetJson);
    return getJsonDiffList(sourceMap, targetMap);
  }

  /**
   * Private method working directly with JsonObjects to create POJO of JSON PATCH diff list from source to target.
   * IMPORTANT: Unlike the public overloaded methods, this method assumes input JsonObjects are already sorted alphabetically.
   *
   * @param sourceJson source JSON as JsonObject
   * @param targetJson source JSON as JsonObject
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
        nestedMap.put(k, convertToPlainJavaType(v));
      });
      finalList.add(nestedMap);
    });

    return finalList;
  }

  private static Object convertToPlainJavaType(JsonValue v) {
    var type = v.getValueType();
    Object value;
    switch (type) {
      case TRUE: case FALSE:
        value = Boolean.parseBoolean(v.toString());
        break;
      case NUMBER:
        value = ((JsonNumber) v).numberValue();
        break;
      case STRING:
        value = ((JsonString) v).getString();
        break;
      case ARRAY:
        var it = ((JsonArray) v).listIterator();
        var list = new ArrayList<>();
        while(it.hasNext()) {
          list.add(convertToPlainJavaType(it.next()));
        }
        value = list;
        break;
      case OBJECT:
        var entries = ((JsonObject) v).entrySet();
        var map = new LinkedHashMap<String, Object>();
        entries.forEach(e -> map.put(e.getKey(), convertToPlainJavaType(e.getValue())));
        value = map;
        break;
      default:
        // This handles case NULL and v == null
        value = null;
        break;
    }
    return value;
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
}
