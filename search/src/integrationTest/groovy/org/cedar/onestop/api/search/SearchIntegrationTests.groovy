package org.cedar.onestop.api.search

import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@Unroll
@ActiveProfiles("integration")
@SpringBootTest(classes = [Application, IntegrationTestConfig], webEnvironment = RANDOM_PORT)
class SearchIntegrationTests extends Specification {

  @Autowired
  RestClient restClient

  @Value('${local.server.port}')
  private String port

  @Value('${server.context-path}')
  private String contextPath

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.name}')
  private String SEARCH_INDEX

  @Value('${elasticsearch.index.search.collectionType}')
  private String COLLECTION_TYPE

  @Value('${elasticsearch.index.search.granuleType}')
  private String GRANULE_TYPE

  private MediaType contentType = MediaType.APPLICATION_JSON_UTF8

  private RestTemplate restTemplate
  private String searchBaseUriString
  private URI searchBaseUri

  void setup() {
    def cl = ClassLoader.systemClassLoader
    def indexJson = cl.getResourceAsStream('indexSettings.json').text
    def indexSettings = new NStringEntity(indexJson, ContentType.APPLICATION_JSON)
    restClient.performRequest('DELETE', '_all')
    restClient.performRequest('PUT', 'search', Collections.EMPTY_MAP, indexSettings)

    for (e in ['GHRSST', 'DEM']) {
      for (c in ['C1', 'C2', 'C3']) {
        def metadata = cl.getResourceAsStream("data/${e}/${c}.json").text
        HttpEntity record = new NStringEntity(metadata, ContentType.APPLICATION_JSON)
        restClient.performRequest('PUT', '/search/collection', Collections.EMPTY_MAP, record)
      }
      for (g in ['G1', 'G2', 'G3']) {
        def metadata = cl.getResourceAsStream("data/${e}/${g}.json").text
        HttpEntity record = new NStringEntity(metadata, ContentType.APPLICATION_JSON)
        restClient.performRequest('PUT', '/search/granule', Collections.EMPTY_MAP, record)
      }
    }

    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()
    searchBaseUriString = "http://localhost:${port}/${contextPath}/search"
    searchBaseUri = searchBaseUriString.toURI()
  }

  def 'Valid query-only search with facets returns OK with expected results'() {
    setup:
    def request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ],
          "facets" : true
        }""".stripIndent()

    def requestEntity = RequestEntity
        .post(searchBaseUri)
        .contentType(contentType)
        .body(request)

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: "Search returns OK"
    result.statusCode == HttpStatus.OK
    result.headers.getContentType() == contentType

    and: "Result contains 3 items"
    def items = result.body.data
    items.size() == 3

    and: "Expected results are returned"
    def actualIds = items.collect { it.attributes.fileIdentifier }
    actualIds.containsAll([
        'gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED',
        'gov.noaa.nodc:GHRSST-OSDPD-L2P-MTSAT1R',
        'gov.noaa.nodc:GHRSST-Geo_Polar_Blended_Night-OSPO-L4-GLOB'
    ])

    and: 'The correct number of facets is returned'
    def aggs = result.body.meta.facets
    aggs.size() == 6

    and: 'The facets are as expected'
    aggs.science != null
    aggs.instruments != null
    aggs.platforms != null
    aggs.projects != null
    aggs.dataCenters != null
    aggs.dataResolution != null

    and: 'The cleaned aggregations are actually cleaned'
    def locationTerms = aggs.locations.collect { it }
    // Bad planted keywords should be removed
    !locationTerms.contains('Alaska')
    !locationTerms.contains('Alaska > Unalaska')
  }

}
