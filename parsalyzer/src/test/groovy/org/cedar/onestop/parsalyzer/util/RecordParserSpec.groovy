package org.cedar.onestop.parsalyzer.util

import groovy.json.JsonOutput
import org.cedar.schemas.avro.psi.*
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class RecordParserSpec extends Specification {

  static final String isoXml = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
  static final String isoXmlFileId = 'gov.super.important:FILE-ID'

  static final String jsonFileId = 'TEST_ID'
  static final String jsonFileName = 'TEST_NAME'
  static final Map testRecordData = [
      discovery: [
          fileIdentifier: jsonFileId
      ],
      fileInformation: [
          name: jsonFileName
      ]
  ]
  static final String testRecordJson = JsonOutput.toJson(testRecordData)


  def "handles AggregatedInput with #testcase content"() {
    def input = AggregatedInput.newBuilder()
        .setType(RecordType.collection)
        .setDeleted(false)
        .setRawXml(xml)
        .setRawJson(json)
        .build()

    when:
    def result = RecordParser.parseInput(input)

    then:
    result instanceof ParsedRecord
    result?.discovery?.fileIdentifier == expectedId
    result?.fileInformation?.name == expectedName

    where:
    testcase            | xml   | json            || expectedId   | expectedName
    "both xml and json" | isoXml| testRecordJson  || jsonFileId   | jsonFileName
    "only xml"          | isoXml| null            || isoXmlFileId | null
    "only json"         | null  | testRecordJson  || jsonFileId   | jsonFileName
  }

  def "AggregatedInput with no content results in a error"() {
    def input = AggregatedInput.newBuilder().build()

    when:
    def result = RecordParser.parseInput(input)

    then:
    result instanceof ParsedRecord
    result.errors instanceof List
    result.errors.size() == 1
    result.errors[0].title == "No content provided"
  }

  def "AggregatedInput which is #testcase results in a tombstone"() {
    expect:
    RecordParser.parseInput(input) == null

    where:
    testcase  | input
    "null"    | null
    "deleted" | AggregatedInput.newBuilder().setDeleted(true).build()
    "in error"| AggregatedInput.newBuilder().setErrors([ErrorEvent.newBuilder().build()]).build()
  }

  def "A raw iso xml string is parsed correctly"() {
    when:
    def result = RecordParser.parseRaw(isoXml, RecordType.collection)

    then:
    result instanceof ParsedRecord
    result.discovery.fileIdentifier == isoXmlFileId
  }

  def "ParsedRecord-shaped json is parsed correctly"() {
    when:
    def result = RecordParser.parseRaw(testRecordJson, RecordType.collection)

    then:
    result instanceof ParsedRecord
    result.discovery.fileIdentifier == jsonFileId
    result.fileInformation.name == jsonFileName
  }

  def "Parsing #testcase string input results in a record with errors"() {
    when:
    def result = RecordParser.parseRaw(input, RecordType.collection)

    then:
    result instanceof ParsedRecord
    result.errors instanceof List
    result.errors.size() == 1
    result.errors[0].title instanceof String
    result.errors[0].detail instanceof String

    where:
    testcase    | input
    "null"      | null
    "empty"     | ''
    "malformed" | 'lorem ipsum'
  }

  def "Input-shaped json with iso xml content is parsed correctly"() {
    given:
    def incomingMsg = JsonOutput.toJson([
        contentType: 'application/xml',
        content    : isoXml
    ])

    when:
    def response = RecordParser.parseRaw(incomingMsg, RecordType.collection)

    then:
    response instanceof ParsedRecord
    response.errors.size() == 0
    response.discovery instanceof Discovery

    and:
    // Verify a field; in-depth validation in ISOParserSpec
    response.discovery.fileIdentifier == isoXmlFileId
  }

  def "Input-shaped json with ParsedRecord-shaped json content is parsed correctly"() {
    given:
    def incomingMsg = JsonOutput.toJson([
        contentType: 'application/json',
        content    : testRecordJson
    ])

    when:
    def response = RecordParser.parseRaw(incomingMsg, RecordType.collection)

    then:
    response instanceof ParsedRecord
    response.errors.size() == 0
    response.discovery instanceof Discovery
    response.discovery.fileIdentifier == jsonFileId
    response.fileInformation.name == jsonFileName
  }

  def "Input-shaped json with #testcase content results in a record with an error"() {
    given:
    def incomingMsg = JsonOutput.toJson([
        contentType: 'application/xml',
        content    : metadata
    ])

    when:
    def response = RecordParser.parseRaw(incomingMsg, RecordType.collection)

    then:
    response instanceof ParsedRecord
    response.errors.size() == 1
    response.errors[0].title == 'No content provided'

    where:
    testcase| metadata
    'Null'  | null
    'Empty' | ''
  }

  def "Input-shaped json with malformed xml results in a record with an error"() {
    given:
    def incomingMsg = JsonOutput.toJson([
        contentType: 'application/xml',
        content    : '<xml>oh no look out for the'
    ])

    when:
    def response = RecordParser.parseRaw(incomingMsg, RecordType.collection)

    then:
    response instanceof ParsedRecord
    response.errors.size() == 1
    response.errors[0].title == 'Unable to parse malformed xml'
    response.errors[0].detail == 'SAXParseException: XML document structures must start and end within the same entity.'
  }

  def "Input-shaped json with unknown metadata content type results in a record with an error"() {
    given:
    def incomingMsg = JsonOutput.toJson([
        contentType: 'Not supported',
        content    : "Won't be parsed"
    ])

    when:
    def response = RecordParser.parseRaw(incomingMsg, RecordType.collection)

    then:
    response instanceof ParsedRecord
    response.errors.size() == 1
    response.errors[0].title == 'Unable to parse input'
    response.errors[0].detail == "Input content does not appear to be either xml or json"
  }

  def 'parses publishing info'() {
    when:
    def json = JsonOutput.toJson([publishing: input])
    def result = RecordParser.parseRaw(json, RecordType.collection)

    then:
    result instanceof ParsedRecord
    result.publishing instanceof Publishing
    result.publishing.isPrivate == isPrivate
    result.publishing.until == until

    where:
    input                        | isPrivate | until
    [:]                          | false     | null
    [isPrivate: true]            | true      | null
    [until: 42]                  | false     | 42
    [isPrivate: true, until: 42] | true      | 42
  }

}
