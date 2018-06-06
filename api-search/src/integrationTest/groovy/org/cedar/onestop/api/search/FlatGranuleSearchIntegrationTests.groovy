package org.cedar.onestop.api.search

import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@Unroll
@ActiveProfiles("integration")
@SpringBootTest(classes = [Application, IntegrationTestConfig], webEnvironment = RANDOM_PORT)
class FlatGranuleSearchIntegrationTests extends Specification {

  @Autowired
  RestClient restClient

  @Autowired
  Map testData

  @LocalServerPort
  private String port

  @Value('${server.servlet.context-path}')
  private String contextPath

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.flattened-granule.name}')
  private String FLATTENED_GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.universal-type}')
  private String TYPE

  private MediaType contentType = MediaType.APPLICATION_JSON_UTF8

  private RestTemplate restTemplate
  private String searchBaseUriString
  private String searchFlattenedGranuleUriString
  private String flatGranuleBaseUriString

  void setup() {
    def cl = ClassLoader.systemClassLoader
    def searchFlattenedGranuleIndexJson = cl.getResourceAsStream('search_flattened_granuleIndex.json').text
    def flattenedGranuleIndexSettings = new NStringEntity(searchFlattenedGranuleIndexJson, ContentType.APPLICATION_JSON)

    Response response = restClient.performRequest('DELETE', '_all')
    println("DELETE _all: ${response}")

    def flattenedGranuleResponse = restClient.performRequest('PUT', FLATTENED_GRANULE_SEARCH_INDEX, Collections.EMPTY_MAP, flattenedGranuleIndexSettings)
    println("PUT new flattened-granule index: ${flattenedGranuleResponse}")

    testData.each{ name, dataset ->
      dataset.each { collection, collectionData ->
        collectionData.flattenedGranules.each { flattenedGranule, flattenedGranuleData ->
          def metadata = cl.getResourceAsStream("data/${name}/${flattenedGranule}.json").text
          def flattenedGranuleEndpoint = "/$FLATTENED_GRANULE_SEARCH_INDEX/$TYPE/$flattenedGranuleData.id"
          def record = new NStringEntity(metadata, ContentType.APPLICATION_JSON)
          response = restClient.performRequest('PUT', flattenedGranuleEndpoint, Collections.EMPTY_MAP, record)
          println("PUT new flattened granule: ${response}")
        }
      }
    }

    def refreshEndpoint = "/${FLATTENED_GRANULE_SEARCH_INDEX}/_refresh"
    response = restClient.performRequest('POST', refreshEndpoint)
    println("Refresh search index: ${response}")

    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()
    searchBaseUriString = "http://localhost:${port}/${contextPath}/search/"
    searchFlattenedGranuleUriString = "http://localhost:${port}/${contextPath}/search/flattened-granule"
    flatGranuleBaseUriString = "http://localhost:${port}/${contextPath}/flattened-granule/"
  }
}
