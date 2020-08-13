package org.cedar.onestop.registry.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.kafka.clients.admin.MockAdminClient
import org.apache.kafka.common.Node
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.apache.kafka.streams.test.OutputVerifier
import org.cedar.onestop.registry.util.TimeFormatUtils
import org.cedar.schemas.avro.psi.*
import org.cedar.schemas.avro.util.AvroUtils
import org.cedar.schemas.avro.util.MockSchemaRegistrySerde
import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import static org.apache.kafka.streams.StreamsConfig.*
import static org.cedar.onestop.kafka.common.constants.Topics.*
import static org.cedar.schemas.avro.util.StreamSpecUtils.STRING_SERIALIZER
import static org.cedar.schemas.avro.util.StreamSpecUtils.readAllOutput

class FullTopologySpec extends Specification {

  static final UTC_ID = ZoneId.of('UTC')

  def config = [
      (APPLICATION_ID_CONFIG)           : 'full_topology_spec',
      (BOOTSTRAP_SERVERS_CONFIG)        : 'http://localhost:9092',
      (SCHEMA_REGISTRY_URL_CONFIG)      : 'http://localhost:8081',
      (DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
      (DEFAULT_VALUE_SERDE_CLASS_CONFIG): MockSchemaRegistrySerde.class.name,
      (AUTO_OFFSET_RESET_CONFIG)        : 'earliest'
  ]

  def mockNode= new Node(0, 'FullTopologySpecNode', 9092)
  def mockAdminClient = new MockAdminClient([mockNode], mockNode)
  def topology = TopologyBuilders.buildTopology(5000, mockAdminClient)
  def driver = new TopologyTestDriver(topology, new Properties(config))
  def inputFactory = new ConsumerRecordFactory(STRING_SERIALIZER, new MockSchemaRegistrySerde().serializer())
  def parsedFactory = new ConsumerRecordFactory(STRING_SERIALIZER, new MockSchemaRegistrySerde().serializer())

  def inputType = RecordType.granule
  def inputSource = DEFAULT_SOURCE
  def inputTopic = inputTopic(inputType, inputSource)
  def inputChangelogTopic = inputChangelogTopicCombined(config[APPLICATION_ID_CONFIG], inputType)
  def parsedTopic = parsedTopic(inputType)
  def publishedTopic = publishedTopic(inputType)
  def inputStore = driver.getKeyValueStore(inputStoreCombined(inputType))
  def parsedStore = driver.getKeyValueStore(parsedStore(inputType))

  def cleanup(){
    driver.close()
  }

  def 'ingests and aggregates raw granule info'() {
    def key = '101cccf3-2f54-4dec-9804-192545496955'
    def value1 = buildTestGranule('{"size":42}',  Method.POST)
    def value2 = buildTestGranule('{"name":"test"}', Method.PATCH)

    when:
    driver.pipeInput(inputFactory.create(inputTopic, key, value1))
    driver.pipeInput(inputFactory.create(inputTopic, key, value2))

    and:
    def aggregate = inputStore.get('101cccf3-2f54-4dec-9804-192545496955')

    then:
    aggregate instanceof AggregatedInput
    aggregate.rawJson == '{"size":42,"name":"test"}'
    aggregate.events.size() == 2
  }

  def 'handles duplicated inputs'() {
    def key = '101cccf3-2f54-4dec-9804-192545496955'
    def json1 = '{"relationships":[{"type":"COLLECTION","id":"11111111-1111-1111-1111-111111111111"}]}'
    def json2 = '{"relationships":[{"type":"COLLECTION","id":"11111111-1111-1111-1111-111111111111"}]}'
    def value1 = buildTestGranule(json1,  Method.PATCH)
    def value2 = buildTestGranule(json2,  Method.PATCH)

    when:
    driver.pipeInput(inputFactory.create(inputTopic, key, value1))
    driver.pipeInput(inputFactory.create(inputTopic, key, value2))

    and:
    def aggregate = inputStore.get('101cccf3-2f54-4dec-9804-192545496955')

    then:
    aggregate instanceof AggregatedInput
    aggregate.rawJson == json2
    aggregate.relationships.size() == 1
    aggregate.relationships[0].id == '11111111-1111-1111-1111-111111111111'
    aggregate.events.size() == 2
    aggregate.errors.size() == 0
  }

  def 'handles real world inputs with duplicated values from common ingest'() {
    def key = '101cccf3-2f54-4dec-9804-192545496955'
    def jsonInputs = new JsonSlurper().parse(ClassLoader.systemClassLoader.getResourceAsStream('real-world-inputs.json'))
    def avroInputs = jsonInputs.collect {
      AvroUtils.jsonToAvro(JsonOutput.toJson(it), Input.classSchema)
    }
    def recordInputs = avroInputs.collect {
      inputFactory.create(inputTopic, key, it)
    }

    when:
    driver.pipeInput(recordInputs)

    then:
    def (out1, out2, out3, out4) = readAllOutput(driver, inputChangelogTopic)
    out1.value() instanceof AggregatedInput
    out1.value().errors.size() == 0
    out2.value() instanceof AggregatedInput
    out2.value().errors.size() == 0
    out3.value() instanceof AggregatedInput
    out3.value().errors.size() == 0
    out4.value() instanceof AggregatedInput
    out4.value().errors.size() == 0
  }

  def 'handles a delete input for nonexistent record'() {
    def key = '101cccf3-2f54-4dec-9804-192545496955'
    def value1 = Input.newBuilder().setMethod(Method.DELETE).setType(inputType).build()

    when:
    driver.pipeInput(inputFactory.create(inputTopic, key, value1))

    and:
    def aggregate = inputStore.get('101cccf3-2f54-4dec-9804-192545496955')

    then:
    aggregate == null

    and:
    //TODO: delete request for non exsiting request should be null, right?
    def output = readAllOutput(driver, inputChangelogTopic)
    output.size() == 0
  }

  def 'values for discovery and publishing are set to the default values'() {
    def key = '101cccf3-2f54-4dec-9804-192545496955'
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
    parsedStore.get(key).equals(value1)
    def output = readAllOutput(driver, publishedTopic)
    OutputVerifier.compareKeyValue(output[0], key, value1)
    output.size() == 1
  }

  def 'handles tombstones for parsed information'() {
    def key = 'tombstone'
    def record = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscovery(Discovery.newBuilder().setFileIdentifier('tombstone').build())
        .setPublishing(Publishing.newBuilder().build())
        .build()

    when: 'publish a value, then a tombstone'
    driver.pipeInput(parsedFactory.create(parsedTopic, key, record))
    driver.pipeInput(parsedFactory.create(parsedTopic, key, null, new RecordHeaders()))

    then:
    parsedStore.get(key) == null

    and:
    def output = readAllOutput(driver, publishedTopic)
    output.size() == 2
    OutputVerifier.compareKeyValue(output[0], key, record)
    OutputVerifier.compareKeyValue(output[1], key, null)
  }

  def 'saves and updates parsed granule values with  '() {
    def key = '101cccf3-2f54-4dec-9804-192545496955'
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
    parsedStore.get(key).equals(value2)
    def output = readAllOutput(driver, publishedTopic)
    OutputVerifier.compareKeyValue(output[0], key, value1)
    OutputVerifier.compareKeyValue(output[1], key, value2)
    output.size() == 2
  }

  def 'sends tombstones for private granules'() {
    def key = '101cccf3-2f54-4dec-9804-192545496955'
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
    parsedStore.get(key).equals(value)
    def output = readAllOutput(driver, publishedTopic)
    OutputVerifier.compareKeyValue(output[0], key, null)
    output.size() == 1
  }

  def 're-publishes granules at an indicated time'() {
    def key = '101cccf3-2f54-4dec-9804-192545496955'
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
    parsedStore.get(key).equals(plusFiveMessage)
    def output1 = readAllOutput(driver, publishedTopic)
    OutputVerifier.compareKeyValue(output1[0], key, null)
    output1.size() == 1

    when:
    driver.advanceWallClockTime(6000)

    then:
    parsedStore.get(key).equals(plusFiveMessage)
    def output2 = readAllOutput(driver, publishedTopic)
    OutputVerifier.compareKeyValue(output2[0], key, plusFiveMessage)
    output2.size() == 1
  }

  def 'flattens granules with their collections'() {
    def collectionKey = '1e9fa20d-3126-4141-bf04-648a24f63bb4'
    def collectionDiscovery = Discovery.newBuilder()
        .setTitle("collection")
        .setAlternateTitle("inherit me")
        .build()
    def collection = ParsedRecord.newBuilder()
        .setType(RecordType.granule)
        .setDiscovery(collectionDiscovery)
        .build()

    def granuleKey = '101cccf3-2f54-4dec-9804-192545496955'
    def granuleDiscovery = Discovery.newBuilder()
        .setTitle("granule")
        .build()
    def granuleRelationship = Relationship.newBuilder()
        .setId(collectionKey)
        .setType(RelationshipType.COLLECTION)
        .build()
    def granule = ParsedRecord.newBuilder()
        .setType(RecordType.granule)
        .setDiscovery(granuleDiscovery)
        .setRelationships([granuleRelationship])
        .build()

    def flattenedGranuleBuilder = ParsedRecord.newBuilder(granule)
    flattenedGranuleBuilder.getDiscoveryBuilder().setAlternateTitle(collectionDiscovery.alternateTitle)
    def flattenedGranule = flattenedGranuleBuilder.build()

    when:
    driver.pipeInput(parsedFactory.create(parsedTopic(RecordType.collection), collectionKey, collection))
    driver.pipeInput(parsedFactory.create(parsedTopic(RecordType.granule), granuleKey, granule))

    then:
    parsedStore.get(granuleKey).equals(granule)
    def output = readAllOutput(driver, flattenedGranuleTopic())
    OutputVerifier.compareKeyValue(output[0], granuleKey, flattenedGranule)
    output.size() == 1
  }

  def 'granule tombstones result in flattened granule tombstones'() {
    def granuleKey = 'flatteningtombstone'
    def dummyValue = ParsedRecord.newBuilder()
        .setType(RecordType.granule)
        .build()

    when:
    // send a dummy value first, else nothing is stored in the ktable => nothing is deleted => no tombstone is emitted
    driver.pipeInput(parsedFactory.create(parsedTopic(RecordType.granule), granuleKey, dummyValue))
    driver.pipeInput(parsedFactory.create(parsedTopic(RecordType.granule), granuleKey, null, new RecordHeaders()))

    then:
    parsedStore.get(granuleKey) == null
    def output = readAllOutput(driver, flattenedGranuleTopic())
    OutputVerifier.compareKeyValue(output[0], granuleKey, null)
    output.size() == 1
  }


  private static buildTestGranule(String content, Method method) {
    def builder = Input.newBuilder()
    builder.type = RecordType.granule
    builder.method = method
    builder.contentType = 'application/json'
    builder.source = 'test'
    builder.content = content
    return builder.build()
  }

}
