package org.cedar.onestop.api.metadata

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.metadata.service.ETLService
import org.cedar.onestop.api.metadata.service.ElasticsearchService
import org.cedar.onestop.api.metadata.service.MetadataManagementService
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchTestConfig
import org.elasticsearch.Version
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

import static org.cedar.onestop.elastic.common.DocumentUtil.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles(["integration"])
@SpringBootTest(
    classes = [
        Application,

        // provides:
        // - `RestClient` 'restClient' bean via test containers
        ElasticsearchTestConfig,
    ],
    webEnvironment = RANDOM_PORT
)
@Unroll
@Slf4j
class ETLIntegrationTests extends Specification {

  @Autowired
  @Qualifier("restClient")
  RestClient restClient

  @Autowired
  private ElasticsearchService elasticsearchService

  @Autowired
  private ETLService etlService

  @Autowired
  private MetadataManagementService metadataIndexService

  // this is simply used as a convenience/consistency as opposed to typing `elasticsearchService.esConfig.*`
  ElasticsearchConfig esConfig

  void setup() {
    this.esConfig = elasticsearchService.esConfig
    this.metadataIndexService = metadataIndexService
    this.etlService = etlService

    // the "Script compilation circuit breaker" limits the number of inline script compilations within a period of time
    if (elasticsearchService.version.onOrAfter(Version.V_6_0_0)) {
      // 'max_compilation_rate' (default: 75/5m, meaning 75 every 5 minutes)
      // https://www.elastic.co/guide/en/elasticsearch/reference/6.7/circuit-breaker.html#script-compilation-circuit-breaker
      log.info("Using \'script.max_compilation_rate\' because Elasticsearch version is >= 6.0")
      elasticsearchService.performRequest("PUT", "_cluster/settings", ['transient': ['script.max_compilations_rate': '200/1m']])
    } else {
      // 'max_compilations_per_minute' (default: 15)
      // deprecation warning in 5.6.0 to be replaced in 6.0 with 'max_compilations_rate'
      // https://www.elastic.co/guide/en/elasticsearch/reference/5.6/circuit-breaker.html#script-compilation-circuit-breaker
      log.info("Using \'script.max_compilations_per_minute\' because Elasticsearch version is >= 6.0")
      elasticsearchService.performRequest("PUT", "_cluster/settings", ['transient': ['script.max_compilations_per_minute': 200]])
    }

    elasticsearchService.dropSearchIndices()
    elasticsearchService.dropStagingIndices()
  }

  def 'update does nothing when staging is empty'() {
    when:
    etlService.updateSearchIndices()

    then:
    noExceptionThrown()

    and:
    documentsByType(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS).every({
      it.size() == 0
    })
  }

