package org.cedar.onestop.registry.service

import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture

import static org.apache.kafka.streams.KafkaStreams.State.*

@Unroll
class KafkaBeanConfigSpec extends Specification {

  def beanConfig = new KafkaBeanConfig()

  def "streams app completes future when transitioning to error state"() {
    def streamsConfig = beanConfig.streamsConfig([
        'bootstrap.servers': 'http://localhost:9092',
        'schema.registry.url': 'http://localhost:8081'
    ])
    def testFuture = new CompletableFuture()
    def streamsApp = beanConfig.streamsApp(streamsConfig, testFuture)
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
