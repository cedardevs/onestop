package org.cedar.onestop.registry.service

import org.apache.kafka.streams.KafkaStreams
import org.cedar.onestop.kafka.common.util.TopicInitializer
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class KafkaBeanConfigSpec extends Specification {

  def beanConfig = new KafkaBeanConfig()

  def "streams app throws exception when transitioning to bad state #state"() {
    def mockInitializer = Mock(TopicInitializer)
    def streamsConfig = beanConfig.streamsConfig([
        'bootstrap.servers': 'http://localhost:9092',
        'schema.registry.url': 'http://localhost:8081'
    ])
    def streamsApp = beanConfig.streamsApp(streamsConfig, mockInitializer)

    when:
    streamsApp.setState(state)

    then:
    thrown(IllegalStateException)

    where:
    state << [KafkaStreams.State.NOT_RUNNING, KafkaStreams.State.ERROR]
  }

}
