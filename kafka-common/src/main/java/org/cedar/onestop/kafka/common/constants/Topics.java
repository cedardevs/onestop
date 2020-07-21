package org.cedar.onestop.kafka.common.constants;

import org.cedar.schemas.avro.psi.RecordType;

import java.util.*;
import java.util.stream.Collectors;

public class Topics {

  public static final String DEFAULT_SOURCE = "unknown";

  public static final Map<RecordType, List<String>> INPUTS = new HashMap<>();
  static {
    // wrap immutable lists with regular ones so they can be queried with `null`
    INPUTS.put(RecordType.collection, new ArrayList<>(List.of("comet", DEFAULT_SOURCE)));
    INPUTS.put(RecordType.granule, new ArrayList<>(List.of("common-ingest", "class", DEFAULT_SOURCE)));
  }

  public static Set<RecordType> inputTypes() {
    return INPUTS.keySet();
  }

  public static List<String> inputSources() {
    return INPUTS
        .values()
        .stream()
        .flatMap(Collection::stream)
        .distinct()
        .collect(Collectors.toList());
  }
  public static List<String> inputSources(RecordType type) {
    return INPUTS.getOrDefault(type , Collections.emptyList());
  }

  public static Boolean isValidInput(RecordType type) {
    return INPUTS.containsKey(type);
  }
  public static Boolean isValidInput(RecordType type, String source) {
    return inputSources(type).contains(source);
  }

  public static List<String> inputTopics() {
    return inputTypes().stream().map(Topics::inputTopics).flatMap(Collection::stream).collect(Collectors.toList());
  }

  public static List<String> inputTopics(RecordType type) {
    if (!isValidInput(type)) { return Collections.emptyList(); }
    return inputSources(type).stream().map(source -> inputTopic(type, source)).collect(Collectors.toList());
  }

  public static String inputTopic(RecordType type, String source) {
    if (!isValidInput(type, source)) { return null; }
    return String.format("psi-%s-input-%s", type, source);
  }

  public static String inputStoreSplit(RecordType type, String source) {
    if (!isValidInput(type, source)) { return null; }
    return String.format("%s-input-%s", type, source);
  }

  public static String inputStoreCombined(RecordType type) {
    if (!isValidInput(type)) { return null; }
    return String.format("%s-input", type);
  }

  public static List<String> inputChangelogTopicsSplit(String appName) {
    return inputTypes().stream().map(type -> inputChangelogTopicsSplit(appName, type)).flatMap(Collection::stream).collect(Collectors.toList());
  }

  public static List<String> inputChangelogTopicsSplit(String appName, RecordType type) {
    if (!isValidInput(type)) { return Collections.emptyList(); }
    return inputSources(type).stream().map(source -> inputChangelogTopicSplit(appName, type, source)).collect(Collectors.toList());
  }

  public static String inputChangelogTopicSplit(String appName, RecordType type, String source) {
    if (!isValidInput(type, source)) { return null; }
    return String.format("%s-%s-changelog", appName, inputStoreSplit(type, source));
  }

  public static String inputChangelogTopicCombined(String appName, RecordType type) {
    return String.format("%s-%s-changelog", appName, inputStoreCombined(type));
  }

  public static List<String> parsedTopics() {
    return inputTypes().stream().map(Topics::parsedTopic).collect(Collectors.toList());
  }

  public static String parsedTopic(RecordType type) {
    if (!isValidInput(type)) { return null; }
    return String.format("psi-%s-parsed", type);
  }

  public static String parsedStore(RecordType type) {
    if (!isValidInput(type)) { return null; }
    return String.format("%s-parsed", type);
  }

  public static List<String> parsedChangelogTopics(String appName) {
    return inputTypes().stream().map(type -> parsedChangelogTopic(appName, type)).collect(Collectors.toList());
  }

  public static String parsedChangelogTopic(String appName, RecordType type) {
    if (!isValidInput(type)) { return null; }
    return String.format("%s-%s-changelog", appName, parsedStore(type));
  }

  public static List<String> toExtractorTopics() {
    return inputTypes().stream().map(Topics::toExtractorTopic).collect(Collectors.toList());
  }

  public static String toExtractorTopic(RecordType type) {
    if (!isValidInput(type)) { return null; }
    return String.format("psi-%s-extractor-to", type);
  }

  public static List<String> fromExtractorTopics() {
    return inputTypes().stream().map(Topics::fromExtractorTopic).collect(Collectors.toList());
  }

  public static String fromExtractorTopic(RecordType type) {
    if (!isValidInput(type)) { return null; }
    return String.format("psi-%s-extractor-from", type);
  }

  public static List<String> publishedTopics() {
    return inputTypes().stream().map(Topics::publishedTopic).collect(Collectors.toList());
  }

  public static String publishedTopic(RecordType type) {
    if (!isValidInput(type)) { return null; }
    return String.format("psi-%s-published", type);
  }

  public static String flattenedGranuleTopic() {
    return "psi-flattened-granules";
  }

  public static String granulesByCollectionId() {
    return "psi-granules-by-collection";
  }

  public static String publishTimeStore(RecordType type) {
    if (!isValidInput(type)) { return null; }
    return String.format("%s-publish-times", type);
  }

  public static String publishKeyStore(RecordType type) {
    if (!isValidInput(type)) { return null; }
    return String.format("%s-publish-keys", type);
  }

}
