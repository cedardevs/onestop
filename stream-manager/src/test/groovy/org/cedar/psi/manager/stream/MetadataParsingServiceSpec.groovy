package org.cedar.psi.manager.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MetadataParsingServiceSpec extends Specification {

  def "ISO metadata in incoming message parsed correctly"() {
    given:
    def xml = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
    def incomingMsg = [
        contentType: 'application/xml',
        content: xml
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
    def rawMetadata = metadata
    def incomingMsg = [
        contentType: 'application/xml',
        content: rawMetadata
    ]

    when:
    def response = MetadataParsingService.parseToInternalFormat(incomingMsg)

    then:
    response.containsKey("error")
    !response.containsKey("discovery")
    response.error == errorMessage

    where:
    type    | metadata  | errorMessage
    'Null'  | null      | 'Malformed data encountered; unable to parse. Root cause: NullPointerException:'
    'Empty' | ''        | 'Malformed XML encountered; unable to parse. Root cause: SAXParseException: Premature end of file.'
  }

  def "Unknown metadata type in incoming message results in error"() {
    given:
    def incomingMsg = [
        contentType: 'Not supported',
        content: "Won't be parsed"
    ]

    when:
    def response = MetadataParsingService.parseToInternalFormat(incomingMsg)

    then:
    response.containsKey("error")
    !response.containsKey("discovery")
    response.error == 'Unknown raw format of metadata'
  }

  def "test stuff"() {
    given:
    def envMap = System.getenv()

    when:
    def output = JsonOutput.prettyPrint(JsonOutput.toJson(envMap))
    println(output)

    then:
    1 == 1
  }
}
