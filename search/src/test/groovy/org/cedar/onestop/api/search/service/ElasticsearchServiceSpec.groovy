package org.cedar.onestop.api.search.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.http.HttpEntity
import org.apache.http.StatusLine
import org.apache.http.nio.entity.NStringEntity
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchTestVersion
import org.elasticsearch.Version
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
@Unroll
class ElasticsearchServiceSpec extends Specification {

  RestClient mockRestClient = Mock(RestClient)
  SearchConfig searchConfig = new SearchConfig()
  SearchRequestParserService searchRequestParserService = new SearchRequestParserService(searchConfig)
  Map<Version, ElasticsearchConfig> esVersionedConfigs = [:]

  def setup() {
    esVersionedConfigs = ElasticsearchTestVersion.configs()
  }

  def "preserve page max 0 offset 0 into request using ES version #dataPipe.version" () {
    given:
    Version version = dataPipe.version as Version
    ElasticsearchConfig esConfig = esVersionedConfigs[version]
    ElasticsearchService elasticsearchService = new ElasticsearchService(searchRequestParserService, mockRestClient, esConfig)
    // post processing on the request was altering the results after addPagination
    when:
    def queryResult = elasticsearchService.buildRequestBody([page:[max: 0, offset:0]])

    then:
    queryResult.size == 0
    queryResult.from == 0

    where:
    dataPipe << ElasticsearchTestVersion.versionedTestCases()
  }

  def 'executes a search using ES version #dataPipe.version'() {
    def testIndex = 'test_index'
    def searchRequest = [
        queries: [[type: 'queryText', value: 'test']],
        filters: [[type: 'year', beginYear: 1999]],
        page   : [max: 42, offset: 24]
    ]
    Version version = dataPipe.version as Version
    ElasticsearchConfig esConfig = esVersionedConfigs[version]
    ElasticsearchService elasticsearchService = new ElasticsearchService(searchRequestParserService, mockRestClient, esConfig)

    Response mockResponse = buildMockElasticResponse(200, [
        hits: [
            total: 1,
            hits: [
                [
                    _id       : 'ABC',
                    _index    : esConfig.COLLECTION_SEARCH_INDEX_ALIAS,
                    attributes: [
                        title: 'THIS IS A TEST'
                    ]
                ]
            ]
        ],
        took: 1234,
    ])

    when:
    def result = elasticsearchService.searchFromRequest(searchRequest, esConfig.COLLECTION_SEARCH_INDEX_ALIAS)

    then:
    1 * mockRestClient.performRequest({
      Request request = it as Request
      HttpEntity requestEntity = request.entity
      InputStream requestContent = requestEntity.content
      Map searchContent = new JsonSlurper().parse(requestContent) as Map
      assert request.method == 'GET'
      assert request.endpoint.startsWith(esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
      assert request.endpoint.endsWith('_search')
      assert searchContent.size == searchRequest.page.max
      assert searchContent.from == searchRequest.page.offset
      return true
    }) >> mockResponse

    and:
    result instanceof Map
    result.data.size() == 1
    result.data[0].id == 'ABC'
    result.meta.took == 1234

    where:
    dataPipe << ElasticsearchTestVersion.versionedTestCases()
  }

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


  private Response buildMockElasticResponse(int status, Map body) {
    Response mockResponse = Mock(Response)
    StatusLine mockStatusLine = Mock(StatusLine)
    mockResponse.statusLine >> mockStatusLine
    mockStatusLine.statusCode >> status
    mockResponse.entity >> new NStringEntity(JsonOutput.toJson(body))
    return mockResponse
  }

}
