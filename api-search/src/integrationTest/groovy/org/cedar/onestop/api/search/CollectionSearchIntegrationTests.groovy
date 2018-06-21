package org.cedar.onestop.api.search

import groovy.json.JsonOutput
import org.apache.http.HttpEntity
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
class CollectionSearchIntegrationTests extends Specification {

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

  @Value('${elasticsearch.index.universal-type}')
  private String TYPE

  private MediaType contentType = MediaType.APPLICATION_JSON_UTF8

  private RestTemplate restTemplate
  private String searchBaseUriString
  private String searchCollectionUriString
  private String collectionBaseUriString

  void setup() {
    def cl = ClassLoader.systemClassLoader
    def searchCollectionIndexJson = cl.getResourceAsStream('search_collectionIndex.json').text
    def collectionIndexSettings = new NStringEntity(searchCollectionIndexJson, ContentType.APPLICATION_JSON)

    Response response = restClient.performRequest('DELETE', '_all')
    println("DELETE _all: ${response}")

    def collectionResponse = restClient.performRequest('PUT', COLLECTION_SEARCH_INDEX, Collections.EMPTY_MAP, collectionIndexSettings)
    println("PUT new collection index: ${collectionResponse}")

    testData.each{ name, dataset ->
      dataset.each { collection, collectionData ->
        def metadata = cl.getResourceAsStream("data/${name}/${collection}.json").text
        def id = collectionData.id
        def collectionEndpoint = "/$COLLECTION_SEARCH_INDEX/$TYPE/$id"
        HttpEntity record = new NStringEntity(metadata, ContentType.APPLICATION_JSON)
        response = restClient.performRequest('PUT', collectionEndpoint, Collections.EMPTY_MAP, record)
        println("PUT new collection: ${response}")
      }
    }

    def refreshEndpoint = "/${COLLECTION_SEARCH_INDEX}/_refresh"
    response = restClient.performRequest('POST', refreshEndpoint)
    println("Refresh all search indices: ${response}")

    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()
    searchBaseUriString = "http://localhost:${port}/${contextPath}/search/"
    searchCollectionUriString = "http://localhost:${port}/${contextPath}/search/collection"
    collectionBaseUriString = "http://localhost:${port}/${contextPath}/collection/"
  }

  def 'Collection endpoint reports count of collections'() {
    setup:
    def searchCollectionBaseUri = (collectionBaseUriString).toURI()
    def requestEntityCollection = RequestEntity.get(searchCollectionBaseUri).build()

    when:
    def resultCollection = restTemplate.exchange(requestEntityCollection, Map)

    then:
    resultCollection.statusCode == HttpStatus.OK
    resultCollection.headers.getContentType() == contentType
    resultCollection.body.data[0].count == 7
    resultCollection.body.data*.id.containsAll(['collection'])
    resultCollection.body.data*.count.every({ it instanceof Number })
  }

