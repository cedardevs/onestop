package org.cedar.onestop.api.search

import groovy.json.JsonOutput
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
class SearchIntegrationTests extends Specification {

  @Autowired
  RestClient restClient

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
  private String searchFlattenedGranuleUriString
  private String collectionBaseUriString
  private String granuleBaseUriString
  private String flatGranuleBaseUriString

  void setup() {
    def cl = ClassLoader.systemClassLoader
    def searchCollectionIndexJson = cl.getResourceAsStream('search_collectionIndex.json').text
    def searchGranuleIndexJson = cl.getResourceAsStream('search_granuleIndex.json').text
    def searchFlattenedGranuleIndexJson = cl.getResourceAsStream('search_flattened_granuleIndex.json').text

    def collectionIndexSettings = new NStringEntity(searchCollectionIndexJson, ContentType.APPLICATION_JSON)
    def granuleIndexSettings = new NStringEntity(searchGranuleIndexJson, ContentType.APPLICATION_JSON)
    def flattenedGranuleIndexSettings = new NStringEntity(searchFlattenedGranuleIndexJson, ContentType.APPLICATION_JSON)

    Response response = restClient.performRequest('DELETE', '_all')
    println("DELETE _all: ${response}")

    def collectionResponse = restClient.performRequest('PUT', COLLECTION_SEARCH_INDEX, Collections.EMPTY_MAP, collectionIndexSettings)
    println("PUT new collection index: ${collectionResponse}")

    def granuleResponse = restClient.performRequest('PUT', GRANULE_SEARCH_INDEX, Collections.EMPTY_MAP, granuleIndexSettings)
    println("PUT new granule index: ${granuleResponse}")

    def flattenedGranuleResponse = restClient.performRequest('PUT', FLATTENED_GRANULE_SEARCH_INDEX, Collections.EMPTY_MAP, flattenedGranuleIndexSettings)
    println("PUT new flattened-granule index: ${flattenedGranuleResponse}")

    Map data = [
      'DEM': [
        'C1': [
          id: 'e7a36e60-1bcb-47b1-ac0d-3c2a2a743f9b',
          granules: [],
          flattenedGranules: []
        ],
        'C2': [
          id: 'e5820283-3686-44d0-8edd-28a086eb500e',
          granules: [],
          flattenedGranules: []
        ],
        'C3': [
          id: '1415b3db-c602-4dbb-a502-4091fe9df1cf',
          granules: [],
          flattenedGranules: []
        ],
      ],
      'GHRSST': [
        'C1': [
          id: '920d8155-f764-4777-b7e5-14442b7275b8',
          granules: [],
          flattenedGranules: []
        ],
        'C2': [
          id: '882511bc-e99e-4597-b634-47a59ddf9fda',
          granules: [],
          flattenedGranules: []
        ],
        'C3': [
          id: '42ea683d-e4e7-434c-8823-abff32e00f34',
          granules: [],
          flattenedGranules: []
        ],
      ],
      'COOPS': [
        'C1': [
          id: 'fcf83ec9-964b-45b9-befe-378ea6ce52cb',
          granules: [
            'G1': [id: '783089c4-3484-4f70-ac8d-d4818d0cd0dd'],
            'G2': [id: 'a207b48f-29fc-4d79-a676-1f265cd9971f'],
          ],
          flattenedGranules: [
              'FG1': [id: '783089c4-3484-4f70-ac8d-d4818d0cd0dd'],
              'FG2': [id: 'a207b48f-29fc-4d79-a676-1f265cd9971f']
          ]
        ]
      ]
    ]

    data.each{ name, dataset ->
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

//        collectionData.flattenedGranules.each { flattenedGranule, flattenedGranuleData ->
//          metadata = cl.getResourceAsStream("data/${name}/${flattenedGranule}.json").text
//          def flattenedGranuleEndpoint = "/$FLATTENED_GRANULE_SEARCH_INDEX/$TYPE/$flattenedGranuleData.id"
//          record = new NStringEntity(metadata, ContentType.APPLICATION_JSON)
//          response = restClient.performRequest('PUT', flattenedGranuleEndpoint, Collections.EMPTY_MAP, record)
//          println("PUT new flattened granule: ${response}")
//        }
      }
    }

