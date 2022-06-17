package org.cedar.onestop.api.search.service

import groovy.util.logging.Slf4j
import org.cedar.onestop.data.util.JsonUtils
import org.cedar.onestop.data.util.MapUtils
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchReadService
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class ElasticsearchService {

  private SearchRequestParserService searchRequestParserService
  private ElasticsearchReadService esReadService

  private RestHighLevelClient restHighLevelClient
  ElasticsearchConfig esConfig

  @Autowired
  ElasticsearchService(SearchRequestParserService searchRequestParserService, RestHighLevelClient restHighLevelClient, ElasticsearchConfig elasticsearchConfig) {
    this.searchRequestParserService = searchRequestParserService
    this.restHighLevelClient = restHighLevelClient
    this.esConfig = elasticsearchConfig
    this.esReadService = new ElasticsearchReadService(this.restHighLevelClient, this.esConfig)
  }

  ElasticsearchReadService getReadService() {
    // FIXME This is ONLY here to support TrendingSearchService and should be removed upon refactoring/removing that code
    return esReadService;
  }

  ////////////
  // Counts //
  ////////////
  Map getTotalCollections() {
    return esReadService.getTotalCountInIndex(esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
  }

  Map getTotalGranules() {
    return esReadService.getTotalCountInIndex(esConfig.GRANULE_SEARCH_INDEX_ALIAS)
  }

  Map getTotalFlattenedGranules() {
    return esReadService.getTotalCountInIndex(esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
  }


  ///////////////
  // Get By ID //
  ///////////////
  Map getCollectionById(String id) {
    def getCollection = esReadService.getById(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, id)

    if(getCollection.data) {
      // get the total number of granules for this collection id
      def totalGranulesForCollection = esReadService.getTotalCountInIndexByTerm(esConfig.GRANULE_SEARCH_INDEX_ALIAS, "internalParentIdentifier", id)
      def attributes = (Map) ((Map) ((List) totalGranulesForCollection.get("data")).first()).get("attributes")
      getCollection.meta = [
          totalGranules: attributes.get("count")
      ]
    }

    return getCollection
  }

  Map getGranuleById(String id) {
    return esReadService.getById(esConfig.GRANULE_SEARCH_INDEX_ALIAS, id)
  }

  Map getFlattenedGranuleById(String id) {
    return esReadService.getById(esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS, id)
  }

  Map getSitemapById(String id) {
    return esReadService.getById(esConfig.SITEMAP_INDEX_ALIAS, id)
  }


  //////////////
  // Mappings //
  //////////////
  Map getCollectionMapping() {
    return esReadService.getIndexMapping(esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
  }

  Map getGranuleMapping() {
    return esReadService.getIndexMapping(esConfig.GRANULE_SEARCH_INDEX_ALIAS)
  }

  Map getFlattenedGranuleMapping() {
    return esReadService.getIndexMapping(esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
  }


  //////////////
  // Searches //
  //////////////
  Map searchCollections(Map searchParams) {
    return searchFromRequest(searchParams, esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
  }

  Map searchGranules(Map searchParams) {
    return searchFromRequest(searchParams, esConfig.GRANULE_SEARCH_INDEX_ALIAS)
  }

  Map searchFlattenedGranules(Map searchParams) {
    return searchFromRequest(searchParams, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
  }

  Map<String, Object> searchSitemap() {
    def requestBody = [
      _source: ["lastUpdatedDate",]
    ]
    log.debug("Searching for sitemap")
    return esReadService.getSearchResults(esConfig.SITEMAP_INDEX_ALIAS, requestBody);
  }

  Map<String, Object> searchFromRequest(Map params, String index) {
    def requestBody = buildRequestBody(params)
    def marshalledResponse = esReadService.performRequest('GET', "$index/_search", JsonUtils.toJson(requestBody))

    def searchResponse = esReadService.constructSearchResponse(marshalledResponse)
    if (searchResponse.containsKey('errors')) // This is spelled out in ElasticReadService.constructSearchResponse.
      return searchResponse

    def facets = prepareFacets(marshalledResponse)
    if (facets) {
      def meta = new HashMap<String, Object>((Map) searchResponse.get("meta"))
      meta.facets = facets
      searchResponse.put("meta", meta)
    }
    return searchResponse
  }

  Map<String, Object> buildRequestBody(Map params) {
    def query = searchRequestParserService.parseSearchQuery(params)
    def getFacets = params.facets as boolean

    Map requestBody = addAggregations(query, getFacets)

    // We need to include "track_total_hits" param, otherwise we won't display counts > 10,000
    requestBody.track_total_hits = true

    // default summary to true
    def summary = params.summary == null ? true : params.summary as boolean
    if (summary) {
      requestBody = addSourceFilter(requestBody)
    }
    if (params.containsKey('page')) {
      requestBody = addPagination(requestBody, params.page as Map)
    }
    if (params.containsKey('sort')) {
      requestBody = addSort(requestBody, params.sort as List)
    }
    if (params.containsKey('search_after')) {
      requestBody = addSearchAfter(requestBody, params.search_after as List)
    }
    requestBody = MapUtils.pruneEmptyElements(requestBody)
    return requestBody
  }

  private Map addAggregations(Map query, boolean getFacets) {
    def aggregations = [:]

    if (getFacets) {
      aggregations.putAll(searchRequestParserService.createFacetAggregations())
    }

    def requestBody = [
        query       : query,
        aggregations: aggregations
    ]
    return requestBody
  }

  private static Map addSourceFilter(Map requestBody) {
    requestBody._source = DocumentationService.summaryFields
    return requestBody
  }

  private static Map addPagination(Map requestBody, Map pageParams) {
    requestBody.size = pageParams?.max != null ? pageParams.max : 10
    requestBody.from = pageParams?.offset ?: 0
    return requestBody
  }

  private static Map addSort(Map requestBody, List sortParams) {
//    requestBody.sort = sortParams ? sortParams + ["_doc": "desc"] : [["_score":"desc"], ["_doc": "desc"]]
    requestBody.sort = sortParams ? sortParams : [["_score":"desc"], ["_doc": "desc"]]
    return requestBody
  }

  private static Map addSearchAfter(Map requestBody, List searchAfterParams) {
    requestBody.search_after = searchAfterParams //just pass through
    return requestBody
  }

  // TODO This really needs to be part of config that can change -- properly setup @RefreshScope beans & a Config Manager microservice...?
  private static final topLevelKeywords = [
      'science' : [
          'Agriculture', 'Atmosphere', 'Biological Classification', 'Biosphere', 'Climate Indicators',
          'Cryosphere', 'Human Dimensions', 'Land Surface', 'Oceans', 'Paleoclimate', 'Solid Earth',
          'Spectral/Engineering', 'Sun-Earth Interactions', 'Terrestrial Hydrosphere'
      ],
      'locations': [
          'Continent', 'Geographic Region', 'Ocean', 'Solid Earth', 'Space', 'Vertical Location'
      ]
  ]

  private Map prepareFacets(Map searchResponse) {
    def aggregations = searchResponse.aggregations
    if (!aggregations) {
      return null
    }
    def hasFacets = false
    def result = [:]
    DocumentationService.facetNameMappings.each { name, field ->
      def topLevelKeywords = topLevelKeywords[name]
      def buckets
      def nestedParts = field.split(/\./, 2)
      if (nestedParts.length == 1) {
        buckets = aggregations."$name"?.buckets
      } else {
        // 'foobar' here can be named anything, but it MUST correspond to the same key in SearchRequestParserService::createFacetAggregations
        buckets = aggregations."$name"?.foobar?.buckets
      }
      if (buckets) {
        hasFacets = true
      }
      result[name] = cleanAggregation(topLevelKeywords, buckets)
    }
    return hasFacets ? result : null
  }

  static private Map cleanAggregation(List<String> topLevelKeywords, List<Map> originalAgg) {
    def cleanAgg = [:]
    originalAgg.each { e ->
      def term = e.key
      def count = e.doc_count
      if (!topLevelKeywords) {
        cleanAgg.put(term, [count: count])
      } else {
        if (term.contains('>')) {
          def splitTerms = term.split('>', 2)
          if (topLevelKeywords.contains(splitTerms[0].trim())) {
            cleanAgg.put(term, [count: count])
          }
        } else {
          if (topLevelKeywords.contains(term)) {
            cleanAgg.put(term, [count: count])
          }
        }
      }
    }
    return cleanAgg
  }

  private Map pruneEmptyElements(Map requestBody) {
    def prunedRequest = requestBody.collectEntries { k, v -> [k, v instanceof Map ? pruneEmptyElements(v) : v]}.findAll { k, v -> v != null && v != [:] }
    return prunedRequest
  }
}
