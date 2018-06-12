package org.cedar.onestop.api.search

import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.Response
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
class MiscSearchIntegrationTests extends Specification {

  @Autowired
  RestClient restClient

  @Autowired
  Map testData

  @LocalServerPort
  private String port

  @Value('${server.servlet.context-path}')
  private String contextPath

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.collection.name}')
  private String COLLECTION_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.granule.name}')
  private String GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.flattened-granule.name}')
  private String FLATTENED_GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.universal-type}')
  private String TYPE

  private MediaType contentType = MediaType.APPLICATION_JSON_UTF8

  private RestTemplate restTemplate
  private String searchBaseUriString
  private String searchCollectionUriString
  private String searchGranuleUriString
  private String collectionBaseUriString
  private String granuleBaseUriString

  void setup() {
    def cl = ClassLoader.systemClassLoader
    def searchCollectionIndexJson = cl.getResourceAsStream('search_collectionIndex.json').text
    def searchGranuleIndexJson = cl.getResourceAsStream('search_granuleIndex.json').text

    def collectionIndexSettings = new NStringEntity(searchCollectionIndexJson, ContentType.APPLICATION_JSON)
    def granuleIndexSettings = new NStringEntity(searchGranuleIndexJson, ContentType.APPLICATION_JSON)

    Response response = restClient.performRequest('DELETE', '_all')
    println("DELETE _all: ${response}")

    def collectionResponse = restClient.performRequest('PUT', COLLECTION_SEARCH_INDEX, Collections.EMPTY_MAP, collectionIndexSettings)
    println("PUT new collection index: ${collectionResponse}")

    def granuleResponse = restClient.performRequest('PUT', GRANULE_SEARCH_INDEX, Collections.EMPTY_MAP, granuleIndexSettings)
    println("PUT new granule index: ${granuleResponse}")

    testData.each{ name, dataset ->
      dataset.each { collection, collectionData ->
        def metadata = cl.getResourceAsStream("data/${name}/${collection}.json").text
        def id = collectionData.id
        def collectionEndpoint = "/$COLLECTION_SEARCH_INDEX/$TYPE/$id"
        HttpEntity record = new NStringEntity(metadata, ContentType.APPLICATION_JSON)
        response = restClient.performRequest('PUT', collectionEndpoint, Collections.EMPTY_MAP, record)
        println("PUT new collection: ${response}")

        collectionData.granules.each { granule, granuleData ->
          metadata = cl.getResourceAsStream("data/${name}/${granule}.json").text
          def granuleEndpoint = "/$GRANULE_SEARCH_INDEX/$TYPE/$granuleData.id"
          record = new NStringEntity(metadata, ContentType.APPLICATION_JSON)
          response = restClient.performRequest('PUT', granuleEndpoint, Collections.EMPTY_MAP, record)
          println("PUT new granule: ${response}")
        }
      }
    }

    def refreshEndpoint = "/${COLLECTION_SEARCH_INDEX},${GRANULE_SEARCH_INDEX}/_refresh"
    response = restClient.performRequest('POST', refreshEndpoint)
    println("Refresh all search indices: ${response}")

    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()
    searchBaseUriString = "http://localhost:${port}/${contextPath}/search/"
    searchCollectionUriString = "http://localhost:${port}/${contextPath}/search/collection"
    searchGranuleUriString = "http://localhost:${port}/${contextPath}/search/granule"
    collectionBaseUriString = "http://localhost:${port}/${contextPath}/collection/"
    granuleBaseUriString = "http://localhost:${port}/${contextPath}/granule/"
  }

  def 'Collection GET request returns expected record and count of granules in collection'() {
    setup:
    // Obtain ES ID for COOPS collection
    def searchBaseUri = (searchCollectionUriString).toURI()
    def request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "buoy"}
            ],
          "summary": false
        }""".stripIndent()

    def requestEntity = RequestEntity
        .post(searchBaseUri)
        .contentType(contentType)
        .body(request)

    def result = restTemplate.exchange(requestEntity, Map)
    def esId = result.body.data[0].id

    def getBaseUri = (collectionBaseUriString + esId).toURI()
    requestEntity = RequestEntity.get(getBaseUri).build()

    when:
    result = restTemplate.exchange(requestEntity, Map)

    then: "Result found"
    assert result.body.data

    and: "Result is COOPS collection"
    result.body.data[0].attributes.fileIdentifier == "gov.noaa.nodc:NDBC-COOPS"

    and: "Collection has 2 granules"
    result.body.meta.totalGranules == 2
  }

  def 'Invalid search; returns BAD_REQUEST error when not conforming to schema'() {
    setup:
    def searchBaseUri = (searchCollectionUriString).toURI()
    def invalidSchemaRequest = """\
        {
          "filters": [
            {"type": "dateTime", "before": "2012-01-01", "after": "2011-01-01"}
          ]
        }""".stripIndent()
    def requestEntity = RequestEntity
        .post(searchBaseUri)
        .contentType(contentType)
        .body(invalidSchemaRequest)

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: "Request invalid"
    result.statusCode == HttpStatus.BAD_REQUEST

    and: "result contains errors list"
    result.body.errors instanceof List
    result.body.errors.every { it.status == '400' }
    result.body.errors.every { it.detail instanceof String }
  }

  def 'Invalid search; returns UNSUPPORTED_MEDIA_TYPE error when request body not specified as json content'() {
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
        .body(request)

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: "Bad request"
    result.statusCode == HttpStatus.UNSUPPORTED_MEDIA_TYPE
    result.headers.getContentType() == contentType
    and: "result contains no items"
    result.body.data == null
  }

  def 'Invalid search; returns BAD_REQUEST error when no request body'() {
    setup:
    def searchBaseUri = (searchCollectionUriString).toURI()
    def requestEntity = RequestEntity
        .post(searchBaseUri)
        .contentType(contentType)
        .body("")

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: "Bad request"
    result.statusCode == HttpStatus.BAD_REQUEST
    result.headers.getContentType() == contentType
    and: "result contains no items"
    result.body.data == null
  }

  def 'Invalid search; returns BAD_REQUEST error when request body is malformed json'() {
    setup:
    def searchBaseUri = (searchGranuleUriString).toURI()
    def badJsonSearch = """\
        {
          "queries": [
            {"type": "queryText", "value": "}
          ]
        }""".stripIndent()
    def requestEntity = RequestEntity
        .post(searchBaseUri)
        .contentType(contentType)
        .body(badJsonSearch)

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: "Bad request"
    result.statusCode == HttpStatus.BAD_REQUEST
    result.headers.getContentType() == contentType
    and: "result contains no items"
    result.body.data == null
    def errors = result.body.errors
    errors.any { it.title?.contains("Bad Request") }
  }

  def 'Invalid search; returns BAD_REQUEST error when request body is invalid'() {
    setup:
    def searchBaseUri = (searchCollectionUriString).toURI()
    def invalidJsonSearch = """\
        {
          "queries": [
            {"type": "NOTAREALTYPE", "value": "NONSENSE"}
          ]
        }""".stripIndent()
    def requestEntity = RequestEntity
        .post(searchBaseUri)
        .contentType(contentType)
        .body(invalidJsonSearch)

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: "Bad request"
    result.statusCode == HttpStatus.BAD_REQUEST
    result.headers.getContentType() == contentType
    and: "result contains no items"
    result.body.data == null
    def errors = result.body.errors
    errors.any { it.title?.contains("Bad Request") }
  }
}
