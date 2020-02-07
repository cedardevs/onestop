package org.cedar.onestop.api.search.service

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.stream.Collectors

@Slf4j
@Service
class SearchRequestParserService {

  private SearchConfig config

  private static final Map<String, String> facetNameMappings = DocumentationService.facetNameMappings

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

  Map createFacetAggregations() {
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

    def allTextQueries = []

    if(queries) {
      def groupedQueries = queries.groupBy { it.type }

      // Assemble query text
      def queryTextQueries = groupedQueries.queryText
      List<String> queryValues = []
      queryTextQueries.each {it -> queryValues.add(it.value as String)}
      String query
      if(queryValues.size() > 1) {
        query = queryValues.stream()
            .map({s -> new String("(" + s.trim() + ")")})
            .collect(Collectors.joining(" AND "))
      }
      else {
        query = (queryValues.get(0) as String).trim()
      }

      // Assemble fields list, if given, and create the query string query object
      def fields = config?.boosts?.collect({ field, boost -> "${field}^${boost ?: 1}" }) ?: null

      def queryStringMap = [
          query_string: [
              query               : query,
              phrase_slop         : config?.phraseSlop ?: 0,
              tie_breaker         : config?.tieBreaker ?: 0,
              minimum_should_match: config?.minimumShouldMatch ?: '75%',
              lenient             : true
          ]
      ]

      if(fields != null) { queryStringMap.query_string.put("fields", fields) }

      allTextQueries.add(queryStringMap)
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

    groupedFilters.year.each {
      allFilters.add(constructYearFilter(it))
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

  protected List<Map> constructYearFilter(Map filterRequest) {
    return constructTemporalFilter(filterRequest, 'beginYear', 'endYear')
  }

  private List<Map> constructTemporalFilter(Map filterRequest, String beginField, String endField) {

    def x = filterRequest.after
    def y = filterRequest.before

    def relation = filterRequest.relation

    def esFilters = []

    switch (relation) {
    // Results contain query (aka query contained by results)
      case 'contains':
        if (x != null && y == null) {
          esFilters.add([
              bool: [
                  must: [
                      [ range: [ (beginField): [ lte: x ] ] ]
                  ],
                  must_not: [
                      [ exists: [ field: endField ] ]
                  ]
              ]
          ])
        }
        else if (x == null && y != null) {
          esFilters.add([
              bool: [
                  must: [
                      [ range: [ (endField): [ gte: y ] ] ]
                  ],
                  must_not: [
                      [ exists: [ field: beginField ] ]
                  ]
              ]
          ])
        }
        else if (x != null && y != null) {
          esFilters.add([
              bool: [
                  minimum_should_match: 1,
                  should: [
                      [ bool: [
                          must: [
                              [ range: [ (beginField): [ lte: x ] ] ]
                          ],
                          must_not: [
                              [ exists: [ field: endField ] ]
                          ]
                      ]],
                      [ bool: [
                          must: [
                              [ range: [ (endField): [ gte: y ] ] ]
                          ],
                          minimum_should_match: 1,
                          should: [
                              [ range: [ (beginField): [ lte: x ]] ],
                              [ bool: [
                                  must_not: [
                                      [ exists: [ field: beginField ] ]
                                  ]
                              ]]
                          ]
                      ]]
                  ]
              ]
          ])
        }
        break

      case 'within':
        // Results within query (aka results contained by query)
        if (x != null) {
          esFilters.add([
              range: [ (beginField): [ gte: x ] ]
          ])
        }
        if (y != null) {
          esFilters.add([
              range: [ (endField): [ lte: y ] ]
          ])
        }
        // If x != null && y != null then both statements must be true (elasticsearch AND created)
        break

      case 'disjoint':
        // Results have nothing in common with query
        if (x != null && y == null) {
          esFilters.add([
              range: [ (endField): [ lt: x ] ]
          ])
        }
        else if (x == null && y != null) {
          esFilters.add([
              range: [ (beginField): [ gt: y ] ]
          ])
        }
        else if (x != null && y != null) {
          esFilters.add([
              bool: [
                  minimum_should_match: 1,
                  should: [
                      [ range: [ (beginField): [ gt: y ] ] ],
                      [ range: [ (endField): [ lt: x ] ] ]
                  ]
              ]
          ])
        }
        break

      default:
        // Null or 'intersects'
        if (x != null && y == null) {
          // End date is greater than x; if endDate "ongoing" (null), make sure results actually have a beginDate
          // (otherwise we'll match ones without a time bounding)
          esFilters.add([
              bool: [
                  minimum_should_match: 1,
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
        else if (x == null && y != null) {
          esFilters.add([
              bool: [
                  minimum_should_match: 1,
                  should: [
                      [ range: [ (beginField): [ lte: y ]] ],
                      [ bool: [
                          must: [
                              [ exists: [ field: endField ] ]
                          ],
                          must_not: [
                              [ exists: [ field: beginField ] ]
                          ]
                      ]]
                  ]
              ]
          ])
        }
        else if (x != null && y != null){
          esFilters.add([
              bool: [
                  minimum_should_match: 1,
                  should: [
                      [ bool: [
                          must: [
                              [ range: [ (beginField): [ lte: y ]] ],
                              [ range: [ (endField): [ gte: x ]] ]
                          ]
                      ] ],
                      [ bool: [
                          must: [
                              [ range: [ (endField): [ gte: x ]] ]
                          ],
                          must_not: [
                              [ exists: [ field: beginField ] ]
                          ]
                      ] ],
                      [ bool: [
                          must: [
                              [ range: [ (beginField): [ lte: y ]] ]
                          ],
                          must_not: [
                              [ exists: [ field: endField ] ]
                          ]
                      ] ]
                  ]
              ]
          ])
        }

    }

    return esFilters
  }

  static private Map constructSpatialFilter(Map filterRequest) {
    return [
        geo_shape: [
            spatialBounding: [
                shape   : filterRequest.geometry,
                relation: filterRequest.relation ?: 'intersects'
            ]
        ]
    ]
  }

  static private Map constructFacetFilter(Map filterRequest) {
    def fieldName = facetNameMappings[filterRequest.name] ?: filterRequest.name
    return [
        terms: [
            (fieldName): filterRequest.values
        ]
    ]
  }
}
