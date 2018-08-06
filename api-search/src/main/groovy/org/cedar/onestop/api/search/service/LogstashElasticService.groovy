package org.cedar.onestop.api.search.service

import org.springframework.stereotype.Service

@Service
class LogstashElasticService {
    private String LOGSTASH_PREFIX = 'logstash'

    String getTopTenSearchQueries() {
        'Return Hello World from Logstash Service'
    }
}
