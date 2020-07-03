package org.cedar.onestop.registry.stream;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.common.config.TopicConfig;
import org.cedar.onestop.kafka.common.util.KafkaHelpers;
import org.cedar.onestop.registry.service.TopicsConfigurationProps;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.cedar.onestop.kafka.common.constants.Topics.*;

public class TopicInitializer {

  private AdminClient adminClient;
  private int numPartitions;
  private short replicationFactor;

  // add custom config by topic name here
  static Map<String, String> topicConfigs = new LinkedHashMap<>();

  public TopicInitializer(AdminClient adminClient, TopicsConfigurationProps topicProps) {
    this(adminClient, topicProps.getNumPartitions(), topicProps.getReplicationFactor());
  }

  public TopicInitializer(AdminClient adminClient, int numPartitions, short replicationFactor) {
    this.adminClient = adminClient;
    this.numPartitions = numPartitions;
    this.replicationFactor = replicationFactor;
  }

  public void initialize() throws InterruptedException, ExecutionException {
    declareTopics(this.adminClient, topicConfigs, numPartitions, replicationFactor);
  }

  private static void declareTopics(AdminClient adminClient, Map<String, String> topicConfigs, int numPartitions, short replicationFactor) throws InterruptedException, ExecutionException {
    Set<String> declaredTopics = new HashSet<>();
    declaredTopics.addAll(inputTopics());
    declaredTopics.addAll(parsedTopics());
    declaredTopics.addAll(fromExtractorTopics());
    declaredTopics.addAll(toExtractorTopics());
    declaredTopics.addAll(publishedTopics());
    declaredTopics.add(flattenedGranuleTopic());
    declaredTopics.add(granulesByCollectionId());

    CreateTopicsResult result = KafkaHelpers.ensureTopics(adminClient, declaredTopics, numPartitions, replicationFactor, topicConfigs);
    result.all().get();
  }

  private static Map createChangelogTopicConfig(Map additionalConfig) {
    var config = new LinkedHashMap<>();
    config.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT);
    config.put(TopicConfig.RETENTION_MS_CONFIG, "-1");

    if(additionalConfig != null) {
      config.putAll(additionalConfig);
    }

    return config;
  }

}
