package org.cedar.onestop.api.metadata.service

import groovy.json.JsonOutput
import org.apache.http.HttpEntity
import org.apache.http.RequestLine
import org.apache.http.StatusLine
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import spock.lang.Specification


class ElasticsearchServiceTests extends Specification {


  static TEST_PREFIX = 'prefix-'
  static TEST_COLLECTION_SEARCH = TEST_PREFIX + 'search_collection'

  def mockRestClient = Mock(RestClient)
  def elasticsearchService = new ElasticsearchService(mockRestClient)

  def setup() {
    elasticsearchService.COLLECTION_SEARCH_INDEX = TEST_COLLECTION_SEARCH
    elasticsearchService.PREFIX = TEST_PREFIX
  }

  def 'can create index with prefix'() {
    when:
    elasticsearchService.create(TEST_COLLECTION_SEARCH)

    then:
    1 * mockRestClient.performRequest('PUT', {it.startsWith(TEST_COLLECTION_SEARCH)}, *_) >> buildMockResponse([dummy: "response"])
    noExceptionThrown()
  }


  private buildMockResponse(Map data) {
    return Mock(Response).with { response ->
      response.requestLine >> Mock(RequestLine)
      response.statusLine >> Mock(StatusLine).with { statusLine ->
        statusLine.statusCode >> 200
        return statusLine
      }
      response.entity >> Mock(HttpEntity).with { entity ->
        entity.content >> new ByteArrayInputStream(JsonOutput.toJson(data).bytes)
      }
      return response
    }
  }

  private static buildJsonEntity(String json) {
    new NStringEntity(json, ContentType.APPLICATION_JSON)
  }

}
