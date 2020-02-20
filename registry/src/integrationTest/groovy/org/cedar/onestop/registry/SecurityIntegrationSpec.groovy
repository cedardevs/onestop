package org.cedar.onestop.registry

import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.HttpClientErrorException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@Unroll
@DirtiesContext
@EmbeddedKafka
@ActiveProfiles(['integration', 'cas'])
@SpringBootTest(classes = [MetadataRegistryMain], webEnvironment = RANDOM_PORT)
class SecurityIntegrationSpec extends Specification {

  @Value('${local.server.port}')
  String port

  @Value('${server.servlet.context-path:}')
  String contextPath

  @Shared
  String granulePayload

  @Shared
  String granuleTrackingId

  @Shared
  String granuleEndpoint

  def setupSpec() {
    granulePayload = ClassLoader.systemClassLoader.getResourceAsStream('test_granule.json').text
    Map granuleMap = new JsonSlurper().parseText(granulePayload) as Map
    granuleTrackingId = granuleMap.trackingId
    granuleEndpoint = "metadata/granule/common-ingest/${granuleTrackingId}"
  }

  def 'secure endpoints return 401 without authorization header'() {
    given:
    String baseUrl = "http://localhost:${port}/${contextPath}"
    TestRestTemplate restTemplate = new TestRestTemplate()
    // HttpComponentsClientHttpRequestFactory allows for PATCH
    restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

    when:
    URI uri = new URI("${baseUrl}${testCase.endpoint}")
    RequestEntity.BodyBuilder requestEntityBuilder = RequestEntity.method(testCase.method, uri)
    if (testCase.contentType instanceof MediaType) {
      requestEntityBuilder.contentType(testCase.contentType)
    }
    RequestEntity requestEntity
    if (testCase.payload instanceof String) {
      requestEntity = requestEntityBuilder.body(testCase.payload)
    }
    else {
      requestEntity = requestEntityBuilder.build()
    }

    HttpStatus status
    try {
      ResponseEntity<Map> response = restTemplate.exchange(requestEntity, Map)
      status = response.statusCode
    } catch(HttpClientErrorException e) {
      status = e.statusCode
    }

    then:
    status == testCase.status

    where:
    testCase << [
        [method: HttpMethod.GET, endpoint: "${granuleEndpoint}/resurrection", status: HttpStatus.UNAUTHORIZED],
        [method: HttpMethod.POST, endpoint: granuleEndpoint, payload: granulePayload, contentType: MediaType.APPLICATION_JSON, status: HttpStatus.UNAUTHORIZED],
        [method: HttpMethod.PUT, endpoint: granuleEndpoint, payload: granulePayload, contentType: MediaType.APPLICATION_JSON, status: HttpStatus.UNAUTHORIZED],
        [method: HttpMethod.PATCH, endpoint: granuleEndpoint, payload: granulePayload, contentType: MediaType.APPLICATION_JSON, status: HttpStatus.UNAUTHORIZED],
        [method: HttpMethod.DELETE, endpoint: granuleEndpoint, payload: granulePayload, contentType: MediaType.APPLICATION_JSON, status: HttpStatus.UNAUTHORIZED],
        [method: HttpMethod.GET, endpoint: granuleEndpoint, status: HttpStatus.NOT_FOUND],
    ]
  }
}
