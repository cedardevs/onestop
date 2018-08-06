package org.cedar.onestop.api.search.service

import groovy.json.JsonSlurper
import org.springframework.stereotype.Service

@Service
class LogstashETLService {

    Map reformatJSON(Map esResponse) {
//        def jsonSlurper = new JsonSlurper()
//        def parsedResponse = jsonSlurper.parseText(esResponse)

        Map queryCounts = [:]
        List queries = esResponse["aggregations"]["group_by_query"]["buckets"]
        queries.each { Map searchQuery ->
            def key = searchQuery["key"]
            def doc_count = searchQuery["doc_count"]
            queryCounts[key] = doc_count
        }
        return queryCounts
    }
}
