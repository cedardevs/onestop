package org.cedar.psi.registry.stream

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig

import static org.cedar.psi.common.constants.Topics.*

class TopicInitializer {

  private AdminClient adminClient

  // add custom config by topic name here
  static Map<String, Map> topicConfigs = [:]

  TopicInitializer(AdminClient adminClient) {
    this.adminClient = adminClient
  }

  void initialize() {
    declareTopics(this.adminClient, topicConfigs)
  }


  private static void declareTopics(AdminClient adminClient, Map<String, Map> topicConfigs) {
    def currentTopics = adminClient.listTopics().names().get()
    def declaredTopics = inputTopics() + parsedTopics() + fromExtractorTopics() + toExtractorTopics() + publishedTopics()
    def missingTopics = declaredTopics.findAll({ !currentTopics.contains(it) })
    def numBrokers = adminClient.describeCluster().nodes().get().size()
    def replicationFactor = numBrokers > 1 ? 2 : 1
    def newTopics = missingTopics.collect { name ->
      return new NewTopic(name, DEFAULT_NUM_PARTITIONS, replicationFactor as short)
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
