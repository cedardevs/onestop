package org.cedar.onestop.api.admin.service

import groovy.json.JsonOutput
import org.apache.http.HttpEntity
import org.apache.http.RequestLine
import org.apache.http.StatusLine
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchTestVersion
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import spock.lang.Specification

class ElasticsearchServiceTests extends Specification {

  ElasticsearchConfig esConfig
  RestClient mockRestClient
  ElasticsearchService elasticsearchService

  def 'constructor ensures indicies and pipelines'(){
    given:
    esConfig = ElasticsearchTestVersion.esConfigLatest()
    mockRestClient = Mock(RestClient)

    when: 'the constructor is called'
    elasticsearchService = new ElasticsearchService(mockRestClient, esConfig)

    then: 'we create/update the pipelines'
    1 * mockRestClient.performRequest({
      Request request = it as Request
      request.method == 'PUT' && request.endpoint.endsWith(esConfig.COLLECTION_PIPELINE)
    }) >> buildMockResponse([dummy: "response"])
    1 * mockRestClient.performRequest({
      Request request = it as Request
      request.method == 'PUT' && request.endpoint.endsWith(esConfig.GRANULE_PIPELINE)
    }) >> buildMockResponse([dummy: "response"])

    and: 'can create index with prefix'
    String alias = elasticsearchService.esConfig.COLLECTION_SEARCH_INDEX_ALIAS

    when:
    elasticsearchService.createIndex(alias)

    then:
    1 * mockRestClient.performRequest({
      Request request = it as Request
      request.method == 'PUT' && request.endpoint.startsWith(alias)
    }) >> buildMockResponse([dummy: "response"])
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
}