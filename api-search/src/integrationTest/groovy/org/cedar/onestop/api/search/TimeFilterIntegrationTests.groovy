package org.cedar.onestop.api.search

import org.cedar.onestop.api.search.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

@Unroll
class TimeFilterIntegrationTests extends IntegrationTest {

  private final String DATES_INDEX = 'time_filter'

  @Autowired
  ElasticsearchService esService

  void setup() {
    refreshAndLoadGenericTestIndex(DATES_INDEX)
  }

  def 'Datetime filter after 1995 and `#relation` relation matches #expectedMatchingIds'() {
    given:
    def requestParams = [
        filters: [[
            type: "datetime",
            relation: relation,
            after: "1995-01-01"
        ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX)
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
    'intersects' | ['1','2','3','5','6','7','8','9','11','12','13','14','15','16','17','19']
    'within'     | ['1','3','7','8','9','16','17','18','19']
  }

  def 'Datetime filter until 2012 and `#relation` relation matches #expectedMatchingIds'() {
    given:
    def requestParams = [
        filters: [[
            type: "datetime",
            relation: relation,
            before: "2012-01-01"
        ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX)
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

  def 'Datetime filter from 1995 to 2012 and `#relation` relation matches #expectedMatchingIds'() {
    given:
    def requestParams = [
        filters: [[
            type: "datetime",
            relation: relation,
            after: "1995-01-01",
            before: "2012-01-01"
        ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX)
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

  def 'Year filter after year 0'() {
    given:
    def requestParams = [
        filters: [[
                      type: "year",
                      relation: 'intersects',
                      after: 0
                  ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX)

    then:
    queryResponse.meta.total > 0
  }


  def 'Year filter until year 0'() {
    given:
    def requestParams = [
        filters: [[
                      type: "year",
                      relation: 'intersects',
                      before: 0
                  ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX)

    then:
    queryResponse.meta.total > 0
  }

  def 'Year filter around year 0'() {
    def requestParams = [
        filters: [[
                      type: "year",
                      relation: 'intersects',
                      after: -1000,
                      before: 1000
                  ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX)

    then:
    queryResponse.meta.total > 0
  }

  def 'Year filter after -1000000000 and `#relation` relation matches #expectedMatchingIds'() {
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
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX)
    def actualMatchingIds = queryResponse.data.collect { it.id }

    then:
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    actualMatchingIds.containsAll(expectedMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | ['p9']
    'disjoint'   | ['p8', 'p10']
    'intersects' | ['z1', 'z2', 'p7', 'z3', 'p9', 'p12', 'p11']
    'within'     | ['z1', 'p7', 'z3', 'p12', 'p11']
  }

  def 'Year filter until -1100000000 and `#relation` relation matches #expectedMatchingIds'() {
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
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX)
    def actualMatchingIds = queryResponse.data.collect { it.id }

    then:
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    actualMatchingIds.containsAll(expectedMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | ['p9']
    'disjoint'   | ['z1', 'p7', 'z3', 'p12', 'p11']
    'intersects' | ['p9', 'p10']
    'within'     | ['p8', 'p10']
  }

  def 'Year filter from -1500000000 to -100000 and `#relation` relation matches #expectedMatchingIds'() {
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
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX)
    def actualMatchingIds = queryResponse.data.collect { it.id }

    then:
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    actualMatchingIds.containsAll(expectedMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | ['p9']
    'disjoint'   | ['z1', 'z3']
    'intersects' | ['p7', 'p9', 'p12', 'p10', 'p11']
    'within'     | ['p7', 'p11']
  }
}
