package org.cedar.psi.manager.stream

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.Method
import org.cedar.psi.common.constants.StreamsApps
import org.cedar.psi.common.constants.Topics
import org.cedar.psi.common.serde.JsonSerdes
import org.cedar.psi.common.util.MockSchemaRegistrySerde
import org.cedar.psi.manager.config.ManagerConfig
import spock.lang.Specification

import java.time.ZoneOffset
import java.time.temporal.ChronoUnit


class StreamManagerSpec extends Specification {

  def DESERIALIZER = Serdes.String().deserializer()

  def streamsConfig = StreamManager.streamsConfig(StreamsApps.MANAGER_ID, new ManagerConfig()).with {
    it.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, MockSchemaRegistrySerde.class.name)
    it
  }
  def topology = StreamManager.buildTopology()
  def driver = new TopologyTestDriver(topology, streamsConfig)
  def inputFactory = new ConsumerRecordFactory(Serdes.String().serializer(), new MockSchemaRegistrySerde().serializer())
  def jsonFactory = new ConsumerRecordFactory(Serdes.String().serializer(), JsonSerdes.Map().serializer())

  def testType = 'granule'
  def testSource = Topics.DEFAULT_SOURCE
  def testChangelog = Topics.inputChangelogTopic(StreamsApps.REGISTRY_ID, testType, testSource)

  final String analysisSchemaString = ClassLoader.systemClassLoader.getResourceAsStream('analysis-schema.json').text
  final String discoverySchemaString = ClassLoader.systemClassLoader.getResourceAsStream('discovery-schema.json').text
  final String identifierSchemaString = ClassLoader.systemClassLoader.getResourceAsStream('identifiers-schema.json').text
  final String inputSchemaString = ClassLoader.systemClassLoader.getResourceAsStream('input-schema.json').text
  final String registryResponseSchemaString = ClassLoader.systemClassLoader.getResourceAsStream('registryResponse-schema.json').text

  final ObjectMapper mapper = new ObjectMapper()
  final JsonSchemaFactory factory = JsonSchemaFactory.byDefault()

  final JsonNode analysisSchemaNode = mapper.readTree(analysisSchemaString)
  final JsonNode discoverySchemaNode = mapper.readTree(discoverySchemaString)
  final JsonNode identifierSchemaNode = mapper.readTree(identifierSchemaString)
  final JsonNode inputSchemaNode = mapper.readTree(inputSchemaString)
  final JsonNode registryResponseSchemaNode = mapper.readTree(registryResponseSchemaString)

  final JsonSchema analysisSchema = factory.getJsonSchema(analysisSchemaNode)
  final JsonSchema discoverySchema = factory.getJsonSchema(discoverySchemaNode)
  final JsonSchema identifierSchema = factory.getJsonSchema(identifierSchemaNode)
  final JsonSchema inputSchema = factory.getJsonSchema(inputSchemaNode)
  final JsonSchema registryResponseSchema = factory.getJsonSchema(registryResponseSchemaNode)

  def cleanup() {
    driver.close()
  }

  def "Non-SME ISO granule parsed as expected"() {
    given:
    def xml = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
    def key = 'A123'
    def input = buildInput(contentType: 'application/xml', content: xml)

    when:
//    inputSchema.validate(mapper.readTree(JsonOutput.toJson(input)))
    driver.pipeInput(inputFactory.create(testChangelog, key, input))

    then:
    // Not found in error or SME topics
    driver.readOutput(Topics.errorTopic(), DESERIALIZER, DESERIALIZER) == null
    driver.readOutput(Topics.smeTopic('granule'), DESERIALIZER, DESERIALIZER) == null

    and:
    // There is only 1 record in the PARSED_TOPIC
    def finalOutput = driver.readOutput(Topics.parsedTopic('granule'), DESERIALIZER, DESERIALIZER)
    finalOutput != null
    driver.readOutput(Topics.parsedTopic('granule'), DESERIALIZER, DESERIALIZER) == null

    and:
    // Verify some fields
    finalOutput.key() == key
    def output = new JsonSlurper().parseText(finalOutput.value()) as Map
    !output.containsKey('error')
    output.containsKey('discovery')
    output.discovery.fileIdentifier == 'gov.super.important:FILE-ID'
    output.containsKey('analysis')
    output.analysis == [
        identification  : [
            fileIdentifier    : [
                exists              : true,
                fileIdentifierString: 'gov.super.important:FILE-ID'
            ],
            doi               : [
                exists   : true,
                doiString: 'doi:10.5072/FK2TEST'

            ],
            parentIdentifier  : [
                exists                : true,
                parentIdentifierString: 'gov.super.important:PARENT-ID'
            ],
            hierarchyLevelName: [
                exists            : true,
                matchesIdentifiers: true
            ]
        ],
        temporalBounding: [
            begin  : [
                exists           : true,
                precision        : ChronoUnit.NANOS.toString(),
                validSearchFormat: true,
                zoneSpecified    : ZoneOffset.UTC.toString(),
                utcDateTimeString: '2005-05-09T00:00:00Z'
            ],
            end    : [
                exists           : true,
                precision        : ChronoUnit.DAYS.toString(),
                validSearchFormat: true,
                zoneSpecified    : 'UNDEFINED',
                utcDateTimeString: '2010-10-01T23:59:59Z'
            ],
            instant: [
                exists           : false,
                precision        : 'UNDEFINED',
                validSearchFormat: 'UNDEFINED',
                zoneSpecified    : 'UNDEFINED',
                utcDateTimeString: 'UNDEFINED'
            ],
            range  : [
                descriptor : 'BOUNDED',
                beginLTEEnd: true
            ]
        ],
        spatialBounding : [
            exists: true
        ],
        titles          : [
            title         : [
                exists    : true,
                characters: 63
            ],
            alternateTitle: [
                exists    : true,
                characters: 51
            ]
        ],
        description     : [
            exists    : true,
            characters: 65
        ],
        thumbnail       : [
            exists: true,
        ],
        dataAccess      : [
            exists: true
        ]
    ]
    //validate against schema
    analysisSchema.validate(mapper.readTree(JsonOutput.toJson(output.analysis)))
    discoverySchema.validate(mapper.readTree(JsonOutput.toJson(output.discovery)))
    identifierSchema.validate(mapper.readTree(JsonOutput.toJson(output.discovery.identifiers)))
    //this one effectively does all the others, but it is easier to debug when we do one at a time
    registryResponseSchema.validate(mapper.readTree(JsonOutput.toJson(output)))

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
    def smeOutput = driver.readOutput(Topics.smeTopic('granule'), DESERIALIZER, DESERIALIZER)
    smeOutput.key() == smeKey
    smeOutput.value() == smeValue.content

    and:
    // There are no errors and nothing in the parsed topic
    driver.readOutput(Topics.parsedTopic('granule'), DESERIALIZER, DESERIALIZER) == null
    driver.readOutput(Topics.errorTopic(), DESERIALIZER, DESERIALIZER) == null
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
    inputSchema.validate(mapper.readTree(JsonOutput.toJson(xmlSME)))
    inputSchema.validate(mapper.readTree(JsonOutput.toJson(xmlNonSME)))
    // Simulate SME ending up in unparsed-granule since that's another app's responsibility
    driver.pipeInput(inputFactory.create(testChangelog, nonSMEInputKey, nonSMEInputValue))
    driver.pipeInput(jsonFactory.create(Topics.unparsedTopic('granule'), unparsedKey, unparsedValue))

    then:
    // Both records are in the parsed topic
    def results = [:]
    2.times {
      def record = driver.readOutput(Topics.parsedStore('granule'), DESERIALIZER, DESERIALIZER)
      results[record.key()] = record.value()
    }
    results.containsKey(nonSMEInputKey)
    results.containsKey(unparsedKey)

    // Verify some parsed fields:
    and:
    def nonSMEResult = new JsonSlurper().parseText(results[nonSMEInputKey] as String) as Map
    nonSMEResult.containsKey('discovery')
    !nonSMEResult.containsKey('error')
    nonSMEResult.discovery.fileIdentifier == 'gov.super.important:FILE-ID'

    and:
    def smeResult = new JsonSlurper().parseText(results[unparsedKey] as String) as Map
    smeResult.containsKey('discovery')
    !smeResult.containsKey('error')
    smeResult.discovery.fileIdentifier == 'dummy-file-identifier'

    and:
    // No errors
    driver.readOutput(Topics.errorTopic(), DESERIALIZER, DESERIALIZER) == null
  }

  def "Unparsable granule ends up on error-granule topic"() {
    given:
    def key = 'failure101'
    def value = buildInput(
        contentType: 'text/csv',
        content: 'it,does,not,parse'
    )

    when:
    driver.pipeInput(inputFactory.create(testChangelog, key, value))

    then:
    // Nothing in the parsed or sme topics
    driver.readOutput(Topics.parsedTopic('granule'), DESERIALIZER, DESERIALIZER) == null
    driver.readOutput(Topics.smeTopic('granule'), DESERIALIZER, DESERIALIZER) == null

    and:
    // An error has appeared
    def error = driver.readOutput(Topics.errorTopic(), DESERIALIZER, DESERIALIZER)
    error.key() == key
    error.value() == JsonOutput.toJson([
        error: 'Unknown raw format of metadata'
    ])
  }

  private static inputDefaults = [
      source    : 'test',
      method    : Method.POST,
      protocol  : 'http',
      host      : 'localhost',
      requestUrl: '/test'
  ]

  private static buildInput(Map values) {
    new Input(inputDefaults + values)
  }
}
