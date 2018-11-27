package org.cedar.psi.registry.stream

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.apache.kafka.streams.test.OutputVerifier
import org.cedar.psi.common.avro.Discovery
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.Method
import org.cedar.psi.common.avro.ParsedRecord
import org.cedar.psi.common.avro.Publishing
import org.cedar.psi.common.avro.RecordType
import org.cedar.psi.common.util.MockSchemaRegistrySerde
import org.cedar.psi.registry.util.TimeFormatUtils
import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import static org.apache.kafka.streams.StreamsConfig.*
import static org.cedar.psi.common.constants.Topics.*
import static org.cedar.psi.common.util.StreamSpecUtils.STRING_SERIALIZER
import static org.cedar.psi.common.util.StreamSpecUtils.readAllOutput

class FullTopologySpec extends Specification {

  static final UTC_ID = ZoneId.of('UTC')

  def config = [
      (APPLICATION_ID_CONFIG)           : 'delayed_publisher_spec',
      (BOOTSTRAP_SERVERS_CONFIG)        : 'http://localhost:9092',
      (SCHEMA_REGISTRY_URL_CONFIG)      : 'http://localhost:8081',
      (DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
      (DEFAULT_VALUE_SERDE_CLASS_CONFIG): MockSchemaRegistrySerde.class.name,
      (AUTO_OFFSET_RESET_CONFIG)        : 'earliest'
  ]

  def topology = TopologyBuilders.buildTopology(5000)
  def driver = new TopologyTestDriver(topology, new Properties(config))
  def inputFactory = new ConsumerRecordFactory(STRING_SERIALIZER, new MockSchemaRegistrySerde().serializer())
  def parsedFactory = new ConsumerRecordFactory(STRING_SERIALIZER, new MockSchemaRegistrySerde().serializer())

  def inputType = RecordType.granule
  def inputSource = DEFAULT_SOURCE
  def inputTopic = inputTopic(inputType, inputSource)
  def parsedTopic = parsedTopic(inputType)
  def publishedTopic = publishedTopic(inputType)
  def inputStore = driver.getKeyValueStore(inputStore(inputType, inputSource))
  def parsedStore = driver.getKeyValueStore(parsedStore(inputType))

  def cleanup(){
    driver.close()
  }

  def 'ingests and aggregates raw granule info'() {
    def key = 'A'
    def value1 = buildTestGranule('{"size":42}')
    def value2 = buildTestGranule('{"name":"test"}')

    when:
    driver.pipeInput(inputFactory.create(inputTopic, key, value1))
    driver.pipeInput(inputFactory.create(inputTopic, key, value2))

    then:
    inputStore.get('A') == buildTestGranule('{"size":42,"name":"test"}')
  }

  def 'values for disvocery and publishing are set to the default values '() {
    def key = 'A'
    def discovery1 = Discovery.newBuilder()
        .build()

    def publishing = Publishing.newBuilder()
        .build()

    def value1 = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery1)
        .setPublishing(publishing)
        .build()

    when:
    driver.pipeInput(parsedFactory.create(parsedTopic, key, value1))

    then:
    parsedStore.get(key) == value1
    def output = readAllOutput(driver, publishedTopic)
    OutputVerifier.compareKeyValue(output[0], key, value1)
    output.size() == 1
  }

  def 'saves and updates parsed granule values with  '() {
    def key = 'A'
    def discovery1 = Discovery.newBuilder()
        .setFileIdentifier('gov.super.important:FILE-ID')
        .setTitle("Title")
        .setHierarchyLevelName('granule')
        .setParentIdentifier(null )
        .build()
    def discovery2 = Discovery.newBuilder()
        .setFileIdentifier('gov.super.important:FILE-ID')
        .setTitle("SuperTitle")
        .setHierarchyLevelName('granule')
        .setParentIdentifier('gov.super.important:PARENT-ID')
        .setAlternateTitle('Still title')
        .setDescription('Important')
        .build()
    def publishing = Publishing.newBuilder()
        .build()

    def value1 = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery1)
        .setPublishing(publishing)
        .build()
    def value2 = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery2)
        .setPublishing(publishing)
        .build()

    when:
    driver.pipeInput(parsedFactory.create(parsedTopic, key, value1))
    driver.pipeInput(parsedFactory.create(parsedTopic, key, value2))

    then:
    parsedStore.get(key) == value2
    def output = readAllOutput(driver, publishedTopic)
    OutputVerifier.compareKeyValue(output[0], key, value1)
    OutputVerifier.compareKeyValue(output[1], key, value2)
    output.size() == 2
  }

  def 'sends tombstones for private granules'() {
    def key = 'A'
    def discovery = Discovery.newBuilder()
        .setTitle("secret")
        .build()

    def publishing = Publishing.newBuilder()
        .setIsPrivate(true)
        .build()

    def value = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery)
        .setPublishing(publishing)
        .build()

    when:
    driver.pipeInput(parsedFactory.create(parsedTopic, key, value))

    then:
    parsedStore.get(key) == value
    def output = readAllOutput(driver, publishedTopic)
    OutputVerifier.compareKeyValue(output[0], key, null)
    output.size() == 1
  }

  def 're-publishes granules at an indicated time'() {
    def key = 'A'
    def plusFiveTime = ZonedDateTime.now(UTC_ID).plusSeconds(5)
    def plusFiveString = ISO_OFFSET_DATE_TIME.format(plusFiveTime)
    Long plusFiveLong = TimeFormatUtils.parseTimestamp(plusFiveString)
    def discovery = Discovery.newBuilder()
        .setTitle("secret")
        .build()

    def publishing = Publishing.newBuilder()
        .setIsPrivate(true)
        .setUntil(plusFiveLong)
        .build()

    def plusFiveMessage = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(discovery)
        .setPublishing(publishing)
        .build()

    when:
    driver.pipeInput(parsedFactory.create(parsedTopic, key, plusFiveMessage))

    then: // a tombstone is published
    parsedStore.get(key) == plusFiveMessage
    def output1 = readAllOutput(driver, publishedTopic)
    OutputVerifier.compareKeyValue(output1[0], key, null)
    output1.size() == 1

    when:
    driver.advanceWallClockTime(6000)

    then:
    parsedStore.get(key) == plusFiveMessage
    def output2 = readAllOutput(driver, publishedTopic)
    OutputVerifier.compareKeyValue(output2[0], key, plusFiveMessage)
    output2.size() == 1
  }


  private static buildTestGranule(String content) {
    def builder = Input.newBuilder()
    builder.type = RecordType.granule
    builder.method = Method.POST
    builder.contentType = 'application/json'
    builder.host = 'localhost'
    builder.protocol = 'http'
    builder.requestUrl = '/test'
    builder.source = 'test'
    builder.content = content
    return builder.build()
  }

}
