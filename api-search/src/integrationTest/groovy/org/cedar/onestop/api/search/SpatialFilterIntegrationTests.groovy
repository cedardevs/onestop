package org.cedar.onestop.api.search

import org.cedar.onestop.api.search.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

@Unroll
class SpatialFilterIntegrationTests extends IntegrationTest {

  private final String SPATIAL_INDEX = 'spatial_filter'

  @Autowired
  ElasticsearchService esService

  void setup() {
    refreshAndLoadGenericTestIndex(SPATIAL_INDEX)
  }

  def 'Spatial filter with #relation relation returns correct results'() {
    // This test is mostly just testing Elasticsearch, but at least gives piece of mind we're
    // generating the right request, regardless
    given:
    def requestParams = [
        filters: [[
                      type: 'geometry',
                      relation: relation,
                      geometry: [
                          type: 'Point',
                          coordinates: [-60, 47]
                      ]
                  ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, SPATIAL_INDEX)

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
            type: 'excludeGlobal',
            value: true
        ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, SPATIAL_INDEX)

    then:
    def actualMatchingIds = queryResponse.data.collect { it.id }
    ['2', '3', '4', '5'].containsAll(actualMatchingIds)
  }
}
