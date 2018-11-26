package org.cedar.psi.common.constants

import groovy.transform.CompileStatic

@CompileStatic
class Topics {

  static final int DEFAULT_NUM_PARTITIONS = 1
  static final short DEFAULT_REPLICATION_FACTOR = 1

  static final String DEFAULT_SOURCE = 'unknown'

  static final Map<String, List<String>> INPUTS = [
      'collection': ['comet', DEFAULT_SOURCE],
      'granule'   : ['common-ingest', 'class', DEFAULT_SOURCE],
  ]

  static Set<String> inputTypes() {
    INPUTS.keySet()
  }

  static List<String> inputSources() {
    def uniqueSources = INPUTS.inject(new HashSet()) { result, t, sources ->
      result.addAll(sources)
      result
    }
    return uniqueSources as List
  }
  static List<String> inputSources(String type) {
    return INPUTS[type] ?: Collections.<String>emptyList()
  }

  static Boolean isValidInput(String type) {
    INPUTS.containsKey(type)
  }
  static Boolean isValidInput(String type, String source) {
    INPUTS[type]?.contains(source)
  }

  static List<String> inputTopics() {
    inputTypes().collect({ type -> inputTopics(type) }).flatten() as List<String>
  }

  static List<String> inputTopics(String type) {
    if (!isValidInput(type)) { return null }
    inputSources(type).collect { source -> inputTopic(type, source) }
  }

  static String inputTopic(String type, String source) {
    if (!isValidInput(type, source)) { return null }
    "psi-${type}-input-${source}"
  }

  static String inputStore(String type, String source) {
    if (!isValidInput(type, source)) { return null }
    "${type}-input-${source}"
  }

  static List<String> inputChangelogTopics(String appName) {
    inputTypes().collect({ type -> inputChangelogTopics(appName, type) }).flatten() as List<String>
  }

  static List<String> inputChangelogTopics(String appName, String type) {
    if (!isValidInput(type)) { return Collections.<String>emptyList() }
    inputSources(type).collect { source -> inputChangelogTopic(appName, type, source) }
  }

  static String inputChangelogTopic(String appName, String type, String source) {
    if (!isValidInput(type, source)) { return null }
    "$appName-${inputStore(type, source)}-changelog"
  }

  static List<String> parsedTopics() {
    inputTypes().collect { type -> parsedTopic(type) }
  }

  static String parsedTopic(String type) {
    if (!isValidInput(type)) { return null }
    "psi-${type}-parsed"
  }

  static String parsedStore(String type) {
    if (!isValidInput(type)) { return null }
    "${type}-parsed"
  }

  static List<String> toExtractorTopics() {
    inputTypes().collect { type -> toExtractorTopic(type) }
  }

  static String toExtractorTopic(String type) {
    if (!isValidInput(type)) { return null }
    "psi-${type}-extractor-to"
  }

  static List<String> fromExtractorTopics() {
    inputTypes().collect { type -> fromExtractorTopic(type) }
  }

  static String fromExtractorTopic(String type) {
    if (!isValidInput(type)) { return null }
    "psi-${type}-extractor-from"
  }

  static List<String> publishedTopics() {
    inputTypes().collect { type -> publishedTopic(type) }
  }

  static String publishedTopic(String type) {
    if (!isValidInput(type)) { return null }
    "psi-${type}-published"
  }

  static String publishTimeStore(String type) {
    if (!isValidInput(type)) { return null }
    "$type-publish-times"
  }

  static String publishKeyStore(String type) {
    if (!isValidInput(type)) { return null }
    "$type-publish-keys"
  }

}
