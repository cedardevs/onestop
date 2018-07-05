package org.cedar.onestop.api.metadata

import org.cedar.onestop.api.metadata.service.ETLService
import org.cedar.onestop.api.metadata.service.MetadataManagementService
import org.cedar.onestop.api.metadata.service.ElasticsearchService
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
  private ElasticsearchService elasticsearchService

  @Autowired
  private MetadataManagementService metadataIndexService

  @Autowired
  private ETLService etlService

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.collection.name}')
  String COLLECTION_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.collection.name}')
  String COLLECTION_STAGING_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.granule.name}')
  String GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.granule.name}')
  String GRANULE_STAGING_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.flattened-granule.name}')
  private String FLAT_GRANULE_SEARCH_INDEX

  private final String COLLECTION_TYPE = 'collection'
  private final String GRANULE_TYPE = 'granule'
  private final String FLAT_GRANULE_TYPE = 'flattenedGranule'

  void setup() {
    elasticsearchService.dropStagingIndices()
    elasticsearchService.dropSearchIndices()
    elasticsearchService.ensureIndices()
  }

  def 'update does nothing when staging is empty'() {
    when:
    etlService.updateSearchIndices()

    then:
    noExceptionThrown()

    and:
    documentsByType(COLLECTION_SEARCH_INDEX, GRANULE_SEARCH_INDEX, FLAT_GRANULE_SEARCH_INDEX).every({it.size() == 0})
  }

  def 'updating a new collection indexes only a collection'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')

    when:
    etlService.updateSearchIndices()

    then:
    def indexed = documentsByType(COLLECTION_SEARCH_INDEX, GRANULE_SEARCH_INDEX, FLAT_GRANULE_SEARCH_INDEX)
    def collection = indexed[COLLECTION_TYPE][0]
    collection._source.fileIdentifier == 'gov.noaa.nodc:NDBC-COOPS'

    and:
    // No flattened granules were made
    !indexed[FLAT_GRANULE_TYPE]
  }

  def 'updating an orphan granule indexes nothing'() {
    setup:
    insertMetadataFromPath('data/COOPS/G1.xml')

    when:
    etlService.updateSearchIndices()

    then:
    indexedCollectionVersions().size() == 0
    indexedGranuleVersions().size() == 0
    indexedFlatGranuleVersions().size() == 0
  }

  def 'updating a collection and granule indexes a collection, a granule, and a flattened granule'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/COOPS/G1.xml')

    when:
    etlService.updateSearchIndices()

    then:
    indexedCollectionVersions().keySet() == ['gov.noaa.nodc:NDBC-COOPS'] as Set
    indexedGranuleVersions().keySet()  == ['CO-OPS.NOS_8638614_201602_D1_v00'] as Set
    indexedFlatGranuleVersions().keySet() == ['CO-OPS.NOS_8638614_201602_D1_v00'] as Set
  }

  def 'updating twice does nothing the second time'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')

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
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/COOPS/G1.xml')
    insertMetadataFromPath('data/COOPS/G2.xml')
    etlService.updateSearchIndices()

    when: 'touch one of the granules'
    insertMetadataFromPath('data/COOPS/G1.xml')
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
    insertMetadataFromPath('data/GHRSST/1.xml')
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/COOPS/G1.xml')
    insertMetadataFromPath('data/COOPS/G2.xml')
    etlService.updateSearchIndices()

    when: 'Touch the collection'
    insertMetadataFromPath('data/COOPS/C1.xml')
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
    documentsByType(COLLECTION_SEARCH_INDEX, GRANULE_SEARCH_INDEX, FLAT_GRANULE_SEARCH_INDEX).every({it.size() == 0})
  }

  def 'rebuilding with an orphan granule indexes nothing'() {
    setup:
    insertMetadataFromPath('data/COOPS/G1.xml')

    when:
    etlService.rebuildSearchIndices()

    then:
    documentsByType(COLLECTION_SEARCH_INDEX, GRANULE_SEARCH_INDEX, FLAT_GRANULE_SEARCH_INDEX).every({it.size() == 0})
  }

  def 'rebuilding with a collection and granule indexes a collection, a granule, and a flattened granule'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/COOPS/G1.xml')

    when:
    etlService.rebuildSearchIndices()
    def staged = documentsByType(COLLECTION_STAGING_INDEX, GRANULE_STAGING_INDEX)
    def indexed = documentsByType(COLLECTION_SEARCH_INDEX, GRANULE_SEARCH_INDEX, FLAT_GRANULE_SEARCH_INDEX)

    then: // one collection and one granule are indexed; one flattened granule is generated
    indexed[COLLECTION_TYPE].size() == 1
    indexed[GRANULE_TYPE].size() == 1
    indexed[FLAT_GRANULE_TYPE].size() == 1

    def indexedCollection = indexed[COLLECTION_TYPE][0]
    def indexedGranule = indexed[GRANULE_TYPE][0]
    def flatGranule = indexed[FLAT_GRANULE_TYPE][0]
    def stagedCollection = staged[COLLECTION_TYPE][0]
    def stagedGranule = staged[GRANULE_TYPE][0]

    and: // the collection is the same as staging
    indexedCollection._id == stagedCollection._id
    indexedCollection._source.fileIdentifier == stagedCollection._source.fileIdentifier
    indexedCollection._source.doi == stagedCollection._source.doi

    and: // the granule is the same as staging
    indexedGranule._id == stagedGranule._id
    indexedGranule._source.fileIdentifier == stagedGranule._source.fileIdentifier
    indexedGranule._source.parentIdentifier == stagedGranule._source.parentIdentifier

    and: // the granule is connected to the collection
    indexedGranule._source.internalParentIdentifier == indexedCollection._id

    and: // the flattened granule has granule and collection data
    flatGranule._id == indexedGranule._id
    flatGranule._source.internalParentIdentifier == indexedCollection._id
    flatGranule._source.doi == indexedCollection._source.doi
  }

  def 'rebuilding with an updated collection builds a whole new index'() {
    setup:
    insertMetadataFromPath('data/GHRSST/1.xml')
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/COOPS/G1.xml')
    insertMetadataFromPath('data/COOPS/G2.xml')
    etlService.rebuildSearchIndices()

    when: 'touch the collection'
    insertMetadataFromPath('data/COOPS/C1.xml')
    etlService.rebuildSearchIndices()
    def indexed = documentsByType(COLLECTION_SEARCH_INDEX, GRANULE_SEARCH_INDEX, FLAT_GRANULE_SEARCH_INDEX)

    then: 'everything has a fresh version in a new index'
    indexed[COLLECTION_TYPE].size() == 2
    indexed[COLLECTION_TYPE].every({it._version == 1})
    indexed[GRANULE_TYPE].size() == 2
    indexed[GRANULE_TYPE].every({it._version == 1})
    indexed[FLAT_GRANULE_TYPE].size() == 2
    indexed[FLAT_GRANULE_TYPE].every({it._version == 1})
  }


  //---- Helpers -----

  private void insertMetadataFromPath(String path) {
    insertMetadata(ClassLoader.systemClassLoader.getResourceAsStream(path).text)
  }

  private void insertMetadata(String document) {
    metadataIndexService.loadMetadata(document)
    elasticsearchService.refresh(COLLECTION_STAGING_INDEX, GRANULE_STAGING_INDEX)
  }

  private Map indexedCollectionVersions() {
    indexedItemVersions(COLLECTION_SEARCH_INDEX)
  }

  private Map indexedGranuleVersions() {
    indexedItemVersions(GRANULE_SEARCH_INDEX)
  }

  private Map indexedFlatGranuleVersions() {
    indexedItemVersions(FLAT_GRANULE_SEARCH_INDEX)
  }

  private Map documentsByType(String collectionIndex, String granuleIndex, String flatGranuleIndex = null) {
    elasticsearchService.refresh(collectionIndex, granuleIndex)
    if(flatGranuleIndex) { elasticsearchService.refresh(flatGranuleIndex) }
    def endpoint = "$collectionIndex,$granuleIndex${flatGranuleIndex ? ",$flatGranuleIndex" : ''}/_search"
    def request = [version: true]
    def response = elasticsearchService.performRequest('GET', endpoint, request)
    return response.hits.hits.groupBy({metadataIndexService.determineType(it._index)})
  }

  private Map indexedItemVersions(String index) {
    elasticsearchService.refresh(index)
    def endpoint = "$index/_search"
    def request = [
        version: true,
        _source: 'fileIdentifier'
    ]
    def response = elasticsearchService.performRequest('GET', endpoint, request)
    return response.hits.hits.collectEntries { [(it._source.fileIdentifier): it._version] }
  }

}
