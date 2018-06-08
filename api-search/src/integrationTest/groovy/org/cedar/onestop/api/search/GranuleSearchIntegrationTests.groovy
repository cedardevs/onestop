package org.cedar.onestop.api.search

import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
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
class GranuleSearchIntegrationTests extends Specification {

  @Autowired
  RestClient restClient

  @Autowired
  Map testData

  @LocalServerPort
  private String port

  @Value('${server.servlet.context-path}')
  private String contextPath

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.granule.name}')
  private String GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.universal-type}')
  private String TYPE

  private MediaType contentType = MediaType.APPLICATION_JSON_UTF8

  private RestTemplate restTemplate
  private String searchBaseUriString
  private String searchGranuleUriString
  private String granuleBaseUriString

  void setup() {
    def cl = ClassLoader.systemClassLoader
    def searchGranuleIndexJson = cl.getResourceAsStream('search_granuleIndex.json').text
    def granuleIndexSettings = new NStringEntity(searchGranuleIndexJson, ContentType.APPLICATION_JSON)

    Response response = restClient.performRequest('DELETE', '_all')
    println("DELETE _all: ${response}")

    def granuleResponse = restClient.performRequest('PUT', GRANULE_SEARCH_INDEX, Collections.EMPTY_MAP, granuleIndexSettings)
    println("PUT new granule index: ${granuleResponse}")

    testData.each{ name, dataset ->
      dataset.each { collection, collectionData ->
        collectionData.granules.each { granule, granuleData ->
          def metadata = cl.getResourceAsStream("data/${name}/${granule}.json").text
          def granuleEndpoint = "/$GRANULE_SEARCH_INDEX/$TYPE/$granuleData.id"
          def record = new NStringEntity(metadata, ContentType.APPLICATION_JSON)
          response = restClient.performRequest('PUT', granuleEndpoint, Collections.EMPTY_MAP, record)
          println("PUT new granule: ${response}")
        }
      }
    }

    def refreshEndpoint = "/${GRANULE_SEARCH_INDEX}/_refresh"
    response = restClient.performRequest('POST', refreshEndpoint)
    println("Refresh search index: ${response}")

    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()
    searchBaseUriString = "http://localhost:${port}/${contextPath}/search/"
    searchGranuleUriString = "http://localhost:${port}/${contextPath}/search/granule"
    granuleBaseUriString = "http://localhost:${port}/${contextPath}/granule/"
  }

  def 'Granule endpoint reports count of granules'() {
    setup:
    def searchGranuleBaseUri = (granuleBaseUriString).toURI()
    def requestEntityGranule = RequestEntity.get(searchGranuleBaseUri).build()

    when:
    def resultGranule = restTemplate.exchange(requestEntityGranule, Map)

    then:
    resultGranule.statusCode == HttpStatus.OK
    resultGranule.headers.getContentType() == contentType
    resultGranule.body.data[0].count == 2
    resultGranule.body.data*.id.containsAll(['granule'])
    resultGranule.body.data*.count.every({ it instanceof Number })
  }

  def 'Valid granule summary search returns OK with expected results'() {
    setup:
    def searchBaseUri = (searchGranuleUriString).toURI()
    def request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ]
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

    and: "Result contains 2 items"
    def items = result.body.data
    items.size() == 2

    and: "Expected results are returned"
    def pids = items.collect { it.attributes.internalParentIdentifier }
    pids.sort().equals([
        'fcf83ec9-964b-45b9-befe-378ea6ce52cb',
        'fcf83ec9-964b-45b9-befe-378ea6ce52cb'
    ].sort())

    // If there are more keys per result than summary should contain, summary source filter is not working!
    items.each {
      assert it.attributes.keySet().size() <= [
          "title",
          "thumbnail",
          "spatialBounding",
          "beginDate",
          "beginYear",
          "endDate",
          "endYear",
          "links",
          "citeAsStatements",
          "internalParentIdentifier"
      ].size()
    }
  }

  def 'Valid query-only granule search with facets returns OK with expected results'() {
    setup:
    def searchBaseUri = (searchGranuleUriString).toURI()
    def request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ],
          "facets" : true,
          "summary" : false
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

    and: "Result contains 2 items"
    def items = result.body.data
    items.size() == 2

    and: "Expected results are returned"
    def actualIds = items.collect { it.attributes.fileIdentifier }
    actualIds.containsAll([
        'CO-OPS.NOS_8638614_201602_D1_v00',
        'CO-OPS.NOS_9410170_201503_D1_v00'
    ])

    and: 'The correct number of facets is returned'
    def aggs = result.body.meta.facets
    aggs.size() == 10

    and: 'The facets are as expected'
    aggs.science != null
    aggs.services != null
    aggs.locations != null
    aggs.instruments != null
    aggs.platforms != null
    aggs.projects != null
    aggs.dataCenters != null
    aggs.horizontalResolution != null
    aggs.verticalResolution != null
    aggs.temporalResolution != null
  }

  def 'Valid filter-only granule search returns OK with expected results'() {
    setup:
    def searchBaseUri = (searchGranuleUriString).toURI()
    def request = """\
        {
          "filters":
            [
              {"type": "geometry", "relation": "intersects", "geometry": {"type": "Point", "coordinates": [-76.315, 36.977]}}
            ],
          "facets": false,
          "summary": false
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

    and: "Result contains 1 item"
    def items = result.body.data
    items.size() == 1

    and: "Expected results are returned"
    def actualIds = items.collect { it.attributes.fileIdentifier }
    actualIds.containsAll([
        'CO-OPS.NOS_8638614_201602_D1_v00'
    ])
  }

  def 'Valid query-and-filter granule search returns OK with expected result'() {
    setup:
    def searchBaseUri = (searchGranuleUriString).toURI()
    def request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ],
          "filters":
            [
              {"type":"facet","name":"science","values":["Oceans > Ocean Optics"]}
            ],
          "summary": false
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

    and: "Result contains 2 items"
    def items = result.body.data
    items.size() == 2

    and: "Expected result is returned"
    def actualIds = items.collect { it.attributes.fileIdentifier }
    actualIds.containsAll([
        'CO-OPS.NOS_8638614_201602_D1_v00',
        'CO-OPS.NOS_9410170_201503_D1_v00'
    ])
  }
}
