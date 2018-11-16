package org.cedar.psi.manager.stream

import org.cedar.psi.common.avro.Discovery
import org.cedar.psi.common.avro.ParsedRecord
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MetadataParsingServiceSpec extends Specification {

  def "ISO metadata in incoming message parsed correctly"() {
    given:
    def xml = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
    def incomingMsg = [
        contentType: 'application/xml',
        content    : xml
    ]

    when:
    def response = MetadataParsingService.parseToInternalFormat(incomingMsg)

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
    def response = MetadataParsingService.parseToInternalFormat(incomingMsg)

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
    def response = MetadataParsingService.parseToInternalFormat(incomingMsg)

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
    def response = MetadataParsingService.parseToInternalFormat(incomingMsg)

    then:
    response instanceof ParsedRecord
    response.errors.size() == 1
    response.errors[0].title == 'Unsupported content type'
    response.errors[0].detail == "Content type [${incomingMsg.contentType}] is not supported"
  }

}
