package org.cedar.onestop.indexer;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.kafka.common.serialization.Serdes;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.kafka.common.conf.KafkaConfigNames;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.util.DataUtils;
import org.cedar.onestop.kafka.common.util.KafkaHelpers;
import org.elasticsearch.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.*;
import static org.cedar.onestop.indexer.SearchIndexTopology.buildSearchIndexTopology;

public class IndexerMain {
  private static final Logger log = LoggerFactory.getLogger(IndexerMain.class);

  public static void main(String[] args) throws IOException {
    var config = new AppConfig();

    var elasticClient = buildElasticClient(config);
    var elasticConfig = buildElasticConfig(config, elasticClient);

    var searchIndexingTopology = buildSearchIndexTopology(elasticClient, elasticConfig, config);
    var streamsConfig = buildStreamsConfig(config);
    var streamsApp = KafkaHelpers.buildStreamsAppWithKillSwitch(searchIndexingTopology, streamsConfig);
    Runtime.getRuntime().addShutdownHook(new Thread(streamsApp::close));
    streamsApp.start();
  }

  private static Properties buildStreamsConfig(AppConfig config) {
    // Filter to only valid config values -- Streams config + possible internal Producer & Consumer config
    var kafkaConfigs = DataUtils.trimMapKeys("kafka.", config.getCurrentConfigMap());
    var filteredConfigs = DataUtils.filterMapKeys(KafkaConfigNames.streams, kafkaConfigs);

    log.info("Building kafka streams appConfig for {}", StreamsApps.INDEXER_ID);
    Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(APPLICATION_ID_CONFIG, StreamsApps.INDEXER_ID);
    streamsConfiguration.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    streamsConfiguration.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
    streamsConfiguration.putAll(filteredConfigs);
    return streamsConfiguration;
  }

  private static RestHighLevelClient buildElasticClient(AppConfig config) {
    var elasticHost = config.getOrDefault("elasticsearch.host", "").toString();
    var elasticPort = Integer.valueOf(config.getOrDefault("elasticsearch.port", "-1").toString());

    if (elasticHost.isBlank() || elasticPort < 0) {
      throw new IllegalStateException("`elastic.host` and `elastic.port` configuration values are required");
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
        .setMaxRetryTimeoutMillis(300000)
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

  private static ElasticsearchConfig buildElasticConfig(AppConfig config, RestHighLevelClient elasticClient) throws IOException {
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

    var elasticVersion = elasticClient.info(RequestOptions.DEFAULT).getVersion();
    return new ElasticsearchConfig(
        elasticPrefix, elasticMaxTasks, elasticRequestsPerSecond, elasticSitemapScrollSize,
        elasticSitemapCollectionsPerSubmap, elasticSitemapEnabled, elasticVersion);
  }
}

