package org.cedar.onestop.api.search.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.http.HttpEntity
import org.apache.http.StatusLine
import org.apache.http.nio.entity.NStringEntity
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchVersion
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
@Unroll
class ElasticsearchServiceSpec extends Specification {

  // FIXME failing here due to call to restHighLevelClient.getLowLevelClient(); how to mock this?
  RestHighLevelClient mockRestHighLevelClient = Mock(RestHighLevelClient)
  RestClient mockRestClient = Mock(RestClient)
  SearchConfig searchConfig = new SearchConfig()
  SearchRequestParserService searchRequestParserService = new SearchRequestParserService(searchConfig)
  ElasticsearchVersion esVersion = new ElasticsearchVersion("7.17.5")
  ElasticsearchConfig esConfig = new ElasticsearchConfig(
      esVersion,
      "ElasticsearchServiceSpec-",
      1,
      1,
      1,
      1,
      false
  )

  def "preserve page max 0 offset 0 into request" () {
    given:
    ElasticsearchService elasticsearchService = new ElasticsearchService(searchRequestParserService, mockRestHighLevelClient, esConfig)
    // post processing on the request was altering the results after addPagination
    when:
    def queryResult = elasticsearchService.buildRequestBody([page:[max: 0, offset:0]])

    then:
    queryResult.size == 0
    queryResult.from == 0

  }

//todo is this necessary?
  def "preserve sort request" () {
    given:
    ElasticsearchService elasticsearchService = new ElasticsearchService(searchRequestParserService, mockRestHighLevelClient, esConfig)
    Map params = [sort:[[stagedDate: "desc"]]]
    List resultingSort = params.sort
    // post processing on the request was altering the results after addPagination
    when:
    def queryResult = elasticsearchService.buildRequestBody(params)

    then:
    queryResult.sort == resultingSort
  }

//  def 'executes a search'() {
//    def testIndex = 'test_index'
//    def searchRequest = [
//        queries: [[type: 'queryText', value: 'test']],
//        filters: [[type: 'year', beginYear: 1999]],
//        page   : [max: 42, offset: 24]
//    ]
//    ElasticsearchService elasticsearchService = new ElasticsearchService(searchRequestParserService, mockRestHighLevelClient, esConfig)
//
//    Response mockResponse = buildMockElasticResponse(200, [
//          hits: [
//              total: [
//                  value: 1,
//                  relation: 'eq'
//              ],
//              hits: [
//                  [
//                      _id       : 'ABC',
//                      _index    : esConfig.COLLECTION_SEARCH_INDEX_ALIAS,
//                      attributes: [
//                          title: 'THIS IS A TEST'
//                      ]
//                  ]
//              ]
//          ],
//          took: 1234,
//      ])
//
//    when:
//    def result = elasticsearchService.searchFromRequest(searchRequest, esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
//
//    then:
//    1 * mockRestClient.performRequest({
//      Request request = it as Request
//      HttpEntity requestEntity = request.entity
//      InputStream requestContent = requestEntity.content
//      Map searchContent = new JsonSlurper().parse(requestContent) as Map
//      assert request.method == 'GET'
//      assert request.endpoint.startsWith(esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
//      assert request.endpoint.endsWith('_search')
//      assert searchContent.size == searchRequest.page.max
//      assert searchContent.from == searchRequest.page.offset
//      return true
//    }) >> mockResponse
//
//    and:
//    result instanceof Map
//    result.data.size() == 1
//    result.data[0].id == 'ABC'
//    result.meta.took == 1234
//
//  }

  def 'supports pagination parameters'() {
    expect:
    ElasticsearchService.addPagination([:], params) == expected

    where:
    params                | expected
    [:]                   | [size: 10, from: 0]
    [max: 42]             | [size: 42, from: 0]
    [offset: 24]          | [size: 10, from: 24]
    [max: 42, offset: 24] | [size: 42, from: 24]
    [max: 0, offset: 0]   | [size: 0, from: 0]
  }

  def 'pass through sort parameters'() {

    expect:
    ElasticsearchService.addSort([:], params).sort == expected

    where:
    params                 | expected
    []                     | [["_score" : "desc"], ["_doc": "desc"]]
    [["beginDate":"desc"]] | [["beginDate":"desc"]]
//    [["beginDate":"desc"]] | [["beginDate":"desc"], ["_doc": "desc"]]
  }

  private Response buildMockElasticResponse(int status, Map body) {
    Response mockResponse = Mock(Response)
    StatusLine mockStatusLine = Mock(StatusLine)
    mockResponse.statusLine >> mockStatusLine
    mockStatusLine.statusCode >> status
    mockResponse.entity >> new NStringEntity(JsonOutput.toJson(body))
    return mockResponse
  }

}
