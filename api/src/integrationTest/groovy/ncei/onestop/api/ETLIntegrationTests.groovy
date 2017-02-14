package ncei.onestop.api

import ncei.onestop.api.service.ETLService
import ncei.onestop.api.service.MetadataIndexService
import ncei.onestop.api.service.SearchIndexService
import org.elasticsearch.client.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
@ActiveProfiles("integration")
@SpringBootTest(classes = [Application, IntegrationTestConfig])
class ETLIntegrationTests extends Specification {

  @Autowired
  private Client client

  @Autowired
  private SearchIndexService searchIndexService

  @Autowired
  private MetadataIndexService metadataIndexService

  @Autowired
  private ETLService etlService

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.name}')
  private String SEARCH_INDEX

  @Value('${elasticsearch.index.search.collectionType}')
  private String COLLECTION_TYPE

  @Value('${elasticsearch.index.search.granuleType}')
  private String GRANULE_TYPE

  void setup() {
    searchIndexService.recreate()
    metadataIndexService.recreate()
  }

  def 'update does nothing when staging is empty'() {
    when:
    etlService.updateSearchIndex()

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
    etlService.updateSearchIndex()

    then:
    indexedCollectionVersions().keySet() == ['gov.noaa.nodc:NDBC-COOPS'] as Set
    indexedGranuleVersions().keySet()  == ['gov.noaa.nodc:NDBC-COOPS'] as Set
  }

  def 'updating an orphan granule indexes nothing'() {
    setup:
    insertMetadataFromPath('data/COOPS/G1.xml')

    when:
    etlService.updateSearchIndex()

    then:
    indexedCollectionVersions().size() == 0
    indexedGranuleVersions().size() == 0
  }

  def 'updating a collection and granule indexes a collection and a granule'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/COOPS/G1.xml')

    when:
    etlService.updateSearchIndex()

    then:
    indexedCollectionVersions().keySet() == ['gov.noaa.nodc:NDBC-COOPS'] as Set
    indexedGranuleVersions().keySet()  == ['CO-OPS.NOS_8638614_201602_D1_v00'] as Set
  }

  def 'touching a granule and updating reindexes only that granule'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/COOPS/G1.xml')
    insertMetadataFromPath('data/COOPS/G2.xml')
    etlService.updateSearchIndex()

    when: 'touch one of the granules'
    insertMetadataFromPath('data/COOPS/G1.xml')
    etlService.updateSearchIndex()

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
    etlService.updateSearchIndex()

    when: 'touch the collection'
    insertMetadataFromPath('data/COOPS/C1.xml')
    etlService.updateSearchIndex()

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
    etlService.rebuildSearchIndex()

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
    etlService.rebuildSearchIndex()

    then:
    indexedCollectionVersions().keySet() == ['gov.noaa.nodc:NDBC-COOPS'] as Set
    indexedGranuleVersions().keySet()  == ['gov.noaa.nodc:NDBC-COOPS'] as Set
  }

  def 'rebuilding with an orphan granule indexes nothing'() {
    setup:
    insertMetadataFromPath('data/COOPS/G1.xml')

    when:
    etlService.rebuildSearchIndex()

    then:
    indexedCollectionVersions().size() == 0
    indexedGranuleVersions().size() == 0
  }

  def 'rebuilding with a collection and granule indexes a collection and a granule'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/COOPS/G1.xml')

    when:
    etlService.rebuildSearchIndex()

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
    etlService.rebuildSearchIndex()

    when: 'touch the collection'
    insertMetadataFromPath('data/COOPS/C1.xml')
    etlService.rebuildSearchIndex()

    then: 'everything has a fresh version in a new index'
    indexedCollectionVersions().values().every { it == 1 }
    indexedGranuleVersions().values().every { it == 1 }
  }


  //---- Helpers -----

  private insertMetadataFromPath(String path) {
    insertMetadata(ClassLoader.systemClassLoader.getResourceAsStream(path).text)
  }

  private insertMetadata(String document) {
    metadataIndexService.loadMetadata(document)
    metadataIndexService.refresh()
  }

  private indexedCollectionVersions() {
    indexedItemVersions(COLLECTION_TYPE)
  }

  private indexedGranuleVersions() {
    indexedItemVersions(GRANULE_TYPE)
  }

  private indexedItemVersions(String type) {
    searchIndexService.refresh()
    return client.prepareSearch(SEARCH_INDEX)
        .setTypes(type).setVersion(true).addField('fileIdentifier')
        .execute().actionGet().hits.hits.collectEntries { [(it.field('fileIdentifier').value()): it.version()] }
  }

}
