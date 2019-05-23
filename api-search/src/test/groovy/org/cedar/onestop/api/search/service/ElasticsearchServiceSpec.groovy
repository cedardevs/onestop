package org.cedar.onestop.api.search.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.http.HttpEntity
import org.apache.http.StatusLine
import org.apache.http.nio.entity.NStringEntity
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.elasticsearch.Version
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ElasticsearchServiceSpec extends Specification {

  String TEST_INDEX = 'test_index'
  Version testVersion = Version.V_6_1_2

  ElasticsearchConfig esConfig = new ElasticsearchConfig(
      TEST_INDEX,
      'staging_collection',
      'search_granule',
      'staging_granule',
      'search_flattened_granule',
      'sitemap',
      null,
      'collection_pipeline',
      'granule_pipeline',
      10,
      null,
      2,
      5,
      testVersion
  )
  RestClient mockRestClient = Mock(RestClient)
  SearchConfig searchConfig = new SearchConfig()
  SearchRequestParserService searchRequestParserService = new SearchRequestParserService(searchConfig)
  ElasticsearchService elasticsearchService = new ElasticsearchService(searchRequestParserService, mockRestClient, testVersion, esConfig)


  def 'executes a search'() {
    Map searchRequest = [
        queries: [[type: 'queryText', value: 'test']],
        filters: [[type: 'year', beginYear: 1999]],
        page   : [max: 42, offset: 24]
    ]

    Response mockResponse = buildMockElasticResponse(200, [
        hits: [
            total: 1,
            hits: [
                [
                    _id       : 'ABC',
                    _index    : TEST_INDEX,
                    attributes: [
                        title: 'THIS IS A TEST'
                    ]
                ]
            ]
        ],
        took: 1234,
    ])


    when:
    def result = elasticsearchService.searchFromRequest(searchRequest, TEST_INDEX)

    then:
    1 * mockRestClient.performRequest({
      Request request = it as Request
      HttpEntity requestEntity = request.entity
      InputStream requestContent = requestEntity.content
      Map searchContent = new JsonSlurper().parse(requestContent) as Map
      assert request.method == 'GET'
      assert request.endpoint.startsWith(TEST_INDEX)
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
