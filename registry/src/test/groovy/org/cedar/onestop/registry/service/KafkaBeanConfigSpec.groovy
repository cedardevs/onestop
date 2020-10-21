package org.cedar.onestop.registry.service

import org.apache.kafka.clients.admin.MockAdminClient
import org.apache.kafka.common.Node
import org.cedar.onestop.registry.stream.TopicInitializer
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture

import static org.apache.kafka.streams.KafkaStreams.State.*

@Unroll
class KafkaBeanConfigSpec extends Specification {

  def mockNode = new Node(0, 'KafkaBeanConfigSpecNode', 9092)
  def mockAdminClient = new MockAdminClient([mockNode], mockNode)
  def beanConfig = new KafkaBeanConfig()

  def "streams app completes future when transitioning to error state"() {
    def streamsConfig = beanConfig.streamsConfig([
        'bootstrap.servers': 'http://localhost:9092',
        'schema.registry.url': 'http://localhost:8081'
    ])
    def mockInitializer = Mock(TopicInitializer)
    def testFuture = new CompletableFuture()
    def streamsApp = beanConfig.streamsApp(streamsConfig, mockInitializer, testFuture, mockAdminClient)
    // valid transitions to running state
    streamsApp.setState(REBALANCING)
    streamsApp.setState(RUNNING)

    when:
    streamsApp.setState(ERROR)

    then:
    noExceptionThrown()
    testFuture.isDone()
  }

}
