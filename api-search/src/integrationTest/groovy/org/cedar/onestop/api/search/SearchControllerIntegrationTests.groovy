package org.cedar.onestop.api.search

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Unroll

@Unroll
class SearchControllerIntegrationTests extends IntegrationTest {

  @LocalServerPort
  private String port

  @Value('${server.servlet.context-path}')
  private String contextPath

  private MediaType contentType = MediaType.APPLICATION_JSON_UTF8
  private RestTemplate restTemplate
  private String baseUri

  void setup() {
    refreshAndLoadSearchIndices()

    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()
    baseUri = "http://localhost:${port}${contextPath}"
  }

  def "CVE-2018-1000840 #desc"() {
    given:
    def endpointUri = "${baseUri}/search/collection".toURI()
    def request = requestString.stripIndent()
    def requestEntity = RequestEntity.post(endpointUri).contentType(contentType).body(request)

    when: 'You attempt to post application/xml content types'
    def result = restTemplate.exchange(requestEntity, Map)

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
    def endpointUri = "${baseUri}/${type}".toURI()
    def requestEntity = RequestEntity.get(endpointUri).build()

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then:
    result.statusCode == HttpStatus.OK
    result.headers.getContentType() == contentType
    result.body.data.size() == 1
    result.body.data[0] == [
        type: 'count',
        id: type,
        count: count
    ]

    where:
    type                | count
    'collection'        | 7
    'granule'           | 2
    'flattened-granule' | 2
  }

  def 'Get existing collection by ID returns expected record and granule count'() {
    given:
    def endpointUri = "${baseUri}/collection/${testData.COOPS.C1.id}".toURI()
    def requestEntity = RequestEntity.get(endpointUri).build()

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: 'Request returns OK'
    result.statusCode == HttpStatus.OK
    result.headers.getContentType() == contentType

    and: "Result found"
    assert result.body.data

    and: "Result is COOPS collection"
    result.body.data[0].attributes.fileIdentifier == "gov.noaa.nodc:NDBC-COOPS"

    and: "Collection has 2 granules"
    result.body.meta.totalGranules == 2
  }

  def 'Get existing #type by ID returns expected record'() {
    given:
    def endpointUri = "${baseUri}/${type}/${idPath}".toURI()
    def requestEntity = RequestEntity.get(endpointUri).build()

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: 'Request returns OK'
    result.statusCode == HttpStatus.OK
    result.headers.getContentType() == contentType

    and: "Result found"
    assert result.body.data

    and: "Result is COOPS ${type}"
    result.body.data[0].attributes.fileIdentifier == fileIdentifier

    where:
    type                | idPath                                     | fileIdentifier
    'granule'           | testData.COOPS.C1.granules.G1.id           | 'CO-OPS.NOS_8638614_201602_D1_v00'
    'flattened-granule' | testData.COOPS.C1.flattenedGranules.FG2.id | 'CO-OPS.NOS_9410170_201503_D1_v00'
  }

  def 'Get nonexisting #type by ID returns NOT FOUND'() {
    given:
    def endpointUri = "${baseUri}/${type}/123-this-is-a-BAD-ID-456".toURI()
    def requestEntity = RequestEntity.get(endpointUri).build()

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then:
    result.statusCode == HttpStatus.NOT_FOUND
    result.headers.getContentType() == contentType
    assert result.body.errors

    where:
    type << ['collection', 'granule', 'flattened-granule']
  }

