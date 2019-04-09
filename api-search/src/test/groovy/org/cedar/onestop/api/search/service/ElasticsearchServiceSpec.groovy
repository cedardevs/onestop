package org.cedar.onestop.api.search.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.http.StatusLine
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ElasticsearchServiceSpec extends Specification {

  def mockRestClient = Mock(RestClient)
  def searchConfig = new SearchConfig()
  def searchRequestParserService = new SearchRequestParserService(searchConfig)
  def elasticsearchService = new ElasticsearchService(searchRequestParserService, mockRestClient)

  def 'executes a search'() {
    def testIndex = 'test_index'
    def searchRequest = [
        queries: [[type: 'queryText', value: 'test']],
        filters: [[type: 'year', beginYear: 1999]],
        page   : [max: 42, offset: 24]
    ]

    def mockResponse = buildMockElasticResponse(200, [
        hits: [
            total: 1,
            hits: [
                [
                    _id       : 'ABC',
                    _index    : testIndex,
                    attributes: [
                        title: 'THIS IS A TEST'
                    ]
                ]
            ]
        ],
        took: 1234,
    ])

    when:
    def result = elasticsearchService.searchFromRequest(searchRequest, testIndex)

    then:
    1 * mockRestClient.performRequest('GET', testIndex + '/_search', [:], { NStringEntity it ->
      def searchContent = new JsonSlurper().parse(it.content)
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
    def mockResponse = Mock(Response)
    mockResponse.getStatusLine() >> buildMockStatusLine(status)
    mockResponse.getEntity() >> new NStringEntity(JsonOutput.toJson(body))
    return mockResponse
  }

  private StatusLine buildMockStatusLine(int status) {
    def result = Mock(StatusLine)
    result.getStatusCode() >> status
    return result
  }

}
