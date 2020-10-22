package org.cedar.onestop.registry

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import io.confluent.kafka.schemaregistry.RestApp
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.streams.KafkaStreams
import org.cedar.onestop.kafka.common.constants.Topics
import org.cedar.schemas.avro.psi.OperationType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@Slf4j
@DirtiesContext
@EmbeddedKafka
@ActiveProfiles('integration')
@SpringBootTest(classes = [MetadataRegistryMain], webEnvironment = RANDOM_PORT)
class RegistryIntegrationSpec extends Specification {

  @Value('${local.server.port}')
  String port

  @Value('${server.servlet.context-path:}')
  String contextPath

  @Autowired
  KafkaStreams streamsApp

  @Autowired
  AdminClient adminClient

  @Autowired
  RestApp schemaRegistryRestApp

  RestTemplate restTemplate
  String baseUrl

  ObjectMapper mapper = new ObjectMapper()
  JsonSchemaFactory factory = JsonSchemaFactory.byDefault()
  String registryResponseSchemaString = ClassLoader.systemClassLoader.getResourceAsStream('jsonSchema/registryResponse-schema.json').text
  JsonNode registryResponseSchemaNode = mapper.readTree(registryResponseSchemaString)
  JsonSchema registryResponseSchema = factory.getJsonSchema(registryResponseSchemaNode)

  def setup() {
    restTemplate = new RestTemplate()
    baseUrl = "http://localhost:${port}/${contextPath}"
  }

  def 'can post then retrieve input info'() {
    def restTemplate = new RestTemplate()
    def granuleText = ClassLoader.systemClassLoader.getResourceAsStream('test_granule.json').text
    def granuleMap = new JsonSlurper().parseText(granuleText) as Map

    when:
    def createEntity = RequestEntity
        .post("${baseUrl}metadata/granule/common-ingest/${granuleMap.trackingId}".toURI())
        .contentType(MediaType.APPLICATION_JSON)
        .body(granuleText)
    def createResponse = restTemplate.exchange(createEntity, Map)
    def granuleId = createResponse.body.id

    then:
    granuleId instanceof String
    createResponse.statusCode == HttpStatus.OK

    when:
    sleep(1000)
    def retrieveEntity = RequestEntity
        .get("${baseUrl}metadata/granule/common-ingest/${granuleId}".toURI())
        .build()
    def retrieveResponse = restTemplate.exchange(retrieveEntity, Map)

    then:
    registryResponseSchema.validate(mapper.readTree(JsonOutput.toJson(retrieveResponse.body)))
    retrieveResponse.statusCode == HttpStatus.OK

    and: // let's verify the full response just this once
    def links = retrieveResponse.body.links
    def data = retrieveResponse.body.data
    links.parsed == "${baseUrl}metadata/granule/common-ingest/${granuleId}/parsed"
    data.id == granuleId
    data.type == 'granule'
    data.attributes.rawJson == JsonOutput.toJson(granuleMap)
    data.attributes.rawXml == null
    data.attributes.initialSource == "common-ingest"
    data.attributes.type == "granule"
    data.attributes.fileInformation instanceof Map
    data.attributes.fileLocations instanceof Map
    data.attributes.publishing == null
    data.attributes.relationships instanceof List
    data.attributes.deleted == false
    data.attributes.events instanceof List
    data.attributes.events.size() == 1
    data.attributes.events[0].timestamp instanceof Long
    data.attributes.events[0].method == "POST"
    data.attributes.events[0].source == "common-ingest"
    data.attributes.events[0].operation == OperationType.NO_OP.name()
    data.attributes.errors == []
  }

  def 'collection that is not yet parsed returns 404'() {
    def restTemplate = new RestTemplate()
    def collectionText = ClassLoader.systemClassLoader.getResourceAsStream('dscovr_fc1.xml').text
    def collectionId = UUID.randomUUID()

    when:
    def createEntity = RequestEntity
        .post("${baseUrl}metadata/collection/${collectionId}".toURI())
        .contentType(MediaType.APPLICATION_XML)
        .body(collectionText)
    def createResponse = restTemplate.exchange(createEntity, Map)

    then:
    createResponse.statusCode == HttpStatus.OK

    when:
    sleep(200)
    def retrieveEntity = RequestEntity
        .get("${baseUrl}metadata/collection/${collectionId}/parsed".toURI())
        .build()
    def retrieveResponse = restTemplate.exchange(retrieveEntity, Map)

    then:
    def e = thrown(HttpClientErrorException)
    def body = new JsonSlurper().parseText(e.responseBodyAsString)

    e.statusCode == HttpStatus.NOT_FOUND
    registryResponseSchema.validate(mapper.readTree(JsonOutput.toJson(body)))
    body.links.input == "${baseUrl}metadata/collection/${Topics.DEFAULT_SOURCE}/${collectionId}"
    body.errors instanceof List
  }

  def 'returns 404 for unsupported type'() {
    def restTemplate = new RestTemplate()
    def collectionText = ClassLoader.systemClassLoader.getResourceAsStream('dscovr_fc1.xml').text

    when:
    def createEntity = RequestEntity
        .post("${baseUrl}metadata/not-a-real-type/".toURI())
        .contentType(MediaType.APPLICATION_XML)
        .body(collectionText)
    def createResponse = restTemplate.exchange(createEntity, Map)

    then:
    def exception = thrown(HttpClientErrorException)
    exception.statusCode.value() == 404
    def body = new JsonSlurper().parse(exception.responseBodyAsByteArray) as Map
    body.errors instanceof List
    body.errors[0] instanceof Map
    body.errors[0].title instanceof CharSequence
  }

}
