package org.cedar.psi.registry.service

import org.apache.kafka.streams.KafkaStreams
import org.cedar.psi.registry.stream.TopicInitializer
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class KafkaBeanConfigSpec extends Specification {

  def beanConfig = new KafkaBeanConfig()

  def "streams app throws exception when transitioning to bad state #state"() {
    def mockInitializer = Mock(TopicInitializer)
    def kafkaProps = beanConfig.kafkaProps([:] as Properties)
    def streamsConfig = beanConfig.streamsConfig(kafkaProps)
    def streamsApp = beanConfig.streamsApp(streamsConfig, mockInitializer)

    when:
    streamsApp.setState(state)

    then:
    thrown(IllegalStateException)

    where:
    state << [KafkaStreams.State.NOT_RUNNING, KafkaStreams.State.ERROR]
  }

}
