package ncei.onestop.api

import ncei.onestop.api.service.ElasticsearchService
import org.elasticsearch.action.get.GetRequest
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
@ContextConfiguration(loader = SpringApplicationContextLoader, classes = [Application, IntegrationTestConfig])
class LoadIntegrationTests extends Specification {

  @Autowired
  private Client client

  @Autowired
  private ElasticsearchService elasticsearchService

  @Value('${local.server.port}')
  private String port

  @Value('${server.context-path}')
  private String contextPath

  @Value('${elasticsearch.index.storage.name}')
  private String INDEX

  @Value('${elasticsearch.index.storage.collectionType}')
  private String TYPE

  RestTemplate restTemplate
  URI loadURI
  URI refreshURI
  URI searchURI

  private final String searchQuery = '{"queries":[{"type":"queryText","value":"temperature"}]}'

  void setup() {
    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()
    loadURI = "http://localhost:${port}/${contextPath}/load".toURI()
    refreshURI = "http://localhost:${port}/${contextPath}/load/refresh".toURI()
    searchURI = "http://localhost:${port}/${contextPath}/search".toURI()
  }

  void cleanup() {
    elasticsearchService.purgeIndex()
  }

  def 'Document is stored, then searchable on reindex'() {
    setup:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("data/GHRSST/1.xml").text
    def loadRequest = RequestEntity.post(loadURI).contentType(MediaType.APPLICATION_XML).body(document)
    def searchRequest = RequestEntity.post(searchURI).contentType(MediaType.APPLICATION_JSON).body(searchQuery)

    when:
    def loadResult = restTemplate.exchange(loadRequest, Map)

    then: "Load returns CREATED"
    loadResult.statusCode == HttpStatus.CREATED

    and: "Storage index contains loaded document"
    def docId = loadResult.body.data.id
    client.get(new GetRequest(INDEX, TYPE, docId)).actionGet().exists

    when: "Refresh elasticsearch then search"
    elasticsearchService.refresh()
    def searchResult = restTemplate.exchange(searchRequest, Map)
    def hits = searchResult.body.data

    then: "Does not appear in search results yet"
    hits.size() == 0

    when: "Reindex storage then search"
    elasticsearchService.reindex()
    elasticsearchService.refresh()
    searchResult = restTemplate.exchange(searchRequest, Map)
    hits = searchResult.body.data

    then:
    hits.size() == 1
    def fileId = hits.attributes[0].fileIdentifier
    fileId == 'gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED'
  }

  def 'Document rejected when whitespace found in fileIdentifier'() {
    setup:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("data/BadFiles/montauk_forecastgrids_2013.xml").text
    def loadRequest = RequestEntity.post(loadURI).contentType(MediaType.APPLICATION_XML).body(document)

    when:
    def loadResult = restTemplate.exchange(loadRequest, Map)

    then: "Load returns BAD_REQUEST"
    loadResult.statusCode == HttpStatus.BAD_REQUEST

    and: "Erroneous file identifier specified"
    loadResult.body.errors.detail == 'gov.noaa.ngdc.mgg.dem: montauk_forecastgrids_2013'
  }
}
