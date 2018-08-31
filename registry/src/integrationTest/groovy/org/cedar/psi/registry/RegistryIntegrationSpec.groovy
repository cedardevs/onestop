package org.cedar.psi.registry

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.admin.AdminClient
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
  AdminClient adminClient

  RestTemplate restTemplate
  String baseUrl


  def setup() {
    restTemplate = new RestTemplate()
    baseUrl = "http://localhost:${port}/${contextPath}"
  }


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
        .get("${baseUrl}/metadata/granule/${granuleId}".toURI())
        .build()
    def retrieveResponse = restTemplate.exchange(retrieveEntity, Map)

    then:
    retrieveResponse.statusCode == HttpStatus.OK
    retrieveResponse.body.id == granuleId
    retrieveResponse.body.type == 'granule'
    retrieveResponse.body.attributes.input.content == granuleText
    retrieveResponse.body.attributes.input.contentType == "application/json"
    retrieveResponse.body.attributes.input.source == "common-ingest"
    retrieveResponse.body.attributes.identifiers.'common-ingest' == granuleMap.trackingId

    and: // let's verify the full response just this once
    retrieveResponse.body == [
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
                "source": "common-ingest"
            ],
            identifiers: [
                'common-ingest': granuleMap.trackingId
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
    retrieveResponse.statusCode == HttpStatus.OK
    retrieveResponse.body.id == granuleId  as String
    retrieveResponse.body.type == 'granule'
    retrieveResponse.body.attributes.input.content == granuleText
    retrieveResponse.body.attributes.input.contentType == "application/xml"
    retrieveResponse.body.attributes.input.source == null
    retrieveResponse.body.attributes.identifiers == [:]
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
    retrieveResponse.statusCode == HttpStatus.OK
    retrieveResponse.body.id == collectionId as String
    retrieveResponse.body.type == 'collection'
    retrieveResponse.body.attributes.input.content == collectionText
    retrieveResponse.body.attributes.input.contentType == "application/xml"
    retrieveResponse.body.attributes.input.source == null
    retrieveResponse.body.attributes.identifiers == [:]
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
    retrieveResponse.body.id == collectionId as String
    retrieveResponse.body.type == 'collection'
    retrieveResponse.body.attributes.input.content == collectionText
    retrieveResponse.body.attributes.input.contentType == "application/xml"
    retrieveResponse.body.attributes.input.source == null
    retrieveResponse.body.attributes.identifiers == [:]
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
