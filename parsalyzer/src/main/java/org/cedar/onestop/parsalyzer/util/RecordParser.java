package org.cedar.onestop.parsalyzer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cedar.onestop.data.util.MapUtils;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.util.DataUtils;
import org.cedar.onestop.kafka.common.util.ValueWithErrors;
import org.cedar.schemas.avro.psi.AggregatedInput;
import org.cedar.schemas.avro.psi.ErrorEvent;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.RecordType;
import org.cedar.schemas.avro.util.AvroUtils;
import org.cedar.schemas.parse.ISOParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class RecordParser {
  private static final Logger log = LoggerFactory.getLogger(RecordParser.class);

  /**
   * Transforms an {@link AggregatedInput} into a {@link ParsedRecord} by parsing its
   * xml and json content. The logic is as follows:
   *   1. Parse rawXml into a {@link ParsedRecord}-shaped map, i.e. with {@link org.cedar.schemas.avro.psi.Discovery}
   *      information under the "discovery" key
   *   2. Parse rawJson into a {@link ParsedRecord}-shaped map
   *   3. Combine the two maps of metadata, as well as any errors that were produced during parsing, and create
   *      a {@link ParsedRecord} instance from them
   *   4. If there is no metadata nor any errors, add an error indicating that no content was provided
   * @param input The {@link AggregatedInput} to parse
   * @return The resulting {@link ParsedRecord} instance
   */
  public static ParsedRecord parseInput(AggregatedInput input) {
    var fieldsToParse = List.of("discovery", "fileInformation", "fileLocations", "publishing", "relationships", "errors");
    if (input == null || input.getDeleted()) {
      return null;
    }

    var inputErrors = input.getErrors();
    if (inputErrors != null && inputErrors.size() > 0) {
      return null;
    }

    final var metadataFromXml = marshalDataAndCollectErrors(input.getRawXml());
    final var metadataFromJson = marshalDataAndCollectErrors(input.getRawJson());

    final List<ErrorEvent> combinedErrors = new ArrayList<>();
    if (metadataFromXml.errors != null && metadataFromXml.errors.size() > 0) {
      combinedErrors.addAll(metadataFromXml.errors);
    }
    if (metadataFromJson.errors != null && metadataFromJson.errors.size() > 0) {
      combinedErrors.addAll(metadataFromJson.errors);
    }

    final Map<String, Object> combinedMetadata = MapUtils.mergeMaps(metadataFromXml.value, metadataFromJson.value);
    final var builder = ParsedRecord.newBuilder().setType(input.getType()).setErrors(combinedErrors);
    if (!combinedMetadata.isEmpty()) {
      DataUtils.updateDerivedFields(builder, combinedMetadata, fieldsToParse);
    }
    else if (builder.getErrors() == null || builder.getErrors().size() == 0) {
      var error = ErrorEvent.newBuilder()
          .setTitle("No content provided")
          .setDetail("Input contains no content")
          .setSource(StreamsApps.PARSALYZER_ID)
          .build();
      builder.setErrors(List.of(error));
    }

    return builder.build();
  }

  /**
   * Produce a {@link ParsedRecord} by parsing metadata from a string in a number of supported formats.
   * Supported formats include:
   *   - An xml string (parsed as {@link org.cedar.schemas.avro.psi.Discovery} information)
   *   - A json string in the shape of a {@link ParsedRecord}
   *   - A json string in the shape of an {@link org.cedar.schemas.avro.psi.Input}, with a "content" string
   *     which is itself in one of the two above formats
   * @param inputStr A string in a supported format
   * @param type The {@link RecordType} to set on the returned {@link ParsedRecord}
   * @return The resulting {@link ParsedRecord} instance
   */
  public static ParsedRecord parseRaw(String inputStr, RecordType type) {
    var fieldsToParse = List.of("discovery", "fileInformation", "fileLocations", "publishing", "relationships", "errors");
    final var builder = ParsedRecord.newBuilder().setType(type);
    final var metadata = marshalDataAndCollectErrors(inputStr);
    if (metadata.errors instanceof List) {
      builder.setErrors(metadata.errors);
    }
    if (metadata.value instanceof Map && !metadata.value.isEmpty()) {
      DataUtils.updateDerivedFields(builder, metadata.value, fieldsToParse);
    }
    else if (builder.getErrors() == null || builder.getErrors().size() == 0) {
      var error = ErrorEvent.newBuilder()
          .setTitle("No content provided")
          .setDetail("Input contains no content")
          .setSource(StreamsApps.PARSALYZER_ID)
          .build();
      builder.setErrors(List.of(error));
    }
    return builder.build();
  }

  private static ValueWithErrors<Map> marshalDataAndCollectErrors(String data) {
    if (data == null || data.isBlank()) {
      return new ValueWithErrors<>(null, null);
    }
    else if (probablyXml(data)) {
      return marshalXml(data);
    }
    else if (probablyJson(data)) {
      return marshalJson(data);
    }
    else {
      var error = ErrorEvent.newBuilder()
          .setTitle("Unable to parse input")
          .setDetail("Input content does not appear to be either xml or json")
          .setSource(StreamsApps.PARSALYZER_ID)
          .build();
      return new ValueWithErrors(null, List.of(error));
    }
  }

  private static ValueWithErrors<Map> marshalXml(String xml) {
    if (xml == null || xml.isBlank()) {
      return new ValueWithErrors<>(null, null);
    }
    try {
      // TODO change this back to debug once done with it.
      log.info("Xml sending to ISOParser: "+xml);
      var discovery = AvroUtils.avroToMap(ISOParser.parseXMLMetadataToDiscovery(xml));
      return new ValueWithErrors(Map.of("discovery", discovery), null);
    }
    catch (Exception e) {
      var error = ErrorEvent.newBuilder()
          .setTitle("Unable to parse malformed xml")
          .setDetail(ExceptionUtils.getRootCauseMessage(e).trim())
          .setSource(StreamsApps.PARSALYZER_ID)
          .build();
      log.info("{}: {}", error.getTitle(), error.getDetail());
      return new ValueWithErrors(null, List.of(error));
    }
  }

  private static ValueWithErrors<Map> marshalJson(String json) {
    if (json == null || json.isBlank()) {
      return new ValueWithErrors<>(null, null);
    }
    try {
      var jsonMap = new ObjectMapper().readValue(json, Map.class);
      return marshalMap(jsonMap);
    }
    catch (Exception e) {
      var error = ErrorEvent.newBuilder()
          .setTitle("Unable to parse malformed json")
          .setDetail(ExceptionUtils.getRootCauseMessage(e).trim())
          .setSource(StreamsApps.PARSALYZER_ID)
          .build();
      log.info("{}: {}", error.getTitle(), error.getDetail());
      return new ValueWithErrors(null, List.of(error));
    }
  }

  private static ValueWithErrors<Map> marshalMap(Map map) {
    if (isInputShaped(map)) {
      return marshalInputMap(map);
    }
    else if (hasEmbeddedXml(map)) {
      return marshalMapWithEmbeddedXml(map);
    }
    else {
      return new ValueWithErrors(map, null);
    }
  }

  public static boolean isInputShaped(Map<String, Object> map) {
    if (map == null || map.isEmpty()) {
      return false;
    }
    return map.containsKey("content") && map.containsKey("contentType");
  }

  private static ValueWithErrors<Map> marshalInputMap(Map<String, Object> input) {
    var contentType = extractString(input, "contentType");
    var content = extractString(input, "content");
    if (content == null || content.isBlank()) {
      return new ValueWithErrors<>(null, null);
    }
    else if (isJsonContentType(contentType)) {
      return marshalJson(content);
    }
    else if (isXmlContentType(contentType)) {
      return marshalXml(content);
    }
    else {
      var error = ErrorEvent.newBuilder()
          .setTitle("Unable to parse input")
          .setDetail("Input content does not appear to be either xml or json")
          .setSource(StreamsApps.PARSALYZER_ID)
          .build();
      return new ValueWithErrors(null, List.of(error));
    }
  }

  public static boolean hasEmbeddedXml(Map<String, Object> map) {
    if (map == null || map.isEmpty()) {
      return false;
    }
    return map.get("content") instanceof String && probablyXml((String) map.get("content"));
  }

  private static ValueWithErrors marshalMapWithEmbeddedXml(Map<String, Object> map) {
    var xml = map.get("discovery");
    if (xml instanceof String) {
      return marshalXml((String) xml);
    }
    else {
      return new ValueWithErrors<>(null, null);
    }
  }

  public static boolean probablyJson(String string) {
    return string != null && string.trim().startsWith("{");
  }

  public static boolean probablyXml(String string) {
    return string != null && string.trim().startsWith("<");
  }

  public static boolean isJsonContentType(String contentType) {
    return contentType != null && (contentType.equals("application/json") || contentType.equals("text/json"));
  }

  public static boolean isXmlContentType(String contentType) {
    return contentType != null && (contentType.equals("application/xml") || contentType.equals("text/xml"));
  }

  private static String extractString(Map data, Object key) {
    if (data == null) {
      return null;
    }
    var value = data.get(key);
    return value != null ? value.toString() : null;
  }

}
