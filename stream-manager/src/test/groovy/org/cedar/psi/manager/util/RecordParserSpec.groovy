package org.cedar.psi.manager.util

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
    def incomingMsg = [
        contentType: 'application/xml',
        content    : xml
    ]

    when:
    def response = RecordParser.parse(incomingMsg, RecordType.collection)

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
    def incomingMsg = [
        contentType: 'application/xml',
        content    : metadata
    ]

    when:
    def response = RecordParser.parse(incomingMsg, RecordType.collection)

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
    def incomingMsg = [
        contentType: 'application/xml',
        content    : '<xml>oh no look out for the'
    ]

    when:
    def response = RecordParser.parse(incomingMsg, RecordType.collection)

    then:
    response instanceof ParsedRecord
    response.errors.size() == 1
    response.errors[0].title == 'Unable to parse malformed content'
    response.errors[0].detail == 'SAXParseException: XML document structures must start and end within the same entity.'
  }

  def "Unknown metadata type in incoming message results in error"() {
    given:
    def incomingMsg = [
        contentType: 'Not supported',
        content    : "Won't be parsed"
    ]

    when:
    def response = RecordParser.parse(incomingMsg, RecordType.collection)

    then:
    response instanceof ParsedRecord
    response.errors.size() == 1
    response.errors[0].title == 'Unsupported content type'
    response.errors[0].detail == "Content type [${incomingMsg.contentType}] is not supported"
  }

  def 'parses publishing info'() {
    when:
    def result = RecordParser.parsePublishing(input)

    then:
    result instanceof Publishing
    result.isPrivate == isPrivate
    result.until == until

    where:
    input                        | isPrivate | until
    [:]                          | false     | null
    [isPrivate: true]            | true      | null
    [until: 42]                  | false     | 42
    [isPrivate: true, until: 42] | true      | 42
  }

}