    def refreshEndpoint = "/${COLLECTION_SEARCH_INDEX},${GRANULE_SEARCH_INDEX},${FLATTENED_GRANULE_SEARCH_INDEX}/_refresh"
    response = restClient.performRequest('POST', refreshEndpoint)
    println("Refresh all search indices: ${response}")

    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()
    searchBaseUriString = "http://localhost:${port}/${contextPath}/search/"
    searchCollectionUriString = "http://localhost:${port}/${contextPath}/search/collection"
    searchGranuleUriString = "http://localhost:${port}/${contextPath}/search/granule"
    searchFlattenedGranuleUriString = "http://localhost:${port}/${contextPath}/search/flattened-granule"
    collectionBaseUriString = "http://localhost:${port}/${contextPath}/collection/"
    granuleBaseUriString = "http://localhost:${port}/${contextPath}/granule/"
    flatGranuleBaseUriString = "http://localhost:${port}/${contextPath}/flattened-granule/"
  }


  def 'Valid query-only collection search summary with facets returns OK with expected results'() {
    setup:
    def searchBaseUri = (searchCollectionUriString).toURI()
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

    and: "Result contains 4 items"
    def items = result.body.data
    items.size() == 4

    and: "Expected results are returned"
    def beginDates = items.collect { it.attributes.beginDate }
    beginDates.sort().equals([
        '2014-06-02T00:00:00.000Z',
        '2013-03-01T00:00:00.000Z',
        '2005-01-30T00:00:00.000Z',
        '2009-11-22T00:00:00.000Z'
    ].sort())
    def citeAsStatements = items.collect { it.attributes.citeAsStatements }
    citeAsStatements.sort().equals([
        ['Cite as: US DOC; NOAA; NESDIS; Office of Satellite and Product Operations (OSPO) (2014). GHRSST Level 4 OSPO Global Nighttime Foundation Sea Surface Temperature Analysis (GDS version 2). National Oceanographic Data Center, NOAA. Dataset. [access date]'],
        ['Cite as: Hervey, R. V. and US DOC; NOAA; NWS; National Data Buoy Center (2013). Coastal meteorological and water temperature data from National Water Level Observation Network (NWLON) and Physical Oceanographic Real-Time System (PORTS) stations of the NOAA Center for Operational Oceanographic Products and Services (CO-OPS). National Oceanographic Data Center, NOAA. Dataset. [access date]'],
        ['Cite as: Medspiration (2005). GHRSST Level 4 EUR Mediterranean Sea Regional Foundation Sea Surface Temperature Analysis (GDS version 2). National Oceanographic Data Center, NOAA. Dataset. [access date]'],
        ['Cite as: US DOC; NOAA; NESDIS; Office of Satellite Data Processing and Distribution (OSDPD) (2009). GHRSST Level 2P Western Pacific Regional Skin Sea Surface Temperature from the Multifunctional Transport Satellite 1R (MTSAT-1R) (GDS version 1). National Oceanographic Data Center, NOAA. Dataset. [access date]']
    ].sort())
    def thumbnail = items.collect { it.attributes.thumbnail }
    thumbnail.sort().equals([
        'http://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:GHRSST-Geo_Polar_Blended_Night-OSPO-L4-GLOB',
        'http://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:NDBC-COOPS',
        'http://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED',
        'http://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:GHRSST-OSDPD-L2P-MTSAT1R'
    ].sort())
    def endDates = items.collect { it.attributes.endDates }
    endDates.sort().equals([
        null,
        null,
        null,
        null
    ].sort())
    def beginYears = items.collect { it.attributes.beginYears }
    beginYears.sort().equals([
        null,
        null,
        null,
        null
    ].sort())
    def spatialBoundings = items.collect { it.attributes.spatialBounding }
    spatialBoundings.collect{ it.toString() }.sort().equals([
        [coordinates:[[[-180, -90], [180, -90], [180, 90], [-180, 90], [-180, -90]]], type: 'Polygon'],
        [coordinates:[[[144.657, -14.28], [-61.821, -14.28], [-61.821, 70.4], [144.657, 70.4], [144.657, -14.28]]], type: 'Polygon'],
        [coordinates:[[[-5.99, 30.01], [36.49, 30.01], [36.49, 45.99], [-5.99, 45.99], [-5.99, 30.01]]], type: 'Polygon'],
        [coordinates:[[[60, -73], [-143, -73], [-143, 73], [60, 73], [60, -73]]], type: 'Polygon']
    ].collect{ it.toString() }.sort())

    // link data is a bit more gnarly, so we'll just compare the number of links
    def links = items.collect { it.attributes.links }
    links.size() == 4
    def endYears = items.collect { it.attributes.endYears }
    endYears.sort().equals([
        null,
        null,
        null,
        null
    ].sort())

    // if we end up with more keys per result than summary should contain, summary source filter is not working!
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
          "citeAsStatements"
      ].size()
    }

    and: 'The correct number of facets is returned'
    def aggs = result.body.meta.facets
    aggs.size() == 9

    and: 'The facets are as expected'
    aggs.science != null
    aggs.services != null
    aggs.instruments != null
    aggs.platforms != null
    aggs.projects != null
    aggs.dataCenters != null
    aggs.horizontalResolution != null
    aggs.verticalResolution != null
    aggs.temporalResolution != null
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
    aggs.size() == 9
//    aggs.size() == 10

    and: 'The facets are as expected'
    aggs.science != null
    aggs.services != null
