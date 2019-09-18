package org.cedar.onestop.api.admin.service

import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchTestVersion
import org.elasticsearch.Version
import org.elasticsearch.client.RestClient
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MetadataManagementServiceTest extends Specification {

  Map<Version, ElasticsearchConfig> esVersionedConfigs = [:]

  def setup() {
    esVersionedConfigs = ElasticsearchTestVersion.configs()
  }

  def "Alias #dataPipe.alias correctly parsed to type using ES version #dataPipe.version"() {
    when:
    String alias = dataPipe.alias
    String expectedType = dataPipe.expectedType
    Version version = dataPipe.version as Version

    ElasticsearchConfig esConfig = esVersionedConfigs[version]
    String type = esConfig.typeFromAlias(alias)

    then:
    type == expectedType

    where:
    dataPipe << ElasticsearchTestVersion.versionedTestCases([
        [ alias: 'prefix-search_collection',            expectedType: ElasticsearchConfig.TYPE_COLLECTION ],
        [ alias: 'prefix-search_granule-1519243661952', expectedType: null ],
        [ alias: 'prefix-search_flattened_granule',     expectedType: ElasticsearchConfig.TYPE_FLATTENED_GRANULE ],
        [ alias: 'not-valid-index',                     expectedType: null ],
    ])
  }

  def "Index #dataPipe.index correctly parsed to type using ES version #dataPipe.version"() {
    when:
    String index = dataPipe.index
    String expectedType = dataPipe.expectedType
    Version version = dataPipe.version

    ElasticsearchConfig esConfig = esVersionedConfigs[version]
    String type = esConfig.typeFromIndex(index)

    then:
    type == expectedType

    where:
    dataPipe << ElasticsearchTestVersion.versionedTestCases([
        [ index: 'prefix-search_collection',            expectedType: ElasticsearchConfig.TYPE_COLLECTION ],
        [ index: 'prefix-search_granule-1519243661952', expectedType: ElasticsearchConfig.TYPE_GRANULE ],
        [ index: 'prefix-search_flattened_granule',     expectedType: ElasticsearchConfig.TYPE_FLATTENED_GRANULE ],
        [ index: 'not-valid-index',                     expectedType: null ],
    ])
  }
}
