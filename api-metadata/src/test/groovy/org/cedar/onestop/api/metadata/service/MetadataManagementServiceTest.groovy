package org.cedar.onestop.api.metadata.service

import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.elasticsearch.Version
import org.elasticsearch.client.RestClient
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MetadataManagementServiceTest extends Specification {

  Version testVersion = Version.V_6_1_2

  ElasticsearchConfig esConfig = new ElasticsearchConfig(
          'search_collection',
          'staging_collection',
          'search_granule',
          'staging_granule',
          'search_flattened_granule',
          'sitemap',
          'prefix-',
          'collection_pipeline',
          'granule_pipeline',
          10,
          null,
          2,
          5,
          testVersion
  )
  RestClient mockRestClient = Mock(RestClient)
  ElasticsearchService elasticsearchService = new ElasticsearchService(mockRestClient, testVersion, esConfig)
  MetadataManagementService metadataManagementService = new MetadataManagementService(elasticsearchService)

  def setup() {
  }

  def "Alias #alias correctly parsed to type"() {
    given:
    def aliasToParse = alias

    when:
    String type = esConfig.typeFromAlias(aliasToParse)

    then:
    type == expectedType

    where:
    alias                                 | expectedType
    'prefix-search_collection'            | ElasticsearchConfig.TYPE_COLLECTION
    'prefix-search_granule-1519243661952' | null
    'prefix-search_flattened_granule'     | ElasticsearchConfig.TYPE_FLATTENED_GRANULE
    'not-valid-index'                     | null

  }

  def "Index #index correctly parsed to type"() {
    given:
    def indexToParse = index

    when:
    String type = metadataManagementService.esConfig.typeFromIndex(indexToParse)

    then:
    type == expectedType

    where:
    index                                 | expectedType
    'prefix-search_collection'            | null
    'prefix-search_granule-1519243661952' | ElasticsearchConfig.TYPE_GRANULE
    'prefix-search_flattened_granule'     | null
    'not-valid-index'                     | null

  }
}
