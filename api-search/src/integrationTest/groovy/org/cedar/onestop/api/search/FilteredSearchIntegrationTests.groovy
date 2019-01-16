package org.cedar.onestop.api.search

import groovy.json.JsonOutput
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.cedar.onestop.api.search.service.ElasticsearchService
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired

class FilteredSearchIntegrationTests extends IntegrationTest {

  private final String DATES_INDEX = 'dates_testing'

  @Autowired
  RestClient restClient

  @Autowired
  ElasticsearchService esService

  void setup() {
    def cl = ClassLoader.systemClassLoader
    def datesIndexJson = cl.getResourceAsStream("testIndices/${DATES_INDEX}.json").text
    def datesIndexMapping = new NStringEntity(datesIndexJson, ContentType.APPLICATION_JSON)
    def bulkRequests = cl.getResourceAsStream("data/GenericFilterData/dates.txt").text
    def bulkRequestBody = new NStringEntity(bulkRequests, ContentType.APPLICATION_JSON)

    Response response = restClient.performRequest('DELETE', '_all')
    println("DELETE _all: ${response}")

    def newIndexResponse = restClient.performRequest('PUT', DATES_INDEX, Collections.EMPTY_MAP, datesIndexMapping)
    println("PUT new $DATES_INDEX index: ${newIndexResponse}")

    def dataLoadResponse = restClient.performRequest('POST', '_bulk', Collections.EMPTY_MAP, bulkRequestBody)
    println("POST bulk data load to $DATES_INDEX: $dataLoadResponse")

    restClient.performRequest('POST', '_refresh')
  }

  def 'Time filter with begin date only and #relation relation returns correct results'() {
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
    println(JsonOutput.prettyPrint(JsonOutput.toJson(queryResponse))) //fixme delete

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

  def 'Time filter with end date only and #relation relation returns correct results'() {
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
    println(JsonOutput.prettyPrint(JsonOutput.toJson(queryResponse))) //fixme delete

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

  def 'Time filter with begin and end dates and #relation relation returns correct results'() {
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
    println(JsonOutput.prettyPrint(JsonOutput.toJson(queryResponse))) //fixme delete

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
}
