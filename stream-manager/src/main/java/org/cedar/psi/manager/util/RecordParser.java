package org.cedar.psi.manager.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cedar.schemas.avro.psi.*;
import org.cedar.schemas.parse.ISOParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class RecordParser {
  private static final Logger log = LoggerFactory.getLogger(RecordParser.class);


  public static ParsedRecord parse(Object key, AggregatedInput value) {
    if (value.getDeleted()) {
      return null;
    }

    var errors = value.getErrors();
    if (errors != null && errors.size() > 0) {
      return null;
    }

    var builder = ParsedRecord.newBuilder()
        .setType(value.getType())
        .setFileInformation(value.getFileInformation())
        .setFileLocations(value.getFileLocations())
        .setPublishing(value.getPublishing())
        .setRelationships(value.getRelationships());

    if (value.getRawXml() != null) {
      try {
        builder.setDiscovery(ISOParser.parseXMLMetadataToDiscovery(value.getRawXml()));
      }
      catch (Exception e) {
        var error = ErrorEvent.newBuilder()
            .setTitle("Unable to parse malformed rawXml")
            .setDetail(ExceptionUtils.getRootCauseMessage(e).trim())
            .build();
        log.error("{}: {}", error.getTitle(), error.getDetail());
        builder.setErrors(Arrays.asList(error));
      }
    }

    return builder.build();
  }

  // TODO - this needs be MUCH more flexible
  public static ParsedRecord parse(Object key, String inputStr, RecordType type) {
    try {
      var map = new ObjectMapper().readValue(inputStr, LinkedHashMap.class);
      return parse(map, type);
    }
    catch (Exception e) {
      log.error("Exception while reading json string for key" + key.toString(), e);
      return null;
    }
  }

  public static ParsedRecord parse(Map msgMap, RecordType type) {
    try {
      var contentType = extractString(msgMap, "contentType");
      var content = extractString(msgMap, "content");
      var deleted = msgMap.get("deleted");
      if (deleted instanceof Boolean && (Boolean) deleted) {
        // if the input is a deletion then tombstone the parsed info
        return null;
      }
      if (StringUtils.isEmpty(content)) {
        var error = ErrorEvent.newBuilder().setTitle("No content provided").build();
        return ParsedRecord.newBuilder().setType(type).setErrors(Arrays.asList(error)).build();
      }
      if (!contentType.equals("application/xml")) {
        var error = ErrorEvent.newBuilder()
            .setTitle("Unsupported content type")
            .setDetail("Content type [" + contentType + "] is not supported")
            .build();
        return ParsedRecord.newBuilder().setType(type).setErrors(Arrays.asList(error)).build();
      }

      return ParsedRecord.newBuilder()
          .setType(type)
          .setDiscovery(ISOParser.parseXMLMetadataToDiscovery(content))
          .setPublishing(parsePublishing((Map) msgMap.get("publishing")))
          .build();
    }
    catch (Exception e) {
      var error = ErrorEvent.newBuilder()
          .setTitle("Unable to parse malformed content")
          .setDetail(ExceptionUtils.getRootCauseMessage(e).trim())
          .build();
      log.error("{}: {}", error.getTitle(), error.getDetail());
      return ParsedRecord.newBuilder().setType(type).setErrors(Arrays.asList(error)).build();
    }
  }

  private static String extractString(Map map, Object key) {
    if (map == null) {
      return null;
    }
    var value = map.get(key);
    return value != null ? value.toString() : null;
  }

  private static Publishing parsePublishing(Map input) {
    if (input == null) {
      return null;
    }
    var builder = Publishing.newBuilder();
    if (input.containsKey("isPrivate")) {
      builder.setIsPrivate((Boolean) input.get("isPrivate"));
    }
    var until = input.get("until");
    if (until != null) {
      if (until instanceof String) {
        builder.setUntil(Long.parseLong((String) until));
      }
      else if (until instanceof Integer) {
        builder.setUntil(Long.valueOf((Integer) until));
      }
      else if (until instanceof Long) {
        builder.setUntil((Long) until);
      }
      else {
        throw new IllegalStateException("Parsing a publishing object with unsupported value for 'until': " +
            "[" + until + "] with class [" + until.getClass() + "]");
      }
    }
    return builder.build();
  }

}
