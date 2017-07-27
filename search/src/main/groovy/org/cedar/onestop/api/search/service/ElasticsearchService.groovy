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
    def aggregations = []

    if (getFacets) {
      def gcmdKeywords = searchRequestParserService.createGCMDAggregations(getCollections)
      gcmdKeywords.each { k, v -> aggregations.add([(k): v]) }
    }

    if (getCollections) {
      aggregations.add([collections: searchRequestParserService.createCollectionsAggregation()])
    }

    def requestBody = [
        query       : query,
        aggregations: aggregations
    ]
    return requestBody
  }

  private Map getCollectionResults(Map requestBody, Map pageParams) {
    def result = []
    // TODO need to set size in first request to 0 here; size & offset in multiget
    // TODO
    // new NStringEntity(JsonOutput.toJson(requestBody), ContentType.APPLICATION_JSON)
    return result
  }

  private Map getGranuleResults(Map requestBody, Map pageParams) {
    def result = []
    // TODO set size/offset w/pageParams
    // new NStringEntity(JsonOutput.toJson(requestBody), ContentType.APPLICATION_JSON)
    return result
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
