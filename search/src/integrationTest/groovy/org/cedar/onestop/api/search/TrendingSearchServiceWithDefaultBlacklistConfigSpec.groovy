package org.cedar.onestop.api.search

import groovy.json.JsonOutput
import org.cedar.onestop.api.search.service.TrendingBlacklistConfig
import org.cedar.onestop.api.search.service.TrendingSearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

@DirtiesContext
@ActiveProfiles(["integration", "test", "unit-test"])
@SpringBootTest
class TrendingSearchServiceWithDefaultBlacklistConfigSpec extends Specification {

  @Autowired
  private TrendingBlacklistConfig blacklistConfig

  private TrendingSearchService service

  void setup() {
    service = new TrendingSearchService(null, blacklistConfig)
  }

  @Unroll
  def "Builds the correct indices with #numIndices previous"() {
    when:
    String indices = service.indicesBuilder(numIndices)

    then:
    indices == expectedResult

    where:
    numIndices | expectedResult
    // Note: the 'null' in there is because this test doesn't let Spring autowire the service, so the const is not populated from the config
    -1         | '%3Cnull%7Bnow%2Fd%7D%3E' // it always includes at least 1
    0          | '%3Cnull%7Bnow%2Fd%7D%3E' // it always includes at least 1
    1          | '%3Cnull%7Bnow%2Fd%7D%3E'
    2          | '%3Cnull%7Bnow%2Fd%7D%3E%2C%3Cnull%7Bnow%2Fd-1d%7D%3E'
    3          | '%3Cnull%7Bnow%2Fd%7D%3E%2C%3Cnull%7Bnow%2Fd-1d%7D%3E%2C%3Cnull%7Bnow%2Fd-2d%7D%3E'
    4          | '%3Cnull%7Bnow%2Fd%7D%3E%2C%3Cnull%7Bnow%2Fd-1d%7D%3E%2C%3Cnull%7Bnow%2Fd-2d%7D%3E%2C%3Cnull%7Bnow%2Fd-3d%7D%3E'
  }

  def "Cannot build query for unknown term type"() {
    when:
    Map result = service.queryBuilder(0, 'invalid')

    then:
    result == null
  }

  @Unroll
  def "Builds the correct query for '#term'"() {
    when:
    Map result = service.queryBuilder(0, term)

    then:
    println("result for ${term}:")
    println(JsonOutput.toJson(result))
    println(JsonOutput.toJson(result.query.bool.must_not[0].terms[nestedPath]))

    result.query.bool.must_not[0].terms[nestedPath] == expected

    where:
    term                                  | nestedPath                        | expected
    TrendingSearchService.SEARCH_TERM     | 'logParams.queries.value.keyword' | ["weather", "climate", "satellites", "fisheries", "coasts", "oceans"]
    TrendingSearchService.COLLECTION_TERM | 'logParams.id.keyword'            | []
  }

}
