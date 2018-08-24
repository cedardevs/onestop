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


  def 'can post then retrieve granule info'() {
    def restTemplate = new RestTemplate()
    def granuleText = ClassLoader.systemClassLoader.getResourceAsStream('test_granule.json').text
    def granuleMap = new JsonSlurper().parseText(granuleText) as Map


    when:
    def createEntity = RequestEntity
        .post("${baseUrl}/metadata/granule/".toURI())
        .contentType(MediaType.APPLICATION_JSON)
        .body(granuleText)
    def createResponse = restTemplate.exchange(createEntity, Map)

    then:
    createResponse.statusCode == HttpStatus.OK

    when:
    sleep(200)
    def retrieveEntity = RequestEntity
        .get("${baseUrl}/metadata/granule/${granuleMap.trackingId}".toURI())
        .build()
    def retrieveResponse = restTemplate.exchange(retrieveEntity, Map)

    then:
    retrieveResponse.statusCode == HttpStatus.OK
    retrieveResponse.body == [
        id: granuleMap.trackingId,
        type: 'granule',
        attributes: [
            raw: granuleMap,
            parsed: null
        ]
    ]
  }

  def 'can post granule iso then get it back out'() {
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
    retrieveResponse.body == [
        id: granuleId,
        type: 'granule',
        attributes: [
            raw: [id: granuleId, rawFormat: "isoXml", rawMetadata: granuleText],
            parsed: null
        ]
    ]
  }

  def 'can post then retrieve collection info'() {
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
    retrieveResponse.body == [
        id: collectionId,
        type: 'collection',
        attributes: [
            raw: [id: collectionId, rawFormat: "isoXml", rawMetadata: collectionText],
            parsed: null
        ]
    ]
  }

}
