package org.cedar.psi.common.constants

import groovy.transform.CompileStatic

@CompileStatic
class Topics {

  static final int DEFAULT_NUM_PARTITIONS = 1
  static final short DEFAULT_REPLICATION_FACTOR = 1

  static final Map<String, List<String>> INPUTS = [
      'collection': ['comet', 'adhoc'],
      'granule'   : ['common-ingest', 'class', 'adhoc'],
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

  static String inputTopic(String type) {
    if (!isValidInput(type)) { return null }
    "raw-${type}-events"
  }
  static String inputTopic(String type, String source) {
    if (!isValidInput(type, source)) { return null }
    "raw-${source}-${type}-events"
  }

  static String inputStore(String type) {
    if (!isValidInput(type)) { return null }
    "raw-${type}s"
  }
  static String inputStore(String type, String source) {
    if (!isValidInput(type, source)) { return null }
    "raw-${source}-${type}s"
  }

  static String inputChangelogTopic(String appName, String type) {
    if (!isValidInput(type)) { return null }
    "$appName-${inputStore(type)}-changelog"
  }
  static String inputChangelogTopic(String appName, String type, String source) {
    if (!isValidInput(type, source)) { return null }
    "$appName-${inputStore(type, source)}-changelog"
  }

  static String parsedTopic(String type) {
    if (!isValidInput(type)) { return null }
    "parsed-${type}s"
  }

  static String parsedStore(String type) {
    if (!isValidInput(type)) { return null }
    "parsed-${type}s"
  }

  static String smeTopic(String type) {
    if (!isValidInput(type)) { return null }
    "sme-${type}s"
  }

  static String unparsedTopic(String type) {
    if (!isValidInput(type)) { return null }
    "unparsed-${type}s"
  }

  static String combinedTopic(String type) {
    if (!isValidInput(type)) { return null }
    "combined-${type}s"
  }

  static String publishTimeStore(String type) {
    if (!isValidInput(type)) { return null }
    "$type-publish-times"
  }

  static String publishKeyStore(String type) {
    if (!isValidInput(type)) { return null }
    "$type-publish-keys"
  }

  static String errorTopic() {
    'error-events'
  }

  static String errorStore() {
    'error-store'
  }

}
