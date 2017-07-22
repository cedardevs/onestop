package org.cedar.onestop.api.search.service

import groovy.util.logging.Slf4j
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class SearchRequestBuilder {

  private SearchConfig config

  @Autowired
  SearchRequestParserService(SearchConfig config) {
    this.config = config
  }

  HttpEntity buildSearchQuery(Map params) {

    def requestBody = [
        query: [
            bool: [
                must  : [],
                filter: []
            ]
        ],
        aggs : [],

    ]

    return new NStringEntity(requestBody, ContentType.APPLICATION_JSON)
  }

  private Map assembleScoringContext(List<Map> queries) {
    def allTextQueries = []

    def groupedQueries = queries.groupBy { it.type }

    groupedQueries.queryText.each {
      def text = (it.value as String).trim()
      def queryObject = [
          query_string: [
              query: text,
              fields: [],
              phrase_slop: 0,
              tie_breaker: 0,
              lenient: true
          ]
      ]

      config?.boosts?.each { field, boost ->
        queryObject.query_string.fields.add("${field}^${boost ?: 1}")
      }
      if (!queryObject.query_string.fields) {
        // FIXME: Need to test if this is necessary; also if we're to use '_all' still, we should control the fields in it
        queryObject.query_string.fields.add("_all")
      }
      if (config?.minimumShouldMatch) {
        queryObject.query_string.minimum_should_match = config.minimumShouldMatch
      }
      if (config?.phraseSlop) {
        queryObject.query_string.phrase_slop = config.phraseSlop
      }
      if (config?.tieBreaker) {
        queryObject.query_string.tie_breaker = config.tieBreaker
      }

      allTextQueries.add(queryObject)
    }

    if (config?.dsmm?.factor || config?.dsmm?.modifier) {
      def functionScoreQuery = [
          function_score: [
              query: [
                  bool: [
                      must: []
                  ]
              ],
              field_value_factor: [
                  field: "dsmmAverage",
                  modifier: "${config.dsmm.modifier ?: 'log1p'}",
                  factor: "${config.dsmm.factor ?: 1f}",
                  missing: 0
              ],
              boost_mode: 'sum'
          ]
      ]

      allTextQueries.each { query ->
        functionScoreQuery.function_score.query.bool.must.add(query)
      }
      return functionScoreQuery
    }

    else {
      return allTextQueries
    }
  }

  private List<String> buildQueryFields() {

  }

  private Map assembleFilteringContext(List<Map> filters) {}


}
