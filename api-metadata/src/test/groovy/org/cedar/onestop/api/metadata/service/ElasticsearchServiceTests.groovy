package org.cedar.onestop.api.metadata.service

import groovy.json.JsonOutput
import org.apache.http.HttpEntity
import org.apache.http.RequestLine
import org.apache.http.StatusLine
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.elasticsearch.Version
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import spock.lang.Specification

class ElasticsearchServiceTests extends Specification {

  static TEST_PREFIX = 'prefix-'
  static TEST_COLLECTION_SEARCH_INDEX_ALIAS = 'search_collection'

  Version testVersion = Version.V_6_1_2

  ElasticsearchConfig esConfig = new ElasticsearchConfig(
      TEST_COLLECTION_SEARCH_INDEX_ALIAS,
      'staging_collection',
      'search_granule',
      'staging_granule',
      'search_flattened_granule',
      'sitemap',
      TEST_PREFIX,
      'collection_pipeline',
      'granule_pipeline',
      10,
      null,
      2,
      5,
      testVersion
  )
  RestClient mockRestClient = Mock(RestClient)
  ElasticsearchService elasticsearchService = new ElasticsearchService(mockRestClient, testVersion, esConfig)


  def setup() {
    elasticsearchService.esConfig.COLLECTION_SEARCH_INDEX_ALIAS = TEST_COLLECTION_SEARCH_INDEX_ALIAS
    elasticsearchService.esConfig.PREFIX = TEST_PREFIX
  }

  def 'can create index with prefix'() {
    when:
    elasticsearchService.createIndex(TEST_COLLECTION_SEARCH_INDEX_ALIAS)

    then:
    1 * mockRestClient.performRequest({
      Request request = it as Request
      request.method == 'PUT' && request.endpoint.startsWith(TEST_COLLECTION_SEARCH_INDEX_ALIAS)
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

  private static buildJsonEntity(String json) {
    new NStringEntity(json, ContentType.APPLICATION_JSON)
  }

}