package org.cedar.onestop.api.search

import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchTestConfig
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles(["integration"])
@SpringBootTest(
    classes = [
        Application,
        DefaultApplicationConfig,

        // provides:
        // - `RestClient` 'restClient' bean via test containers
        ElasticsearchTestConfig,
    ],
    webEnvironment = RANDOM_PORT
)
@Unroll
class SearchControllerIntegrationTests extends Specification {

  @Autowired
  RestClient restClient

  @Autowired
  ElasticsearchConfig esConfig

  @LocalServerPort
  private String port

  @Value('${server.servlet.context-path}')
  private String contextPath

  private MediaType contentType = MediaType.APPLICATION_JSON_UTF8
  private RestTemplate restTemplate
  private String baseUri

  void setup() {
    TestUtil.resetLoadAndRefreshSearchIndices(restClient, esConfig)
    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()
    baseUri = "http://localhost:${port}${contextPath}"
  }

  def "CVE-2018-1000840 #desc"() {
    given:
    URI endpointUri = "${baseUri}/search/collection".toURI()
    String request = requestString.stripIndent()
    RequestEntity requestEntity = RequestEntity.post(endpointUri).contentType(contentType).body(request)

    when: 'You attempt to post application/xml content types'
    ResponseEntity result = restTemplate.exchange(requestEntity, Map)

    then: 'XML is not parsed by the endpoint, preventing the attack vector'
    result.statusCode == expectedStatusCode

    where:
    desc | requestString | contentType | expectedStatusCode
    'post xml' | """{"search":"name","value":"spy"}""" | MediaType.APPLICATION_XML | HttpStatus.UNSUPPORTED_MEDIA_TYPE
    'attack xml in request body' | """<?xml version="1.0" encoding="ISO-8859-1"?>
    <!DOCTYPE a [
    <!ENTITY % remote_dtd SYSTEM "http://localhost:8000/xxe-test.dtd"> %remote_dtd; ]>
    <body>
      <data>&attack;</data>
      <data>Whatever</data>
    </body>""" | MediaType.APPLICATION_JSON_UTF8 | HttpStatus.BAD_REQUEST
    'any xml in request body' | """<?xml version="1.0" encoding="UTF-8" ?>
    <root>
   <queries>
      <element>
         <type>queryText</type>
         <value>temperature</value>
      </element>
   </queries>
</root>""" | MediaType.APPLICATION_JSON_UTF8 | HttpStatus.BAD_REQUEST
  }

  def '#type info endpoint reports #type count'() {
    given:
    URI endpointUri = "${baseUri}/${type}".toURI()
    RequestEntity requestEntity = RequestEntity.get(endpointUri).build()

    when:
    ResponseEntity result = restTemplate.exchange(requestEntity, Map)
    Map body = result.body
    List<Map> data = body.data as List<Map>

    then:
    result.statusCode == HttpStatus.OK
    result.headers.getContentType() == contentType
    data.size() == 1
    data[0] == [
        type: 'count',
        id: type,
        count: count
    ]

    where:
    type                | count
    ElasticsearchConfig.TYPE_COLLECTION        | 7
    ElasticsearchConfig.TYPE_GRANULE           | 2
    ElasticsearchConfig.TYPE_FLATTENED_GRANULE | 2
  }

  def 'Get existing collection by ID returns expected record and granule count'() {
    given:
    URI endpointUri = "${baseUri}/collection/${TestUtil.testData.COOPS.C1.id}".toURI()
    RequestEntity requestEntity = RequestEntity.get(endpointUri).build()

    when:
    ResponseEntity result = restTemplate.exchange(requestEntity, Map)
    Map body = result.body
    List<Map> data = body.data as List<Map>
    Map meta = body.meta as Map
    Map firstAttributes = data[0].attributes as Map
    String firstFileIdentifier = firstAttributes.fileIdentifier

    then: 'Request returns OK'
    result.statusCode == HttpStatus.OK
    result.headers.getContentType() == contentType

    and: "Result found"
    assert data

    and: "Result is COOPS collection"
    firstFileIdentifier == "gov.noaa.nodc:NDBC-COOPS"

    and: "Collection has 2 granules"
    meta.totalGranules == 2
  }

