package org.cedar.psi.manager.util

import groovy.json.JsonOutput
import org.cedar.schemas.avro.psi.Discovery
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.Publishing
import org.cedar.schemas.avro.psi.RecordType
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class RecordParserSpec extends Specification {

  def "ISO metadata in incoming message parsed correctly"() {
    given:
    def xml = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
    def incomingMsg = JsonOutput.toJson([
        contentType: 'application/xml',
        content    : xml
    ])

    when:
    def response = RecordParser.parseRaw(incomingMsg, RecordType.collection)

    then:
    response instanceof ParsedRecord
    response.errors.size() == 0
    response.discovery instanceof Discovery

    and:
    // Verify a field; in-depth validation in ISOParserSpec
    response.discovery.fileIdentifier == 'gov.super.important:FILE-ID'
  }

  def "#type ISO metadata in incoming message results in error"() {
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
    type    | metadata
    'Null'  | null
    'Empty' | ''
  }

  def "malformed xml metadata in incoming message results in error"() {
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

  def "Unknown metadata type in incoming message results in error"() {
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
