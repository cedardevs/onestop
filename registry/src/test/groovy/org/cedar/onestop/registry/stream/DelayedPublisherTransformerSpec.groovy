package org.cedar.onestop.registry.stream

import groovy.util.logging.Slf4j
import io.confluent.kafka.schemaregistry.RestApp
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider
import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.ValueTransformerSupplier
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.Stores
import org.apache.kafka.streams.test.OutputVerifier
import org.cedar.schemas.avro.psi.Discovery
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.Publishing
import org.cedar.schemas.avro.psi.RecordType
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.AUTO_REGISTER_SCHEMAS
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG
import static org.cedar.schemas.avro.util.StreamSpecUtils.readAllOutput

@Slf4j
@Unroll
class DelayedPublisherTransformerSpec extends Specification {

  static final UTC_ID = ZoneId.of('UTC')
  static final INPUT_TOPIC_NAME = 'input'
  static final OUTPUT_TOPIC_NAME = 'output'
  static final TIME_STORE_NAME = 'timestamp'
  static final KEY_STORE_NAME = 'key'
  static final LOOKUP_STORE_NAME = 'lookup'
  static final long TEST_INTERVAL = 500

  def discovery1 = Discovery.newBuilder()
      .setTitle("first")
      .build()

  def discovery2 = Discovery.newBuilder()
      .setTitle("second")
      .build()

  DelayedPublisherTransformer transformer
  TopologyTestDriver driver
  KeyValueStore keyStore
  KeyValueStore timeStore
  KeyValueStore lookupStore
  TestInputTopic inputTopic
  TestOutputTopic outputTopic

