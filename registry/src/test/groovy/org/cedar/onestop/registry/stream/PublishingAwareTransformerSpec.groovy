package org.cedar.onestop.registry.stream

import groovy.util.logging.Slf4j
import org.apache.kafka.streams.processor.ProcessorContext
import org.cedar.schemas.avro.psi.Discovery
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.Publishing
import org.cedar.schemas.avro.psi.RecordType
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

  PublishingAwareTransformer transformer
  ProcessorContext mockContext

  def setup() {
    mockContext = Mock(ProcessorContext)
    mockContext.timestamp() >> System.currentTimeMillis()

    transformer = new PublishingAwareTransformer()
    transformer.init(mockContext)
  }

  def 'sends tombstone when message is private'() {
    def publishing1 = Publishing.newBuilder()
        .setIsPrivate(true)
        .build()
    def value1 = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery)
        .setPublishing(publishing1)
        .build()
    def publishing2 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(futureDateLong)
        .build()
    def value2 = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery)
        .setPublishing(publishing2)
        .build()
    def publishing3 = Publishing.newBuilder()
        .setIsPrivate(false)
        .setUntil(pastDateLong)
        .build()
    def value3 = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery)
        .setPublishing(publishing3)
        .build()

    expect:
    transformer.transform(value1) == null
    transformer.transform(value2) == null
    transformer.transform(value3) == null

  }

  def 'passes message through when not private'() {
    def notPrivate1 = Publishing.newBuilder()
        .setIsPrivate(false)
        .build()
    def notPrivate2 = Publishing.newBuilder()
        .setIsPrivate(false)
        .setUntil(futureDateLong)
        .build()
    def notPrivate3 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(pastDateLong)
        .build()
    def value1 = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery)
        .setPublishing(notPrivate1)
        .build()
    def value2 = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery)
        .setPublishing(notPrivate2)
        .build()
    def value3 = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery)
        .setPublishing(notPrivate3)
        .build()
    expect:
    transformer.transform(value1) == value1
    transformer.transform(value2) == value2
    transformer.transform(value3) == value3
  }

}
