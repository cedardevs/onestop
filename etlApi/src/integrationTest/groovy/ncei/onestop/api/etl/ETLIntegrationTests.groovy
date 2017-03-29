package ncei.onestop.api.etl

import ncei.onestop.api.Application
import org.elasticsearch.client.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
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
class ETLIntegrationTests extends Specification {

  RestTemplate restTemplate
  URI loadURI
  URI updateSearchURI
  URI rebuildSearchURI

  @Autowired
  private Client client

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.name}')
  private String SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.name}')
  private String STAGING_INDEX

  @Value('${elasticsearch.index.search.collectionType}')
  private String COLLECTION_TYPE

  @Value('${elasticsearch.index.search.granuleType}')
  private String GRANULE_TYPE

  @Value('${local.server.port}')
  private String port

  @Value('${server.context-path}')
  private String contextPath

  void setup() {
    def baseURI = "http://localhost:${port}/${contextPath}/"
    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()

    loadURI = (baseURI + "metadata").toURI()
    updateSearchURI = (baseURI + "admin/index/search/update").toURI()
    rebuildSearchURI = (baseURI + "admin/index/search/rebuild").toURI()

    def recreateMetadataURI = (baseURI + "admin/index/metadata/recreate?sure=true").toURI()
    def recreateSearchURI = (baseURI + "admin/index/search/recreate?sure=true").toURI()

    executeGetRequest(recreateMetadataURI)
    executeGetRequest(recreateSearchURI)

    sleep(1000)
  }

  def 'update does nothing when staging is empty'() {
    when:
    executeGetRequest(updateSearchURI)
    sleep(1000)

    then:
    noExceptionThrown()

    and:
    indexedCollectionVersions().size() == 0
    indexedGranuleVersions().size() == 0
  }

  def 'updating a new collection indexes a collection and a synthesized granule'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')

    when:
    executeGetRequest(updateSearchURI)
    sleep(1000)

    then:
    indexedCollectionVersions().keySet() == ['gov.noaa.nodc:NDBC-COOPS'] as Set
    indexedGranuleVersions().keySet()  == ['gov.noaa.nodc:NDBC-COOPS'] as Set
  }

  def 'updating an orphan granule indexes nothing'() {
    setup:
    insertMetadataFromPath('data/COOPS/G1.xml')

    when:
    executeGetRequest(updateSearchURI)
    sleep(1000)

    then:
    indexedCollectionVersions().size() == 0
    indexedGranuleVersions().size() == 0
  }

  def 'updating a collection and granule indexes a collection and a granule'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/COOPS/G1.xml')

    when:
    executeGetRequest(updateSearchURI)
    sleep(1000)

    then:
    indexedCollectionVersions().keySet() == ['gov.noaa.nodc:NDBC-COOPS'] as Set
    indexedGranuleVersions().keySet()  == ['CO-OPS.NOS_8638614_201602_D1_v00'] as Set
  }

  def 'touching a granule and updating reindexes only that granule'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/COOPS/G1.xml')
    insertMetadataFromPath('data/COOPS/G2.xml')
    executeGetRequest(updateSearchURI)
    sleep(1000)

    when: 'touch one of the granules'
    insertMetadataFromPath('data/COOPS/G1.xml')
    executeGetRequest(updateSearchURI)
    sleep(1000)

    then: 'only that granule is reindexed'
    indexedCollectionVersions()['gov.noaa.nodc:NDBC-COOPS'] == 1
    indexedGranuleVersions()['CO-OPS.NOS_9410170_201503_D1_v00'] == 1
    indexedGranuleVersions()['CO-OPS.NOS_8638614_201602_D1_v00'] == 2
  }

  def 'touching a collection and updating reindexes that collection and its granules'() {
    setup:
    insertMetadataFromPath('data/GHRSST/1.xml')
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/COOPS/G1.xml')
    insertMetadataFromPath('data/COOPS/G2.xml')
    executeGetRequest(updateSearchURI)
    sleep(1000)

    when: 'touch the collection'
    insertMetadataFromPath('data/COOPS/C1.xml')
    executeGetRequest(updateSearchURI)
    sleep(1000)

    then: 'the collection and both its granules are reindexed'
    def collections = indexedCollectionVersions()
    collections['gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED'] == 1
    collections['gov.noaa.nodc:NDBC-COOPS'] == 2
    def granules = indexedGranuleVersions()
    granules['gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED'] == 1
    granules['CO-OPS.NOS_8638614_201602_D1_v00'] == 2
    granules['CO-OPS.NOS_9410170_201503_D1_v00'] == 2
  }

  def 'rebuild does nothing when staging is empty'() {
    when:
    executeGetRequest(rebuildSearchURI)
    sleep(1000)

    then:
    noExceptionThrown()

    and:
    indexedCollectionVersions().size() == 0
    indexedGranuleVersions().size() == 0
  }

  def 'rebuilding with a collection indexes a collection and a synthesized granule'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')

    when:
    executeGetRequest(rebuildSearchURI)
    sleep(1000)

    then:
    indexedCollectionVersions().keySet() == ['gov.noaa.nodc:NDBC-COOPS'] as Set
    indexedGranuleVersions().keySet()  == ['gov.noaa.nodc:NDBC-COOPS'] as Set
  }

  def 'rebuilding with an orphan granule indexes nothing'() {
    setup:
    insertMetadataFromPath('data/COOPS/G1.xml')

    when:
    executeGetRequest(rebuildSearchURI)
    sleep(1000)

    then:
    indexedCollectionVersions().size() == 0
    indexedGranuleVersions().size() == 0
  }

  def 'rebuilding with a collection and granule indexes a collection and a granule'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/COOPS/G1.xml')

    when:
    executeGetRequest(rebuildSearchURI)
    sleep(1000)

    then:
    indexedCollectionVersions().keySet() == ['gov.noaa.nodc:NDBC-COOPS'] as Set
    indexedGranuleVersions().keySet()  == ['CO-OPS.NOS_8638614_201602_D1_v00'] as Set
  }

  def 'rebuilding with an updated collection builds a whole new index'() {
    setup:
    insertMetadataFromPath('data/GHRSST/1.xml')
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/COOPS/G1.xml')
    insertMetadataFromPath('data/COOPS/G2.xml')
    executeGetRequest(rebuildSearchURI)
    sleep(1000)

    when: 'touch the collection'
    insertMetadataFromPath('data/COOPS/C1.xml')
    executeGetRequest(rebuildSearchURI)
    sleep(1000)

    then: 'everything has a fresh version in a new index'
    indexedCollectionVersions().values().every { it == 1 }
    indexedGranuleVersions().values().every { it == 1 }
  }


  //---- Helpers -----

  private insertMetadataFromPath(String path) {
    insertMetadata(ClassLoader.systemClassLoader.getResourceAsStream(path).text)
  }

  private insertMetadata(String document) {
    def loadRequest = RequestEntity.post(loadURI).contentType(MediaType.APPLICATION_XML).body(document)
    restTemplate.exchange(loadRequest, Map)
    def refreshMetadataURI = "http://localhost:${port}/${contextPath}/admin/index/metadata/refresh".toURI()
    executeGetRequest(refreshMetadataURI)
  }

  private indexedCollectionVersions() {
    indexedItemVersions(COLLECTION_TYPE)
  }

  private indexedGranuleVersions() {
    indexedItemVersions(GRANULE_TYPE)
  }

  private indexedItemVersions(String type) {
    def refreshSearchURI = "http://localhost:${port}/${contextPath}/admin/index/search/refresh".toURI()
    executeGetRequest(refreshSearchURI)
    return client.prepareSearch(SEARCH_INDEX)
        .setTypes(type).setVersion(true).addField('fileIdentifier')
        .execute().actionGet().hits.hits.collectEntries { [(it.field('fileIdentifier').value()): it.version()] }
  }

  private executeGetRequest(URI uri) {
    def request = RequestEntity.get(uri).build()
    restTemplate.exchange(request, Map)
  }

}
