package org.cedar.onestop.elastic.common;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticsearchCompatibility {

  private static final Logger log = LoggerFactory.getLogger(ElasticsearchCompatibility.class);
  private static final Integer MAJOR_VERSION_6 = 6;
  private static final Integer MAJOR_VERSION_7 = 7;
  private static final Integer MINIMUM_COMPATIBLE_MAJOR_VERSION = MAJOR_VERSION_6;

  public static int parseMajorVersion(String versionNumber) {
    return Integer.parseInt(versionNumber.split("\\.")[0]);
  }

  public static boolean isMajorVersion6(String versionNumber) {
    int majorVersion = parseMajorVersion(versionNumber);
    return majorVersion == MAJOR_VERSION_6;
  }

  public static boolean isMajorVersion7(String versionNumber) {
    int majorVersion = parseMajorVersion(versionNumber);
    return majorVersion == MAJOR_VERSION_7;
  }

  public static String checkVersion(RestHighLevelClient restHighLevelClient) {
    MainResponse.Version version = null;
    try {
      version = restHighLevelClient.info(RequestOptions.DEFAULT).getVersion();
    } catch (IOException e) {
      throw new IllegalStateException("Service could not retrieve running Elasticsearch version");
    }
    String versionNumber = version.getNumber();
    int majorVersion = parseMajorVersion(versionNumber);
    if (majorVersion < MINIMUM_COMPATIBLE_MAJOR_VERSION) {
      throw new IllegalStateException("Service does not work against Elasticsearch versions < " + MINIMUM_COMPATIBLE_MAJOR_VERSION.toString());
    }
    log.info("Found running Elasticsearch version: " + versionNumber);
    return versionNumber;
  }
}
