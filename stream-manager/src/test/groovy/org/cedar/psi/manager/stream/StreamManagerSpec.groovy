package org.cedar.psi.manager.stream

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.cedar.psi.manager.config.Constants
import org.cedar.psi.common.constants.Topics
import org.cedar.psi.common.serde.JsonSerdes
import spock.lang.Specification

class StreamManagerSpec extends Specification {

  def DESERIALIZER = Serdes.String().deserializer()

  def streamsConfig = StreamManager.streamsConfig(Constants.APP_ID, Constants.BOOTSTRAP_DEFAULT)
  def topology = StreamManager.buildTopology()
  def driver = new TopologyTestDriver(topology, streamsConfig)
  def consumerFactory = new ConsumerRecordFactory(Topics.RAW_GRANULE_CHANGELOG_TOPIC,
      Serdes.String().serializer(), JsonSerdes.Map().serializer())

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

  def cleanup(){
    driver.close()
  }

  def "Non-SME ISO granule parsed as expected"() {
    given:
    def xml = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
    def key = 'A123'
    def value = [
        contentType: 'application/xml',
        content: xml
    ]

    when:
    inputSchema.validate(mapper.readTree(JsonOutput.toJson(value)))
    driver.pipeInput(consumerFactory.create(Topics.RAW_GRANULE_CHANGELOG_TOPIC, key, value))

    then:
    // Not found in error or SME topics
    driver.readOutput(Topics.ERROR_HANDLER_TOPIC, DESERIALIZER, DESERIALIZER) == null
    driver.readOutput(Topics.SME_GRANULE_TOPIC, DESERIALIZER, DESERIALIZER) == null

    and:
    // There is only 1 record in the PARSED_TOPIC
    def finalOutput = driver.readOutput(Topics.PARSED_GRANULE_TOPIC, DESERIALIZER, DESERIALIZER)
    finalOutput != null
    driver.readOutput(Topics.PARSED_GRANULE_TOPIC, DESERIALIZER, DESERIALIZER) == null

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
            fileIdentifier  : [
                exists: true,
                fileIdentifierString: 'gov.super.important:FILE-ID'
            ],
            doi             : [
                exists: true,
                doiString: 'doi:10.5072/FK2TEST'

            ],
            parentIdentifier: [
                exists: true,
                parentIdentifierString: 'gov.super.important:PARENT-ID'
            ],
            hierarchyLevelName: [
                exists: true,
                matchesIdentifiers: true
            ]
        ],
        temporalBounding: [
            beginDate: [
                exists: true,
                valid : true
            ],
            endDate  : [
                exists: true,
                valid : true
            ],
            instant  : [
                exists: false,
                valid : true
            ]
        ],
        spatialBounding : [
            exists: true
        ],
        titles          : [
            title: [
                exists: true,
                characters: 63
            ],
            alternateTitle: [
                exists: true,
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
    def smeValue = [
        source: 'common-ingest',
        contentType: 'application/xml',
        content: xmlSME
    ]

    when:
    inputSchema.validate(mapper.readTree(JsonOutput.toJson(smeValue)))
    driver.pipeInput(consumerFactory.create(Topics.RAW_GRANULE_CHANGELOG_TOPIC, smeKey, smeValue))

    then:
    // The record is in the SME topic
    def smeOutput = driver.readOutput(Topics.SME_GRANULE_TOPIC, DESERIALIZER, DESERIALIZER)
    smeOutput.key() == smeKey
    smeOutput.value() == smeValue.content

    and:
    // There are no errors and nothing in the parsed topic
    driver.readOutput(Topics.PARSED_GRANULE_TOPIC, DESERIALIZER, DESERIALIZER) == null
    driver.readOutput(Topics.ERROR_HANDLER_TOPIC, DESERIALIZER, DESERIALIZER) == null

  }

  def "Non-SME granule and SME granule end up in parsed-granule topic"() {
    given:
    def xmlNonSME = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
    def xmlSME = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-sme-dummy.xml").text
    def nonSMEKey = 'notSME'
    def nonSMEValue = [
        source: null,
        contentType: 'application/xml',
        content: xmlNonSME
    ]
    def smeKey = 'sme'
    def smeValue = [
        source: 'common-ingest',
        contentType: 'application/xml',
        content: xmlSME
    ]

    when:
    inputSchema.validate(mapper.readTree(JsonOutput.toJson(smeValue)))
    inputSchema.validate(mapper.readTree(JsonOutput.toJson(nonSMEValue)))
    // Simulate SME ending up in unparsed-granule since that's another app's responsibility
    driver.pipeInput(consumerFactory.create(Topics.RAW_GRANULE_CHANGELOG_TOPIC, nonSMEKey, nonSMEValue))
    driver.pipeInput(consumerFactory.create(Topics.UNPARSED_GRANULE_TOPIC, smeKey, smeValue))

    then:
    // Both records are in the parsed topic
    def results = [:]
    2.times {
      def record = driver.readOutput(Topics.PARSED_GRANULE_STORE, DESERIALIZER, DESERIALIZER)
      results[record.key()] = record.value()
    }
    results.containsKey(nonSMEKey)
    results.containsKey(smeKey)

    // Verify some parsed fields:
    and:
    def nonSMEResult = new JsonSlurper().parseText(results[nonSMEKey] as String) as Map
    nonSMEResult.containsKey('discovery')
    !nonSMEResult.containsKey('error')
    nonSMEResult.discovery.fileIdentifier == 'gov.super.important:FILE-ID'
    analysisSchema.validate(mapper.readTree(JsonOutput.toJson(nonSMEResult)))

    and:
    def smeResult = new JsonSlurper().parseText(results[smeKey] as String) as Map
    smeResult.containsKey('discovery')
    !smeResult.containsKey('error')
    smeResult.discovery.fileIdentifier == 'dummy-file-identifier'
    analysisSchema.validate(mapper.readTree(JsonOutput.toJson(smeResult)))

    and:
    // No errors
    driver.readOutput(Topics.ERROR_HANDLER_TOPIC, DESERIALIZER, DESERIALIZER) == null
  }

  def "Unparsable granule ends up on error-granule topic"() {
    given:
    def key = 'failure101'
    def value = [
        source: null,
        contentType: 'text/csv',
        content: 'it,does,not,parse'
    ]

    when:
    inputSchema.validate(mapper.readTree(JsonOutput.toJson(value)))
    driver.pipeInput(consumerFactory.create(Topics.RAW_GRANULE_CHANGELOG_TOPIC, key, value))

    then:
    // Nothing in the parsed or sme topics
    driver.readOutput(Topics.PARSED_GRANULE_TOPIC, DESERIALIZER, DESERIALIZER) == null
    driver.readOutput(Topics.SME_GRANULE_TOPIC, DESERIALIZER, DESERIALIZER) == null

    and:
    // An error has appeared
    def error = driver.readOutput(Topics.ERROR_HANDLER_TOPIC, DESERIALIZER, DESERIALIZER)
    error.key() == key
    error.value() == JsonOutput.toJson([
        error: 'Unknown raw format of metadata'
    ])
  }
}
