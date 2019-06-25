package org.cedar.psi.common.util;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class DataUtils {

  public static <T> List<T> addOrInit(List<T> list, T item) {
    var result = list == null ? new ArrayList<T>() : list;
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
  private static <T extends Object> Optional<Method> findSetterForValue(T pojo, String fieldName, Object value) {
    var setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    return Arrays.stream(pojo.getClass().getMethods())
        .filter(m -> m.getName().equals(setterName))
        .filter(m -> m.getParameterCount() == 1)
        .filter(m -> m.getParameterTypes()[0].isAssignableFrom(value.getClass()))
        .findFirst();
  }

}