//    aggs.locations != null
    aggs.instruments != null
    aggs.platforms != null
    aggs.projects != null
    aggs.dataCenters != null
    aggs.horizontalResolution != null
    aggs.verticalResolution != null
    aggs.temporalResolution != null

//    and: 'The cleaned aggregations are actually cleaned'
//    def locationTerms = aggs.locations.collect { it }
//    // Bad planted keywords should be removed
//    !locationTerms.contains('Alaska')
//    !locationTerms.contains('Alaska > Unalaska')
  }

  def 'Valid query-only granule summary search with facets returns OK with expected results'() {
    setup:
    def searchBaseUri = (searchGranuleUriString).toURI()
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

    and: "Result contains 2 items"
    def items = result.body.data
    items.size() == 2

    and: "Expected results are returned"
    def beginDates = items.collect { it.attributes.beginDate }
    beginDates.sort().equals([
        '2015-03-01T00:00:00.000Z',
        '2016-02-01T00:00:00.000Z'
    ].sort())
    def citeAsStatements = items.collect { it.attributes.citeAsStatements }
    citeAsStatements.sort().equals([
        [],
        []
    ].sort())
    def thumbnail = items.collect { it.attributes.thumbnail }
    thumbnail.sort().equals([
        'http://maps.googleapis.com/maps/api/staticmap?center=32.714,-117.174&zoom=14&scale=false&size=600x600&maptype=terrain&format=png&visual_refresh=true&markers=color:red%7C32.714,-117.174&stream=true&stream_ID=plot_image',
        'http://maps.googleapis.com/maps/api/staticmap?center=36.977,-76.315&zoom=14&scale=false&size=600x600&maptype=terrain&format=png&visual_refresh=true&markers=color:red%7C36.977,-76.315&stream=true&stream_ID=plot_image',
    ].sort())
    def endDates = items.collect { it.attributes.endDates }
    endDates.sort().equals([
        null,
        null
    ].sort())
    def beginYears = items.collect { it.attributes.beginYears }
    beginYears.sort().equals([
        null,
        null
    ].sort())
    def spatialBoundings = items.collect { it.attributes.spatialBounding }
    spatialBoundings.collect{ it.toString() }.sort().equals([
        [coordinates:[-76.315, 36.977], type: 'Point'],
        [coordinates:[-117.174, 32.714], type: 'Point']
    ].collect{ it.toString() }.sort())

    // link data is a bit more gnarly, so we'll just compare the number of links
    def links = items.collect { it.attributes.links }
    links.size() == 2
    def endYears = items.collect { it.attributes.endYears }
    endYears.sort().equals([
        null,
        null
    ].sort())

    // if we end up with more keys per result than summary should contain, summary source filter is not working!
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

    and: 'The correct number of facets is returned'
    def aggs = result.body.meta.facets
    aggs.size() == 9

    and: 'The facets are as expected'
    aggs.science != null
    aggs.services != null
    aggs.instruments != null
    aggs.platforms != null
    aggs.projects != null
    aggs.dataCenters != null
    aggs.horizontalResolution != null
    aggs.verticalResolution != null
    aggs.temporalResolution != null
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
    aggs.size() == 9
//    aggs.size() == 10

    and: 'The facets are as expected'
    aggs.science != null
    aggs.services != null
//    aggs.locations != null
    aggs.instruments != null
    aggs.platforms != null
    aggs.projects != null
    aggs.dataCenters != null
    aggs.horizontalResolution != null
    aggs.verticalResolution != null
    aggs.temporalResolution != null
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
    def searchBaseUri = (searchFlattenedGranuleUriString).toURI()
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
    def searchBaseUri = (searchCollectionUriString).toURI()
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
    def searchBaseUri = (searchGranuleUriString).toURI()
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

  def 'collection and granule endpoints report counts of collections and granules'() {
    setup:
    def searchCollectionBaseUri = (collectionBaseUriString).toURI()
    def searchGranuleBaseUri = (granuleBaseUriString).toURI()
    def requestEntityCollection = RequestEntity.get(searchCollectionBaseUri).build()
    def requestEntityGranule = RequestEntity.get(searchGranuleBaseUri).build()

    when:
    def resultCollection = restTemplate.exchange(requestEntityCollection, Map)
    def resultGranule = restTemplate.exchange(requestEntityGranule, Map)

    then:
    resultCollection.statusCode == HttpStatus.OK
    resultCollection.headers.getContentType() == contentType
    resultCollection.body.data[0].count == 7
    resultCollection.body.data*.id.containsAll(['collection'])
    resultCollection.body.data*.count.every({ it instanceof Number })

    resultGranule.statusCode == HttpStatus.OK
    resultGranule.headers.getContentType() == contentType
    resultGranule.body.data[0].count == 2
    resultGranule.body.data*.id.containsAll(['granule'])
    resultGranule.body.data*.count.every({ it instanceof Number })
  }

  def 'collection GET request returns expected record and count of granules in collection'() {
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
}
