package org.cedar.onestop.api.search.service

import org.springframework.stereotype.Service

@Service
class LogstashETLService {

    Map etlResponse(Map esResponse) {

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