  def 'updating a new collection indexes only a collection'() {
    setup:
    insertMetadataFromPath('test/data/COOPS/C1.xml')

    when:
    etlService.updateSearchIndices()

    then:
    def indexed = documentsByType(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
    List<Map> indexedCollections = indexed[esConfig.TYPE_COLLECTION] as List<Map>
    def collection = indexedCollections.first()
    getFileIdentifier(collection) == 'gov.noaa.nodc:NDBC-COOPS'

    and:
    // No flattened granules were made
    !indexed[esConfig.TYPE_FLATTENED_GRANULE]
  }

  def 'updating an orphan granule indexes nothing'() {
    setup:
    insertMetadataFromPath('test/data/COOPS/G1.xml')

    when:
    etlService.updateSearchIndices()

    then:
    indexedCollectionVersions().size() == 0
    indexedGranuleVersions().size() == 0
    indexedFlatGranuleVersions().size() == 0
  }

  def 'updating a collection and granule indexes a collection, a granule, and a flattened granule'() {
    setup:
    insertMetadataFromPath('test/data/COOPS/C1.xml')
    insertMetadataFromPath('test/data/COOPS/G1.xml')

    when:
    etlService.updateSearchIndices() // runs the ETLs

    def collectionVersions = indexedCollectionVersions().keySet()
    def granuleVersions = indexedGranuleVersions().keySet()
    def flatGranuleVersions = indexedFlatGranuleVersions().keySet()

    then:
    collectionVersions == ['gov.noaa.nodc:NDBC-COOPS'] as Set
    granuleVersions == ['CO-OPS.NOS_8638614_201602_D1_v00'] as Set
    flatGranuleVersions == ['CO-OPS.NOS_8638614_201602_D1_v00'] as Set
  }

  def 'updating twice does nothing the second time'() {
    setup:
    insertMetadataFromPath('test/data/COOPS/C1.xml')

    when:
    etlService.updateSearchIndices()

    then:
    indexedCollectionVersions()['gov.noaa.nodc:NDBC-COOPS'] == 1

    when: 'again!'
    etlService.updateSearchIndices()

    then: 'no change'
    indexedCollectionVersions()['gov.noaa.nodc:NDBC-COOPS'] == 1
  }

  def 'touching a granule and updating reindexes only that granule'() {
    setup:
    insertMetadataFromPath('test/data/COOPS/C1.xml')
    insertMetadataFromPath('test/data/COOPS/G1.xml')
    insertMetadataFromPath('test/data/COOPS/G2.xml')
    etlService.updateSearchIndices()

    when: 'touch one of the granules'
    insertMetadataFromPath('test/data/COOPS/G1.xml')
    etlService.updateSearchIndices()

    then: 'only that granule is reindexed'
    indexedCollectionVersions()['gov.noaa.nodc:NDBC-COOPS'] == 1
    indexedGranuleVersions()['CO-OPS.NOS_9410170_201503_D1_v00'] == 1
    indexedGranuleVersions()['CO-OPS.NOS_8638614_201602_D1_v00'] == 2
    indexedFlatGranuleVersions()['CO-OPS.NOS_9410170_201503_D1_v00'] == 1
    indexedFlatGranuleVersions()['CO-OPS.NOS_8638614_201602_D1_v00'] == 2
  }

  def 'touching a collection and updating reindexes only that collection but re-flattens all granules'() {
    setup:
    insertMetadataFromPath('test/data/GHRSST/1.xml')
    insertMetadataFromPath('test/data/COOPS/C1.xml')
    insertMetadataFromPath('test/data/COOPS/G1.xml')
    insertMetadataFromPath('test/data/COOPS/G2.xml')
    etlService.updateSearchIndices()

    when: 'Touch the collection'
    insertMetadataFromPath('test/data/COOPS/C1.xml')
    etlService.updateSearchIndices()

    then: 'Only the collection is reindexed, not the granules'
    def collections = indexedCollectionVersions()
    collections['gov.noaa.nodc:GHRSST-EUR-L4UHFnd-MED'] == 1
    collections['gov.noaa.nodc:NDBC-COOPS'] == 2
    def granules = indexedGranuleVersions()
    granules['CO-OPS.NOS_8638614_201602_D1_v00'] == 1
    granules['CO-OPS.NOS_9410170_201503_D1_v00'] == 1

    and: 'But all granules are re-flattened'
    def flatGranules = indexedFlatGranuleVersions()
    flatGranules['CO-OPS.NOS_8638614_201602_D1_v00'] == 2
    flatGranules['CO-OPS.NOS_9410170_201503_D1_v00'] == 2
  }

  def 'rebuild does nothing when staging is empty'() {
    when:
    etlService.rebuildSearchIndices()

    then:
    noExceptionThrown()

    and:
    documentsByType(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS).every({
      it.size() == 0
    })
  }

  def 'rebuilding with an orphan granule indexes nothing'() {
    setup:
    insertMetadataFromPath('test/data/COOPS/G1.xml')

    when:
    etlService.rebuildSearchIndices()

    then:
    documentsByType(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS).every({
      it.size() == 0
    })
  }

  def 'rebuilding with a collection and granule indexes a collection, a granule, and a flattened granule'() {
    setup:
    insertMetadataFromPath('test/data/COOPS/C1.xml')
    insertMetadataFromPath('test/data/COOPS/G1.xml')

    when:
    etlService.rebuildSearchIndices()
    Map staged = documentsByType(esConfig.COLLECTION_STAGING_INDEX_ALIAS, esConfig.GRANULE_STAGING_INDEX_ALIAS)
    List<Map> stagedCollections = staged[esConfig.TYPE_COLLECTION] as List<Map>
    List<Map> stagedGranules = staged[esConfig.TYPE_GRANULE] as List<Map>

    Map indexed = documentsByType(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
    List<Map> indexedCollections = indexed[esConfig.TYPE_COLLECTION] as List<Map>
    List<Map> indexedGranules = indexed[esConfig.TYPE_GRANULE] as List<Map>
    List<Map> indexedFlatGranules = indexed[esConfig.TYPE_FLATTENED_GRANULE] as List<Map>


    then: // one collection and one granule are indexed; one flattened granule is generated
    indexedCollections.size() == 1
    indexedGranules.size() == 1
    indexedFlatGranules.size() == 1

    def indexedCollection = indexedCollections.first()
    def indexedGranule = indexedGranules.first()
    def flatGranule = indexedFlatGranules.first()
    def stagedCollection = stagedCollections.first()
    def stagedGranule = stagedGranules.first()

    and: // the collection is the same as staging
    getId(indexedCollection) == getId(stagedCollection)
    getFileIdentifier(indexedCollection) == getFileIdentifier(stagedCollection)
    getDOI(indexedCollection) == getDOI(stagedCollection)

    and: // the granule is the same as staging
    getId(indexedGranule) == getId(stagedGranule)
    getFileIdentifier(indexedGranule) == getFileIdentifier(stagedGranule)
    getParentIdentifier(indexedGranule) == getParentIdentifier(stagedGranule)

    and: // the granule is connected to the collection
    getInternalParentIdentifier(indexedGranule) == getId(indexedCollection)

    and: // the flattened granule has granule and collection data
    getId(flatGranule) == getId(indexedGranule)
    getInternalParentIdentifier(flatGranule) == getId(indexedCollection)
    getDOI(flatGranule) == getDOI(indexedCollection)
  }

  def 'rebuilding with an updated collection builds a whole new index'() {
    setup:
    insertMetadataFromPath('test/data/GHRSST/1.xml')
    insertMetadataFromPath('test/data/COOPS/C1.xml')
    insertMetadataFromPath('test/data/COOPS/G1.xml')
    insertMetadataFromPath('test/data/COOPS/G2.xml')
    etlService.rebuildSearchIndices()

    when: 'touch the collection'
    insertMetadataFromPath('test/data/COOPS/C1.xml')
    etlService.rebuildSearchIndices()
    def indexed = documentsByType(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
    List<Map> indexedCollections = indexed[esConfig.TYPE_COLLECTION] as List<Map>
    List<Map> indexedGranules = indexed[esConfig.TYPE_GRANULE] as List<Map>
    List<Map> indexedFlatGranules = indexed[esConfig.TYPE_FLATTENED_GRANULE] as List<Map>

    then: 'everything has a fresh version in a new index'
    indexedCollections.size() == 2
    indexedCollections.every({ it._version == 1 })
    indexedGranules.size() == 2
    indexedGranules.every({ it._version == 1 })
    indexedFlatGranules.size() == 2
    indexedFlatGranules.every({ it._version == 1 })
  }


  //---- Helper functions -----

  private void insertMetadataFromPath(String path) {
    insertMetadata(ClassLoader.systemClassLoader.getResourceAsStream(path).text)
  }

  private void insertMetadata(String document) {
    metadataIndexService.loadMetadata(document)
    elasticsearchService.refresh(esConfig.COLLECTION_STAGING_INDEX_ALIAS, esConfig.GRANULE_STAGING_INDEX_ALIAS)
  }

  private Map indexedCollectionVersions() {
    indexedDocumentVersions(esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
  }

  private Map indexedGranuleVersions() {
    indexedDocumentVersions(esConfig.GRANULE_SEARCH_INDEX_ALIAS)
  }

  private Map indexedFlatGranuleVersions() {
    indexedDocumentVersions(esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
  }

  private Map documentsByType(String collectionIndex, String granuleIndex, String flatGranuleIndex = null) {
    elasticsearchService.refresh(collectionIndex, granuleIndex)
    if (flatGranuleIndex) {
      elasticsearchService.refresh(flatGranuleIndex)
    }
    def endpoint = "$collectionIndex,$granuleIndex${flatGranuleIndex ? ",$flatGranuleIndex" : ''}/_search"
    def request = [version: true]
    def response = elasticsearchService.performRequest('GET', endpoint, request)
    return getDocuments(response).groupBy({ esConfig.typeFromIndex(getIndex(it)) })
  }

  private Map<String, Integer> indexedDocumentVersions(String alias) {
    def endpoint = "${alias}/_search"
    def request = [
        version: true,
        _source: 'fileIdentifier'
    ]
    def response = elasticsearchService.performRequest('GET', endpoint, request)
    return getDocuments(response).collectEntries { [(getFileIdentifier(it)): getVersion(it)] }
  }

}
