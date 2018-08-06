package org.cedar.onestop.api.search.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.http.Header
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LogstashElasticService {
    private String LOGSTASH_PREFIX = 'logstash'

    private final RestClient restClient

    @Autowired
    LogstashElasticService(RestClient restClient) {
        this.restClient = restClient
    }

    Map getTopTenSearchQueries() {
        Map query = [
                "size": 0,
                "aggs": [
                        "group_by_query": [
                                "terms": [
                                        "field": "logParams.queries.value.keyword"
                                ]
                        ]
                ]
        ]

//        Map emptyMap = [:]
        def headers = new NStringEntity(JsonOutput.toJson(query), ContentType.APPLICATION_JSON)
        //TODO: Dynamically handle date in the indices
        def response = restClient.performRequest('GET', '/logstash-2018.08.06/_search', Collections.EMPTY_MAP, headers)
        Map result = [:]
//        try {
            if (response.getEntity()) {
                result += new JsonSlurper().parse(response?.getEntity()?.getContent()) as Map
            }
//        }

        return result
    }
}
