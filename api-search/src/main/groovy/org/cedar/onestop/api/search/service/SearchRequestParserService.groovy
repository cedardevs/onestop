package org.cedar.onestop.api.search.service

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class SearchRequestParserService {

  private SearchConfig config

  public static final Map<String, String> facetNameMappings = [
      'science'             : 'gcmdScience',
      'services'            : 'gcmdScienceServices',
      'locations'           : 'gcmdLocations',
      'instruments'         : 'gcmdInstruments',
      'platforms'           : 'gcmdPlatforms',
      'projects'            : 'gcmdProjects',
      'dataCenters'         : 'gcmdDataCenters',
      'horizontalResolution': 'gcmdHorizontalResolution',
      'verticalResolution'  : 'gcmdVerticalResolution',
      'temporalResolution'  : 'gcmdTemporalResolution',
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
            must  : assembleScoringContext(params.queries) ?: [:],
            filter: assembleFilteringContext(params.filters) ?: [:]
        ]
    ]
    return requestQuery
  }

  Map createCollectionsAggregation() {
    return [
        terms       : [
            field: "internalParentIdentifier",
            size : Integer.MAX_VALUE,
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

  Map createGCMDAggregations() {
    def aggregations = [:]
    facetNameMappings.each { name, field ->
      def agg = [
          terms: [
              field: field,
              size : Integer.MAX_VALUE,
              order: [
                  "_term": "asc"
              ]
          ]
      ]
      aggregations.put(name, agg)
    }
    return aggregations
  }

  private List<Map> assembleScoringContext(List<Map> queries) {
    if (!queries) {
      return null
    }

    def groupedQueries = queries.groupBy { it.type }
    def allTextQueries = groupedQueries.queryText.collect {
      return [
          query_string: [
              query               : (it.value as String).trim(),
              // FIXME: Test if default of _all is necessary; if so, we should control the fields in it. #190
              fields              : config?.boosts?.collect({ field, boost -> "${field}^${boost ?: 1}" }) ?: ['_all'],
              phrase_slop         : config?.phraseSlop ?: 0,
              tie_breaker         : config?.tieBreaker ?: 0,
              minimum_should_match: config?.minimumShouldMatch ?: '75%',
              lenient             : true
          ]
      ]
    }

    // FIXME returning as an array because our queries are arrays... ridiculous! #171 >:[
    return [[
        function_score: [
            query             : [
                bool: [
                    must: allTextQueries
                ]
            ],
            field_value_factor: [
                field   : 'dsmmAverage',
                modifier: 'log1p',
                factor  : 1f,
                missing : 0
            ],
            boost_mode        : 'sum'
        ]
    ]]
  }

  /* For filters:
   *  - union: A | B | A & B; intersection: A & B
   *  - union with bool > must > bool > should [] for multiple selections on same term
   *  - union of multiple unions is  bool > must >> bool > should []
   *    -- (does this mean a match must come from each nested filter?)
   *  - intersection probably bool > must > bool > must (single term)
   */
  private List<Map> assembleFilteringContext(List<Map> filters) {
    if (!filters) {
      return null
    }

    def allFilters = []
    def groupedFilters = filters.groupBy { it.type }

    // Temporal filters:
    groupedFilters.datetime.each {
      if (it.before) {
        allFilters.add([
            range: [
                'beginDate': [
                    lte: it.before
                ]
            ]
        ])
      }
      if (it.after) {
        allFilters.add([
            range: [
                'endDate': [
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
          terms: [internalParentIdentifier: parentIds]
      ])
    }

    return allFilters
  }

}