  def 'Get existing #type by ID returns expected record'() {
    given:
    URI endpointUri = "${baseUri}/${type}/${idPath}".toURI()
    RequestEntity requestEntity = RequestEntity.get(endpointUri).build()

    when:
    ResponseEntity result = restTemplate.exchange(requestEntity, Map)
    Map body = result.body
    List<Map> data = body.data as List<Map>
    Map firstAttributes = data[0].attributes as Map
    String firstFileIdentifier = firstAttributes.fileIdentifier

    then: 'Request returns OK'
    result.statusCode == HttpStatus.OK
    result.headers.getContentType() == contentType

    and: "Result found"
    assert data

    and: "Result is COOPS ${type}"
    firstFileIdentifier == fileIdentifier

    where:
    type                | idPath                                     | fileIdentifier
    ElasticsearchConfig.TYPE_GRANULE           | TestUtil.testData.COOPS.C1.granules.G1.id           | 'CO-OPS.NOS_8638614_201602_D1_v00'
    ElasticsearchConfig.TYPE_FLATTENED_GRANULE | TestUtil.testData.COOPS.C1.flattenedGranules.FG2.id | 'CO-OPS.NOS_9410170_201503_D1_v00'
  }

  def 'Get nonexisting #type by ID returns NOT FOUND'() {
    given:
    URI endpointUri = "${baseUri}/${type}/123-this-is-a-BAD-ID-456".toURI()
    RequestEntity requestEntity = RequestEntity.get(endpointUri).build()

    when:
    ResponseEntity result = restTemplate.exchange(requestEntity, Map)
    Map body = result.body
    List<Map> errors = body.errors as List<Map>

    then:
    result.statusCode == HttpStatus.NOT_FOUND
    result.headers.getContentType() == contentType
    assert errors

    where:
    type << ['collection', 'granule', 'flattened-granule']
  }

