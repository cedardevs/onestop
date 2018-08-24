package org.cedar.onestop.api.search.service

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties

@Slf4j
@Service
@ConditionalOnProperty("features.trending.search")
class TrendingSearchService {
  private final ElasticsearchService elasticsearchService
  private final TrendingBlacklistConfig blacklistConfig

  private final String SEARCH_TERM = 'queries'
  private final String COLLECTION_TERM = 'id'

  @Value('${elasticsearch.index.trending.name}')
  private final String indiceName

  @Autowired
  TrendingSearchService(ElasticsearchService elasticsearchService, TrendingBlacklistConfig blacklistConfig) {
      this.elasticsearchService = elasticsearchService
      this.blacklistConfig = blacklistConfig
  }

  Map topRecentSearchTerms(int numResults, int numDays) {
    return recentTermCounts(numResults, numDays, SEARCH_TERM)
  }

  Map topRecentCollections(int numResults, int numDays) {
    return recentTermCounts(numResults, numDays, COLLECTION_TERM)
  }

  private Map recentTermCounts(int numResults, int numDays, String term) {
    Map response = getTopRecentTerms(numResults, numDays, term)
    return numOccurencesOfTerms(response)
  }

  Map getTopRecentTerms(Integer size, Integer previousIndices, String term) {
    Map query = queryBuilder(size, term)
    String indices = indicesBuilder(previousIndices)
    return elasticsearchService.queryElasticsearch(query, indices)
  }

  private String indicesBuilder(Integer previousIndices) {
    StringBuilder indexBuilder = new StringBuilder()
    // TODO do we need to think about including date-sorting in the query, in case logstash needs to be changed to log 1 month at a time
    indexBuilder.append("%3C${indiceName}%7Bnow%2Fd%7D%3E")

    for (int i = 1; i < previousIndices; i++) {
      // Note: %2C is a comma placeholder
      indexBuilder.append("%2C%3C${indiceName}%7Bnow%2Fd-${i}d%7D%3E")
    }

    return indexBuilder.toString()
  }

  private Map queryBuilder(Integer size, String term) {
    switch(term) {
      case SEARCH_TERM:
        return searchQuery(term + ".value.keyword", blacklistConfig.blacklistedSearchTerms, size)
        break
      case COLLECTION_TERM:
        return searchQuery(term + ".keyword", blacklistConfig.blacklistedCollections, size)
        break
    }

    // return null if not matching
  }

  private Map searchQuery(String term, List<String> filters, Integer size) {
    return [
      "query": [
        "bool": [
          "must_not": [
            ["terms": [
              "logParams.${term}": filters
            ]]
          ]
        ]
      ],
      "size": 0,
      "aggs": [
        "group_by_term": [
          "terms": [
            "field": "logParams.${term}",
            "size": size
          ]
        ]
      ]
    ]
  }

  static Map numOccurencesOfTerms(Map response) {
    Map queryCounts = [:]
    List queries = response["aggregations"]["group_by_term"]["buckets"]
    queries.each { Map searchQuery ->
        def key = searchQuery["key"]
        def doc_count = searchQuery["doc_count"]
        queryCounts[key] = doc_count
    }
    return queryCounts
  }

}
