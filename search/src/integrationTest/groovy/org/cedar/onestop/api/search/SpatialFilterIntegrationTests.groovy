package org.cedar.onestop.api.search

import org.cedar.onestop.api.search.service.ElasticsearchService
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchTestConfig
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@DirtiesContext
@ActiveProfiles(["integration"])
@SpringBootTest(
    classes = [
        Application,
        DefaultApplicationConfig,

        // provides:
        // - `RestClient` 'restClient' bean via test containers
        ElasticsearchTestConfig,
    ],
    webEnvironment = RANDOM_PORT,
    properties = ["elasticsearch.index.prefix=search_spatial_filter_"]
)
@Unroll
class SpatialFilterIntegrationTests extends Specification {

  static private final String SPATIAL_INDEX_ALIAS = 'spatial_filter'

  @Autowired
  RestClient restClient

  @Autowired
  ElasticsearchConfig esConfig

  @Autowired
  ElasticsearchService esService

  void setup() {
    TestUtil.resetLoadAndRefreshGenericTestIndex(SPATIAL_INDEX_ALIAS, restClient, esConfig)
  }

  def 'Spatial filter with #relation relation returns correct results'() {
    // This test is mostly just testing Elasticsearch, but at least gives piece of mind we're
    // generating the right request, regardless
    given:
    def requestParams = [
        filters: [[
                      type    : 'geometry',
                      relation: relation,
                      geometry: [
                          type       : 'Point',
                          coordinates: [-60, 47]
                      ]
                  ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, SPATIAL_INDEX_ALIAS)

    then:
    def actualMatchingIds = queryResponse.data.collect { it.id }
    expectedMatchingIds.containsAll(actualMatchingIds)
    println("???? ${queryResponse}")

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | ['1']
    'disjoint'   | ['2', '3', '4']
    'intersects' | ['1']
    'within'     | []
  }

  def 'Exclude global filter enabled excludes global records only'() {
    // Null spatial boundings should not be excluded
    given:
    def requestParams = [
        filters: [[
                      type : 'excludeGlobal',
                      value: true
                  ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, SPATIAL_INDEX_ALIAS)

    then:
    def actualMatchingIds = queryResponse.data.collect { it.id }
    ['2', '3', '4', '5'].containsAll(actualMatchingIds)
  }
}
