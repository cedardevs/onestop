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
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Slf4j
@Service
class ElasticsearchService {

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.collection.name}')
  private String COLLECTION_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.granule.name}')
  private String GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.flattenedGranule.name}')
  private String FLATTENED_GRANULE_SEARCH_INDEX

  private SearchRequestParserService searchRequestParserService

  private RestClient restClient

  @Autowired
  ElasticsearchService(SearchRequestParserService searchRequestParserService, RestClient restClient) {
    this.searchRequestParserService = searchRequestParserService
    this.restClient = restClient
  }

  Map searchFlattenedGranules(Map searchParams) {
    return search(searchParams, FLATTENED_GRANULE_SEARCH_INDEX)
  }

  Map searchGranules(Map searchParams) {
    return search(searchParams, GRANULE_SEARCH_INDEX)
  }

  Map searchCollections(Map searchParams) {
    return search(searchParams, COLLECTION_SEARCH_INDEX)
  }

  Map search(Map searchParams, String index) {
    def response = queryElasticsearch(searchParams, index)
    return response
  }

  Map getCollectionById(String id) {
    String collectionEndpoint = "/$COLLECTION_SEARCH_INDEX/_all/${id}"
    def response = parseResponse(restClient.performRequest("GET", collectionEndpoint))

    // get the total number of granules for this collection id
    String granuleEndpoint = "/$GRANULE_SEARCH_INDEX/_search"
    HttpEntity granuleRequest = new NStringEntity(JsonOutput.toJson([
        query: [
            term: [
                internalParentIdentifier: "${id}"
            ]
        ],
        size : 0
    ]), ContentType.APPLICATION_JSON)
    def granuleResponse = restClient.performRequest("GET", granuleEndpoint, Collections.EMPTY_MAP, granuleRequest)
    def totalGranulesForCollection = parseResponse(granuleResponse).hits.total

    if (response.found) {
      return [
          data: [[
                     id        : response._id,
                     type      : response._type,
                     attributes: response._source
                 ]],
          meta: [
              totalGranules: totalGranulesForCollection
          ]
      ]
    }
    else {
      return [
          status: HttpStatus.NOT_FOUND.value(),
          title : 'No such document',
          detail: "Collection with Elasticsearch ID [ ${id} ] does not exist."
      ]
    }
  }

  Map getGranuleById(String id) {
    String granuleEndpoint = "/$GRANULE_SEARCH_INDEX/_all/${id}"
    def response = parseResponse(restClient.performRequest("GET", granuleEndpoint))
    if (response.found) {
      return [
          data: [[
                     id        : response._id,
                     type      : response._type,
                     attributes: response._source
                 ]]
      ]
    }
    else {
      return [
          status: HttpStatus.NOT_FOUND.value(),
          title : 'No such document',
          detail: "Granule with Elasticsearch ID [ ${id} ] does not exist."
      ]
    }
  }

  Map totalCounts() {
    String collectionEndpoint = "/$COLLECTION_SEARCH_INDEX/_search"
    HttpEntity collectionRequest = new NStringEntity(JsonOutput.toJson([
        query: [
            match_all: [:]
        ],
        size : 0
    ]), ContentType.APPLICATION_JSON)
    def collectionResponse = restClient.performRequest("GET", collectionEndpoint, Collections.EMPTY_MAP, collectionRequest)

    String granuleEndpoint = "/$GRANULE_SEARCH_INDEX/_search"
    HttpEntity granuleRequest = new NStringEntity(JsonOutput.toJson([
        query: [
            bool: [
                must: [
                    script: [
                        script: [
                            inline: "String uid = doc['_uid'].value; int hashIndex = uid.indexOf('#'); String id = uid.substring(hashIndex + 1); doc['internalParentIdentifier'].value != id",
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

  private Map queryElasticsearch(Map params, String index) {
    // TODO: does this parse step need to change based on new different endpoints?
    def query = searchRequestParserService.parseSearchQuery(params)
    def getFacets = params.facets as boolean
    def pageParams = params.page as Map

    def requestBody = addAggregations(query, getFacets)

    String searchEndpoint = "${index}/_search"

    requestBody.size = pageParams?.max ?: 10
    requestBody.from = pageParams?.offset ?: 0
    requestBody = pruneEmptyElements(requestBody)

    def searchRequest = new NStringEntity(JsonOutput.toJson(requestBody), ContentType.APPLICATION_JSON)
    def searchResponse = parseResponse(restClient.performRequest("GET", searchEndpoint, Collections.EMPTY_MAP, searchRequest))

    def result = [
        data: searchResponse.hits.hits.collect {
          [id: it._id, type: it._type, attributes: it._source]
        },
        meta: [
            took : searchResponse.took,
            total: searchResponse.hits.total
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
      aggregations.putAll(searchRequestParserService.createGCMDAggregations())
    }

    def requestBody = [
        query       : query,
        aggregations: aggregations
    ]
    return requestBody
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

  private Map cleanAggregation(List<String> topLevelKeywords, List<Map> originalAgg) {
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

  private Map pruneEmptyElements(Map requestBody) {
    def prunedRequest = requestBody.collectEntries { k, v -> [k, v instanceof Map ? pruneEmptyElements(v) : v]}.findAll { k, v -> v }
    return prunedRequest
  }
}
