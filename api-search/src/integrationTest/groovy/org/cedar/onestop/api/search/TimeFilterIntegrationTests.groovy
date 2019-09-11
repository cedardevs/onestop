package org.cedar.onestop.api.search

import org.cedar.onestop.api.search.service.ElasticsearchService
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchTestConfig
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

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
class TimeFilterIntegrationTests extends Specification {

  static private final String DATES_INDEX_ALIAS = 'time_filter'

  @Autowired
  RestClient restClient

  @Autowired
  ElasticsearchConfig esConfig

  @Autowired
  ElasticsearchService esService

  void setup() {
    // See /docs/development/integration-tests/time-filter.md for more information about the test data used in these tests. It explains the logic behind the test cases chosen, and how they are organized to minimize unrelated test data interactions.
    TestUtil.resetLoadAndRefreshGenericTestIndex(DATES_INDEX_ALIAS, restClient, esConfig)
  }

  def 'Datetime filter q: (x, +∞) and `#relation` relation matches #expectedMatchingIds'() {
    given:
    def requestParams = [
        filters: [[
            type: "datetime",
            relation: relation,
            after: "1995-01-01"
        ]],
        summary: false,
        page: [
            max: 20,
            offset: 0
        ]
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX_ALIAS)
    def actualMatchingIds = queryResponse.data.collect { it.id }

    then:
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    actualMatchingIds.containsAll(expectedMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | ['15','16']
    'disjoint'   | ['4','10']
    'intersects' | ['1','2','3','5','6','7','8','9','11','12','13','14','15','16','17', '18', '19']
    'within'     | ['1','3','7','8','9','16','17','18','19']
  }

  def 'Datetime filter with q: (-∞, y) and `#relation` relation matches #expectedMatchingIds'() {
    given:
    def requestParams = [
        filters: [[
            type: "datetime",
            relation: relation,
            before: "2012-01-01"
        ]],
        summary: false,
        page: [
            max: 20,
            offset: 0
        ]
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX_ALIAS)
    def actualMatchingIds = queryResponse.data.collect { it.id }

    then:
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    actualMatchingIds.containsAll(expectedMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | ['13','14']
    'disjoint'   | ['7','19']
    'intersects' | ['1','2','3','4','5','6','8','9','10','11','12','13','14','15','16','17','18']
    'within'     | ['1','3','4','5','6','10','11','12','13']
  }

  def 'Datetime filter with q: (x, y) and `#relation` relation matches #expectedMatchingIds'() {
    given:
    def requestParams = [
        filters: [[
            type: "datetime",
            relation: relation,
            after: "1995-01-01",
            before: "2012-01-01"
        ]],
        summary: false,
        page: [
            max: 20,
            offset: 0
        ]
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX_ALIAS)
    def actualMatchingIds = queryResponse.data.collect { it.id }

    then:
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    actualMatchingIds.containsAll(expectedMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | ['2','3','13','14','15','16']
    'disjoint'   | ['4','7','10','19']
    'intersects' | ['1','2','3','5','6','8','9','11','12','13','14','15','16','17','18']
    'within'     | ['1','3']
  }

  def 'Year filter with q: (x, +∞) (with x in Paleo range) and `#relation` relation matches #expectedMatchingIds'() {
    given:
    def requestParams = [
        filters: [[
                      type: "year",
                      relation: relation,
                      after: -1000000000
                  ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX_ALIAS)
    def actualMatchingIds = queryResponse.data.collect { it.id }

    then:
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    actualMatchingIds.containsAll(expectedMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | []
    'disjoint'   | ['p2', 'p4']
    'intersects' | ['p1', 'p3', 'p5', 'p6', 'p7', 'p8', 'p9']
    'within'     | ['p1', 'p5', 'p6', 'p7', 'p9']
  }

  def 'Year filter with q: (-∞, y) (with y in Paleo range) and `#relation` relation matches #expectedMatchingIds'() {
    given:
    def requestParams = [
        filters: [[
                      type: "year",
                      relation: relation,
                      before: -1100000000
                  ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX_ALIAS)
    def actualMatchingIds = queryResponse.data.collect { it.id }

    then:
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    actualMatchingIds.containsAll(expectedMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | ['p8']
    'disjoint'   | ['p1', 'p5', 'p6', 'p7', 'p9']
    'intersects' | ['p2', 'p3', 'p4', 'p8']
    'within'     | ['p2', 'p4']
  }

  def 'Year filter with q: (x, y) (with x,y in Paleo range) and `#relation` relation matches #expectedMatchingIds'() {
    given:
    def requestParams = [
        filters: [[
                      type: "year",
                      relation: relation,
                      after: -1500000000,
                      before: -100000
                  ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX_ALIAS)
    def actualMatchingIds = queryResponse.data.collect { it.id }

    then:
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    actualMatchingIds.containsAll(expectedMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | ['p8']
    'disjoint'   | ['p2', 'p9']
    'intersects' | ['p1', 'p3', 'p4', 'p5', 'p6', 'p7', 'p8']
    'within'     | ['p1', 'p5']
  }
}
