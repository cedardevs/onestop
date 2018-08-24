package org.cedar.onestop.api.search.service

import groovy.json.JsonOutput
import spock.lang.Specification
import spock.lang.Unroll
import org.springframework.test.context.ActiveProfiles
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@ActiveProfiles(["test", "unit-test"])
@SpringBootTest
class TrendingSearchServiceWithDefaultBlacklistConfigSpec extends Specification {

  @Autowired
  private TrendingBlacklistConfig blacklistConfig

  private TrendingSearchService service

  void setup() {
    service = new TrendingSearchService(null, blacklistConfig)
  }

  @Unroll
  def "Builds the correct query for '#term'" () {
    when:
    Map result = service.queryBuilder(0, term)

    then:
    println("result for ${term}:")
    println(JsonOutput.toJson(result))
    println(JsonOutput.toJson(result.query.bool.must_not[0].terms[nestedPath]))

    result.query.bool.must_not[0].terms[nestedPath] == expected

    where:
    term | nestedPath | expected
    TrendingSearchService.SEARCH_TERM | 'logParams.queries.value.keyword' | ["weather","climate","satellites","fisheries","coasts","oceans"]
    TrendingSearchService.COLLECTION_TERM | 'logParams.id.keyword' | []
  }

}
