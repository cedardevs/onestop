package org.cedar.onestop.registry.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

// ISO/IEC 9834-8:2014 (E) 6.5.4
// https://www.itu.int/rec/T-REC-X.667/en
// Software generating the hexadecimal representation of a UUID shall not use upper case letters.
// NOTE – It is recommended that the hexadecimal representation used in all human-readable formats be restricted to lower-case
// letters. Software processing this representation is, however, required to accept both upper and lower case letters as specified
// in 6.5.2.

public class UUIDValidator {

  private static final Logger log = LoggerFactory.getLogger(UUIDValidator.class);

  // prevent UUIDs with uppercase A-F to align strictly with spec in our incoming string representations of IDs
  // this allows us to prevent confusion between reading and writing and to know we are keying downstream consistently
  private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

  // create pattern ahead of time to avoid creating one on every incoming request
  private static final Pattern pattern = Pattern.compile(UUID_REGEX);

  public static boolean isValid(String key) {
    var valid = true;
    try {
      // catch Java problems with the UUID.
      UUID.fromString(key);

      // matcher enforces strict lowercase requirement that Java does not
      valid = pattern.matcher(key).matches();

    } catch(IllegalArgumentException e) {
      valid = false;
    }

    if(!valid){
      log.error("Invalid UUID String (ensure lowercase), UUID = {}" , key);
    }
    return valid;
  }

  public static Map<String, Object> uuidErrorMsg(String id){
    return Map.of(
        "status", 500,
        "content", Map.of("errors", Map.of("title", "Invalid UUID String (ensure lowercase): " + id )));
  }
}