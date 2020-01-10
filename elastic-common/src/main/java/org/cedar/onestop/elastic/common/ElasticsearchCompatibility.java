package org.cedar.onestop.elastic.common;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticsearchCompatibility {

  private static final Logger log = LoggerFactory.getLogger(ElasticsearchCompatibility.class);
  private static final Integer MINIMUM_COMPATIBLE_MAJOR_VERSION = 6;

  public static void checkVersion(RestHighLevelClient restHighLevelClient) {
    MainResponse.Version version = null;
    try {
      version = restHighLevelClient.info(RequestOptions.DEFAULT).getVersion();
    } catch (IOException e) {
      throw new IllegalStateException("Service could not retrieve running Elasticsearch version");
    }
    String versionNumber = version.getNumber();
    int majorVersion = Integer.parseInt(versionNumber.split("\\.")[0]);
    if (majorVersion < MINIMUM_COMPATIBLE_MAJOR_VERSION) {
      throw new IllegalStateException("Service does not work against Elasticsearch versions < " + MINIMUM_COMPATIBLE_MAJOR_VERSION.toString());
    }
    log.info("Found running Elasticsearch version: " + versionNumber);
  }
}
