package org.cedar.onestop.api.search.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchReadService
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

import java.util.stream.Collectors

import static org.cedar.onestop.elastic.common.DocumentUtil.*

@Slf4j
@Service
class ElasticsearchService {

  private SearchRequestParserService searchRequestParserService
  private ElasticsearchReadService esService

  private RestClient restClient
  ElasticsearchConfig esConfig

  boolean isES6

  @Autowired
  ElasticsearchService(SearchRequestParserService searchRequestParserService, RestHighLevelClient restHighLevelClient, RestClient restClient, ElasticsearchConfig elasticsearchConfig) {
    this.searchRequestParserService = searchRequestParserService
    this.restClient = restClient
    this.esConfig = elasticsearchConfig
    this.isES6 = esConfig.version.isMajorVersion(6)
    this.esService = new ElasticsearchReadService(this.restClient, this.esConfig)
  }

  ////////////
  // Counts //
  ////////////
  Map totalCollections() {
    return esService.getTotalCounts(esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
  }

  Map totalGranules() {
    return esService.getTotalCounts(esConfig.GRANULE_SEARCH_INDEX_ALIAS)
  }

  Map totalFlattenedGranules() {
    return esService.getTotalCounts(esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
  }


  ///////////////
  // Get By ID //
  ///////////////
  Map getCollectionById(String id) {
    def getCollection = esService.getById(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, id)

    if(getCollection.data) {
      // get the total number of granules for this collection id
      String granuleEndpoint = "/${esConfig.GRANULE_SEARCH_INDEX_ALIAS}/_search"
      Map granuleRequestMap = [
          query: [
              term: [
                  internalParentIdentifier: id
              ]
          ],
          size : 0
      ]
      if (!isES6) {
        granuleRequestMap.track_total_hits = true
      }
      HttpEntity granuleRequestQuery = new NStringEntity(JsonOutput.toJson(granuleRequestMap), ContentType.APPLICATION_JSON)
      Request granuleRequest = new Request('GET', granuleEndpoint)
      granuleRequest.entity = granuleRequestQuery
      Response granuleResponse = restClient.performRequest(granuleRequest)
      Map parsedGranuleResponse = parseSearchResponse(granuleResponse)
      int totalGranulesForCollection = getHitsTotalValue(parsedGranuleResponse, isES6)
      getCollection.meta = [
          totalGranules: totalGranulesForCollection
      ]
    }

    return getCollection
  }

  Map getGranuleById(String id) {
    return esService.getById(esConfig.GRANULE_SEARCH_INDEX_ALIAS, id)
  }

  Map getFlattenedGranuleById(String id) {
    return esService.getById(esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS, id)
  }

  Map getSitemapById(String id) {
    return esService.getById(esConfig.SITEMAP_INDEX_ALIAS, id)
  }


  //////////////
  // Mappings //
  //////////////
  Map getCollectionMapping() {
    return getIndexMapping(esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
  }

  Map getGranuleMapping() {
    return getIndexMapping(esConfig.GRANULE_SEARCH_INDEX_ALIAS)
  }

  Map getFlattenedGranuleMapping() {
    return getIndexMapping(esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
  }

  Map getIndexMapping(String alias) {
    String endpoint = "/${alias}?include_type_name=false"
    log.debug("GET mapping for ${alias}")

    def request = new Request('GET', endpoint)
    def response = restClient.performRequest(request)
    Map result = parseSearchResponse(response)

    if(!result.error) {
      // Actual timestamped name is used, not the alias, but need to drop the "statusCode"
      def keys = result.keySet().stream().filter({String key -> !key.equals('statusCode')}).collect(Collectors.toList())
      def indexName = keys.first()
      return [
          data: [[
              id: alias,
              type: 'index-map',
              attributes: result.get(indexName)
          ]]
      ]
    }
    else {
      return [
          status: HttpStatus.NOT_FOUND.value(),
          title : 'No such index',
          detail: "Index with alias [ ${alias} ] does not exist."
      ]
    }
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

  Map searchSitemap() {
    def requestBody = [
      _source: ["lastUpdatedDate",]
    ]
    String searchEndpoint = "${esConfig.SITEMAP_INDEX_ALIAS}/_search"
    log.debug("searching for sitemap against endpoint ${searchEndpoint}")
    Request searchRequest = new Request('GET', searchEndpoint).setJsonEntity(requestBody)
    Response searchResponse = restClient.performRequest(searchRequest)
    Map parsedSearchResponse = parseSearchResponse(searchResponse)

    def result = [
      data: getDocuments(parsedSearchResponse).collect {
        [id: getId(it), type: esConfig.typeFromIndex(getIndex(it)), attributes: getSource(it)]
      },
      meta: [
          took : getTook(parsedSearchResponse),
          total: getHitsTotalValue(parsedSearchResponse, isES6)
      ]
    ]
    return result
  }

  Map searchFromRequest(Map params, String index) {
    def requestBody = buildRequestBody(params)

    Map searchResponse = queryElasticsearch(requestBody, index)
    def result = [
        data: getDocuments(searchResponse).collect {
          [id: getId(it), type: esConfig.typeFromIndex(getIndex(it)), attributes: getSource(it)]
        },
        meta: [
            took : getTook(searchResponse),
            total: getHitsTotalValue(searchResponse, isES6)
        ]
    ]

    def facets = prepareFacets(searchResponse)
    if (facets) {
      result.meta.facets = facets
    }
    return result
  }

  Map queryElasticsearch(Map query, String index) {
    log.debug("Querying Elasticsearch index: ${index}")
    String jsonQuery = JsonOutput.toJson(query)
    log.trace("jsonQuery: ${jsonQuery}")
    HttpEntity searchQuery = new NStringEntity(jsonQuery, ContentType.APPLICATION_JSON)
    String endpoint = "${index}/_search"
    Request searchRequest = new Request('GET', endpoint)
    searchRequest.entity = searchQuery
    log.debug("search request: ${searchRequest.toString()}")
    Response searchResponse = restClient.performRequest(searchRequest)
    log.debug("search response: ${searchResponse.toString()}")
    return parseSearchResponse(searchResponse)
  }

  Map buildRequestBody(Map params) {
    def query = searchRequestParserService.parseSearchQuery(params)
    def getFacets = params.facets as boolean

    Map requestBody = addAggregations(query, getFacets)

    // If ES7, we need to include "track_total_hits" param, otherwise we won't display counts > 10,000
    if (!isES6) {
      requestBody.track_total_hits = true
    }

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
    requestBody = pruneEmptyElements(requestBody)
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
    def facetNames = DocumentationService.facetNameMappings.keySet()
    def hasFacets = false
    def result = [:]
    facetNames.each { name ->
      def topLevelKeywords = topLevelKeywords[name]
      def buckets = aggregations."$name"?.buckets
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
