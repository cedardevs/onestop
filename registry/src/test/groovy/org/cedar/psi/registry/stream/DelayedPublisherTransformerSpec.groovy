package org.cedar.psi.registry.stream

import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.Stores
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.apache.kafka.streams.test.OutputVerifier
import org.cedar.psi.registry.utils.StreamSpecUtils
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import static org.cedar.psi.registry.utils.StreamSpecUtils.STRING_SERIALIZER
import static org.cedar.psi.registry.utils.StreamSpecUtils.STRING_DESERIALIZER

@Slf4j
@Unroll
class DelayedPublisherTransformerSpec extends Specification {

  static final UTC_ID = ZoneId.of('UTC')
  static final INPUT_TOPIC = 'input'
  static final OUTPUT_TOPIC = 'output'
  static final TIMESTAMP_STORE_NAME = 'timestamp'
  static final LOOKUP_STORE_NAME = 'lookup'
  static final long TEST_INTERVAL = 500
  static final consumerFactory = new ConsumerRecordFactory(INPUT_TOPIC, STRING_SERIALIZER, STRING_SERIALIZER)

  DelayedPublisherTransformer transformer
  TopologyTestDriver driver
  KeyValueStore timestampStore
  KeyValueStore lookupStore

  def setup() {
    transformer = new DelayedPublisherTransformer(TIMESTAMP_STORE_NAME, LOOKUP_STORE_NAME, TEST_INTERVAL)

    def config = [
        (StreamsConfig.APPLICATION_ID_CONFIG)           : 'delayed_publisher_spec',
        (StreamsConfig.BOOTSTRAP_SERVERS_CONFIG)        : 'localhost:9092',
        (StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
        (StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG): Serdes.String().class.name,
        (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)       : 'earliest'
    ]
    def builder = new StreamsBuilder()
    builder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.inMemoryKeyValueStore(TIMESTAMP_STORE_NAME), Serdes.Long(), Serdes.String()))
    builder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.inMemoryKeyValueStore(LOOKUP_STORE_NAME), Serdes.String(), Serdes.String()))

    builder
        .stream(INPUT_TOPIC)
        .transform({-> transformer}, TIMESTAMP_STORE_NAME, LOOKUP_STORE_NAME)
        .to(OUTPUT_TOPIC)

    def topology = builder.build()
    driver = new TopologyTestDriver(topology, new Properties(config))

    timestampStore = driver.getKeyValueStore(TIMESTAMP_STORE_NAME)
    lookupStore = driver.getKeyValueStore(LOOKUP_STORE_NAME)
  }

  def cleanup() {
    driver.close()
  }

  def 'value with private #isPrivate is saved and passes through'() {
    def key = 'A'
    def value = '{"discovery":{"metadata":"yes"},"publishing":{"private":' + isPrivate + '}}'

    when:
    sendInput(driver, INPUT_TOPIC, key, value)

    then:
    lookupStore.get(key) == null
    !timestampStore.all().hasNext()

    and:
    def output = driver.readOutput(OUTPUT_TOPIC, STRING_DESERIALIZER, STRING_DESERIALIZER)
    output.key() == key
    output.value() == value

    where:
    isPrivate << [true, false]
  }

  def 'value with no publishing info gets private false added'() {
    def key = 'A'
    def value = '{"discovery":{"metadata":"yes"}}'
    def expected = '{"discovery":{"metadata":"yes"},"publishing":{"private":false}}'

    when:
    sendInput(driver, INPUT_TOPIC, key, value)

    then:
    lookupStore.get(key) == null
    !timestampStore.all().hasNext()

    and:
    def output = driver.readOutput(OUTPUT_TOPIC, STRING_DESERIALIZER, STRING_DESERIALIZER)
    output.key() == key
    output.value() == expected
  }

  def 'value with future publishing date saves a publishing trigger'() {
    def futureDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def futureString = ISO_OFFSET_DATE_TIME.format(futureDate)
    def futureMillis = Instant.from(futureDate).toEpochMilli()
    def key = 'A'
    def value = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + futureString +'"}}'

    when:
    sendInput(driver, INPUT_TOPIC, key, value)

    then:
    timestampStore.get(futureMillis) == key
    lookupStore.get(key) == value

    and:
    def output = driver.readOutput(OUTPUT_TOPIC, STRING_DESERIALIZER, STRING_DESERIALIZER)
    output.key() == key
    output.value() == value
  }

  def 'values with future publishing dates are republished when time elapses'() {
    def plusFiveKey = '5'
    def plusFiveTime = ZonedDateTime.now(UTC_ID).plusSeconds(5)
    def plusFiveString = ISO_OFFSET_DATE_TIME.format(plusFiveTime)
    def plusFiveMillis = Instant.from(plusFiveTime).toEpochMilli()
    def plusFiveMessage = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + plusFiveString +'"}}'

    def plusSixKey = '6'
    def plusSixTime = ZonedDateTime.now(UTC_ID).plusSeconds(6)
    def plusSixString = ISO_OFFSET_DATE_TIME.format(plusSixTime)
    def plusSixMillis = Instant.from(plusSixTime).toEpochMilli()
    def plusSixMessage = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + plusSixString +'"}}'

    def plusTenKey = '10'
    def plusTenTime = ZonedDateTime.now(UTC_ID).plusSeconds(10)
    def plusTenString = ISO_OFFSET_DATE_TIME.format(plusTenTime)
    def plusTenMillis = Instant.from(plusTenTime).toEpochMilli()
    def plusTenMessage = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + plusTenString +'"}}'

    def plusFivePublished = plusFiveMessage.replaceFirst('true', 'false')
    def plusSixPublished = plusSixMessage.replaceFirst('true', 'false')

    when: // publish messages, and do it out of order
    sendInput(driver, INPUT_TOPIC, plusSixKey, plusSixMessage)
    sendInput(driver, INPUT_TOPIC, plusTenKey, plusTenMessage)
    sendInput(driver, INPUT_TOPIC, plusFiveKey, plusFiveMessage)

    then: // values and future publish dates are stored
    timestampStore.get(plusFiveMillis) == plusFiveKey
    timestampStore.get(plusSixMillis) == plusSixKey
    timestampStore.get(plusTenMillis) == plusTenKey
    lookupStore.get(plusFiveKey) == plusFiveMessage
    lookupStore.get(plusSixKey) == plusSixMessage
    lookupStore.get(plusTenKey) == plusTenMessage

    when: // 8 seconds pass
    driver.advanceWallClockTime(8000)

    then: // 5- and 6-second messages have triggers removed and state is updated
    timestampStore.get(plusFiveMillis) == null
    timestampStore.get(plusSixMillis) == null
    timestampStore.get(plusTenMillis) == plusTenKey // <-- not removed
    lookupStore.get(plusFiveKey) == null
    lookupStore.get(plusSixKey) == null
    lookupStore.get(plusTenKey) == plusTenMessage // <-- original value remains

    and: // 3 original messages plus 2 republished messages have come out
    def output = StreamSpecUtils.readAllOutput(driver, OUTPUT_TOPIC)
    output.size() == 5
    OutputVerifier.compareKeyValue(output[0], plusSixKey, plusSixMessage)
    OutputVerifier.compareKeyValue(output[1], plusTenKey, plusTenMessage)
    OutputVerifier.compareKeyValue(output[2], plusFiveKey, plusFiveMessage)
    OutputVerifier.compareKeyValue(output[3], plusFiveKey, plusFivePublished)
    OutputVerifier.compareKeyValue(output[4], plusSixKey, plusSixPublished)
  }

  def 'second value with past publishing date removes an already stored publish trigger'() {
    def key = 'A'
    // first message has future date
    def futureDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def futureString = ISO_OFFSET_DATE_TIME.format(futureDate)
    def futureMillis = Instant.from(futureDate).toEpochMilli()
    def firstValue = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + futureString +'"}}'

    // second message has past date
    def pastDate = ZonedDateTime.now(UTC_ID).minusDays(1)
    def pastString = ISO_OFFSET_DATE_TIME.format(pastDate)
    def pastMillis = Instant.from(pastDate).toEpochMilli()
    def secondValue = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + pastString +'"}}'

    def finalValue = secondValue.replaceFirst('true', 'false')

    when:
    sendInput(driver, INPUT_TOPIC, key, firstValue)
    sendInput(driver, INPUT_TOPIC, key, secondValue)

    then:
    timestampStore.get(futureMillis) == null
    timestampStore.get(pastMillis) == null
    lookupStore.get(key) == null

    and:
    def output = StreamSpecUtils.readAllOutput(driver, OUTPUT_TOPIC)
    output.size() == 2
    OutputVerifier.compareKeyValue(output[0], key, firstValue)
    OutputVerifier.compareKeyValue(output[1], key, finalValue)
  }

  def 'second value with private false removes an already stored publish trigger'() {
    def key = 'A'
    // first message has future date
    def futureDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def futureString = ISO_OFFSET_DATE_TIME.format(futureDate)
    def futureMillis = Instant.from(futureDate).toEpochMilli()
    def firstValue = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + futureString +'"}}'

    // second message is no longer private
    def secondValue = '{"discovery":{"metadata":"yes"},"publishing":{"private":false}}'

    when:
    sendInput(driver, INPUT_TOPIC, key, firstValue)
    sendInput(driver, INPUT_TOPIC, key, secondValue)

    then:
    timestampStore.get(futureMillis) == null
    lookupStore.get(key) == null

    and:
    def output = StreamSpecUtils.readAllOutput(driver, OUTPUT_TOPIC)
    output.size() == 2
    OutputVerifier.compareKeyValue(output[0], key, firstValue)
    OutputVerifier.compareKeyValue(output[1], key, secondValue)
  }

  def 'second value with future publishing date arrives after initial delay has elapsed'() {
    def key = 'A'
    // first message has near future date
    def firstDate = ZonedDateTime.now(UTC_ID).plus(200, ChronoUnit.MILLIS)
    def firstString = ISO_OFFSET_DATE_TIME.format(firstDate)
    def firstMillis = Instant.from(firstDate).toEpochMilli()
    def firstValue = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + firstString +'"}}'

    // second message has far future date
    def secondDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def secondString = ISO_OFFSET_DATE_TIME.format(secondDate)
    def secondMillis = Instant.from(secondDate).toEpochMilli()
    def secondValue = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + secondString +'"}}'

    when:
    sendInput(driver, INPUT_TOPIC, key, firstValue)
    sleep(500)
    driver.advanceWallClockTime(500)
    sendInput(driver, INPUT_TOPIC, key, secondValue)

    then:
    timestampStore.get(firstMillis) == null
    timestampStore.get(secondMillis) == key
    lookupStore.get(key) == secondValue

    and:
    def output = StreamSpecUtils.readAllOutput(driver, OUTPUT_TOPIC)
    output.size() == 3
    OutputVerifier.compareKeyValue(output[0], key, firstValue)
    OutputVerifier.compareKeyValue(output[1], key, firstValue.replaceFirst('true', 'false'))
    OutputVerifier.compareKeyValue(output[2], key, secondValue)
  }

  def 'second value with future publishing date arrives before initial delay has elapsed'() {
    def key = 'A'
    // first message has near future date
    def firstDate = ZonedDateTime.now(UTC_ID).plusSeconds(1)
    def firstString = ISO_OFFSET_DATE_TIME.format(firstDate)
    def firstMillis = Instant.from(firstDate).toEpochMilli()
    def firstValue = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + firstString +'"}}'

    // second message has far future date
    def secondDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def secondString = ISO_OFFSET_DATE_TIME.format(secondDate)
    def secondMillis = Instant.from(secondDate).toEpochMilli()
    def secondValue = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + secondString +'"}}'

    when: // both messages arrive, then the initial delay elapses
    sendInput(driver, INPUT_TOPIC, key, firstValue)
    sendInput(driver, INPUT_TOPIC, key, secondValue)
    driver.advanceWallClockTime(5000)

    then: // initial publish time is removed, second is still there, state is updated
    timestampStore.get(firstMillis) == null
    timestampStore.get(secondMillis) == key
    lookupStore.get(key) == secondValue

    and: // only the two input values come out; the first republishing event doesn't go off
    def output = StreamSpecUtils.readAllOutput(driver, OUTPUT_TOPIC)
    output.size() == 2
    OutputVerifier.compareKeyValue(output[0], key, firstValue)
    OutputVerifier.compareKeyValue(output[1], key, secondValue)
  }

  // TODO - publish: current lookup value has past date
  // TODO - publish: current lookup value has future date
  // TODO - look for more edge cases

  static sendInput(TopologyTestDriver driver, String topic, String key, String value) {
    driver.pipeInput(consumerFactory.create(topic, key, value))
  }

}
