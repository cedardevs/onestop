package org.cedar.psi.manager.stream

import org.cedar.psi.common.avro.ErrorEvent
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
    !response.containsKey("error")
    response.containsKey("discovery")

    and:
    // Verify a field; in-depth validation in ISOParserSpec
    def parsed = response.discovery
    parsed.fileIdentifier == 'gov.super.important:FILE-ID'
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
    response instanceof ErrorEvent
    response.title == 'No content provided'

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
    response instanceof ErrorEvent
    response.title == 'Unable to parse malformed content'
    response.detail == 'SAXParseException: XML document structures must start and end within the same entity.'
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
    response instanceof ErrorEvent
    response.title == 'Unsupported content type'
    response.detail == "Content type [${incomingMsg.contentType}] is not supported"
  }

}
