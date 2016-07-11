package ncei.onestop.api

import ncei.onestop.api.service.MetadataParser
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest

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

    @Value('${local.server.port}')
    private String port

    @Value('${server.context-path}')
    private String contextPath

    private MediaType contentType = MediaType.APPLICATION_JSON_UTF8
    private List datasets = ['GHRSST', 'DEM']

    private RestTemplate restTemplate
    private URI searchBaseUri


    void setup() {
        def cl = ClassLoader.systemClassLoader

        def bulkLoad = new BulkRequest()
        for(e in datasets) {
            for(i in 1..3) {
                def metadata = cl.getResourceAsStream("data/${e}/${i}.xml").text
                def resource = MetadataParser.parseXMLMetadata(metadata)
                bulkLoad.add(new IndexRequest(INDEX, TYPE, "${e}${i}").source(resource))
            }
        }
        bulkLoad.refresh(true)
        client.bulk(bulkLoad).actionGet()

        restTemplate = new RestTemplate()
        restTemplate.errorHandler = new TestResponseErrorHandler()
        searchBaseUri = "http://localhost:${port}/${contextPath}/search".toURI()
    }

    void cleanup() {
        def items = client.search(new SearchRequest(INDEX).types(TYPE)).actionGet()
        def ids = items.hits.hits*.id
        def bulkDelete = ids.inject(new BulkRequest()) {bulk, id ->
            bulk.add(new DeleteRequest(INDEX, TYPE, id))
        }
        bulkDelete.refresh(true)
        client.bulk(bulkDelete)
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

    def 'Valid filter-only search returns OK with expected results'() {
        setup:
        def request = """\
        {
          "filters":
            [
              {"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [12.34, 145.5]}}
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
                'gov.noaa.nodc:GHRSST-Geo_Polar_Blended_Night-OSPO-L4-GLOB',
                'gov.noaa.ngdc.mgg.dem:4870'
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

    def 'Invalid search; returns BAD_REQUEST error when not conforming to schema'() {
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

    def 'Invalid search; returns UNSUPPORTED_MEDIA_TYPE error when request body not specified as json content'() {
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

    def 'Invalid search; returns BAD_REQUEST error when no request body'() {
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

    def 'Invalid search; returns BAD_REQUEST error when request body is invalid json'() {
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
