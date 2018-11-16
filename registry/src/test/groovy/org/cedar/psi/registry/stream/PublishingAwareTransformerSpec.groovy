package org.cedar.psi.registry.stream

import groovy.util.logging.Slf4j
import org.apache.kafka.streams.processor.ProcessorContext
import org.cedar.psi.common.avro.Discovery
import org.cedar.psi.common.avro.ParsedRecord
import org.cedar.psi.common.avro.Publishing
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

@Slf4j
@Unroll
class PublishingAwareTransformerSpec extends Specification {

  static  pastDateString = '1000-01-01T00:00:00Z'
  static long pastDateLong = Instant.parse( pastDateString).toEpochMilli()

  static futureDateString = '3000-01-01T00:00:00Z'
  static long futureDateLong = Instant.parse(futureDateString).toEpochMilli()

  static discovery = Discovery.newBuilder()
      .build()
  static publishing = Publishing.newBuilder()
      .build()
  def value = ParsedRecord.newBuilder()
      .setDiscovery(discovery)
      .setPublishing(publishing)
      .build()

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
    transformer.transform(value) == null

    where:
    value << [
        publishing.setIsPrivate(true),
        publishing.setIsPrivate(true), publishing.setUntil(futureDateLong),
        publishing.setIsPrivate(false), publishing.setUntil(pastDateLong),
    ]
  }

  def 'passes message through when not private'() {
    expect:
    transformer.transform(value) == value

    where:
    value  << [
        publishing.setIsPrivate(false),
        publishing.setIsPrivate(false), publishing.setUntil(futureDateLong),
        publishing.setIsPrivate(true), publishing.setUntil(pastDateLong),
    ]
  }

}