  def 'Valid collection search request returns OK with expected results'() {
    given:
    def summaryFields = [
        'title', 'thumbnail', 'spatialBounding', 'beginDate', 'beginYear', 'endDate',
        'endYear', 'links', 'citeAsStatements'
    ]
    def endpointUri = "${baseUri}/search/collection".toURI()
    def request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ]
        }""".stripIndent()
    def requestEntity = RequestEntity.post(endpointUri).contentType(contentType).body(request)

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: 'Search returns OK'
    result.statusCode == HttpStatus.OK
    result.headers.getContentType() == contentType

    and: 'Expected results are returned'
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

    and: 'Returned summary fields only'
    items.each {
      assert it.attributes.keySet().containsAll(summaryFields)
      assert it.attributes.keySet().size() == summaryFields.size()
    }
  }

  def 'Valid #type search request returns OK with expected results'() {
    given:
    // Summary fields for granule types contain internalParentIdentifier field unlike collections
    def summaryFields = [
        'title', 'thumbnail', 'spatialBounding', 'beginDate', 'beginYear', 'endDate',
        'endYear', 'links', 'citeAsStatements', 'internalParentIdentifier'
    ]
    def endpointUri = "${baseUri}/search/${type}".toURI()
    def request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ]
        }""".stripIndent()
    def requestEntity = RequestEntity.post(endpointUri).contentType(contentType).body(request)

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: 'Search returns OK'
    result.statusCode == HttpStatus.OK
    result.headers.getContentType() == contentType

    and: 'Expected results are returned'
    def items = result.body.data
    items.size() == 2

    // TODO verify records somehow....

    and: 'Returned summary fields only'
    items.each {
      assert it.attributes.keySet().containsAll(summaryFields)
      assert it.attributes.keySet().size() == summaryFields.size()
    }

    where:
    type << ['granule', 'flattened-granule']
  }

  def 'Invalid search; #type endpoint returns BAD_REQUEST error when not conforming to schema'() {
    setup:
    def endpointUri = "${baseUri}/search/${type}".toURI()
    def invalidSchemaRequest = """\
        {
          "filters": [
            {"type": "dateTime", "before": "2012-01-01", "after": "2011-01-01"}
          ]
        }""".stripIndent()
    def requestEntity = RequestEntity
        .post(endpointUri)
        .contentType(contentType)
        .body(invalidSchemaRequest)

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: "Request invalid"
    result.statusCode == HttpStatus.BAD_REQUEST

    and: "Result contains errors and no data"
    assert result.body.errors
    result.body.errors instanceof List
    result.body.errors.every { it.status == '400' }
    assert !result.body.data

    where:
    type << ['collection', 'granule', 'flattened-granule']
  }

  def 'Invalid search; #type endpoint returns UNSUPPORTED_MEDIA_TYPE error when request body not specified as json content'() {
    setup:
    def endpointUri = "${baseUri}/search/${type}".toURI()
    def request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ]
        }""".stripIndent()
    def requestEntity = RequestEntity
        .post(endpointUri)
        .body(request)

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: "Bad request"
    result.statusCode == HttpStatus.UNSUPPORTED_MEDIA_TYPE
    result.headers.getContentType() == contentType

    and: "result contains errors and no data"
    assert result.body.errors
    result.body.errors instanceof List
    result.body.errors.every { it.status == '415' }
    assert !result.body.data

    where:
    type << ['collection', 'granule', 'flattened-granule']
  }

  def 'Invalid search; #type endpoint returns BAD_REQUEST error when no request body'() {
    setup:
    def endpointUri = "${baseUri}/search/${type}".toURI()
    def requestEntity = RequestEntity
        .post(endpointUri)
        .contentType(contentType)
        .body("")

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: "Bad request"
    result.statusCode == HttpStatus.BAD_REQUEST
    result.headers.getContentType() == contentType

    and: "Result contains errors and no data"
    assert result.body.errors
    result.body.errors instanceof List
    result.body.errors.every { it.status == '400' }
    assert !result.body.data

    where:
    type << ['collection', 'granule', 'flattened-granule']
  }

  def 'Invalid search; #type endpoint returns BAD_REQUEST error when request body is malformed json'() {
    setup:
    def endpointUri = "${baseUri}/search/${type}".toURI()
    def badJsonSearch = """\
        {
          "queries": [
            {"type": "queryText", "value": "}
          ]
        }""".stripIndent()
    def requestEntity = RequestEntity
        .post(endpointUri)
        .contentType(contentType)
        .body(badJsonSearch)

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: "Bad request"
    result.statusCode == HttpStatus.BAD_REQUEST
    result.headers.getContentType() == contentType

    and: "Result contains errors and no data"
    assert result.body.errors
    result.body.errors instanceof List
    result.body.errors.every { it.status == '400' }
    assert !result.body.data

    where:
    type << ['collection', 'granule', 'flattened-granule']
  }

  def 'Invalid search; #type endpoint returns BAD_REQUEST error when request body is invalid'() {
    setup:
    def endpointUri = "${baseUri}/search/${type}".toURI()
    def invalidJsonSearch = """\
        {
          "queries": [
            {"type": "NOTAREALTYPE", "value": "NONSENSE"}
          ]
        }""".stripIndent()
    def requestEntity = RequestEntity
        .post(endpointUri)
        .contentType(contentType)
        .body(invalidJsonSearch)

    when:
    def result = restTemplate.exchange(requestEntity, Map)

    then: "Bad request"
    result.statusCode == HttpStatus.BAD_REQUEST
    result.headers.getContentType() == contentType

    and: "Result contains errors and no data"
    assert result.body.errors
    result.body.errors instanceof List
    result.body.errors.every { it.status == '400' }
    assert !result.body.data

    where:
    type << ['collection', 'granule', 'flattened-granule']
  }
}
