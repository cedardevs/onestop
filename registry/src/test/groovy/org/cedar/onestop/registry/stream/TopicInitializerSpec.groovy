package org.cedar.onestop.registry.stream

import org.apache.kafka.clients.admin.MockAdminClient
import org.apache.kafka.common.Node
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class TopicInitializerSpec extends Specification {

  def 'can set partitions and replication explicitly'() {
    def nodes = nodeList(5)
    def adminClient = new MockAdminClient(nodes, nodes.first())

    when:
    def initializer = new TopicInitializer(adminClient, 1, 1 as short)

    then:
    initializer.numPartitions == 1
    initializer.replicationFactor == 1
  }

  def 'initializes partitions'() {
    def nodes = nodeList(5)
    def adminClient = new MockAdminClient(nodes, nodes.first())
    int numPartitions = 2
    short numReplicas = 3

    when:
    def initializer = new TopicInitializer(adminClient, numPartitions, numReplicas)

    then:
    adminClient.listTopics().names().get().size() == 0

    when:
    initializer.initialize()

    then:
    def names = adminClient.listTopics().names().get()
    names.size() > 0
    adminClient.describeTopics(names).all().get().each { name, description ->
      assert description.partitions().size() == numPartitions
      description.partitions().each {
        assert it.replicas().size() == numReplicas
      }
    }
  }

  private List<Node> nodeList(int numNodes) {
    (0..(numNodes-1)).collect {
      new Node(it, 'host' + it, 9092)
    }
  }

}