  def 'Valid collection search summary returns OK with expected results'() {
    setup:
    def searchBaseUri = (searchCollectionUriString).toURI()
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

    and: "Result contains 4 items"
    def items = result.body.data
    items.size() == 4

    and: "Expected results are returned"
    // Not returning IDs so need to check another way
    def thumbnails = items.collect { it.attributes.thumbnail }
    thumbnails.sort().equals([
        'http://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:GHRSST-Geo_Polar_Blended_Night-OSPO-L4-GLOB',
        'http://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:NDBC-COOPS',
        'http://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED',
        'http://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:GHRSST-OSDPD-L2P-MTSAT1R'
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

  def 'Valid query-only collection search with facets returns OK with expected results'() {
    setup:
    def searchBaseUri = (searchCollectionUriString).toURI()
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

    and: "Result contains 4 items"
    def items = result.body.data
    items.size() == 4

    and: "Expected results are returned"
    def actualIds = items.collect { it.attributes.fileIdentifier }
    actualIds.containsAll([
        'gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED',
        'gov.noaa.nodc:GHRSST-OSDPD-L2P-MTSAT1R',
        'gov.noaa.nodc:GHRSST-Geo_Polar_Blended_Night-OSPO-L4-GLOB',
        'gov.noaa.nodc:NDBC-COOPS'
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

    and: 'The cleaned aggregations are actually cleaned'
    def locationTerms = aggs.locations.collect { it }
    // Bad planted keywords should be removed
    !locationTerms.contains('Alaska')
    !locationTerms.contains('Alaska > Unalaska')
  }

  def 'Valid filter-only collection search returns OK with expected results'() {
    setup:
    def searchBaseUri = (searchCollectionUriString).toURI()
    def request = """\
        {
          "filters":
            [
              {"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [145.5, 12.34]}}
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

    and: "Result contains 4 items"
    def items = result.body.data
    items.size() == 4

    and: "Expected results are returned"
    def actualIds = items.collect { it.attributes.fileIdentifier }
    actualIds.containsAll([
        'gov.noaa.nodc:GHRSST-Geo_Polar_Blended_Night-OSPO-L4-GLOB',
        'gov.noaa.ngdc.mgg.dem:4870',
        'gov.noaa.nodc:GHRSST-OSDPD-L2P-MTSAT1R',
        'gov.noaa.nodc:NDBC-COOPS'
    ])
  }

  def 'Valid query-and-filter collection search returns OK with expected result'() {
    setup:
    def searchBaseUri = (searchCollectionUriString).toURI()
    def request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ],
          "filters":
            [
              {"type": "datetime", "before": "2007-12-31T23:59:59.999Z", "after": "2007-01-01T00:00:00Z"}
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

    and: "Result contains 1 item"
    def items = result.body.data
    items.size() == 1

    and: "Expected result is returned"
    def actualIds = items.collect { it.attributes.fileIdentifier }
    actualIds.containsAll([
        'gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED'
    ])
  }

  def 'Valid query-and-exclude-global collection search returns OK with expected results'() {
    setup:
    def searchBaseUri = (searchCollectionUriString).toURI()
    def request = """\
        {
          "queries":
            [
              {"type": "queryText", "value": "ghrsst"}
            ],
          "filters":
            [
              {"type": "excludeGlobal", "value": true}
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
        'gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED',
        'gov.noaa.nodc:GHRSST-OSDPD-L2P-MTSAT1R'
    ])
  }

  def 'Time filter with #situation an item\'s time range returns the correct results'() {
    setup:
    def searchBaseUri = (searchCollectionUriString).toURI()
    def ghrsst1FileId = 'gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED'
    def request = [filters: [[type: 'datetime']]]
    if (before) {
      request.filters[0].before = before
    }
    if (after) {
      request.filters[0].after = after
    }

    request.summary = false

    def requestEntity = RequestEntity
        .post(searchBaseUri)
        .contentType(contentType)
        .body(JsonOutput.toJson(request))

    when:
    def result = restTemplate.exchange(requestEntity, Map)
    def ids = result.body.data.collect { it.attributes.fileIdentifier }

    then:
    result.statusCode == HttpStatus.OK
    ids.contains(ghrsst1FileId) == matches

    where: // NOTE: time range for GHRSST/1.xml is: 2005-01-30T00:00:00.000Z <-> 2008-01-14T00:00:00.000Z
    after                  | before                 | matches | situation
    '2005-01-01T00:00:00Z' | '2005-01-02T00:00:00Z' | false   | 'range that is fully before'
    '2005-01-01T00:00:00Z' | '2008-01-01T00:00:00Z' | true    | 'range that overlaps the beginning of'
    '2005-02-01T00:00:00Z' | '2008-01-01T00:00:00Z' | true    | 'range that is fully within'
    '2005-01-01T00:00:00Z' | '2008-02-01T00:00:00Z' | true    | 'range that fully encloses'
    '2005-02-01T00:00:00Z' | '2008-02-01T00:00:00Z' | true    | 'range that overlaps the end of'
    '2008-02-01T00:00:00Z' | '2008-02-02T00:00:00Z' | false   | 'range that is fully after'
    '2005-01-01T00:00:00Z' | null                   | true    | 'start time before'
    '2005-02-01T00:00:00Z' | null                   | true    | 'start time within'
    '2008-02-01T00:00:00Z' | null                   | false   | 'start time after'
    null                   | '2005-01-01T00:00:00Z' | false   | 'end time before'
    null                   | '2008-01-01T00:00:00Z' | true    | 'end time within'
    null                   | '2008-02-01T00:00:00Z' | true    | 'end time after'
  }

  def 'Search with pagination specified returns OK with expected results'() {
    setup:
    def searchBaseUri = (searchCollectionUriString).toURI()
    def request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ],
            "page":
            {
              "max": 1,
              "offset": 0
            },
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

    and: "Expected result is returned"
    def actualIds = items.collect { it.attributes.fileIdentifier }
    actualIds.containsAll([
        'gov.noaa.nodc:GHRSST-Geo_Polar_Blended_Night-OSPO-L4-GLOB'
    ])
  }
}
