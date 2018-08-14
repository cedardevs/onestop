package org.cedar.onestop.api.search.service

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties

@Service
@ConditionalOnProperty("features.trending.search")
class TrendingSearchService {
  private final ElasticsearchService elasticsearchService
  private final FilterConfig filterConfig

  @Autowired
  TrendingSearchService(ElasticsearchService elasticsearchService, FilterConfig filterConfig) {
      this.elasticsearchService = elasticsearchService
      this.filterConfig = filterConfig
  }

  Map getTopSearchQueries(Integer size, Integer previousIndices) {
    Map query = queryBuilder(size)
    String indices = indicesBuilder(previousIndices)
    return elasticsearchService.queryElasticsearch(query, indices)
  }

  @Value('${trending.indiceName}')
  private final String indiceName
  
  private String indicesBuilder(Integer previousIndices) {
    StringBuilder indexBuilder = new StringBuilder()
    // TODO do we need to think about including date-sorting in the query, in case logstash needs to be changed to log 1 month at a time
    indexBuilder.append("%3C${indiceName}%7Bnow%2Fd%7D%3E")

    for (int i = 1; i < previousIndices; i++) {
      indexBuilder.append("%2C%3C${indiceName}%7Bnow%2Fd-${i}d%7D%3E")
    }

    return indexBuilder.toString()
  }


  private Map queryBuilder(Integer size) {
    return [
      "query": [
        "bool": [
          "must_not": [
            ["terms": [
              "logParams.queries.value.keyword": filterTerms
            ]]
          ]
        ]
      ],
      "size": 0,
      "aggs": [
        "group_by_query": [
          "terms": [
            "field": "logParams.queries.value.keyword",
            "size": size
          ]
        ]
      ]
    ]
  }

  Map topRecentSearches(int numResults, int numDays) {
    Map response = getTopSearchQueries(numResults, numDays)
    return numOccurencesOfTerms(response)
  }

  Map numOccurencesOfTerms(Map response) {
    Map queryCounts = [:]
    List queries = response["aggregations"]["group_by_query"]["buckets"]
    queries.each { Map searchQuery ->
        def key = searchQuery["key"]
        def doc_count = searchQuery["doc_count"]
        queryCounts[key] = doc_count
    }
    return queryCounts
  }

}
