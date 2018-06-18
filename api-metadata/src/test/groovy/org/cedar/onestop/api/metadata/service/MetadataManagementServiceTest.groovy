package org.cedar.onestop.api.metadata.service

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MetadataManagementServiceTest extends Specification {

  def mockElasticsearchService = Mock(ElasticsearchService)
  def metadataManagementService = new MetadataManagementService(mockElasticsearchService)

  def setup() {
    metadataManagementService.PREFIX = 'prefix-'
    metadataManagementService.COLLECTION_SEARCH_INDEX = 'search_collection'
    metadataManagementService.COLLECTION_STAGING_INDEX = 'staging_collection'
    metadataManagementService.GRANULE_SEARCH_INDEX = 'search_granule'
    metadataManagementService.GRANULE_STAGING_INDEX = 'staging_granule'
    metadataManagementService.FLAT_GRANULE_SEARCH_INDEX = 'search_flattened_granule'
  }

  def "Index #index correctly parsed to type"() {
    given:
    def indexToParse = index

    when:
    def parsedType = metadataManagementService.determineType(indexToParse)

    then:
    parsedType == expectedType

    where:
    index                                 | expectedType
    'prefix-search_collection'            | 'collection'
    'prefix-search_granule-1519243661952' | 'granule'
    'prefix-search_flattened_granule'     | 'flattenedGranule'
    'not-valid-index'                     | null

  }
}
