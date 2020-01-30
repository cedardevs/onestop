package org.cedar.onestop.indexer;

import org.apache.kafka.clients.admin.AdminClient;
import org.cedar.onestop.indexer.util.ElasticsearchFactory;
import org.cedar.onestop.indexer.util.ElasticsearchService;
import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.kafka.common.util.KafkaHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.cedar.onestop.indexer.SearchIndexTopology.buildSearchIndexTopology;

public class IndexerMain {
  private static final Logger log = LoggerFactory.getLogger(IndexerMain.class);

  public static void main(String[] args) {
    try {
      var config = new AppConfig();

      var elasticClient = ElasticsearchFactory.buildElasticClient(config);
      var elasticConfig = ElasticsearchFactory.buildElasticConfig(config, elasticClient);
      var elasticService = new ElasticsearchService(elasticClient, elasticConfig);
      elasticService.initializeCluster();

      var adminConfig = KafkaHelpers.buildAdminConfig(config);
      var adminClient = AdminClient.create(adminConfig);
      var flatteningName = config.get("flattening.topic.name").toString();
      int flatteningPartitions = Integer.parseInt(config.get("flattening.topic.partitions").toString());
      short flatteningReplication = Short.parseShort(config.get("flattening.topic.replication").toString());
      var flatteningDefinition = new KafkaHelpers.TopicDefinition(flatteningName, flatteningPartitions, flatteningReplication);
      var sitemapName = config.get("sitemap.topic.name").toString();
      int sitemapPartitions = Integer.parseInt(config.get("sitemap.topic.partitions").toString());
      short sitemapReplication = Short.parseShort(config.get("sitemap.topic.replication").toString());
      var sitemapDefinition = new KafkaHelpers.TopicDefinition(sitemapName, sitemapPartitions, sitemapReplication);
      KafkaHelpers.ensureTopics(adminClient, List.of(flatteningDefinition, sitemapDefinition)).all().get();

      var searchIndexingTopology = buildSearchIndexTopology(elasticService, config);
      var streamsConfig = KafkaHelpers.buildStreamsConfig(config);
      var streamsApp = KafkaHelpers.buildStreamsAppWithKillSwitch(searchIndexingTopology, streamsConfig);
      Runtime.getRuntime().addShutdownHook(new Thread(streamsApp::close));
      streamsApp.start();
    }
    catch (Exception e) {
      log.error("Application failed", e);
      System.exit(1);
    }
  }

}

