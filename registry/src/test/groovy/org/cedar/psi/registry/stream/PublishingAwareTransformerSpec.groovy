package org.cedar.psi.registry.stream

import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
@Unroll
class PublishingAwareTransformerSpec extends Specification {

  def transformer = new PublishingAwareTransformer()

  def 'sends tombstone when message is private'() {
    expect:
    transformer.transform(input) == null

    where:
    input << [
        '{"metadata": "yes", "publishing": {"private": true}}',
        '{"metadata": "yes", "publishing": {"private": true, "date": "2020-01-01T00:00:00Z"}}',
    ]
  }

  def 'passes message through when not private'() {
    expect:
    transformer.transform(input) == input

    where:
    input << [
        '{"metadata": "yes"}',
        '{"metadata": "yes", "publishing": {"private": false}}',
        '{"metadata": "yes", "publishing": {"private": false, "date": "1900-01-01T00:00:00Z"}}',
    ]
  }

}
