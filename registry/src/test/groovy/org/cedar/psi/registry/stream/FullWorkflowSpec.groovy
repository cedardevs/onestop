package org.cedar.psi.registry.stream

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.clients.admin.MockAdminClient
import org.apache.kafka.common.Node
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.apache.kafka.streams.test.OutputVerifier
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.Method
import org.cedar.psi.common.serde.JsonSerdes
import org.cedar.psi.registry.service.MetadataStreamService
import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import static org.apache.kafka.streams.StreamsConfig.*
import static org.cedar.psi.common.constants.Topics.*
import static org.cedar.psi.registry.util.StreamSpecUtils.STRING_SERIALIZER
import static org.cedar.psi.registry.util.StreamSpecUtils.readAllOutput


class FullWorkflowSpec extends Specification {

  static final UTC_ID = ZoneId.of('UTC')

  def config = [
      (APPLICATION_ID_CONFIG)           : 'delayed_publisher_spec',
      (BOOTSTRAP_SERVERS_CONFIG)        : 'localhost:9092',
      (SCHEMA_REGISTRY_URL_CONFIG)      : 'localhost:8081',
      (DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
      (DEFAULT_VALUE_SERDE_CLASS_CONFIG): JsonSerdes.Map() .class.name,
      (AUTO_OFFSET_RESET_CONFIG)        : 'earliest'
  ]
  def mockScremaRegistryClient = new MockSchemaRegistryClient()
  def inputSerdeProps = [
      (KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG): "localhost:8081",
      (KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG): "true"
  ]
  def inputSerde = new SpecificAvroSerde<Input>(mockScremaRegistryClient).with {
    it.configure(["schema.registry.url": "localhost:8081"], false)
    it
  }

  def mockNode = new Node(0, 'localhost', 9200)
  def mockAdminClient = new MockAdminClient([mockNode], mockNode)

//  def streamService = new MetadataStreamService(mockAdminClient, 5000, Files.newTemporaryFolder())
  def topology = MetadataStreamService.buildTopology(5000)
  def driver = new TopologyTestDriver(topology, new Properties(config))
  def consumerFactory = new ConsumerRecordFactory(STRING_SERIALIZER, inputSerde.serializer())

  def inputType = 'granule'
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
    def value1 = new Input(content: '{"size":42}', method: Method.POST, contentType: 'application/json', host: 'localhost', protocol: 'http', requestUrl: '/test', source: 'test')
    def value2 = new Input(content: '{"name":"test"}', method: Method.POST, contentType: 'application/json', host: 'localhost', protocol: 'http', requestUrl: '/test', source: 'test')

    when:
    driver.pipeInput(consumerFactory.create(inputTopic, key, value1))
    driver.pipeInput(consumerFactory.create(inputTopic, key, value2))

    then:
    inputStore.get('A') == ["size":42, name: "test"]
  }

  def 'saves and updates parsed granule info'() {
    def key = 'A'
    def value1 = ["discovery":["title":"replace me"]]
    def value2 = ["discovery":["title":"test"]]
    def value2PlusPublishing = ["discovery":["title":"test"],"publishing":["private":false]]

    when:
    driver.pipeInput(consumerFactory.create(parsedTopic, key, value1))
    driver.pipeInput(consumerFactory.create(parsedTopic, key, value2))

    then:
    parsedStore.get(key) == value2PlusPublishing
    def output = readAllOutput(driver, publishedTopic)
    OutputVerifier.compareKeyValue(output[0], key, ["discovery":["title":"replace me"],"publishing":["private":false]])
    OutputVerifier.compareKeyValue(output[1], key, ["discovery":["title":"test"],"publishing":["private":false]])
    output.size() == 2
  }

  def 'sends tombstones for private granules'() {
    def key = 'A'
    def value = ["discovery":["title":"secret"],"publishing":["private":true]]

    when:
    driver.pipeInput(consumerFactory.create(parsedTopic, key, value))

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
    def plusFiveMessage = ["discovery":["metadata":"yes"],"publishing":["private":true,"until":plusFiveString]]

    when:
    driver.pipeInput(consumerFactory.create(parsedTopic, key, plusFiveMessage))

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
    OutputVerifier.compareKeyValue(output2[0], key, ["discovery":["metadata":"yes"],"publishing":["private":true,"until":plusFiveString]])
    output2.size() == 1
  }

}
