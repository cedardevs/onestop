package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.search.service.TrendingSearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile

@Slf4j
@Profile("trending-search")
@RestController
class TrendingSearchController {

  @Value('${trending.numResults}')
  private final Integer DEFAULT_NUM_RESULTS

  @Value('${trending.numDays}')
  private final Integer DEFAULT_NUM_DAYS

  private final TrendingSearchService trendingSearchService

  @Autowired
  TrendingSearchController(TrendingSearchService trendingSearchService) {
    this.trendingSearchService = trendingSearchService
  }

  /**
   * @param numResults is the top search results logged by Logstash
   * @param numDays indicates the range of days inclusive today.
   * @return top search results
   */
  @GetMapping(path = "/trending/searches")
  Map topSearches(@RequestParam(required = false) Optional<Integer> numResults, @RequestParam(required = false) Optional<Integer> numDays) {
    Integer extractedNumResults = numResults.isPresent() ? numResults.get() : DEFAULT_NUM_RESULTS
    Integer extractedNumDays = numDays.isPresent() ? numDays.get() : DEFAULT_NUM_DAYS
    return trendingSearchService.topRecentSearchTerms(extractedNumResults, extractedNumDays)
  }

  @GetMapping(path = "/trending/collections")
  Map topCollections(@RequestParam(required = false) Optional<Integer> numResults, @RequestParam(required = false) Optional<Integer> numDays) {
    Integer extractedNumResults = numResults.isPresent() ? numResults.get() : DEFAULT_NUM_RESULTS
    Integer extractedNumDays = numDays.isPresent() ? numDays.get() : DEFAULT_NUM_DAYS
    return trendingSearchService.topRecentCollections(extractedNumResults, extractedNumDays)
  }
}
