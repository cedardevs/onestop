package org.cedar.onestop.indexer;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.cedar.onestop.indexer.util.ElasticsearchFactory;
import org.cedar.onestop.indexer.util.ElasticsearchService;
import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.kafka.common.conf.KafkaConfigNames;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.util.DataUtils;
import org.cedar.onestop.kafka.common.util.KafkaHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.*;
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

      var searchIndexingTopology = buildSearchIndexTopology(elasticService, config);
      var streamsConfig = buildStreamsConfig(config);
      var streamsApp = KafkaHelpers.buildStreamsAppWithKillSwitch(searchIndexingTopology, streamsConfig);
      Runtime.getRuntime().addShutdownHook(new Thread(streamsApp::close));
      streamsApp.start();
    }
    catch (Exception e) {
      log.error("Application failed", e);
      System.exit(1);
    }
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

}

