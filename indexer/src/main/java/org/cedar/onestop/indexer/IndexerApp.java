package org.cedar.onestop.indexer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.indexer.util.ElasticsearchFactory;
import org.cedar.onestop.indexer.util.ElasticsearchService;
import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.kafka.common.util.KafkaHelpers;
import org.cedar.onestop.kafka.common.util.KafkaHealthProbeServer;
import org.cedar.onestop.kafka.common.util.UncaughtExceptionHandler;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.cedar.onestop.indexer.SearchIndexTopology.buildSearchIndexTopology;

public class IndexerApp {
  private static final Logger log = LoggerFactory.getLogger(IndexerApp.class);

  private final AppConfig appConfig;
  private final ElasticsearchConfig elasticConfig;
  private final RestHighLevelClient elasticClient;
  private final ElasticsearchService elasticService;
  private final AdminClient adminClient;
  private final Topology streamsTopology;
  private final Properties streamsProps;
  private final KafkaStreams streamsApp;
  private final KafkaHealthProbeServer probeServer;
  private final UncaughtExceptionHandler exceptionHandler;

  private boolean initialized;

  public IndexerApp(AppConfig appConfig) throws IOException {
    this.appConfig = appConfig;
    elasticClient = ElasticsearchFactory.buildElasticClient(appConfig);
    elasticConfig = ElasticsearchFactory.buildElasticConfig(appConfig, elasticClient);
    elasticService = new ElasticsearchService(elasticClient, elasticConfig);
    adminClient = AdminClient.create(KafkaHelpers.buildAdminConfig(appConfig));
    streamsTopology = buildSearchIndexTopology(elasticService, appConfig);
    streamsProps = KafkaHelpers.buildStreamsConfig(appConfig);
    streamsApp = new KafkaStreams(streamsTopology, streamsProps);
    var maxFailures = appConfig.getOrDefault("streams.exception.max.failures", 2, Integer.class);
    var maxTimeInterval = appConfig.getOrDefault("streams.exception.max.time.millis", 3600000, Integer.class);
    exceptionHandler = new UncaughtExceptionHandler(maxFailures, maxTimeInterval);
    probeServer = new KafkaHealthProbeServer(streamsApp);
  }

  public synchronized void init() throws Exception {
    if (!initialized) {
      elasticService.initializeCluster();
      initTopics();
    }
    initialized = true;
  }

  public void start() throws Exception {
    init();
    KafkaHelpers.onError(streamsApp, exceptionHandler).thenAcceptAsync(o -> stop());
    streamsApp.start();
    probeServer.start();
  }

  public void stop() {
    stop(Duration.ofSeconds(5));
  }

  public void stop(Duration duration) {
    if (initialized) {
      streamsApp.close(duration);
      probeServer.stop(duration);
    }
  }

  private void initTopics() throws ExecutionException, InterruptedException {
    var flatteningName = appConfig.get("flattening.topic.name").toString();
    int flatteningPartitions = Integer.parseInt(appConfig.get("flattening.topic.partitions").toString());
    short flatteningReplication = Short.parseShort(appConfig.get("flattening.topic.replication").toString());
    var flatteningDefinition = new KafkaHelpers.TopicDefinition(flatteningName, flatteningPartitions, flatteningReplication);
    var sitemapName = appConfig.get("sitemap.topic.name").toString();
    int sitemapPartitions = Integer.parseInt(appConfig.get("sitemap.topic.partitions").toString());
    short sitemapReplication = Short.parseShort(appConfig.get("sitemap.topic.replication").toString());
    var sitemapDefinition = new KafkaHelpers.TopicDefinition(sitemapName, sitemapPartitions, sitemapReplication);
    KafkaHelpers.ensureTopics(adminClient, List.of(flatteningDefinition, sitemapDefinition)).all().get();
  }

  public AppConfig getAppConfig() {
    return appConfig;
  }

  public ElasticsearchConfig getElasticConfig() {
    return elasticConfig;
  }

  public RestHighLevelClient getElasticClient() {
    return elasticClient;
  }

  public ElasticsearchService getElasticService() {
    return elasticService;
  }

  public AdminClient getAdminClient() {
    return adminClient;
  }

  public Topology getStreamsTopology() {
    return streamsTopology;
  }

  public Properties getStreamsProps() {
    return streamsProps;
  }

  public KafkaStreams getStreamsApp() {
    return streamsApp;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public static void main(String[] args) {
    try {
      var config = new AppConfig();
      var app = new IndexerApp(config);
      Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
      app.start();
    }
    catch (Error | Exception e) {
      log.error("Application failed", e);
      System.exit(1);
    }
  }

}

