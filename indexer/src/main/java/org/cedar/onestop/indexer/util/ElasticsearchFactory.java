package org.cedar.onestop.indexer.util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.cedar.onestop.elastic.common.ElasticsearchVersion;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class ElasticsearchFactory {
  private static final Logger log = LoggerFactory.getLogger(ElasticsearchFactory.class);

  public static RestHighLevelClient buildElasticClient(AppConfig config) {
    var elasticHost = config.getOrDefault("elasticsearch.host", "").toString();
    var elasticPort = Integer.valueOf(config.getOrDefault("elasticsearch.port", "-1").toString());

    if (elasticHost.isBlank() || elasticPort < 0) {
      throw new IllegalStateException("`elasticsearch.host` and `elasticsearch.port` configuration values are required");
    }

    var sslEnabled = Boolean.valueOf(config.getOrDefault("elasticsearch.ssl.enabled", "").toString());
    var rwUser = config.getOrDefault("elasticsearch.rw.user", "").toString();
    var rwPassword = config.getOrDefault("elasticsearch.rw.pass", "").toString();

    var hosts = Arrays.stream(elasticHost.split(","))
        .map(host -> new HttpHost(host, elasticPort, sslEnabled ? "https" : "http"))
        .toArray(HttpHost[]::new);
    var elasticBuilder = RestClient.builder(hosts)
        .setRequestConfigCallback(requestConfigBuilder -> {
          // Set connect timeout to 1 minute and socket timeout to 5 minutes
          return requestConfigBuilder.setConnectTimeout(60000).setSocketTimeout(300000);
        })
        .setHttpClientConfigCallback(httpClientBuilder -> {
          if (!rwUser.isBlank() && !rwPassword.isBlank()) {
            var credentials = new BasicCredentialsProvider();
            credentials.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(rwUser, rwPassword));
            httpClientBuilder.setDefaultCredentialsProvider(credentials);
          }
          // causes the builder to take system properties into account when building the
          // default ssl context, e.g. javax.net.ssl.trustStore, etc.
          httpClientBuilder.useSystemProperties();
          return httpClientBuilder;
        });

    return new RestHighLevelClient(elasticBuilder);
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
