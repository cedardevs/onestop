package org.cedar.psi.registry

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
import org.cedar.psi.common.constants.Topics
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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

  @Configuration
  static class IntegrationConfig {
    @Value('${spring.embedded.zookeeper.connect}')
    String zkConnect

    @Bean(initMethod = 'start')
    RestApp schemaRegistryRestApp() {
      new RestApp(8081, zkConnect, '_schemas')
    }
  }

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

  final ObjectMapper mapper = new ObjectMapper()
  final JsonSchemaFactory factory = JsonSchemaFactory.byDefault()
  final String registryResponseSchemaString = ClassLoader.systemClassLoader.getResourceAsStream('registryResponse-schema.json').text
  final JsonNode registryResponseSchemaNode = mapper.readTree(registryResponseSchemaString)
  final JsonSchema registryResponseSchema = factory.getJsonSchema(registryResponseSchemaNode)

  def setup() {
    restTemplate = new RestTemplate()
    baseUrl = "http://localhost:${port}/${contextPath}"
  }


//  @spock.lang.Ignore
  def 'can post then retrieve granule info with source identifier'() {
    def restTemplate = new RestTemplate()
    def granuleText = ClassLoader.systemClassLoader.getResourceAsStream('test_granule.json').text

    def granuleMap = new JsonSlurper().parseText(granuleText) as Map

    when:
    def createEntity = RequestEntity
        .post("${baseUrl}/metadata/granule/common-ingest/${granuleMap.trackingId}".toURI())
        .contentType(MediaType.APPLICATION_JSON)
        .body(granuleText)
    def createResponse = restTemplate.exchange(createEntity, Map)
    def granuleId = createResponse.body.id

    then:
    granuleId instanceof String
    createResponse.statusCode == HttpStatus.OK

    when:
    sleep(200)
    def retrieveEntity = RequestEntity
        .get("${baseUrl}/metadata/granule/common-ingest/${granuleId}".toURI())
        .build()
    def retrieveResponse = restTemplate.exchange(retrieveEntity, Map)

    then:
    registryResponseSchema.validate(mapper.readTree(JsonOutput.toJson(retrieveResponse.body)))
    retrieveResponse.statusCode == HttpStatus.OK
    retrieveResponse.body.data.id == granuleId
    retrieveResponse.body.data.type == 'granule'
    retrieveResponse.body.data.attributes.input.content == granuleText
    retrieveResponse.body.data.attributes.input.contentType == "application/json"
    retrieveResponse.body.data.attributes.input.source == "common-ingest"

    and: // let's verify the full response just this once
    retrieveResponse.body.data == [
        id: granuleId,
        type: 'granule',
        attributes: [
            input: [
                "content": granuleText,
                "contentType": "application/json",
                "host": "127.0.0.1",
                "method": "POST",
                "protocol": "HTTP/1.1",
                "requestUrl": "${baseUrl}/metadata/granule/common-ingest/${granuleMap.trackingId}",
                "source": "common-ingest",
                "type": "granule"
            ]
        ]
    ]
  }

  def 'can post then retrieve granule iso with an existing key'() {
    def restTemplate = new RestTemplate()
    def granuleText = ClassLoader.systemClassLoader.getResourceAsStream('dscovr_fc1.xml').text
    def granuleId = UUID.randomUUID()


    when:
    def createEntity = RequestEntity
        .post("${baseUrl}/metadata/granule/${granuleId}".toURI())
        .contentType(MediaType.APPLICATION_XML)
        .body(granuleText)
    def createResponse = restTemplate.exchange(createEntity, Map)

    then:
    createResponse.statusCode == HttpStatus.OK

    when:
    sleep(200)
    def retrieveEntity = RequestEntity
        .get("${baseUrl}/metadata/granule/${granuleId}".toURI())
        .build()
    def retrieveResponse = restTemplate.exchange(retrieveEntity, Map)

    then:
    registryResponseSchema.validate(mapper.readTree(JsonOutput.toJson(retrieveResponse.body)))
    retrieveResponse.statusCode == HttpStatus.OK
    retrieveResponse.body.data.id == granuleId  as String
    retrieveResponse.body.data.type == 'granule'
    retrieveResponse.body.data.attributes.input.content == granuleText
    retrieveResponse.body.data.attributes.input.contentType == "application/xml"
    retrieveResponse.body.data.attributes.input.source == Topics.DEFAULT_SOURCE
  }

  def 'can post then retrieve collection info with an existing key'() {
    def restTemplate = new RestTemplate()
    def collectionText = ClassLoader.systemClassLoader.getResourceAsStream('dscovr_fc1.xml').text
    def collectionId = UUID.randomUUID()

    when:
    def createEntity = RequestEntity
        .post("${baseUrl}/metadata/collection/${collectionId}".toURI())
        .contentType(MediaType.APPLICATION_XML)
        .body(collectionText)
    def createResponse = restTemplate.exchange(createEntity, Map)

    then:
    createResponse.statusCode == HttpStatus.OK

    when:
    sleep(200)
    def retrieveEntity = RequestEntity
        .get("${baseUrl}/metadata/collection/${collectionId}".toURI())
        .build()
    def retrieveResponse = restTemplate.exchange(retrieveEntity, Map)

    then:
    registryResponseSchema.validate(mapper.readTree(JsonOutput.toJson(retrieveResponse.body)))
    retrieveResponse.statusCode == HttpStatus.OK
    retrieveResponse.body.data.id == collectionId as String
    retrieveResponse.body.data.type == 'collection'
    retrieveResponse.body.data.attributes.input.content == collectionText
    retrieveResponse.body.data.attributes.input.contentType == "application/xml"
    retrieveResponse.body.data.attributes.input.source == Topics.DEFAULT_SOURCE
  }

  def 'can post then retrieve collection info with no key'() {
    def restTemplate = new RestTemplate()
    def collectionText = ClassLoader.systemClassLoader.getResourceAsStream('dscovr_fc1.xml').text

    when:
    def createEntity = RequestEntity
        .post("${baseUrl}/metadata/collection/".toURI())
        .contentType(MediaType.APPLICATION_XML)
        .body(collectionText)
    def createResponse = restTemplate.exchange(createEntity, Map)
    def collectionId = createResponse.body.id

    then:
    collectionId instanceof String
    createResponse.statusCode == HttpStatus.OK

    when:
    sleep(200)
    def retrieveEntity = RequestEntity
        .get("${baseUrl}/metadata/collection/${collectionId}".toURI())
        .build()
    def retrieveResponse = restTemplate.exchange(retrieveEntity, Map)

    then:
    retrieveResponse.statusCode == HttpStatus.OK
    retrieveResponse.body.data.id == collectionId as String
    retrieveResponse.body.data.type == 'collection'
    retrieveResponse.body.data.attributes.input.content == collectionText
    retrieveResponse.body.data.attributes.input.contentType == "application/xml"
    retrieveResponse.body.data.attributes.input.source == Topics.DEFAULT_SOURCE
  }

  def 'returns 404 for unsupported type'() {
    def restTemplate = new RestTemplate()
    def collectionText = ClassLoader.systemClassLoader.getResourceAsStream('dscovr_fc1.xml').text

    when:
    def createEntity = RequestEntity
        .post("${baseUrl}/metadata/not-a-real-type/".toURI())
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
