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
    List<String> elasticHosts = Arrays.asList(elasticHost.split(","));

    int elasticPort = Integer.parseInt(config.getOrDefault("elasticsearch.port", "-1").toString());

    if (elasticHost.isBlank() || elasticPort < 0) {
      throw new IllegalStateException("`elasticsearch.host` and `elasticsearch.port` configuration values are required");
    }

    boolean sslEnabled = Boolean.parseBoolean(config.getOrDefault("elasticsearch.ssl.enabled", "").toString());
    String certPath = config.getOrDefault("elasticsearch.ssl.cert.path", "").toString();
    String certTLS = config.getOrDefault("elasticsearch.ssl.cert.tls", "").toString();
    String certFilePath = certPath + "/" + certTLS;

    String rwUser = config.getOrDefault("elasticsearch.rw.user", "").toString();
    String rwPassword = config.getOrDefault("elasticsearch.rw.pass", "").toString();

    return ElasticsearchClient.create(elasticHosts, elasticPort, sslEnabled, certFilePath, rwUser, rwPassword);
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
    var elasticSitemapScrollSize = Optional.ofNullable(config.get("sitemap.scroll-size"))
        .map(Object::toString)
        .map(Integer::valueOf)
        .orElse(null);
    var elasticSitemapEnabled = Optional.ofNullable(config.get("sitemap.enabled"))
        .map(Object::toString)
        .map(Boolean::valueOf)
        .orElse(true);

    // check for compatible elastic version (will throw exception if not compatible)
    ElasticsearchVersion elasticVersion = new ElasticsearchVersion(elasticClient);

    return new ElasticsearchConfig(
        elasticVersion, elasticPrefix, elasticMaxTasks, elasticRequestsPerSecond, elasticSitemapScrollSize,
        elasticSitemapScrollSize, elasticSitemapEnabled);
  }
}
