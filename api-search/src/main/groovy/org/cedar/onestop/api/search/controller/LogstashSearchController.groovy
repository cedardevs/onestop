package org.cedar.onestop.api.search.controller

import org.cedar.onestop.api.search.service.LogstashETLService
import org.cedar.onestop.api.search.service.LogstashElasticService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class LogstashSearchController {
    private final LogstashElasticService logstashElasticService
    private final LogstashETLService logstashETLService

    @Autowired
    LogstashSearchController(LogstashElasticService service, LogstashETLService etlService) {
        this.logstashElasticService = service
        this.logstashETLService = etlService
    }

    @GetMapping(path='/search/logstash')
    Map helloWorld() {
        Map result = logstashElasticService.topTenSearchQueries
        Map etlResult = logstashETLService.reformatJSON(result)
        return etlResult
    }
}
