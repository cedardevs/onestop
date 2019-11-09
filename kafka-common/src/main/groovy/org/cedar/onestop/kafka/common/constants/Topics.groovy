package org.cedar.onestop.kafka.common.constants

import groovy.transform.CompileStatic
import org.cedar.schemas.avro.psi.RecordType

@CompileStatic
class Topics {

  public static final String DEFAULT_SOURCE = 'unknown'

  public static final Map<RecordType, List<String>> INPUTS = [
      (RecordType.collection): ['comet', DEFAULT_SOURCE],
      (RecordType.granule)   : ['common-ingest', 'class', DEFAULT_SOURCE],
  ]

  public static Set<RecordType> inputTypes() {
    INPUTS.keySet()
  }

  public static List<String> inputSources() {
    def uniqueSources = INPUTS.inject(new HashSet()) { result, t, sources ->
      result.addAll(sources)
      result
    }
    return uniqueSources as List
  }
  public static List<String> inputSources(RecordType type) {
    return INPUTS[type] ?: Collections.<String>emptyList()
  }

  public static Boolean isValidInput(RecordType type) {
    INPUTS.containsKey(type)
  }
  public static Boolean isValidInput(RecordType type, String source) {
    INPUTS[type]?.contains(source)
  }

  public static List<String> inputTopics() {
    inputTypes().collect({ type -> inputTopics(type) }).flatten() as List<String>
  }

  public static List<String> inputTopics(RecordType type) {
    if (!isValidInput(type)) { return null }
    inputSources(type).collect { source -> inputTopic(type, source) }
  }

  public static String inputTopic(RecordType type, String source) {
    if (!isValidInput(type, source)) { return null }
    "psi-${type}-input-${source}"
  }

  public static String inputStore(RecordType type, String source) {
    if (!isValidInput(type, source)) { return null }
    "${type}-input-${source}"
  }

  public static List<String> inputChangelogTopics(String appName) {
    inputTypes().collect({ type -> inputChangelogTopics(appName, type) }).flatten() as List<String>
  }

  public static List<String> inputChangelogTopics(String appName, RecordType type) {
    if (!isValidInput(type)) { return Collections.<String>emptyList() }
    inputSources(type).collect { source -> inputChangelogTopic(appName, type, source) }
  }

  public static String inputChangelogTopic(String appName, RecordType type, String source) {
    if (!isValidInput(type, source)) { return null }
    "$appName-${inputStore(type, source)}-changelog"
  }

  public static List<String> parsedTopics() {
    inputTypes().collect { type -> parsedTopic(type) }
  }

  public static String parsedTopic(RecordType type) {
    if (!isValidInput(type)) { return null }
    "psi-${type}-parsed"
  }

  public static String parsedStore(RecordType type) {
    if (!isValidInput(type)) { return null }
    "${type}-parsed"
  }

  public static List<String> parsedChangelogTopics(String appName) {
    inputTypes().collect({ type -> parsedChangelogTopic(appName, type) }).flatten() as List<String>
  }

  public static String parsedChangelogTopic(String appName, RecordType type) {
    if (!isValidInput(type)) { return null }
    "$appName-${parsedStore(type)}-changelog"
  }

  public static List<String> toExtractorTopics() {
    inputTypes().collect { type -> toExtractorTopic(type) }
  }

  public static String toExtractorTopic(RecordType type) {
    if (!isValidInput(type)) { return null }
    "psi-${type}-extractor-to"
  }

  public static List<String> fromExtractorTopics() {
    inputTypes().collect { type -> fromExtractorTopic(type) }
  }

  public static String fromExtractorTopic(RecordType type) {
    if (!isValidInput(type)) { return null }
    "psi-${type}-extractor-from"
  }

  public static List<String> publishedTopics() {
    inputTypes().collect { type -> publishedTopic(type) }
  }

  public static String publishedTopic(RecordType type) {
    if (!isValidInput(type)) { return null }
    "psi-${type}-published"
  }

  public static String publishTimeStore(RecordType type) {
    if (!isValidInput(type)) { return null }
    "$type-publish-times"
  }

  public static String publishKeyStore(RecordType type) {
    if (!isValidInput(type)) { return null }
    "$type-publish-keys"
  }

}
