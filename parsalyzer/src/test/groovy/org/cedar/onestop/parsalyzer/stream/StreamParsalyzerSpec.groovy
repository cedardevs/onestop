package org.cedar.onestop.parsalyzer.stream

import groovy.json.JsonOutput
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.cedar.onestop.kafka.common.conf.AppConfig
import org.cedar.onestop.kafka.common.constants.StreamsApps
import org.cedar.onestop.kafka.common.constants.Topics
import org.cedar.onestop.kafka.common.serde.JsonSerdes
import org.cedar.schemas.avro.psi.*
import org.cedar.schemas.avro.util.AvroUtils
import org.cedar.schemas.avro.util.MockSchemaRegistrySerde
import org.cedar.schemas.avro.util.StreamSpecUtils
import spock.lang.Specification

class StreamParsalyzerSpec extends Specification {

  def STRING_DESERIALIZER = Serdes.String().deserializer()
  def AVRO_DESERIALIZER = new MockSchemaRegistrySerde().deserializer()

  def streamsConfig = StreamParsalyzer.streamsConfig(StreamsApps.PARSALYZER_ID, new AppConfig()).with {
    it.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, MockSchemaRegistrySerde.class.name)
    it
  }
  def topology = StreamParsalyzer.buildTopology()
  def driver = new TopologyTestDriver(topology, streamsConfig)
  def inputFactory = new ConsumerRecordFactory(Serdes.String().serializer(), new MockSchemaRegistrySerde().serializer())
  def jsonFactory = new ConsumerRecordFactory(Serdes.String().serializer(), JsonSerdes.Map().serializer())

  def testType = RecordType.granule
  def testSource = Topics.DEFAULT_SOURCE
  def testChangelog = Topics.inputChangelogTopicCombined(StreamsApps.REGISTRY_ID, testType)

  def cleanup() {
    driver.close()
  }

  def "Non-SME ISO granule parsed as expected"() {
    given:
    def xml = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
    def key = 'A123'
    def input = buildAggInput(rawXml: xml)

    when:
    driver.pipeInput(inputFactory.create(testChangelog, key, input))

    then:
    // Not found in SME topic
    driver.readOutput(Topics.toExtractorTopic(testType), STRING_DESERIALIZER, AVRO_DESERIALIZER) == null

    and:
    def finalOutput = driver.readOutput(Topics.parsedTopic(testType), STRING_DESERIALIZER, AVRO_DESERIALIZER)
    finalOutput != null
    driver.readOutput(Topics.parsedTopic(testType), STRING_DESERIALIZER, AVRO_DESERIALIZER) == null

    and:
    // Verify some fields
    finalOutput.key() == key
    def result = finalOutput.value()
    result instanceof ParsedRecord
    result.discovery instanceof Discovery
    result.analysis instanceof Analysis
    result.errors  instanceof List

    result.errors.size() == 0
    result.discovery.fileIdentifier == 'gov.super.important:FILE-ID'
    result.analysis.identification.fileIdentifierExists
    result.analysis.identification.fileIdentifierString == 'gov.super.important:FILE-ID'
  }

  def "deleted input record produces a tombstone"() {
    given:
    def xml = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
    def key = 'A123'
    def postInput = buildAggInput(rawXml: xml)
    def deleteInput = buildAggInput(rawXml: xml, deleted: true)

    when:
    driver.pipeInput(inputFactory.create(testChangelog, key, postInput))
    driver.pipeInput(inputFactory.create(testChangelog, key, deleteInput))
    driver.readOutput(Topics.toExtractorTopic(testType), STRING_DESERIALIZER, AVRO_DESERIALIZER) == null
    def originalOutput = driver.readOutput(Topics.parsedTopic(testType), STRING_DESERIALIZER, AVRO_DESERIALIZER)
    def updatedOutput = driver.readOutput(Topics.parsedTopic(testType), STRING_DESERIALIZER, AVRO_DESERIALIZER)

    then: 'verify original fields'
    originalOutput != null
    originalOutput.key() == key
    def originalValue = originalOutput.value()

    originalValue.discovery instanceof Discovery
    originalValue.analysis instanceof Analysis
    originalValue.errors instanceof List
    originalValue.errors.size() == 0

    and: 'verify updated record is a tombstone'
    updatedOutput.key() == key
    updatedOutput.value() == null
  }

  def "SME granule ends up in both SME and parsed topics"() {
    given:
    def xmlSME = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-sme-dummy.xml").text
    def smeKey = 'sme'
    def smeValue = buildAggInput(
        initialSource : 'common-ingest',
        rawXml        : xmlSME
    )

    when:
    driver.pipeInput(inputFactory.create(testChangelog, smeKey, smeValue))

    then: // The record is in the SME topic as json
    def smeOutput = driver.readOutput(Topics.toExtractorTopic(testType), STRING_DESERIALIZER, STRING_DESERIALIZER)
    smeOutput.key() == smeKey
    smeOutput.value() == JsonOutput.toJson(AvroUtils.avroToMap(smeValue))

    and: // And also in the parsed topic as a ParsedRecord
    def parsedOutput = driver.readOutput(Topics.parsedTopic(testType), STRING_DESERIALIZER, AVRO_DESERIALIZER)
    parsedOutput.key() == smeKey
    parsedOutput.value() instanceof ParsedRecord
  }

  def "Non-SME granule and SME granule end up in parsed-granule topic"() {
    given:
    def xmlNonSME = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
    def xmlSME = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-sme-dummy.xml").text
    def nonSMEInputKey = 'notSME'
    def nonSMEInputValue = buildAggInput(
        rawXml: xmlNonSME
    )
    def unparsedKey = 'sme'
    def unparsedValue = [
        source     : 'common-ingest',
        contentType: 'application/xml',
        content    : xmlSME
    ]

    when:
    // Simulate SME ending up in granule-extractor-to since that's another app's responsibility
    driver.pipeInput(inputFactory.create(testChangelog, nonSMEInputKey, nonSMEInputValue))
    driver.pipeInput(jsonFactory.create(Topics.fromExtractorTopic(testType), unparsedKey, unparsedValue))

    then:
    // Both records are in the parsed topic
    def messages = StreamSpecUtils.readAllOutput(driver, Topics.parsedTopic(testType), STRING_DESERIALIZER, AVRO_DESERIALIZER)
    def results = messages.inject([:], { map, message -> map + [(message.key()): message.value()] })

    results.containsKey(nonSMEInputKey)
    results.containsKey(unparsedKey)

    // Verify some parsed fields:
    and:
    def nonSMEResult = AvroUtils.avroToMap(results[nonSMEInputKey])
    nonSMEResult.containsKey('discovery')
    !nonSMEResult.containsKey('error')
    nonSMEResult.discovery.fileIdentifier == 'gov.super.important:FILE-ID'

    and:
    def smeResult = AvroUtils.avroToMap(results[unparsedKey])
    smeResult.containsKey('discovery')
    !smeResult.containsKey('error')
    smeResult.discovery.fileIdentifier == 'dummy-file-identifier'
  }

  def "Unparsable granule produces record with errors"() {
    given:
    def key = 'failure101'
    def value = buildAggInput(
        rawXml: 'it,does,not,parse'
    )

    when:
    driver.pipeInput(inputFactory.create(testChangelog, key, value))

    then:
    // Nothing on sme topic
    driver.readOutput(Topics.toExtractorTopic(testType), STRING_DESERIALIZER, AVRO_DESERIALIZER) == null

    and:
    // An error has appeared
    def record = driver.readOutput(Topics.parsedTopic(testType), STRING_DESERIALIZER, AVRO_DESERIALIZER)
    record.key() == key
    record.value() instanceof ParsedRecord
    record.value().errors.size() == 1
  }

  def "streams app throws exception when transitioning to bad state #state"() {
    def streamsApp = StreamParsalyzer.buildStreamsApp(new AppConfig())

    when:
    streamsApp.setState(state)

    then:
    thrown(IllegalStateException)

    where:
    state << [KafkaStreams.State.NOT_RUNNING, KafkaStreams.State.ERROR]
  }

  private static inputDefaults = [
      type          : RecordType.granule,
      initialSource : 'test'
  ]

  private static AggregatedInput buildAggInput(Map overrides) {
    (inputDefaults + overrides).inject(AggregatedInput.newBuilder(), { b, k, v ->
      b[k] = v
      b
    }).build()
  }
}
