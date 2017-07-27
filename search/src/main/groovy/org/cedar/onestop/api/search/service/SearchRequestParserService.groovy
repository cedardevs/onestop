package org.cedar.onestop.api.search.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class SearchRequestParserService {

  private SearchConfig config

  public static final Map<String, String> facetNameMappings = [
      'science'       : 'gcmdScience',
      'instruments'   : 'gcmdInstruments.raw',
      'platforms'     : 'gcmdPlatforms.raw',
      'projects'      : 'gcmdProjects.raw',
      'dataCenters'   : 'gcmdDataCenters.raw',
      'dataResolution': 'gcmdDataResolution.raw',
  ]

  @Autowired
  SearchRequestParserService(SearchConfig config) {
    this.config = config
  }

  Map parseSearchQuery(Map params) {
    log.debug("Queries: ${params.queries}")
    log.debug("Filters: ${params.filters}")

    def requestQuery = [
        bool: [
            must  : assembleScoringContext(params.queries),
            filter: assembleFilteringContext(params.filters)
        ]
    ]
    return requestQuery
  }

  Boolean shouldReturnCollections(Map params) {
    !params.filters.any { it.type == 'collection' }
  }

  Map createCollectionsAggregation() {
    return [
        terms       : [
            field: "parentIdentifier",
            size : 0,
            order: [
                "score_agg.max": "desc"
            ]
        ],
        aggregations: [
            "score_agg": [
                stats: [
                    script: [
                        inline: "_score",
                        lang  : "expression"
                    ]
                ]
            ]
        ]
    ]
  }

  Map createGCMDAggregations(boolean forCollections) {
    def aggregations = []
    facetNameMappings.each { name, field ->
      def agg = [
          terms: [
              field: field,
              size : 0,
              order: [
                  "_term": "asc"
              ]
          ]
      ]
      if (forCollections) {
        agg.aggregations = [
            byCollection: [
                terms: [
                    field: "parentIdentifier",
                    size : 0
                ]
            ]
        ]
      }
      aggregations.add([(name): agg])
    }
    return aggregations
  }

  private Map assembleScoringContext(List<Map> queries) {
    def allTextQueries = []

    def groupedQueries = queries.groupBy { it.type }

    groupedQueries.queryText.each {
      def text = (it.value as String).trim()
      def queryObject = [
          query_string: [
              query      : text,
              fields     : [],
              phrase_slop: 0,
              tie_breaker: 0,
              lenient    : true
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
              query             : [
                  bool: [
                      must: []
                  ]
              ],
              field_value_factor: [
                  field   : "dsmmAverage",
                  modifier: "${config.dsmm.modifier ?: 'log1p'}",
                  factor  : "${config.dsmm.factor ?: 1f}",
                  missing : 0
              ],
              boost_mode        : 'sum'
          ]
      ]

      allTextQueries.each { query ->
        functionScoreQuery.function_score.query.bool.must.add(query)
      }
      return functionScoreQuery
    } else {
      return allTextQueries
    }
  }

  private Map assembleFilteringContext(List<Map> filters) {
    /*For filters:
     * union: A | B | A & B; intersection: A & B
     - union with bool > must > bool > should [] for multiple selections on same term
     - union of multiple unions is  bool > must >> bool > should []
            -- (does this mean a match must come from each nested filter?)
     - intersection probably bool > must > bool > must (single term)
*/
    def allFilters = []

    def groupedFilters = filters.groupBy { it.type }

    // Temporal filters:
    groupedFilters.datetime.each {
      if (it.before) {
        allFilters.add([
            range: [
                'temporalBounding.beginDate': [
                    lte: it.before
                ]
            ]
        ])
      }
      if (it.after) {
        allFilters.add([
            range: [
                'temporalBounding.endDate': [
                    gte: it.after
                ]
            ]
        ])
      }
    }

    // Spatial filters:
    groupedFilters.geometry.each {
      allFilters.add([
          geo_shape: [
              spatialBounding: [
                  shape   : it.geometry,
                  relation: it.relation ?: 'intersects'
              ]
          ]
      ])
    }

    // Exclude global results filter:
    if (groupedFilters.excludeGlobal) {
      // Handling filter & null awkwardness of 'isGlobal' property -- passing false through excludes all non-global
      // and null records from results, which isn't exactly what this filter implies
      if (groupedFilters.excludeGlobal[0].value == true) {
        allFilters.add([
            term: [
                isGlobal: false
            ]
        ])
      }
    }

    // Facet filters:
    groupedFilters.facet.each {
      def fieldName = facetNameMappings[it.name] ?: it.name
      allFilters.add([
          terms: [
              (fieldName): it.values
          ]
      ])
    }

    // Collection filter -- force a union since an intersection on multiple parentIds will return nothing
    def parentIds = [] as Set
    groupedFilters.collection.each {
      parentIds.addAll(it.values)
    }
    if (parentIds) {
      allFilters.add([
          terms: [parentIdentifier: parentIds]
      ])
    }

    return allFilters
  }

}
