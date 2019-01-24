package org.cedar.onestop.api.search

import org.cedar.onestop.api.search.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Unroll

@Unroll
class SpatialFilterIntegrationTests extends IntegrationTest {

  private final String SPATIAL_INDEX = 'geometry_testing'

  @Autowired
  ElasticsearchService esService

  void setup() {
    refreshAndLoadGenericTestIndex(SPATIAL_INDEX)
  }

  @Ignore
  def 'Spatial filter with #relation relation returns correct results'() {
    // This test is mostly just testing Elasticsearch, but at least gives piece of mind we're
    // generating the right request, regardless
    // TODO
    given:
    def requestParams = [
        filters: [[
                      type: 'geometry',
                      relation: relation,
                      geometry: [
                          type: 'Point',
                          coordinates: [] //fixme
                      ]
                  ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, SPATIAL_INDEX)

    then:
    def actualMatchingIds = queryResponse.data.collect { it.id }
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | []
    'disjoint'   | []
    'intersects' | []
    'within'     | []
  }

  def 'Exclude global filter enabled excludes global records only'() {
    // Null spatial boundings should not be excluded
  }
}
