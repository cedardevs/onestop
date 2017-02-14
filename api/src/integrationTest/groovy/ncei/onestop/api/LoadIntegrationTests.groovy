package ncei.onestop.api

import ncei.onestop.api.service.ETLService
import ncei.onestop.api.service.SearchIndexService
import ncei.onestop.api.service.MetadataIndexService
import org.elasticsearch.client.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@Unroll
@ActiveProfiles("integration")
@SpringBootTest(classes = [Application, IntegrationTestConfig], webEnvironment = RANDOM_PORT)
class LoadIntegrationTests extends Specification {

  @Autowired
  private Client client

  @Autowired
  private SearchIndexService searchIndexService

  @Autowired
  private MetadataIndexService metadataIndexService

  @Autowired
  private ETLService etlService

  @Value('${local.server.port}')
  private String port

  @Value('${server.context-path}')
  private String contextPath

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.name}')
  private String INDEX

  @Value('${elasticsearch.index.staging.collectionType}')
  private String TYPE

  RestTemplate restTemplate
  URI loadURI
  URI searchURI

  private final String searchQuery = '{"queries":[]}'

  void setup() {
    searchIndexService.recreate()
    metadataIndexService.recreate()

    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()
    loadURI = "http://localhost:${port}/${contextPath}/metadata".toURI()
    searchURI = "http://localhost:${port}/${contextPath}/search".toURI()
  }

  def 'Document is stored, then searchable on reindex'() {
    setup:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("data/GHRSST/1.xml").text
    def loadRequest = RequestEntity.post(loadURI).contentType(MediaType.APPLICATION_XML).body(document)
    def searchRequest = RequestEntity.post(searchURI).contentType(MediaType.APPLICATION_JSON).body(searchQuery)

    when:
    def loadResult = restTemplate.exchange(loadRequest, Map)
    metadataIndexService.refresh()

    then: "Load returns CREATED"
    loadResult.statusCode == HttpStatus.CREATED

    and: "Storage index contains loaded document"
    def docId = loadResult.body.data.id
    def getRequest = RequestEntity.get("http://localhost:${port}/${contextPath}/metadata/${docId}.json".toURI()).build()
    def getResult = restTemplate.exchange(getRequest, Map)
    getResult.body?.data?.id == docId

    when: "Update search index then search"
    searchIndexService.refresh()
    def searchResult = restTemplate.exchange(searchRequest, Map)
    def hits = searchResult.body.data

    then: "Does not appear in search results yet"
    hits.size() == 0

    when: "Reindex then search"
    etlService.rebuildSearchIndex()
    searchResult = restTemplate.exchange(searchRequest, Map)
    hits = searchResult.body.data

    then:
    hits.size() == 1
    def fileId = hits.attributes[0].fileIdentifier
    fileId == 'gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED'

    when: "Document is deleted though api"
    getResult = restTemplate.exchange(getRequest, Map)
    assert getResult.body?.data?.id == docId
    def deleteRequest = RequestEntity.delete("http://localhost:${port}/${contextPath}/metadata/${docId}.json".toURI()).build()
    def deleteResult = restTemplate.exchange(deleteRequest, Map)
    getResult = restTemplate.exchange(getRequest, Map)
    searchResult = restTemplate.exchange(searchRequest, Map)
    metadataIndexService.refresh()

    then: "Document is deleted in staging and search indices"
    deleteResult.body.attributes.successes.count { it.found == true } == 3
    deleteResult.body.attributes.successes.count { it.index == 'search'} == 2  // Collection & synthesized granule
    deleteResult.body.attributes.successes.count { it.index == 'staging'} == 1
    getResult.statusCode.value() == 404
    searchResult.body.data.size() == 0
  }

  def 'Documents loaded are pulled into search index after an ETL update'() {
    setup:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("data/GHRSST/1.xml").text
    def loadRequest = RequestEntity.post(loadURI).contentType(MediaType.APPLICATION_XML).body(document)

    when: "First document is loaded"
    def loadResult = restTemplate.exchange(loadRequest, Map)

    then: "Load returns CREATED"
    loadResult.statusCode == HttpStatus.CREATED

    when: "Reindex then search"
    etlService.rebuildSearchIndex()
    def searchRequest = RequestEntity.post(searchURI).contentType(MediaType.APPLICATION_JSON).body(searchQuery)
    def searchResult = restTemplate.exchange(searchRequest, Map)
    def hits = searchResult.body.data

    then: "Document shows up in search results"
    hits.size() == 1
    def fileId = hits.attributes[0].fileIdentifier
    fileId == 'gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED'

    when: "Another document is loaded"
    document = ClassLoader.systemClassLoader.getResourceAsStream("data/GHRSST/2.xml").text
    loadRequest = RequestEntity.post(loadURI).contentType(MediaType.APPLICATION_XML).body(document)
    loadResult = restTemplate.exchange(loadRequest, Map)

    then: "Load returns CREATED"
    loadResult.statusCode == HttpStatus.CREATED

    when: "Search resent"
    searchResult = restTemplate.exchange(searchRequest, Map)
    hits = searchResult.body.data
    fileId = hits?.attributes[0]?.fileIdentifier

    then: "Search is unchanged"
    hits.size() == 1
    fileId == 'gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED'

    when: "ETL updateSearchIndex requested"
    etlService.updateSearchIndex()
    searchResult = restTemplate.exchange(searchRequest, Map)
    hits = searchResult.body.data

    then: "New document appears in search results"
    def ids = hits.collect { it.attributes.fileIdentifier }
    hits.size() == 2
    ids.containsAll([
        'gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED',
        'gov.noaa.nodc:GHRSST-OSDPD-L2P-MTSAT1R'
    ])
  }

  def 'Multiple documents are ingested through bulk upload'() {
    setup:
    restTemplate.getMessageConverters().add(new FormHttpMessageConverter())
    def parts = new LinkedMultiValueMap<String, Object>()
    parts.add("files", new ClassPathResource("data/GHRSST/1.xml"))
    parts.add("files", new ClassPathResource("data/GHRSST/2.xml"))
    parts.add("files", new ClassPathResource("data/BadFiles/montauk_forecastgrids_2013.xml"))

    when:
    def loadResult = restTemplate.postForEntity(loadURI, parts, Map)
    metadataIndexService.refresh()

    then: "Load returns MULTI-STATUS"
    loadResult.statusCode == HttpStatus.MULTI_STATUS

    and: "Load returns 3 records in data"
    loadResult.body.data.size == 3

    and: "Records in expected order with expected status codes "
    loadResult.body.data[0].attributes.status == HttpStatus.CREATED.value()
    loadResult.body.data[1].attributes.status == HttpStatus.CREATED.value()
    loadResult.body.data[2].attributes.status == HttpStatus.BAD_REQUEST.value()
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
    def body = loadResult.body
    println body
    body.errors?.any { it.title == 'Bad Request' }
    body.errors?.any { it.detail.contains('gov.noaa.ngdc.mgg.dem: montauk_forecastgrids_2013') }
  }

  def 'Orphan granules are not indexed for searching'() {
    setup:
    // COOPS/O1.xml is an orphan: it's parentIdentified doesn't match anything
    def document = ClassLoader.systemClassLoader.getResourceAsStream("data/COOPS/O1.xml").text
    def loadRequest = RequestEntity.post(loadURI).contentType(MediaType.APPLICATION_XML).body(document)
    def searchRequest = RequestEntity.post(searchURI).contentType(MediaType.APPLICATION_JSON).body(searchQuery)

    when:
    def loadResult = restTemplate.exchange(loadRequest, Map)
    metadataIndexService.refresh()
    etlService.rebuildSearchIndex()
    searchIndexService.refresh()
    def hits = restTemplate.exchange(searchRequest, Map).body.data

    then:
    loadResult.statusCode == HttpStatus.CREATED
    hits.size() == 0
  }

  def 'Collections with no granules are indexed for searching'() {
    setup:
    // GHRSST/1.xml is a collection with no granules
    def document = ClassLoader.systemClassLoader.getResourceAsStream("data/GHRSST/1.xml").text
    def loadRequest = RequestEntity.post(loadURI).contentType(MediaType.APPLICATION_XML).body(document)
    def searchRequest = RequestEntity.post(searchURI).contentType(MediaType.APPLICATION_JSON).body(searchQuery)

    when:
    def loadResult = restTemplate.exchange(loadRequest, Map)
    metadataIndexService.refresh()
    etlService.rebuildSearchIndex()
    searchIndexService.refresh()
    def hits = restTemplate.exchange(searchRequest, Map).body.data

    then:
    loadResult.statusCode == HttpStatus.CREATED
    hits.size() == 1
    hits[0].attributes.fileIdentifier == 'gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED'
  }

  def 'Granules are merged with their collections and indexed for searching'() {
    setup:
    // COOPS/C1.xml is a collection, G1.xml and G2.xml are granules belonging to it
    def documents = [
        ClassLoader.systemClassLoader.getResourceAsStream("data/COOPS/C1.xml").text,
        ClassLoader.systemClassLoader.getResourceAsStream("data/COOPS/G1.xml").text,
        ClassLoader.systemClassLoader.getResourceAsStream("data/COOPS/G2.xml").text
    ]
    def loadRequests = documents.collect { document ->
      RequestEntity.post(loadURI).contentType(MediaType.APPLICATION_XML).body(document)
    }
    def searchRequestC = RequestEntity.post(searchURI).contentType(MediaType.APPLICATION_JSON).body(searchQuery)
    def searchRequestG = RequestEntity.post(searchURI).contentType(MediaType.APPLICATION_JSON)
        .body('{"filters":[{"type":"collection", "values":["gov.noaa.nodc:NDBC-COOPS"]}]}')

    when:
    def loadResults = loadRequests.collect { restTemplate.exchange(it, Map) }
    metadataIndexService.refresh()
    etlService.rebuildSearchIndex()
    searchIndexService.refresh()
    def hitsC = restTemplate.exchange(searchRequestC, Map).body.data
    def hitsG = restTemplate.exchange(searchRequestG, Map).body.data

    then: 'two merged granule + collection documents have been indexed along with collection document'
    loadResults.every { it.statusCode == HttpStatus.CREATED }
    hitsC.size() == 1
    def c1Record = hitsC.find { it.attributes.fileIdentifier == 'gov.noaa.nodc:NDBC-COOPS' }
    c1Record != null

    hitsG.size() == 2
    def g1Record = hitsG.find { it.attributes.fileIdentifier == 'CO-OPS.NOS_8638614_201602_D1_v00' }
    def g2Record = hitsG.find { it.attributes.fileIdentifier == 'CO-OPS.NOS_9410170_201503_D1_v00' }
    g1Record != null
    g2Record != null

    and: 'they contain the attribute values from the granule records when present'
    g1Record.attributes.temporalBounding.beginDate == '2016-02-01'
    g1Record.attributes.temporalBounding.endDate == '2016-02-29'
    g2Record.attributes.temporalBounding.beginDate == '2015-03-01'
    g2Record.attributes.temporalBounding.endDate == '2015-03-31'
  }

}
