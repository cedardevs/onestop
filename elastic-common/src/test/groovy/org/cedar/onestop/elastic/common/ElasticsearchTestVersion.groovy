package org.cedar.onestop.elastic.common

import groovy.util.logging.Slf4j
import org.elasticsearch.Version

@Slf4j
class ElasticsearchTestVersion {

  static boolean checkBackwardCompatibility = true

  static final List<Version> compatibleVersions = [Version.V_5_6_0, Version.V_6_0_0, Version.V_6_4_3]

  static String TEST_PREFIX = 'prefix-'
  static Integer TEST_MAX_TASKS = 10
  static Integer TEST_REQUESTS_PER_SECOND = null
  static Integer TEST_SITEMAP_SCROLL_SIZE = 2
  static Integer TEST_SITEMAP_COLLECTIONS_PER_SUBMAP = 5

  static ElasticsearchConfig esConfigForVersion(Version version) {
    return new ElasticsearchConfig(
        TEST_PREFIX,
        TEST_MAX_TASKS,
        TEST_REQUESTS_PER_SECOND,
        TEST_SITEMAP_SCROLL_SIZE,
        TEST_SITEMAP_COLLECTIONS_PER_SUBMAP,
        version
    )
  }

  static ElasticsearchConfig esConfigLatest() {
    Version latestVersion = ElasticsearchTestVersion.compatibleVersions.last()
    return esConfigForVersion(latestVersion)
  }

  static List<Version> testVersions() {
    return checkBackwardCompatibility ? compatibleVersions : [compatibleVersions.last()]
  }

  static Map<Version, ElasticsearchConfig> configs() {
    Map<Version, ElasticsearchConfig> esVersionedConfigs = [:]
    testVersions().each { version ->
      esVersionedConfigs.put(version, esConfigForVersion(version))
    }
    return esVersionedConfigs
  }

  static List<Map> versionedTestCases(List<Map> testCases) {
    List<Map> newTestCases = []
    final String VERSION_KEY = "version"
    testVersions().each { Version version ->
      if (!testCases || testCases.isEmpty()) {
        // when there is no "where" spock test cases, generate one for each version
        newTestCases.push([(VERSION_KEY): version])
      } else {
        testCases.each { Map testCase ->
          // add version to each test case
          testCase.put(VERSION_KEY, version)
          newTestCases.push(testCase)
        }
      }
    }
    return newTestCases
  }
}