package ncei.onestop.api

import static ncei.onestop.api.IntegrationTestConfig.*
import ncei.onestop.api.controller.SearchController
import ncei.onestop.api.service.MetadataParser
import org.elasticsearch.client.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
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
class ExperimentalMappingsTest extends Specification {

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
        def indexSettings = cl.getResourceAsStream("config/experimental_index-settings.json").text
        client.admin().indices().prepareCreate(INDEX).setSettings(indexSettings).execute().actionGet()
        client.admin().cluster().prepareHealth(INDEX).setWaitForActiveShards(1).execute().actionGet()

        // Initialize mapping & load data:
        def mapping = cl.getResourceAsStream("config/experimental_mappings.json").text
        client.admin().indices().preparePutMapping(INDEX).setSource(mapping).setType(TYPE).execute().actionGet()

        def datasets = ['GHRSST', 'DEM']
        for(e in datasets) {
            for(i in 1..3) {
                def metadata = cl.getResourceAsStream("data/${e}/${i}.xml").text
                def resource = MetadataParser.parseXMLMetadata(metadata)
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

    /*
     Setup just to show mappings & MetadataParser work
     */
    def 'nothing test strikes again'() {
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

        then:
        1 == 1
    }
}
