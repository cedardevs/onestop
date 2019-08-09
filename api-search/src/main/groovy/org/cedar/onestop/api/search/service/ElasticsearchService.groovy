package org.cedar.onestop.api.search.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.elasticsearch.Version
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import static org.cedar.onestop.elastic.common.DocumentUtil.*

@Slf4j
@Service
class ElasticsearchService {

  private SearchRequestParserService searchRequestParserService

  private RestClient restClient
  ElasticsearchConfig esConfig
  Version version

  @Autowired
  ElasticsearchService(SearchRequestParserService searchRequestParserService, RestClient restClient, ElasticsearchConfig elasticsearchConfig) {

    this.version = elasticsearchConfig.version
    log.info("Elasticsearch found with version: ${this.version.toString()}" )
    boolean supported = version.onOrAfter(Version.V_5_6_0)
    if(!supported) {
      throw new RuntimeException("Search API does not support version ${version.toString()} of Elasticsearch")
    }

    this.searchRequestParserService = searchRequestParserService
    this.restClient = restClient
    this.esConfig = elasticsearchConfig
  }

  Map searchFlattenedGranules(Map searchParams) {
    return searchFromRequest(searchParams, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
  }

  Map searchGranules(Map searchParams) {
    return searchFromRequest(searchParams, esConfig.GRANULE_SEARCH_INDEX_ALIAS)
  }

  Map searchCollections(Map searchParams) {
    return searchFromRequest(searchParams, esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
  }

  Map getCollectionById(String id) {
    def getCollection = getById(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, id)

    if(getCollection.data) {
      // get the total number of granules for this collection id
      String granuleEndpoint = "/${esConfig.GRANULE_SEARCH_INDEX_ALIAS}/_search"
      HttpEntity granuleRequestQuery = new NStringEntity(JsonOutput.toJson([
          query: [
              term: [
                  internalParentIdentifier: id
              ]
          ],
          size : 0
      ]), ContentType.APPLICATION_JSON)
      Request granuleRequest = new Request('GET', granuleEndpoint)
      granuleRequest.entity = granuleRequestQuery
      Response granuleResponse = restClient.performRequest(granuleRequest)
      Map parsedGranuleResponse = parseSearchResponse(granuleResponse)
      int totalGranulesForCollection = getHitsTotal(parsedGranuleResponse)
      getCollection.meta = [
          totalGranules: totalGranulesForCollection
      ]
    }

    return getCollection
  }

  Map getGranuleById(String id) {
    return getById(esConfig.GRANULE_SEARCH_INDEX_ALIAS, id)
  }

  Map getSitemapById(String id) {
    return getById(esConfig.SITEMAP_INDEX_ALIAS, id)
  }

  Map searchSitemap() {
    def requestBody = [
      _source: ["lastUpdatedDate",]
    ]
    String searchEndpoint = "${esConfig.SITEMAP_INDEX_ALIAS}/_search"
    log.debug("searching for sitemap against endpoint ${searchEndpoint}")
    HttpEntity searchRequestQuery = new NStringEntity(JsonOutput.toJson(requestBody), ContentType.APPLICATION_JSON)
    Request searchRequest = new Request('GET', searchEndpoint)
    searchRequest.entity = searchRequestQuery
    Response searchResponse = restClient.performRequest(searchRequest)
    Map parsedSearchResponse = parseSearchResponse(searchResponse)

    def result = [
      data: getDocuments(parsedSearchResponse).collect {
        [id: getId(it), type: esConfig.typeFromIndex(getIndex(it)), attributes: getSource(it)]
      },
      meta: [
          took : getTook(parsedSearchResponse),
          total: getHitsTotal(parsedSearchResponse)
      ]
    ]
    return result
  }


  Map getFlattenedGranuleById(String id) {
    return getById(esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS, id)
  }

  Map totalCollections() {
    return totalCounts(esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
  }

  Map totalGranules() {
    return totalCounts(esConfig.GRANULE_SEARCH_INDEX_ALIAS)
  }

  Map totalFlattenedGranules() {
    return totalCounts(esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
  }

  Map totalCounts(String alias) {
    String endpoint = "/${alias}/_search"
    HttpEntity requestQuery = new NStringEntity(JsonOutput.toJson([
        query: [
            match_all: [:]
        ],
        size : 0
    ]), ContentType.APPLICATION_JSON)
    Request totalCountsRequest = new Request('GET', endpoint)
    totalCountsRequest.entity = requestQuery
    Response totalCountsResponse = restClient.performRequest(totalCountsRequest)
    Map parsedResponse = parseSearchResponse(totalCountsResponse)

    return [
        data: [
            [
                type : "count",
                id   : esConfig.typeFromAlias(alias),
                count: getHitsTotal(parsedResponse)
            ]
        ]
    ]
  }

  private Map getById(String alias, String id) {
    String endpoint = "/${alias}/${esConfig.TYPE}/${id}"
    log.debug("Get by ID against endpoint: ${endpoint}")
    Request idRequest = new Request('GET', endpoint)
    Response idResponse = restClient.performRequest(idRequest)
    Map collectionDocument = parseSearchResponse(idResponse)
    String type = esConfig.typeFromAlias(alias)
    if (collectionDocument.found) {
      return [
          data: [[
                     id        : getId(collectionDocument),
                     type      : esConfig.typeFromAlias(alias),
                     attributes: getSource(collectionDocument)
                 ]]
      ]
    }
    else {
      return [
          status: HttpStatus.NOT_FOUND.value(),
          title : 'No such document',
          detail: "Record type ${type} with Elasticsearch ID [ ${id} ] does not exist."
      ]
    }
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

    // default summary to true
    def summary = params.summary == null ? true : params.summary as boolean
    if (summary) {
      requestBody = addSourceFilter(requestBody)
    }
    if (params.containsKey('page')) {
      requestBody = addPagination(requestBody, params.page as Map)
    }
    requestBody = pruneEmptyElements(requestBody)
    return requestBody
  }

  Map searchFromRequest(Map params, String index) {
    // TODO: does this parse step need to change based on new different endpoints?
    def requestBody = buildRequestBody(params)

    Map searchResponse = queryElasticsearch(requestBody, index)
    def result = [
        data: getDocuments(searchResponse).collect {
          [id: getId(it), type: esConfig.typeFromIndex(getIndex(it)), attributes: getSource(it)]
        },
        meta: [
            took : getTook(searchResponse),
            total: getHitsTotal(searchResponse)
        ]
    ]

    def facets = prepareFacets(searchResponse)
    if (facets) {
      result.meta.facets = facets
    }
    return result
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
    def sourceFilter = [
        "internalParentIdentifier",
        "title",
        "thumbnail",
        "spatialBounding",
        "beginDate",
        "beginYear",
        "endDate",
        "endYear",
        "links",
        "citeAsStatements",
        "serviceLinks"
    ]
    requestBody._source = sourceFilter
    return requestBody
  }

  private static Map addPagination(Map requestBody, Map pageParams) {
    requestBody.size = pageParams?.max != null ? pageParams.max : 10
    requestBody.from = pageParams?.offset ?: 0
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
    def facetNames = searchRequestParserService.facetNameMappings.keySet()
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
