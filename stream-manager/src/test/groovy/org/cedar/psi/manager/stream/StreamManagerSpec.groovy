package org.cedar.psi.manager.stream

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.cedar.psi.common.avro.Analysis
import org.cedar.psi.common.avro.Discovery
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.Method
import org.cedar.psi.common.avro.ParsedRecord
import org.cedar.psi.common.avro.RecordType
import org.cedar.psi.common.constants.StreamsApps
import org.cedar.psi.common.constants.Topics
import org.cedar.psi.common.serde.JsonSerdes
import org.cedar.psi.common.util.AvroUtils
import org.cedar.psi.common.util.MockSchemaRegistrySerde
import org.cedar.psi.common.util.StreamSpecUtils
import org.cedar.psi.manager.config.ManagerConfig
import spock.lang.Specification

class StreamManagerSpec extends Specification {

  def STRING_DESERIALIZER = Serdes.String().deserializer()
  def AVRO_DESERIALIZER = new MockSchemaRegistrySerde().deserializer()

  def streamsConfig = StreamManager.streamsConfig(StreamsApps.MANAGER_ID, new ManagerConfig()).with {
    it.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, MockSchemaRegistrySerde.class.name)
    it
  }
  def topology = StreamManager.buildTopology()
  def driver = new TopologyTestDriver(topology, streamsConfig)
  def inputFactory = new ConsumerRecordFactory(Serdes.String().serializer(), new MockSchemaRegistrySerde().serializer())
  def jsonFactory = new ConsumerRecordFactory(Serdes.String().serializer(), JsonSerdes.Map().serializer())

  def testType = RecordType.granule
  def testSource = Topics.DEFAULT_SOURCE
  def testChangelog = Topics.inputChangelogTopic(StreamsApps.REGISTRY_ID, testType, testSource)

  def cleanup() {
    driver.close()
  }

  def "Non-SME ISO granule parsed as expected"() {
    given:
    def xml = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
    def key = 'A123'
    def input = buildInput(contentType: 'application/xml', content: xml)

    when:
    driver.pipeInput(inputFactory.create(testChangelog, key, input))

    then:
    // Not found in SME topic
    driver.readOutput(Topics.toExtractorTopic(testType), STRING_DESERIALIZER, AVRO_DESERIALIZER) == null

    and:
    // There is only 1 record in the PARSED_TOPIC
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

  def "SME granule ends up in SME topic"() {
    given:
    def xmlSME = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-sme-dummy.xml").text
    def smeKey = 'sme'
    def smeValue = buildInput(
        source: 'common-ingest',
        contentType: 'application/xml',
        content: xmlSME
    )

    when:
    driver.pipeInput(inputFactory.create(testChangelog, smeKey, smeValue))

    then:
    // The record is in the SME topic
    def smeOutput = driver.readOutput(Topics.toExtractorTopic(testType), STRING_DESERIALIZER, STRING_DESERIALIZER)
    smeOutput.key() == smeKey
    smeOutput.value() == smeValue.content

    and:
    // Nothing in the parsed topic
    driver.readOutput(Topics.parsedTopic(testType), STRING_DESERIALIZER, STRING_DESERIALIZER) == null
  }

  def "Non-SME granule and SME granule end up in parsed-granule topic"() {
    given:
    def xmlNonSME = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
    def xmlSME = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-sme-dummy.xml").text
    def nonSMEInputKey = 'notSME'
    def nonSMEInputValue = buildInput(
        contentType: 'application/xml',
        content: xmlNonSME
    )
    def unparsedKey = 'sme'
    def unparsedValue = [
        source     : 'common-ingest',
        contentType: 'application/xml',
        content    : xmlSME
    ]

    when:
    // Simulate SME ending up in unparsed-granule since that's another app's responsibility
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
    def value = buildInput(
        contentType: 'text/csv',
        content: 'it,does,not,parse'
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

  private static inputDefaults = [
      type      : RecordType.granule,
      source    : 'test',
      method    : Method.POST,
      protocol  : 'http',
      host      : 'localhost',
      requestUrl: '/test'
  ]

  private static buildInput(Map overrides) {
    (inputDefaults + overrides).inject(Input.newBuilder(), { b, k, v ->
      b[k] = v
      b
    }).build()
  }
}
