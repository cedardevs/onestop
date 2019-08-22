package org.cedar.psi.registry.stream;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.cedar.psi.registry.service.TopicsConfigurationProps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.cedar.psi.common.constants.Topics.*;

public class TopicInitializer {

  private AdminClient adminClient;
  private int numPartitions;
  private short replicationFactor;

  // add custom config by topic name here
  static Map<String, Map> topicConfigs = new LinkedHashMap<>();

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

  private static void declareTopics(AdminClient adminClient, Map<String, Map> topicConfigs, int numPartitions, short replicationFactor) throws InterruptedException, ExecutionException {
    var currentTopics = adminClient.listTopics().names().get();
    var declaredTopics = new ArrayList<>();
    declaredTopics.addAll(inputTopics());
    declaredTopics.addAll(parsedTopics());
    declaredTopics.addAll(fromExtractorTopics());
    declaredTopics.addAll(toExtractorTopics());
    declaredTopics.addAll(publishedTopics());

    List missingTopics = declaredTopics.stream().filter(t -> !currentTopics.contains(t)).collect(Collectors.toList());

    List<NewTopic> newTopics = new ArrayList();
    missingTopics.forEach(name -> {
      var topic = new NewTopic((String) name, numPartitions, replicationFactor);
      var topicConfig = topicConfigs.containsKey(name) ? topicConfigs.get(name) : new LinkedHashMap();
      newTopics.add(topic.configs(topicConfig));
    });

    CreateTopicsResult result = adminClient.createTopics(newTopics, new CreateTopicsOptions().timeoutMs(30000));
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