  def setup() {

    def schemaRegistry = new RestApp(1234, null, )


    def mockSchemaRegistryClient = MockSchemaRegistry
        .getClientForScope('DelayedPublisherTransformerSpec', [new AvroSchemaProvider()])
    def testValueSerde = new SpecificAvroSerde(mockSchemaRegistryClient)
    testValueSerde.configure([
        (SCHEMA_REGISTRY_URL_CONFIG): 'mock://DelayedPublisherTransformerSpec',
        (AUTO_REGISTER_SCHEMAS)     : 'true'
    ], false)
    transformer = new DelayedPublisherTransformer(TIME_STORE_NAME, KEY_STORE_NAME, LOOKUP_STORE_NAME, TEST_INTERVAL)

    def config = [
        (APPLICATION_ID_CONFIG)           : 'delayed_publisher_spec',
        (BOOTSTRAP_SERVERS_CONFIG)        : 'localhost:9092',
        (SCHEMA_REGISTRY_URL_CONFIG)      : 'mock://DelayedPublisherTransformerSpec',
        (AUTO_REGISTER_SCHEMAS)           : 'true',
        (DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
        (DEFAULT_VALUE_SERDE_CLASS_CONFIG): SpecificAvroSerde.class.name,
        (AUTO_OFFSET_RESET_CONFIG)        : 'earliest'
    ]
    config.put("schema.registry.url", "http://localhost:8081")
    def builder = new StreamsBuilder()
    builder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.inMemoryKeyValueStore(TIME_STORE_NAME), Serdes.Long(), Serdes.String()))
    builder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.inMemoryKeyValueStore(KEY_STORE_NAME), Serdes.String(), Serdes.Long()))

    // build a normalized table
    Materialized<String, ParsedRecord, KeyValueStore> materializer =
        Materialized.as(LOOKUP_STORE_NAME)

    KTable<String, ParsedRecord> lookupTable = builder
        .stream(INPUT_TOPIC_NAME)
        .groupByKey()
        .reduce(StreamFunctions.identityReducer, materializer)

    // capture publishing events in the table and publishing them back to input when they fire
    lookupTable
        .toStream()
        .transform({-> transformer}, TIME_STORE_NAME, KEY_STORE_NAME, LOOKUP_STORE_NAME)
        .to(INPUT_TOPIC_NAME)

    // pass all table events to output, sending tombstones if private
    lookupTable
        .toStream()
        .transformValues({-> new PublishingAwareTransformer()} as ValueTransformerSupplier)
        .to(OUTPUT_TOPIC_NAME)

    def topology = builder.build()
    driver = new TopologyTestDriver(topology, new Properties(config))

    inputTopic = driver.createInputTopic(INPUT_TOPIC_NAME, Serdes.String().serializer(), testValueSerde.serializer())
    outputTopic = driver.createOutputTopic(OUTPUT_TOPIC_NAME, Serdes.String().deserializer(), testValueSerde.deserializer())

    keyStore = driver.getKeyValueStore(KEY_STORE_NAME)
    timeStore = driver.getKeyValueStore(TIME_STORE_NAME)
    lookupStore = driver.getKeyValueStore(LOOKUP_STORE_NAME)
  }

  def cleanup() {
    driver.close()
  }

  def 'value with future publishing date saves a publishing trigger'() {
    def futureDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def futureMillis = Instant.from(futureDate).toEpochMilli()
    def key = 'A'

    def publishing = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(futureMillis)
        .build()

    def value = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery1)
        .setPublishing(publishing)
        .build()

    when:
    inputTopic.pipeInput(key, value)

    then:
    keyStore.get(key) == futureMillis
    timeStore.get(futureMillis) == '["' + key + '"]'
    lookupStore.get(key).equals(value)

    and:
    def output = outputTopic.readKeyValuesToList()
    output[0].key == key
    output[0].value == null
  }

  def 'two values with same future publishing date have triggers save correctly'() {
    def futureDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def futureMillis = Instant.from(futureDate).toEpochMilli()
    def key1 = 'A'
    def key2 = 'B'

    def publishing1 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(futureMillis)
        .build()

    def publishing2 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(futureMillis)
        .build()

    def value1 = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery1)
        .setPublishing(publishing1)
        .build()
    def value2 = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery2)
        .setPublishing(publishing2)
        .build()

    when:
    inputTopic.pipeInput(key1, value1)
    inputTopic.pipeInput(key2, value2)

    then:
    keyStore.get(key1) == futureMillis
    keyStore.get(key2) == futureMillis
    timeStore.get(futureMillis) == "[\"$key1\",\"$key2\"]"
    lookupStore.get(key1).equals(value1)
    lookupStore.get(key2).equals(value2)

    and:
    def output = readAllOutput(driver, OUTPUT_TOPIC_NAME)
    OutputVerifier.compareKeyValue(output[0], key1, null)
    OutputVerifier.compareKeyValue(output[1], key2, null)
    output.size() == 2
  }

  def 'values with future publishing dates are republished when time elapses'() {
    def plusFiveKey = '5'
    def plusFiveTime = ZonedDateTime.now(UTC_ID).plusSeconds(5)
    def plusFiveMillis = Instant.from(plusFiveTime).toEpochMilli()

    def plusFiveMessage = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(plusFiveMillis)
        .build()

    def plusFiveValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery1)
        .setPublishing(plusFiveMessage)
        .build()

    def plusSixKey = '6'
    def plusSixTime = ZonedDateTime.now(UTC_ID).plusSeconds(6)
    def plusSixString = ISO_OFFSET_DATE_TIME.format(plusSixTime)
    def plusSixMillis = Instant.from(plusSixTime).toEpochMilli()
    def plusSixMessage = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(plusSixMillis)
        .build()
    def plusSixValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery1)
        .setPublishing(plusSixMessage)
        .build()

    def plusTenKey = '10'
    def plusTenTime = ZonedDateTime.now(UTC_ID).plusSeconds(10)
    def plusTenString = ISO_OFFSET_DATE_TIME.format(plusTenTime)
    def plusTenMillis = Instant.from(plusTenTime).toEpochMilli()
    def plusTenMessage = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(plusTenMillis)
        .build()
    def plusTenValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery1)
        .setPublishing(plusTenMessage)
        .build()
    when: // publish messages, and do it out of order
    inputTopic.pipeInput(plusSixKey, plusSixValue)
    inputTopic.pipeInput(plusTenKey, plusTenValue)
    inputTopic.pipeInput(plusFiveKey, plusFiveValue)

    then: // values and future publish dates are stored
    keyStore.get(plusFiveKey) == plusFiveMillis
    keyStore.get(plusSixKey) == plusSixMillis
    keyStore.get(plusTenKey) == plusTenMillis
    timeStore.get(plusFiveMillis) == "[\"$plusFiveKey\"]"
    timeStore.get(plusSixMillis) == "[\"$plusSixKey\"]"
    timeStore.get(plusTenMillis) == "[\"$plusTenKey\"]"
    lookupStore.get(plusFiveKey).equals(plusFiveValue)
    lookupStore.get(plusSixKey).equals(plusSixValue)
    lookupStore.get(plusTenKey).equals(plusTenValue)

    when: // 8 seconds pass
    driver.advanceWallClockTime(8000)

    then: // 5- and 6-second messages have triggers removed and state is updated
    keyStore.get(plusFiveKey) == null
    keyStore.get(plusSixKey) == null
    keyStore.get(plusTenKey) == plusTenMillis // <-- not removed
    timeStore.get(plusFiveMillis) == null
    timeStore.get(plusSixMillis) == null
    timeStore.get(plusTenMillis) == '["' + plusTenKey + '"]' // <-- not removed
    lookupStore.get(plusFiveKey).equals(plusFiveValue)
    lookupStore.get(plusSixKey).equals(plusSixValue)
    lookupStore.get(plusTenKey).equals(plusTenValue) // <-- original value remains

    and: // 3 original messages plus 2 republished messages have come out
    def output = readAllOutput(driver, OUTPUT_TOPIC_NAME)
    output.size() == 5
    OutputVerifier.compareKeyValue(output[0], plusSixKey, null)
    OutputVerifier.compareKeyValue(output[1], plusTenKey, null)
    OutputVerifier.compareKeyValue(output[2], plusFiveKey, null)
    OutputVerifier.compareKeyValue(output[3], plusFiveKey, plusFiveValue)
    OutputVerifier.compareKeyValue(output[4], plusSixKey, plusSixValue)
  }

  def 'two values with same future publishing date are republished when time elapses'() {
    def futureDate = ZonedDateTime.now(UTC_ID).plusSeconds(5)
    def futureMillis = Instant.from(futureDate).toEpochMilli()
    def key1 = 'A'
    def key2 = 'B'
    def publishing1 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(futureMillis)
        .build()
    def value1 = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery1)
        .setPublishing(publishing1)
        .build()
    def publishing2 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(futureMillis)
        .build()
    def value2 = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery2)
        .setPublishing(publishing2)
        .build()
    when:
    inputTopic.pipeInput(key1, value1)
    inputTopic.pipeInput(key2, value2)
    driver.advanceWallClockTime(10000)

    then:
    keyStore.get(key1) == null
    keyStore.get(key2) == null
    timeStore.get(futureMillis) == null
    lookupStore.get(key1).equals(value1)
    lookupStore.get(key2).equals(value2)

    and:
    def output = readAllOutput(driver, OUTPUT_TOPIC_NAME)
    OutputVerifier.compareKeyValue(output[0], key1, null)
    OutputVerifier.compareKeyValue(output[1], key2, null)
    OutputVerifier.compareKeyValue(output[2], key1, value1)
    OutputVerifier.compareKeyValue(output[3], key2, value2)
    output.size() == 4
  }

  def 'second value with past publishing date removes an already stored publish trigger'() {
    def key = 'A'
    // first message has future date
    def futureDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def futureMillis = Instant.from(futureDate).toEpochMilli()
    def publishing1 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(futureMillis)
        .build()

    def firstValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery1)
        .setPublishing(publishing1)
        .build()
    // second message has past date
    def pastDate = ZonedDateTime.now(UTC_ID).minusDays(1)
    def pastMillis = Instant.from(pastDate).toEpochMilli()
    def publishing2 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(pastMillis)
        .build()
    def secondValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery2)
        .setPublishing(publishing2)
        .build()


    when:
    inputTopic.pipeInput(key, firstValue)
    inputTopic.pipeInput(key, secondValue)

    then:
    keyStore.get(key) == null
    timeStore.get(futureMillis) == null
    timeStore.get(pastMillis) == null
    lookupStore.get(key).equals(secondValue)

    and:
    def output = readAllOutput(driver, OUTPUT_TOPIC_NAME)
    output.size() == 2
    OutputVerifier.compareKeyValue(output[0], key, null)
    OutputVerifier.compareKeyValue(output[1], key, secondValue)
  }

  def 'second value with private false removes an already stored publish trigger'() {
    def key = 'A'
    // first message has future date
    def futureDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def futureMillis = Instant.from(futureDate).toEpochMilli()

    // second message is no longer private

    def publishing1 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(futureMillis)
        .build()

    def publishing2 = Publishing.newBuilder()
        .build()

    def firstValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery1)
        .setPublishing(publishing1)
        .build()
    def secondValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery2)
        .setPublishing(publishing2)
        .build()

    when:
    inputTopic.pipeInput(key, firstValue)
    inputTopic.pipeInput(key, secondValue)

    then:
    keyStore.get(key) == null
    timeStore.get(futureMillis) == null
    lookupStore.get(key).equals(secondValue)

    and:
    def output = readAllOutput(driver, OUTPUT_TOPIC_NAME)
    output.size() == 2
    OutputVerifier.compareKeyValue(output[0], key, null)
    OutputVerifier.compareKeyValue(output[1], key, secondValue)
  }

  def 'second value with private false removes an already stored trigger when another trigger at that time exists'() {
    def key1 = 'A'
    def key2 = 'B'
    // first message has future date
    def futureDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def futureMillis = Instant.from(futureDate).toEpochMilli()

    // second message is no longer private

    def publishing1 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(futureMillis)
        .build()

    def publishing2 = Publishing.newBuilder()
        .build()

    def firstValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery1)
        .setPublishing(publishing1)
        .build()
    def secondValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery2)
        .setPublishing(publishing2)
        .build()
    when:
    inputTopic.pipeInput(key1, firstValue)
    inputTopic.pipeInput(key2, firstValue)
    inputTopic.pipeInput(key1, secondValue)

    then:
    keyStore.get(key1) == null
    keyStore.get(key2) == futureMillis
    timeStore.get(futureMillis) == "[\"$key2\"]"
    lookupStore.get(key1).equals(secondValue)
    lookupStore.get(key2).equals(firstValue)

    and:
    def output = readAllOutput(driver, OUTPUT_TOPIC_NAME)
    output.size() == 3
    OutputVerifier.compareKeyValue(output[0], key1, null)
    OutputVerifier.compareKeyValue(output[1], key2, null)
    OutputVerifier.compareKeyValue(output[2], key1, secondValue)
  }

  def 'second value with future publishing date arrives after initial delay has elapsed'() {
    def key = 'A'
    // first message has near future date
    def firstDate = ZonedDateTime.now(UTC_ID).plus(200, ChronoUnit.MILLIS)
    def firstMillis = Instant.from(firstDate).toEpochMilli()
    def publishing1 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(firstMillis)
        .build()

    def firstValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery1)
        .setPublishing(publishing1)
        .build()
    // second message has far future date
    def secondDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def secondMillis = Instant.from(secondDate).toEpochMilli()
    def publishing2 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(secondMillis)
        .build()
    def secondValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery2)
        .setPublishing(publishing2)
        .build()
    when:
    inputTopic.pipeInput(key, firstValue)
    driver.advanceWallClockTime(500)
    inputTopic.pipeInput(key, secondValue)

    then:
    keyStore.get(key) == secondMillis
    timeStore.get(firstMillis) == null
    timeStore.get(secondMillis) == "[\"$key\"]"
    lookupStore.get(key).equals(secondValue)

    and:
    def output = readAllOutput(driver, OUTPUT_TOPIC_NAME)
    output.size() == 3
    OutputVerifier.compareKeyValue(output[0], key, null)
    OutputVerifier.compareKeyValue(output[1], key, firstValue)
    OutputVerifier.compareKeyValue(output[2], key, null)
  }

  def 'second value with future publishing date arrives before initial delay has elapsed'() {
    def key = 'A'
    // first message has near future date
    def firstDate = ZonedDateTime.now(UTC_ID).plusSeconds(1)
    def firstMillis = Instant.from(firstDate).toEpochMilli()
    def publishing1 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(firstMillis)
        .build()
    def firstValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery1)
        .setPublishing(publishing1)
        .build()

    // second message has far future date
    def secondDate = ZonedDateTime.now(UTC_ID).plusYears(1)
    def secondMillis = Instant.from(secondDate).toEpochMilli()
    def publishing2 = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(secondMillis)
        .build()
    def secondValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery2)
        .setPublishing(publishing2)
        .build()
    when: // both messages arrive, then the initial delay elapses
    inputTopic.pipeInput(key, firstValue)
    inputTopic.pipeInput(key, secondValue)
    driver.advanceWallClockTime(5000)

    then: // initial publish time is removed, second is still there, state is updated
    keyStore.get(key) == secondMillis
    timeStore.get(firstMillis) == null
    timeStore.get(secondMillis) == "[\"$key\"]"
    lookupStore.get(key).equals(secondValue)

    and: // only the two input values come out; the first republishing event doesn't go off
    def output = readAllOutput(driver, OUTPUT_TOPIC_NAME)
    output.size() == 2
    OutputVerifier.compareKeyValue(output[0], key, null)
    OutputVerifier.compareKeyValue(output[1], key, null)
  }

}
