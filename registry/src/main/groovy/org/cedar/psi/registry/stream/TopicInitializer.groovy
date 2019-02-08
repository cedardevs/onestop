package org.cedar.psi.registry.stream

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig

import static org.cedar.psi.common.constants.Topics.*

class TopicInitializer {

  private AdminClient adminClient
  private int numPartitions
  private short replicationFactor

  // add custom config by topic name here
  static Map<String, Map> topicConfigs = [:]

  TopicInitializer(AdminClient adminClient) {
    this(adminClient, DEFAULT_NUM_PARTITIONS, determineReplicationFactor(adminClient))
  }

  TopicInitializer(AdminClient adminClient, int numPartitions, short replicationFactor) {
    this.adminClient = adminClient
    this.numPartitions = numPartitions
    this.replicationFactor = replicationFactor
  }

  void initialize() {
    declareTopics(this.adminClient, topicConfigs, numPartitions, replicationFactor)
  }

  static short determineReplicationFactor(AdminClient adminClient) {
    def numNodes = adminClient.describeCluster().nodes().get().size()
    return numNodes > 1 ? 2 :1
  }

  private static void declareTopics(AdminClient adminClient, Map<String, Map> topicConfigs, int numPartitions, short replicationFactor) {
    def currentTopics = adminClient.listTopics().names().get()
    def declaredTopics = inputTopics() + parsedTopics() + fromExtractorTopics() + toExtractorTopics() + publishedTopics()
    def missingTopics = declaredTopics.findAll({ !currentTopics.contains(it) })
    def newTopics = missingTopics.collect { name ->
      return new NewTopic(name, numPartitions, replicationFactor)
          .configs(topicConfigs[name] ?: [:])
    }
    def result = adminClient.createTopics(newTopics)
    result.all().get()
  }

  private static Map createChangelogTopicConfig(Map additionalConfig = [:]) {
    def config = [
        (TopicConfig.CLEANUP_POLICY_CONFIG): TopicConfig.CLEANUP_POLICY_COMPACT,
        (TopicConfig.RETENTION_MS_CONFIG)  : '-1'
    ]
    return config + additionalConfig
  }

}
