package ncei.onestop.api

import ncei.onestop.api.controller.SearchController
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
class IntegrationTests extends Specification {

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
                def resource = cl.getResourceAsStream("data/${e}/${i}.json").text
                client.prepareIndex(INDEX, TYPE, UUID.randomUUID().toString()).setSource(resource).setRefresh(true).execute().actionGet()
            }
        }

        restTemplate = new RestTemplate()
        restTemplate.errorHandler = new TestResponseErrorHandler()
        searchBaseUri = "http://localhost:${port}/${contextPath}/search".toURI()
    }

    void cleanup() {
        client.admin().indices().prepareDelete(INDEX).execute().actionGet()
    }


    def 'valid search returns ok and results'() {
        setup:
        def baresearch = """\
        {
          "queries":
            [
              { "type": "queryText", "value": "temperature"}
            ]
        }""".stripIndent()
        def requestEntity = RequestEntity
                .post(searchBaseUri)
                .contentType(contentType)
                .body(baresearch)

        when:
        def result = restTemplate.exchange(requestEntity, Map)

        then: "Search ok"
        result.statusCode == HttpStatus.OK
        result.headers.getContentType() == contentType
        and: "result contains > 0 items"
        def items = result.body.data
        items.size() > 0
        and: "each item has id, type, and attributes"
        items.every { item ->
            item.id instanceof String &&
                    item.type instanceof String &&
                    item.attributes instanceof Map
        }
    }
}
