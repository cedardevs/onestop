package org.cedar.onestop.indexer.util;

import org.cedar.onestop.elastic.common.ElasticsearchClient;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.elastic.common.ElasticsearchVersion;
import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ElasticsearchFactory {
  private static final Logger log = LoggerFactory.getLogger(ElasticsearchFactory.class);

  public static RestHighLevelClient buildElasticClient(AppConfig config) {
    String elasticHost = config.getOrDefault("elasticsearch.host", "").toString();
    log.info("ElasticsearchFactory::elasticHost = " + elasticHost);
    List<String> elasticHosts = Arrays.asList(elasticHost.split(","));

    int elasticPort = Integer.parseInt(config.getOrDefault("elasticsearch.port", "-1").toString());
    log.info("ElasticsearchFactory::elasticPort = " + elasticPort);

    if (elasticHost.isBlank() || elasticPort < 0) {
      throw new IllegalStateException("`elasticsearch.host` and `elasticsearch.port` configuration values are required");
    }

    boolean sslEnabled = Boolean.parseBoolean(config.getOrDefault("elasticsearch.ssl.enabled", "").toString());
    String rwUser = config.getOrDefault("elasticsearch.rw.user", "").toString();
    String rwPassword = config.getOrDefault("elasticsearch.rw.pass", "").toString();

    return ElasticsearchClient.create(elasticHosts, elasticPort, sslEnabled, rwUser, rwPassword);
  }

  public static ElasticsearchConfig buildElasticConfig(AppConfig config, RestHighLevelClient elasticClient) throws IOException {
    var elasticPrefix = config.getOrDefault("elasticsearch.index.prefix", "").toString();
    var elasticMaxTasks = Optional.ofNullable(config.get("elasticsearch.max-tasks"))
        .map(Object::toString)
        .map(Integer::valueOf)
        .orElse(10);
    var elasticRequestsPerSecond = Optional.ofNullable(config.get("elasticsearch.requests-per-second"))
        .map(Object::toString)
        .map(Integer::valueOf)
        .orElse(null);
    var elasticSitemapScrollSize = Optional.ofNullable(config.get("etl.sitemap.scroll-size"))
        .map(Object::toString)
        .map(Integer::valueOf)
        .orElse(null);
    var elasticSitemapCollectionsPerSubmap = Optional.ofNullable(config.get("etl.sitemap.collections-per-submap"))
        .map(Object::toString)
        .map(Integer::valueOf)
        .orElse(null);
    var elasticSitemapEnabled = true; // TODO - any reason to configure this?

    // check for compatible elastic version (will throw exception if not compatible)
    ElasticsearchVersion elasticVersion = new ElasticsearchVersion(elasticClient);

    return new ElasticsearchConfig(
        elasticVersion, elasticPrefix, elasticMaxTasks, elasticRequestsPerSecond, elasticSitemapScrollSize,
        elasticSitemapCollectionsPerSubmap, elasticSitemapEnabled);
  }
}