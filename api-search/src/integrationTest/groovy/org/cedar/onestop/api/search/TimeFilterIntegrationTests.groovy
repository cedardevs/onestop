package org.cedar.onestop.api.search

import org.cedar.onestop.api.search.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

@Unroll
class TimeFilterIntegrationTests extends IntegrationTest {

  private final String DATES_INDEX = 'dates_testing'

  @Autowired
  ElasticsearchService esService

  void setup() {
    refreshAndLoadGenericTestIndex(DATES_INDEX)
  }

  def 'Datetime filter with begin date only and #relation relation returns correct results'() {
    given:
    def requestParams = [
        filters: [[
            type: "datetime",
            relation: relation,
            after: "1993-01-01"
        ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX)

    then:
    def actualMatchingIds = queryResponse.data.collect { it.id }
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | []
    'disjoint'   | ['5']
    'intersects' | ['2', '3', '4', '6']
    'within'     | ['2', '3', '6']
  }

  def 'Datetime filter with end date only and #relation relation returns correct results'() {
    given:
    def requestParams = [
        filters: [[
            type: "datetime",
            relation: relation,
            before: "2001-01-01"
        ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX)

    then:
    def actualMatchingIds = queryResponse.data.collect { it.id }
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | ['2', '3']
    'disjoint'   | ['6']
    'intersects' | ['2', '3', '5']
    'within'     | ['5']
  }

  def 'Datetime filter with begin and end dates and #relation relation returns correct results'() {
    given:
    def requestParams = [
        filters: [[
            type: "datetime",
            relation: relation,
            after: "1985-01-01",
            before: "1998-01-01"
        ]],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, DATES_INDEX)

    then:
    def actualMatchingIds = queryResponse.data.collect { it.id }
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | []
    'disjoint'   | ['2', '6']
    'intersects' | ['3', '5']
    'within'     | []
  }

  def 'Year filter with begin year only and #relation relation returns correct results'() {
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

    then:
    def actualMatchingIds = queryResponse.data.collect { it.id }
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | ['9']
    'disjoint'   | ['8', '10']
    'intersects' | ['2', '3', '4', '5', '6', '7', '9', '11', '12']
    'within'     | ['2', '3', '5', '6', '7', '11', '12']
  }

  def 'Year filter with end year only and #relation relation returns correct results'() {
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

    then:
    def actualMatchingIds = queryResponse.data.collect { it.id }
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | ['9']
    'disjoint'   | ['2', '3', '5', '6', '7', '11', '12']
    'intersects' | ['9', '10']
    'within'     | ['8', '10']
  }

  def 'Year filter with begin and end years and #relation relation returns correct results'() {
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

    then:
    def actualMatchingIds = queryResponse.data.collect { it.id }
    expectedMatchingIds.containsAll(actualMatchingIds)

    and:
    queryResponse.meta.total == expectedMatchingIds.size()

    where:
    relation     | expectedMatchingIds
    'contains'   | []
    'disjoint'   | ['2', '3', '5', '6']
    'intersects' | ['7', '9', '10', '11', '12']
    'within'     | ['7', '11']
  }
}
