package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.search.service.LogstashETLService
import org.cedar.onestop.api.search.service.LogstashElasticService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Slf4j
@RestController
class LogstashSearchController {
    private final LogstashElasticService logstashElasticService
    private final LogstashETLService logstashETLService

    @Autowired
    LogstashSearchController(LogstashElasticService service, LogstashETLService etlService) {
        this.logstashElasticService = service
        this.logstashETLService = etlService
    }

    /**
     * @param numResults is the top search results logged by Logstash
     * @param numDays indicates the range of days inclusive today.
     * @return top search results
     */
    @GetMapping(path='/search/logstash')
    Map topSearches(@RequestParam(required = false) Optional<Integer> numResults, @RequestParam(required = false) Optional<Integer> numDays) {
        Integer extractedNumResults = numResults.isPresent() ? numResults.get() : 10
        Integer extractedNumDays = numDays.isPresent() ? numDays.get() : 1
        Map result = logstashElasticService.getTopSearchQueries(extractedNumResults, extractedNumDays)
        Map etlResult = logstashETLService.etlResponse(result)
        return etlResult
    }
}
