package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.search.service.TrendingSearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Slf4j
@ConditionalOnProperty("features.trending.search")
@RestController
class TrendingSearchController {
    private final DEFAULT_NUM_RESULTS = 10
    private final DEFAULT_NUM_DAYS = 1
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
    @GetMapping(path = "/search/trending")
    Map topSearches(@RequestParam(required = false) Optional<Integer> numResults, @RequestParam(required = false) Optional<Integer> numDays) {
      Integer extractedNumResults = numResults.isPresent() ? numResults.get() : DEFAULT_NUM_RESULTS
      Integer extractedNumDays = numDays.isPresent() ? numDays.get() : DEFAULT_NUM_DAYS
      return trendingSearchService.topRecentSearches(extractedNumResults, extractedNumDays)
    }
}
