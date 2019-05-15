package org.cedar.onestop.api.search.service

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile

@Slf4j
@Service
@Profile("trending-search")
class TrendingSearchService {
  private final ElasticsearchService elasticsearchService
  private final TrendingBlacklistConfig blacklistConfig

  static final String SEARCH_TERM = 'queries'
  static final String COLLECTION_TERM = 'id'

  @Value('${elasticsearch.index.trending.name}')
  private final String TRENDING_INDEX

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

  Map getTopRecentTerms(Integer size, Integer numIndices, String term) {
    Map query = queryBuilder(size, term)
    String indices = indicesBuilder(numIndices)
    return elasticsearchService.queryElasticsearch(query, indices)
  }

  String indicesBuilder(Integer numIndices) {
    StringBuilder indexBuilder = new StringBuilder()
    // TODO do we need to think about including date-sorting in the query, in case logstash needs to be changed to log 1 month at a time
    indexBuilder.append("%3C${TRENDING_INDEX}%7Bnow%2Fd%7D%3E")

    for (int i = 1; i < numIndices; i++) {
      // Note: %2C is a comma placeholder
      indexBuilder.append("%2C%3C${TRENDING_INDEX}%7Bnow%2Fd-${i}d%7D%3E")
    }

    return indexBuilder.toString()
  }

  Map queryBuilder(Integer size, String term) {
    switch(term) {
      case SEARCH_TERM:
        return searchQuery(term + ".value.keyword", blacklistConfig.defaultBlacklistedSearchTerms + blacklistConfig.additionalBlacklistedSearchTerms, size)
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
              ("logParams.${term}" as String): filters
            ]]
          ]
        ]
      ],
      "size": 0,
      "aggs": [
        "group_by_term": [
          "terms": [
            "field": ("logParams.${term}" as String),
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
