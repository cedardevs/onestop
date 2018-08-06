package org.cedar.onestop.api.search.controller

import org.cedar.onestop.api.search.service.LogstashElasticService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class LogstashSearchController {
    private final LogstashElasticService logstashElasticService

    @Autowired
    LogstashSearchController(LogstashElasticService service) {
        this.logstashElasticService = service
    }

    @GetMapping(path='/search/logstash')
    String helloWorld() {
        logstashElasticService.topTenSearchQueries
    }
}
