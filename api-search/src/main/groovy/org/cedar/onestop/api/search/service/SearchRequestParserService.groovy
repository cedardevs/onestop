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

  private List<Map> assembleFilteringContext(List<Map> filters) {
    if (!filters) {
      return null
    }

    def allFilters = []
    def groupedFilters = filters.groupBy { it.type }

    // Temporal filters:
    groupedFilters.datetime.each {
      allFilters.add(constructDateTimeFilter(it))
    }

    groupedFilters.paleoDate.each {
      allFilters.add(constructPaleoFilter(it))
    }

    // Spatial filters:
    groupedFilters.geometry.each {
      allFilters.add(constructSpatialFilter(it))
    }

    // Facet filters:
    groupedFilters.facet.each {
      allFilters.add(constructFacetFilter(it))
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

  private List<Map> constructDateTimeFilter(Map filterRequest) {
    return constructTemporalFilter(filterRequest, 'beginDate', 'endDate')
  }

  private List<Map> constructPaleoFilter(Map filterRequest) {
    return constructTemporalFilter(filterRequest, 'beginYear', 'endYear')
  }

  private List<Map> constructTemporalFilter(Map filterRequest, String beginField, String endField) {

    def x = filterRequest.after
    def y = filterRequest.before
    def relation = filterRequest.relation

    def esFilters = []

    switch (relation) {
    // Results contain query
      case 'contains':
        if (x || y) {
          def beginVal = x ? x : y
          def endVal = y ? y : x

          esFilters.add([
              bool: [
                  must: [
                      [ range: [ (beginField): [ lte: beginVal ]] ]
                  ],
                  should: [
                      [ range: [ (endField): [ gte: endVal ]] ],
                      [ bool: [
                          must_not: [
                              [ exists: [ field: endField ] ]
                          ]
                      ]]
                  ]
              ]
          ])
        }
        break

      case 'within':
        // Results within query
        if (x) {
          esFilters.add([
              range: [ (beginField): [ gte: x ] ]
          ])
        }
        if (y) {
          esFilters.add([
              range: [ (endField): [ lte: y ] ]
          ])
        }
        break

      case 'disjoint':
        // Results have nothing in common with query
        if (x && !y) {
          esFilters.add([
              range: [ (endField): [ lt: x ] ]
          ])
        }
        else if (!x && y) {
          esFilters.add([
              range: [ (beginField): [ gt: y ] ]
          ])
        }
        else if (x && y) {
          esFilters.add([
              bool: [
                  should: [
                      [ bool: [
                          must: [
                              [range: [ (beginField): [ lt: x ] ]],
                              [range: [ (endField): [ lt: x ] ]]
                          ]
                      ]],
                      [ bool: [
                          must: [
                              [range: [ (beginField): [ gt: y ] ]]
                          ],
                          should: [
                              [range: [ (endField): [ gt: y ] ]],
                              [ bool: [
                                  must_not: [
                                      [ exists: [ field: endField ] ]
                                  ]
                              ]]
                          ]
                      ]]
                  ]
              ]
          ])
        }
        break

      default:
        // Null or 'intersects'
        if (x && !y) {
          // End date is greater than x; if endDate "ongoing" (null), make sure results actually have a beginDate
          // (otherwise we'll match ones without a time bounding)
          esFilters.add([
              bool: [
                  should: [
                      [ range: [ (endField): [ gte: x ]] ],
                      [ bool: [
                          must: [
                              [ exists: [ field: beginField ] ]
                          ],
                          must_not: [
                              [ exists: [ field: endField ] ]
                          ]
                      ]]
                  ]
              ]
          ])
        }
        else if (!x && y) {
          esFilters.add([
              range: [
                  (beginField): [
                      lte: y
                  ]
              ]
          ])
        }
        else if (x && y){
          esFilters.add([
              bool: [
                  must: [
                      [ range: [ (beginField): [ lte: y ]] ]
                  ],
                  should: [
                      [ range: [ (endField): [ gte: x ]] ],
                      [ bool: [
                          must_not: [
                              [ exists: [ field: endField ] ]
                          ]
                      ]]
                  ]
              ]
          ])
        }

    }

    return esFilters
  }

  private Map constructSpatialFilter(Map filterRequest) {
    return [
        geo_shape: [
            spatialBounding: [
                shape   : filterRequest.geometry,
                relation: filterRequest.relation ?: 'intersects'
            ]
        ]
    ]
  }

  private Map constructFacetFilter(Map filterRequest) {
    def fieldName = facetNameMappings[filterRequest.name] ?: filterRequest.name
    return [
        terms: [
            (fieldName): filterRequest.values
        ]
    ]
  }
}