  def 'Valid collection search request returns OK with expected results'() {
    given:
    List<String> summaryFields = [
        'title', 'thumbnail', 'spatialBounding', 'beginDate', 'beginYear', 'endDate',
        'endYear', 'links', 'citeAsStatements'
    ]
    URI endpointUri = "${baseUri}/search/collection".toURI()
    String request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ]
        }""".stripIndent()
    RequestEntity requestEntity = RequestEntity.post(endpointUri).contentType(contentType).body(request)

    when:
    ResponseEntity result = restTemplate.exchange(requestEntity, Map)
    Map body = result.body
    List<Map> data = body.data as List<Map>

    then: 'Search returns OK'
    result.statusCode == HttpStatus.OK
    result.headers.getContentType() == contentType

    and: 'Expected results are returned'
    data.size() == 4

    and: "Expected results are returned"
    // Not returning IDs so need to check another way
    def thumbnails = data.collect {
      Map attributes = it.attributes as Map
      attributes.thumbnail
    }
    def expectedThumbnails = [
        'http://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:GHRSST-Geo_Polar_Blended_Night-OSPO-L4-GLOB',
        'http://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:NDBC-COOPS',
        'http://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED',
        'http://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:GHRSST-OSDPD-L2P-MTSAT1R'
    ]
    thumbnails.sort() == expectedThumbnails.sort()

    and: 'Returned summary fields only'
    data.each {
      Map attributes = it.attributes as Map
      assert attributes.keySet().containsAll(summaryFields)
      assert attributes.keySet().size() == summaryFields.size()
    }
  }

  def 'Valid #type search request returns OK with expected results'() {
    given:
    // Summary fields for granule types contain internalParentIdentifier field unlike collections
    List<String> summaryFields = [
        'title', 'thumbnail', 'spatialBounding', 'beginDate', 'beginYear', 'endDate',
        'endYear', 'links', 'citeAsStatements', 'internalParentIdentifier'
    ]
    URI endpointUri = "${baseUri}/search/${type}".toURI()
    String request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ]
        }""".stripIndent()
    RequestEntity requestEntity = RequestEntity.post(endpointUri).contentType(contentType).body(request)

    when:
    ResponseEntity result = restTemplate.exchange(requestEntity, Map)
    Map body = result.body
    List<Map> data = body.data as List<Map>

    then: 'Search returns OK'
    result.statusCode == HttpStatus.OK
    result.headers.getContentType() == contentType

    and: 'Expected results are returned'
    data.size() == 2

    // TODO: verify records somehow....

    and: 'Returned summary fields only'
    data.each {
      Map attributes = it.attributes as Map
      assert attributes.keySet().containsAll(summaryFields)
      assert attributes.keySet().size() == summaryFields.size()
    }

    where:
    type << ['granule', 'flattened-granule']
  }

  def 'Invalid search; #type endpoint returns BAD_REQUEST error when not conforming to schema'() {
    setup:
    URI endpointUri = "${baseUri}/search/${type}".toURI()
    String invalidSchemaRequest = """\
        {
          "filters": [
            {"type": "dateTime", "before": "2012-01-01", "after": "2011-01-01"}
          ]
        }""".stripIndent()
    RequestEntity requestEntity = RequestEntity
        .post(endpointUri)
        .contentType(contentType)
        .body(invalidSchemaRequest)

    when:
    ResponseEntity result = restTemplate.exchange(requestEntity, Map)
    Map body = result.body
    List<Map> data = body.data as List<Map>
    List<Map> errors = body.errors as List<Map>

    then: "Request invalid"
    result.statusCode == HttpStatus.BAD_REQUEST

    and: "Result contains errors and no data"
    assert errors
    errors instanceof List
    errors.every {
      Map error = it as Map
      error.status == '400'
    }
    assert !data

    where:
    type << ['collection', 'granule', 'flattened-granule']
  }

  def 'Invalid search; #type endpoint returns UNSUPPORTED_MEDIA_TYPE error when request body not specified as json content'() {
    setup:
    URI endpointUri = "${baseUri}/search/${type}".toURI()
    String request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ]
        }""".stripIndent()
    RequestEntity requestEntity = RequestEntity
        .post(endpointUri)
        .body(request)

    when:
    ResponseEntity result = restTemplate.exchange(requestEntity, Map)
    Map body = result.body
    List<Map> data = body.data as List<Map>
    List<Map> errors = body.errors as List<Map>

    then: "Bad request"
    result.statusCode == HttpStatus.UNSUPPORTED_MEDIA_TYPE
    result.headers.getContentType() == contentType

    and: "result contains errors and no data"
    assert errors
    errors instanceof List
    errors.every {
      Map error = it
      error.status == '415'
    }
    assert !data

    where:
    type << [ElasticsearchConfig.TYPE_COLLECTION, ElasticsearchConfig.TYPE_GRANULE, ElasticsearchConfig.TYPE_FLATTENED_GRANULE]
  }

  def 'Invalid search; #type endpoint returns BAD_REQUEST error when no request body'() {
    setup:
    URI endpointUri = "${baseUri}/search/${type}".toURI()
    RequestEntity requestEntity = RequestEntity
        .post(endpointUri)
        .contentType(contentType)
        .body("")

    when:
    ResponseEntity result = restTemplate.exchange(requestEntity, Map)
    Map body = result.body
    List<Map> data = body.data as List<Map>
    List<Map> errors = body.errors as List<Map>

    then: "Bad request"
    result.statusCode == HttpStatus.BAD_REQUEST
    result.headers.getContentType() == contentType

    and: "Result contains errors and no data"
    assert errors
    errors instanceof List
    errors.every { it.status == '400' }
    assert !data

    where:
    type << [ElasticsearchConfig.TYPE_COLLECTION, ElasticsearchConfig.TYPE_GRANULE, ElasticsearchConfig.TYPE_FLATTENED_GRANULE]
  }

  def 'Invalid search; #type endpoint returns BAD_REQUEST error when request body is malformed json'() {
    setup:
    URI endpointUri = "${baseUri}/search/${type}".toURI()
    String badJsonSearch = """\
        {
          "queries": [
            {"type": "queryText", "value": "}
          ]
        }""".stripIndent()
    RequestEntity requestEntity = RequestEntity
        .post(endpointUri)
        .contentType(contentType)
        .body(badJsonSearch)

    when:
    ResponseEntity result = restTemplate.exchange(requestEntity, Map)
    Map body = result.body
    List<Map> data = body.data as List<Map>
    List<Map> errors = body.errors as List<Map>

    then: "Bad request"
    result.statusCode == HttpStatus.BAD_REQUEST
    result.headers.getContentType() == contentType

    and: "Result contains errors and no data"
    assert errors
    errors instanceof List
    errors.every {
      Map error = it
      error.status == '400'
    }
    assert !data

    where:
    type << [ElasticsearchConfig.TYPE_COLLECTION, ElasticsearchConfig.TYPE_GRANULE, ElasticsearchConfig.TYPE_FLATTENED_GRANULE]
  }

  def 'Invalid search; #type endpoint returns BAD_REQUEST error when request body is invalid'() {
    setup:
    URI endpointUri = "${baseUri}/search/${type}".toURI()
    String invalidJsonSearch = """\
        {
          "queries": [
            {"type": "NOTAREALTYPE", "value": "NONSENSE"}
          ]
        }""".stripIndent()
    RequestEntity requestEntity = RequestEntity
        .post(endpointUri)
        .contentType(contentType)
        .body(invalidJsonSearch)

    when:
    ResponseEntity result = restTemplate.exchange(requestEntity, Map)
    Map body = result.body
    List<Map> data = body.data as List<Map>
    List<Map> errors = body.errors as List<Map>

    then: "Bad request"
    result.statusCode == HttpStatus.BAD_REQUEST
    result.headers.getContentType() == contentType

    and: "Result contains errors and no data"
    assert errors
    errors instanceof List
    errors.every {
      Map error = it
      error.status == '400'
    }
    assert !data

    where:
    type << [ElasticsearchConfig.TYPE_COLLECTION, ElasticsearchConfig.TYPE_GRANULE, ElasticsearchConfig.TYPE_FLATTENED_GRANULE]
  }
}
