package org.cedar.psi.registry.stream

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.internals.InMemoryKeyValueStore
import spock.lang.Specification

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME


class DelayedPublisherSpec extends Specification {

  static final ZoneId UTC_ID = ZoneId.of('UTC')
  static final TIMESTAMP_STORE_NAME = 'timestamp'
  static final LOOKUP_STORE_NAME = 'lookup'

  ProcessorContext mockContext = Mock(ProcessorContext)
  KeyValueStore<Long, String> timestampStore = new InMemoryKeyValueStore<Long, String>(TIMESTAMP_STORE_NAME, Serdes.Long(), Serdes.String())
  KeyValueStore<String, String> lookupStore = new InMemoryKeyValueStore<String, String>(LOOKUP_STORE_NAME, Serdes.String(), Serdes.String())
  long testInterval = 500

  DelayedPublisherTransformer transformer = new DelayedPublisherTransformer(TIMESTAMP_STORE_NAME, LOOKUP_STORE_NAME, testInterval)

  def setup() {
    mockContext.applicationId() >> 'DelayedPublisherSpec'
    mockContext.getStateStore(TIMESTAMP_STORE_NAME) >> timestampStore
    mockContext.getStateStore(LOOKUP_STORE_NAME) >> lookupStore

    timestampStore.init(mockContext, null)
    lookupStore.init(mockContext, null)
    transformer.init(mockContext)
  }

  def 'publisher is initialized'() {
    when:
    transformer.init(mockContext)

    then:
    transformer instanceof Transformer
    transformer instanceof DelayedPublisherTransformer

    and:
    1 * mockContext.getStateStore(timestampStore.name())
    1 * mockContext.getStateStore(lookupStore.name())
    1 * mockContext.schedule(testInterval, PunctuationType.WALL_CLOCK_TIME, _)
  }

  def 'value with private false passes through'() {
    def key = 'A'
    def value = '{"discovery":{"metadata":"yes"},"publishing":{"private":false}}'

    when:
    def output = transformer.transform(key, value)

    then:
    !timestampStore.all().hasNext()

    and:
    output.key == key
    output.value == value
  }

  def 'value with no publishing info gets private false added'() {
    def key = 'A'
    def value = '{"discovery":{"metadata":"yes"}}'

    when:
    def output = transformer.transform(key, value)

    then:
    !timestampStore.all().hasNext()

    and:
    output.key == key
    output.value == '{"discovery":{"metadata":"yes"},"publishing":{"private":false}}'
  }

  def 'value with future publishing date gets stored'() {
    def futureDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def futureString = ISO_OFFSET_DATE_TIME.format(futureDate)
    def futureMillis = Instant.from(futureDate).toEpochMilli()
    def key = 'A'
    def value = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + futureString +'"}}'

    when:
    def output = transformer.transform(key, value)

    then:
    timestampStore.get(futureMillis) == key

    and:
    output.key == key
    output.value == value
  }

  def 'publishes documents up to a given timestamp'() {
    setup:
    def futureDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def futureString = ISO_OFFSET_DATE_TIME.format(futureDate)
    def futureMillis = Instant.from(futureDate).toEpochMilli()
    def key = 'A'
    def value = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + futureString +'"}}'
    timestampStore.put(futureMillis, key)
    lookupStore.put(key, value)

    when:
    transformer.publishUpTo(futureMillis + 100L)

    then:
    1 * mockContext.forward(key, value.replaceFirst('true', 'false'))
  }

  // TODO - publish: current lookup value has past date
  // TODO - publish: current lookup value has future date
  // TODO - transform: emit null if lookup value may have been published
  // TODO - transform: delete timestamp if new publish date is in past
  // TODO - transform: delete timestamp if new private is false
  // TODO - look for more edge cases

}
