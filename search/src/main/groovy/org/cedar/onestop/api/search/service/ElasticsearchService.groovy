package org.cedar.onestop.api.search.service

import com.sun.org.apache.regexp.internal.RE
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.RestClient
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

    String granuleEndpoint = ""
    HttpEntity granuleRequest = new NStringEntity(JsonOutput.toJson([]), ContentType.APPLICATION_JSON)
    def granuleResponse = restClient.performRequest("GET", granuleEndpoint, Collections.singletonMap("pretty", "true"), granuleRequest)

    // TODO

    return [
        data: [
            [
                type : "count",
                id   : "collection",
                count: collectionResponse.hits.totalHits
            ],
            [
                type : "count",
                id   : "granule",
                count: granuleResponse.hits.totalHits
            ]
        ]
    ]
  }

  private Map queryElasticsearch(Map params) {
    // TODO
  }
}
