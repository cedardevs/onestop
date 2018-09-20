package org.cedar.psi.manager.stream

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

    and:
    def smeResult = new JsonSlurper().parseText(results[smeKey] as String) as Map
    smeResult.containsKey('discovery')
    !smeResult.containsKey('error')
    smeResult.discovery.fileIdentifier == 'dummy-file-identifier'

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
