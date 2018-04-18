package org.cedar.psi.registry.stream

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.state.Stores
import org.apache.kafka.streams.test.ConsumerRecordFactory
import spock.lang.Specification

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

class DelayedPublisherSpec extends Specification {

  static final UTC_ID = ZoneId.of('UTC')
  static final INPUT_TOPIC = 'input'
  static final OUTPUT_TOPIC = 'output'
  static final TIMESTAMP_STORE_NAME = 'timestamp'
  static final LOOKUP_STORE_NAME = 'lookup'
  static final long TEST_INTERVAL = 500

  DelayedPublisherTransformer transformer
  ConsumerRecordFactory consumerFactory
  TopologyTestDriver driver

  def setup() {
    consumerFactory = new ConsumerRecordFactory(INPUT_TOPIC, Serdes.String().serializer(), Serdes.String().serializer())
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
  }

  def cleanup() {
    driver.close()
  }

  def 'value with private false passes through'() {
    def key = 'A'
    def value = '{"discovery":{"metadata":"yes"},"publishing":{"private":false}}'

    when:
    driver.pipeInput(consumerFactory.create(INPUT_TOPIC, key, value))

    then:
    !driver.getKeyValueStore(TIMESTAMP_STORE_NAME).all().hasNext()

    and:
    def output = driver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), Serdes.String().deserializer())
    output.key() == key
    output.value() == value
  }

  def 'value with no publishing info gets private false added'() {
    def key = 'A'
    def value = '{"discovery":{"metadata":"yes"}}'

    when:
    driver.pipeInput(consumerFactory.create(INPUT_TOPIC, key, value))

    then:
    !driver.getKeyValueStore(TIMESTAMP_STORE_NAME).all().hasNext()

    and:
    def output = driver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), Serdes.String().deserializer())
    output.key() == key
    output.value() == '{"discovery":{"metadata":"yes"},"publishing":{"private":false}}'
  }

  def 'value with future publishing date gets stored'() {
    def futureDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def futureString = ISO_OFFSET_DATE_TIME.format(futureDate)
    def futureMillis = Instant.from(futureDate).toEpochMilli()
    def key = 'A'
    def value = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + futureString +'"}}'

    when:
    driver.pipeInput(consumerFactory.create(INPUT_TOPIC, key, value))

    then:
    driver.getKeyValueStore(TIMESTAMP_STORE_NAME).get(futureMillis) == key

    and:
    def output = driver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), Serdes.String().deserializer())
    output.key() == key
    output.value() == value
  }

  def 'value with future publishing date gets republished with private false'() {
    def futureDate = ZonedDateTime.now(UTC_ID).plusSeconds(1)
    def futureString = ISO_OFFSET_DATE_TIME.format(futureDate)
    def futureMillis = Instant.from(futureDate).toEpochMilli()
    def key = 'A'
    def value = '{"discovery":{"metadata":"yes"},"publishing":{"private":true,"date":"' + futureString +'"}}'

    when:
    driver.pipeInput(consumerFactory.create(INPUT_TOPIC, key, value))

    then:
    driver.getKeyValueStore(TIMESTAMP_STORE_NAME).get(futureMillis) == key

    and:
    def output1 = driver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), Serdes.String().deserializer())
    output1.key() == key
    output1.value() == value

    when:
    driver.getKeyValueStore(LOOKUP_STORE_NAME).put(key, value)
    driver.advanceWallClockTime(10000) // + 10 sec

    then:
    driver.getKeyValueStore(TIMESTAMP_STORE_NAME).get(futureMillis) == null

    and:
    def output2 = driver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), Serdes.String().deserializer())
    output2.key() == key
    output2.value() == value.replaceFirst('true', 'false')
  }

  // TODO - publish: current lookup value has past date
  // TODO - publish: current lookup value has future date
  // TODO - transform: emit null if lookup value may have been published
  // TODO - transform: delete timestamp if new publish date is in past
  // TODO - transform: delete timestamp if new private is false
  // TODO - look for more edge cases

}
