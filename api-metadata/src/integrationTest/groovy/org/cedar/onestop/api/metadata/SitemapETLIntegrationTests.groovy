package org.cedar.onestop.api.metadata

import org.cedar.onestop.api.metadata.authorization.configs.SpringSecurityConfig
import org.cedar.onestop.api.metadata.authorization.configs.SpringSecurityDisabled
import org.cedar.onestop.api.metadata.service.ETLService
import org.cedar.onestop.api.metadata.service.ElasticsearchService
import org.cedar.onestop.api.metadata.service.MetadataManagementService
import org.cedar.onestop.api.metadata.service.SitemapETLService
import org.cedar.onestop.api.metadata.springsecurity.IdentityProviderConfig
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchTestConfig
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll
import static org.cedar.onestop.elastic.common.DocumentUtil.*

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles(["integration", "sitemap"])
@SpringBootTest(
        classes = [
                Application,
                ElasticsearchTestConfig,
                SpringSecurityDisabled,
                SpringSecurityConfig,
                IdentityProviderConfig
        ],
        webEnvironment = RANDOM_PORT
)
@Unroll
class SitemapETLIntegrationTests extends Specification {

  @Autowired
  private ElasticsearchService elasticsearchService

  @Autowired
  private MetadataManagementService metadataIndexService

  @Autowired
  private ETLService etlService

  @Autowired
  private SitemapETLService sitemapEtlService

  @Autowired
  @Qualifier("elasticsearchRestClient")
  RestClient restClient

  ElasticsearchConfig esConfig

  void setup() {
    esConfig = elasticsearchService.esConfig
    elasticsearchService.dropSearchIndices()
    elasticsearchService.dropStagingIndices()
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
    Map indexedByType = documentsByType(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
    List<Map> collections = indexedByType[ElasticsearchConfig.TYPE_COLLECTION] as List<Map>
    collections.size() == 2
    Map indexed = searchSitemap()
    List<Map> data = indexed.data as List<Map>
    data.size() == 1
    Map sitemap = getById(esConfig.SITEMAP_INDEX_ALIAS, data[0].id as String)
    Map sitemapAttributes = sitemap.data[0].attributes as Map

    Set<String> sitemapCollectionIds = sitemapAttributes.content as Set
    sitemapCollectionIds.size() == 2

    and:
    Set<String> collectionIds = collections.collect({ getId(it) }) as Set
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
    Map indexed = searchSitemap()
    List<Map> data = indexed.data as List<Map>
    data.size() == 2
    def submap1 = getById(esConfig.SITEMAP_INDEX_ALIAS, data[0].id as String)
    def submap2 = getById(esConfig.SITEMAP_INDEX_ALIAS, data[1].id as String)

    Map submap1Attributes = submap1.data[0].attributes as Map
    Map submap2Attributes = submap2.data[0].attributes as Map

    Set<String> submapIds1 = submap1Attributes.content as Set
    Set<String> submapIds2 = submap2Attributes.content as Set

    and:
    Set submapSizes = [submapIds1.size(), submapIds2.size()]
    submapSizes == [6,1] as Set

    and:
    Set submapIds = submapIds1 + submapIds2
    submapIds.size() == 7
  }


  //---- Helpers -----

  private void insertMetadataFromPath(String path) {
    insertMetadata(ClassLoader.systemClassLoader.getResourceAsStream(path).text)
  }

  private void insertMetadata(String document) {
    metadataIndexService.loadMetadata(document)
    elasticsearchService.refresh(esConfig.COLLECTION_STAGING_INDEX_ALIAS, esConfig.GRANULE_STAGING_INDEX_ALIAS)
  }

  private Map documentsByType(String collectionIndex, String granuleIndex, String flatGranuleIndex = null) {
    elasticsearchService.refresh(collectionIndex, granuleIndex)
    if(flatGranuleIndex) { elasticsearchService.refresh(flatGranuleIndex) }
    def endpoint = "${collectionIndex},${granuleIndex}${flatGranuleIndex ? ",$flatGranuleIndex" : ''}/_search"
    def request = [version: true]
    def response = elasticsearchService.performRequest('GET', endpoint, request)
    return getHits(response).groupBy({ esConfig.typeFromIndex(getIndex(it as Map)) })
  }

  private Map searchSitemap() {
    def requestBody = [
      _source: ["lastUpdatedDate",]
    ]
    String searchEndpoint = "${esConfig.SITEMAP_INDEX_ALIAS}/_search"
    def searchResponse = elasticsearchService.performRequest('GET', searchEndpoint, requestBody )

    def result = [
      data: getDocuments(searchResponse).collect {
        [id: getId(it), type: esConfig.TYPE, attributes: getSource(it)]
      },
      meta: [
          took : getTook(searchResponse),
          total: getHitsTotal(searchResponse)
      ]
    ]
    return result
  }

  private Map getById(String alias, String id) {
    String endpoint = "/${alias}/${esConfig.TYPE}/${id}"
    def response = elasticsearchService.performRequest('GET', endpoint)
    String type = esConfig.typeFromAlias(alias)
    if (response.found) {
      return [
          data: [[
                     id        : getId(response),
                     type      : esConfig.TYPE,
                     attributes: getSource(response)
                 ]]
      ]
    }
    else {
      return [
          status: HttpStatus.NOT_FOUND.value(),
          title : 'No such document',
          detail: "Record type ${type} with Elasticsearch ID [ ${id} ] does not exist."
      ]
    }
  }

  private refreshIndices() {
    restClient.performRequest('POST', '_refresh')
  }

}
