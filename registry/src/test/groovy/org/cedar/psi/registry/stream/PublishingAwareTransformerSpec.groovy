package org.cedar.psi.registry.stream

import groovy.util.logging.Slf4j
import org.apache.kafka.streams.processor.ProcessorContext
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
@Unroll
class PublishingAwareTransformerSpec extends Specification {

  static pastDate = '1000-01-01T00:00:00Z'
  static futureDate = '3000-01-01T00:00:00Z'

  PublishingAwareTransformer transformer
  ProcessorContext mockContext

  def setup() {
    mockContext = Mock(ProcessorContext)
    mockContext.timestamp() >> System.currentTimeMillis()

    transformer = new PublishingAwareTransformer()
    transformer.init(mockContext)
  }

  def 'sends tombstone when message is private'() {
    expect:
    transformer.transform(input) == null

    where:
    input << [
        ["publishing": ["private": true]],
        ["publishing": ["private": true, "until":futureDate]],
        ["publishing": ["private": false, "until":pastDate]],
    ]
  }

  def 'passes message through when not private'() {
    expect:
    transformer.transform(input) == input

    where:
    input << [
        ["metadata": "there is no publishing info in this json"],
        ["publishing": ["private": false]],
        ["publishing": ["private": false, "until":futureDate]],
        ["publishing": ["private": true, "until":pastDate]],
    ]
  }

}
