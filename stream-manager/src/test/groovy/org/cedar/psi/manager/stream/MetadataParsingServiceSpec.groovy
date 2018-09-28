package org.cedar.psi.manager.stream

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MetadataParsingServiceSpec extends Specification {

  final String inputSchemaString = ClassLoader.systemClassLoader.getResourceAsStream('input-schema.json').text

  final ObjectMapper mapper = new ObjectMapper()
  final JsonSchemaFactory factory = JsonSchemaFactory.byDefault()

  final JsonNode inputSchemaNode = mapper.readTree(inputSchemaString)
  final JsonSchema inputSchema = factory.getJsonSchema(inputSchemaNode)


  def "ISO metadata in incoming message parsed correctly"() {
    given:
    def xml = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
    def incomingMsg = [
          contentType: 'application/xml',
          content: xml
    ]

    when:
    inputSchema.validate(mapper.readTree(JsonOutput.toJson(incomingMsg)))
    def response = MetadataParsingService.parseToInternalFormat(incomingMsg)

    then:
    !response.containsKey("error")
    response.containsKey("discovery")

    and:
    // Verify a field; in-depth validation in ISOParserSpec
    def parsed = response.discovery
    parsed.fileIdentifier == 'gov.super.important:FILE-ID'
    //internal format should match input schema
    inputSchema.validate(mapper.readTree(JsonOutput.toJson(response)))

  }

  def "#type ISO metadata in incoming message results in error"() {
    given:
    def rawMetadata = metadata
    def incomingMsg = [
            contentType: 'application/xml',
            content: rawMetadata
    ]

    when:
    inputSchema.validate(mapper.readTree(JsonOutput.toJson(incomingMsg)))
    def response = MetadataParsingService.parseToInternalFormat(incomingMsg)

    then:
    response.containsKey("error")
    !response.containsKey("discovery")
    response.error == errorMessage
    inputSchema.validate(mapper.readTree(JsonOutput.toJson(response)))

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
    inputSchema.validate(mapper.readTree(JsonOutput.toJson(incomingMsg)))
    def response = MetadataParsingService.parseToInternalFormat(incomingMsg)

    then:
    response.containsKey("error")
    !response.containsKey("discovery")
    response.error == 'Unknown raw format of metadata'
    inputSchema.validate(mapper.readTree(JsonOutput.toJson(response)))

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
