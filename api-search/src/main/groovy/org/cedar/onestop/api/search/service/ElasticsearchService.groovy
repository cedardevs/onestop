package org.cedar.onestop.api.search.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.Response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Slf4j
@Service
class ElasticsearchService {

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.name}')
  private String SEARCH_INDEX

  @Value('${elasticsearch.index.search.collectionType}')
  private String COLLECTION_TYPE

  @Value('${elasticsearch.index.search.granuleType}')
  private String GRANULE_TYPE

  private SearchRequestParserService searchRequestParserService

  private RestClient restClient


  @Autowired
  ElasticsearchService(SearchRequestParserService searchRequestParserService, RestClient restClient) {
    this.searchRequestParserService = searchRequestParserService
    this.restClient = restClient
  }

  Map search(Map searchParams) {
    def response = queryElasticsearch(searchParams)
    return response
  }

  Map totalCounts() {
    String collectionEndpoint = "/$SEARCH_INDEX/$COLLECTION_TYPE/_search"
    HttpEntity collectionRequest = new NStringEntity(JsonOutput.toJson([
        query: [
            match_all: []
        ],
        size : 0
    ]), ContentType.APPLICATION_JSON)
    def collectionResponse = restClient.performRequest("GET", collectionEndpoint, Collections.EMPTY_MAP, collectionRequest)

    String granuleEndpoint = "/$SEARCH_INDEX/$GRANULE_TYPE/_search"
    HttpEntity granuleRequest = new NStringEntity(JsonOutput.toJson([
        query: [
            bool: [
                must: [
                    script: [
                        script: [
                            inline: "doc['fileIdentifier'] != doc['parentIdentifier']",
                            lang  : "painless"
                        ]
                    ]
                ]
            ]
        ],
        size : 0
    ]), ContentType.APPLICATION_JSON)
    def granuleResponse = restClient.performRequest("GET", granuleEndpoint, Collections.EMPTY_MAP, granuleRequest)

    return [
        data: [
            [
                type : "count",
                id   : "collection",
                count: parseResponse(collectionResponse).hits.total
            ],
            [
                type : "count",
                id   : "granule",
                count: parseResponse(granuleResponse).hits.total
            ]
        ]
    ]
  }

  private Map queryElasticsearch(Map params) {
    def query = searchRequestParserService.parseSearchQuery(params)
    def getCollections = searchRequestParserService.shouldReturnCollections(params)
    def getFacets = params.facets as boolean
    def pageParams = params.page as Map

    def requestBody = addAggregations(query, getFacets, getCollections)
    return getCollections ? getCollectionResults(requestBody, pageParams) : getGranuleResults(requestBody, pageParams)
  }

  private Map addAggregations(Map query, boolean getFacets, boolean getCollections) {
    def aggregations = [:]

    if (getFacets) {
      aggregations.putAll(searchRequestParserService.createGCMDAggregations(getCollections))
    }

    if (getCollections) {
      aggregations.put("collections", searchRequestParserService.createCollectionsAggregation())
    }

    def requestBody = [
        query       : query,
        aggregations: aggregations
    ]
    return requestBody
  }

  private Map getCollectionResults(Map requestBody, Map pageParams) {
    requestBody.size = 0

    String searchEndpoint = "/$SEARCH_INDEX/$GRANULE_TYPE/_search"
    def granuleRequest = new NStringEntity(JsonOutput.toJson(requestBody), ContentType.APPLICATION_JSON)
    def searchResponse = parseResponse(restClient.performRequest("GET", searchEndpoint, Collections.EMPTY_MAP, granuleRequest))

    def totalCount = searchResponse.aggregations.collections.buckets.size()
    if (!totalCount) {
      return [
          data: [],
          meta: [
              total: 0,
              took : searchResponse.tookInMillis
          ]
      ]
    }

    def offset = pageParams?.offset ?: 0
    def max = pageParams?.max ?: 10

    def collectionsToRetrieve = searchResponse.aggregations.collections.buckets
        .stream()
        .skip(offset)
        .limit(max)
        .map({ i -> i.key })
        .collect()

    def multiGetEndpoint = "/$SEARCH_INDEX/$COLLECTION_TYPE/_mget"
    def multiGetRequest = new NStringEntity(JsonOutput.toJson([ids: collectionsToRetrieve]), ContentType.APPLICATION_JSON)
    def multiGetResponse = parseResponse(restClient.performRequest("GET", multiGetEndpoint, Collections.EMPTY_MAP, multiGetRequest))
    def result = [
        data: multiGetResponse.docs.collect {
          [id: it._id, type: it._type, attributes: it._source]
        },
        meta: [
            total: totalCount,
            took : searchResponse.took
        ]
    ]

    def facets = prepareFacets(searchResponse, true)
    if (facets) {
      result.meta.facets = facets
    }
    return result
  }

  private Map getGranuleResults(Map requestBody, Map pageParams) {
    requestBody.size = pageParams?.max ?: 10
    requestBody.from = pageParams?.offset ?: 0

    String searchEndpoint = "/$SEARCH_INDEX/$GRANULE_TYPE/_search"
    def granuleRequest = new NStringEntity(JsonOutput.toJson(requestBody), ContentType.APPLICATION_JSON)
    def searchResponse = parseResponse(restClient.performRequest("GET", searchEndpoint, Collections.EMPTY_MAP, granuleRequest))
    def result = [
        data: searchResponse.hits.hits.collect {
          [id: it._id, type: it._type, attributes: it._source]
        },
        meta: [
            took : searchResponse.took,
            total: searchResponse.hits.total
        ]
    ]

    def facets = prepareFacets(searchResponse, false)
    if (facets) {
      result.meta.facets = facets
    }

    return result
  }

  // TODO This really needs to be part of config that can change -- properly setup @RefreshScope beans & a Config Manager microservice...?
  private static final topLevelKeywords = [
      'science' : [
          'Agriculture', 'Atmosphere', 'Biological Classification', 'Biosphere', 'Climate Indicators',
          'Cryosphere', 'Human Dimensions', 'Land Surface', 'Oceans', 'Paleoclimate', 'Solid Earth',
          'Spectral/Engineering', 'Sun-Earth Interactions', 'Terrestrial Hydrosphere'
      ],
      'location': [
          'Continent', 'Geographic Region', 'Ocean', 'Solid Earth', 'Space', 'Vertical Location'
      ]
  ]

  private Map prepareFacets(Map searchResponse, boolean collections) {
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
      result[name] = cleanAggregation(topLevelKeywords, buckets, collections)
    }
    return hasFacets ? result : null
  }

  private Map cleanAggregation(List<String> topLevelKeywords, List<Map> originalAgg, boolean collections) {
    def cleanAgg = [:]
    originalAgg.each { e ->
      def term = e.key
      def count = collections ? e.byCollection.buckets.size() : e.doc_count
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

  private Map parseResponse(Response response) {
    Map result = [statusCode: response?.getStatusLine()?.getStatusCode() ?: 500]
    try {
      if (response?.getEntity()) {
        result += new JsonSlurper().parse(response?.getEntity()?.getContent()) as Map
      }
    }
    catch (e) {
      log.warn("Failed to parse elasticsearch response as json", e)
    }
    return result
  }
}
