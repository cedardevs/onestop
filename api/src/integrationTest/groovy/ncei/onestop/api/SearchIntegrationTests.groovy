package ncei.onestop.api

import ncei.onestop.api.controller.SearchController
import ncei.onestop.api.service.MetadataParser

import static ncei.onestop.api.IntegrationTestConfig.*
import org.elasticsearch.client.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
@WebIntegrationTest
@ActiveProfiles("integration")
@ContextConfiguration(loader = SpringApplicationContextLoader,
        classes = [Application, IntegrationTestConfig])
class SearchIntegrationTests extends Specification {

    @Autowired
    private Client client

    @Autowired
    private SearchController searchController

    @Value('${local.server.port}')
    String port

    @Value('${server.context-path}')
    String contextPath

    static MediaType contentType = MediaType.APPLICATION_JSON_UTF8

    RestTemplate restTemplate
    URI searchBaseUri


    void setup() {
        def cl = ClassLoader.systemClassLoader

        // Initialize index:
        def indexSettings = cl.getResourceAsStream("config/index-settings.json").text
        client.admin().indices().prepareCreate(INDEX).setSettings(indexSettings).execute().actionGet()
        client.admin().cluster().prepareHealth(INDEX).setWaitForActiveShards(1).execute().actionGet()

        // Initialize mapping & load data:
        def mapping = cl.getResourceAsStream("config/item-mapping.json").text
        client.admin().indices().preparePutMapping(INDEX).setSource(mapping).setType(TYPE).execute().actionGet()

        def datasets = ['GHRSST', 'DEM']
        for(e in datasets) {
            for(i in 1..3) {
                def metadata = cl.getResourceAsStream("data/${e}/${i}.xml").text
                def resource = MetadataParser.parseXMLMetadata(metadata)
                client.prepareIndex(INDEX, TYPE).setSource(resource).setRefresh(true).execute().actionGet()
            }
        }

        restTemplate = new RestTemplate()
        restTemplate.errorHandler = new TestResponseErrorHandler()
        searchBaseUri = "http://localhost:${port}/${contextPath}/search".toURI()
    }

    void cleanup() {
        client.admin().indices().prepareDelete(INDEX).execute().actionGet()
    }


    def 'Valid query-only search returns OK with expected results'() {
        setup:
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
    }

    /* FIXME Does not work; should test filter other than datetime once implemented */
    def 'Valid filter-only search returns OK with expected results'() {
        setup:
        def request = """\
        {
          "filters":
            [
              { "type": "facet", "name": "keywords.keywordText.raw", "values": ["Aleutian Islands", "Global"]}
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
        def actualIds = items.collect { it.attributes.fileIdentifier }
        actualIds.containsAll([
                'gov.noaa.ngdc.mgg.dem:258',
                'gov.noaa.nodc:GHRSST-Geo_Polar_Blended_Night-OSPO-L4-GLOB'
        ])
    }


    def 'Valid query-and-filter search returns OK with expected result'() {
        setup:
        def request = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ],
          "filters":
            [
              {"type": "datetime", "before": "2007-12-31T23:59:59.999Z", "after": "2007-01-01T00:00:00Z"}
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

        and: "Result contains 1 item"
        def items = result.body.data
        items.size() == 1

        and: "Expected result is returned"
        def actualIds = items.collect { it.attributes.fileIdentifier }
        actualIds.containsAll([
                'gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED'
        ])
    }


    /* TODO Happy path test cases:
        'Valid search returns OK when sort and page elements specified with expected results'

            def searchqueriesfiltersformatting = """\
{
  "queries": [
    {"type": "queryText", "value": "temperature"}
  ],
  "filters":[
    {"type": "facet", "name": "apiso_TopicCategory_s", "values": ["oceanography", "oceans"]},
    {"type": "datetime", "before": "2016-06-15T20:20:58Z", "after": "2015-09-22T10:30:06.000Z"}
  ],
  "sort": "relevance",
  "page": {"number": 1, "size": 10}
}"""


     */

    def 'invalid search returns errors when not conforming to schema'() {
        setup:
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
        result.body.errors.every { it.status == '400'}
        result.body.errors.every { it.detail instanceof String }
    }

    def 'invalid search returns returns error, need to specify body is json content type'() {
        setup:
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

    def 'invalid search returns returns error, need to specify json body'() {
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

    def 'invalid search returns returns error, json body not parseable'() {
        setup:
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
        String error = result.body.error
        error.contains("Bad Request")
    }
}
