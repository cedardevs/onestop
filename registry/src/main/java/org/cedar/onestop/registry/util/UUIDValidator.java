package org.cedar.onestop.registry.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;

public class UUIDValidator {
  private static final Logger log = LoggerFactory.getLogger(UUIDValidator.class);

  private static final String UUID_PATTERN = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$";

  public static boolean isValid(String key) {
    Pattern pattern = Pattern.compile(UUID_PATTERN);
    var valid = pattern.matcher(key).matches();
    if(!valid){
      log.error("Invalid UUID String, The UUID is {}" , key);
    }

    return valid;
  }

  public static Map<String, Object> uuidErrorMsg(String id){
    return Map.of(
        "status", 500,
        "content", Map.of("errors", Map.of("title", "Invalid UUID String " + id )));
  }
}