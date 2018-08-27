package org.cedar.onestop.api.metadata

import org.cedar.onestop.api.metadata.service.ETLService
import org.cedar.onestop.api.metadata.service.SitemapETLService
import org.cedar.onestop.api.metadata.service.MetadataManagementService
import org.cedar.onestop.api.metadata.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.elasticsearch.client.RestClient
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
@ActiveProfiles("integration")
@SpringBootTest(classes = [Application, IntegrationTestConfig])
class SitemapETLIntegrationTests extends Specification {

  @Autowired
  private ElasticsearchService elasticsearchService

  @Autowired
  private MetadataManagementService metadataIndexService

  @Autowired
  private ETLService etlService

  @Autowired
  private SitemapETLService sitemapEtlService

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

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.sitemap.name}')
  String SITEMAP_INDEX

  @Value('${elasticsearch.index.prefix:}')
  String PREFIX

  @Value('${elasticsearch.index.universal-type}')
  private String TYPE

  private final String COLLECTION_TYPE = 'collection'
  private final String GRANULE_TYPE = 'granule'
  private final String FLAT_GRANULE_TYPE = 'flattenedGranule'

  @Autowired
  RestClient restClient

  void setup() {
    elasticsearchService.dropSearchIndices()
    elasticsearchService.dropStagingIndices()
    elasticsearchService.ensureIndices()
    refreshIndices()
  }

  def 'updating sitemap with collections'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/DEM/1.xml')

    when:
    etlService.updateSearchIndices()
    sitemapEtlService.updateSitemap()

    refreshIndices()

    then:
    def indexedCols = documentsByType(COLLECTION_SEARCH_INDEX, GRANULE_SEARCH_INDEX, FLAT_GRANULE_SEARCH_INDEX)
    def collections = indexedCols[COLLECTION_TYPE]
    collections.size == 2
    def indexed = searchSitemap()
    indexed.data.size == 1
    def sitemap = getById(SITEMAP_INDEX, indexed.data[0].id)
    sitemap.data[0].attributes.content.size == 2

    and:
    def collectionIds = collections.collect({data -> data._id}) as Set
    def sitemapCollectionIds = sitemap.data[0].attributes.content as Set
    sitemapCollectionIds == collectionIds
  }

  def 'sitemap with multiple submaps'() {
    setup:
    insertMetadataFromPath('data/COOPS/C1.xml')
    insertMetadataFromPath('data/DEM/1.xml')
    insertMetadataFromPath('data/DEM/2.xml')
    insertMetadataFromPath('data/DEM/3.xml')
    insertMetadataFromPath('data/GHRSST/1.xml')
    insertMetadataFromPath('data/GHRSST/2.xml')
    insertMetadataFromPath('data/GHRSST/3.xml')

    when:
    etlService.updateSearchIndices()
    sitemapEtlService.updateSitemap()

    refreshIndices()

    then:
    def indexed = searchSitemap()
    indexed.data.size == 2
    def submap1 = getById(SITEMAP_INDEX, indexed.data[0].id)
    def submap2 = getById(SITEMAP_INDEX, indexed.data[1].id)

    and:
    def submapSizes = ([submap1.data[0].attributes.content.size] as Set) + ([submap2.data[0].attributes.content.size] as Set)
    submapSizes == [6,1] as Set

    and:
    def submapIds = (submap1.data[0].attributes.content as Set) + (submap2.data[0].attributes.content as Set)
    submapIds.size() == 7
  }


  //---- Helpers -----

  private void insertMetadataFromPath(String path) {
    insertMetadata(ClassLoader.systemClassLoader.getResourceAsStream(path).text)
  }

  private void insertMetadata(String document) {
    metadataIndexService.loadMetadata(document)
    elasticsearchService.refresh(COLLECTION_STAGING_INDEX, GRANULE_STAGING_INDEX)
  }

  private Map documentsByType(String collectionIndex, String granuleIndex, String flatGranuleIndex = null) {
    elasticsearchService.refresh(collectionIndex, granuleIndex)
    if(flatGranuleIndex) { elasticsearchService.refresh(flatGranuleIndex) }
    def endpoint = "$collectionIndex,$granuleIndex${flatGranuleIndex ? ",$flatGranuleIndex" : ''}/_search"
    def request = [version: true]
    def response = elasticsearchService.performRequest('GET', endpoint, request)
    return response.hits.hits.groupBy({metadataIndexService.determineType(it._index)})
  }

  private Map searchSitemap() {
    def requestBody = [
      _source: ["lastUpdatedDate",]
    ]
    String searchEndpoint = "${SITEMAP_INDEX}/_search"
    def searchResponse = elasticsearchService.performRequest('GET', searchEndpoint, requestBody )

    def result = [
      data: searchResponse.hits.hits.collect {
        [id: it._id, type: TYPE, attributes: it._source]
      },
      meta: [
          took : searchResponse.took,
          total: searchResponse.hits.total
      ]
    ]
    return result
  }

  private Map getById(String index, String id) {
    String endpoint = "/${PREFIX}${index}/${TYPE}/${id}"
    def response = elasticsearchService.performRequest('GET', endpoint)
    if (response.found) {
      return [
          data: [[
                     id        : response._id,
                     type      : TYPE,
                     attributes: response._source
                 ]]
      ]
    }
    else {
      return [
          status: HttpStatus.NOT_FOUND.value(),
          title : 'No such document',
          detail: "Record type $type with Elasticsearch ID [ ${id} ] does not exist."
      ]
    }
  }

  private refreshIndices() {
    restClient.performRequest('POST', '_refresh')
  }

}
