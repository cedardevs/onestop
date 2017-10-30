package org.cedar.onestop.api.metadata.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Response
import org.elasticsearch.client.ResponseException
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Slf4j
@Service
class ElasticsearchService {

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.name}')
  String STAGING_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.name}')
  String SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}')
  String PREFIX

  private RestClient restClient

  @Autowired
  ElasticsearchService(RestClient restClient) {
    this.restClient = restClient
  }

  public void ensureIndices() {
    ensureStagingIndex()
    ensureSearchIndex()
  }

  public void ensureStagingIndex() {
    ensureIndex(STAGING_INDEX)
  }

  public void ensureSearchIndex() {
    ensureIndex(SEARCH_INDEX)
  }

  private void ensureIndex(String index) {
    def indexExists = checkAliasExists(index)
    if (!indexExists) {
      def realName = create(index)
      String endPoint = "/${realName}/_alias/${index}"
      performRequest('PUT', endPoint)
    }
  }

  public String create(String baseName) {
    String indexName = "${baseName}-${System.currentTimeMillis()}"
    def cl = Thread.currentThread().contextClassLoader
    def jsonFilename = baseName.replaceFirst("^${PREFIX}", '') + 'Index.json'
    def indexJson = cl.getResourceAsStream(jsonFilename).text
    performRequest('PUT', indexName, indexJson)

    log.debug "Created new index [${indexName}]"
    return indexName
  }

  public void refresh(String... indices) {
    String endpoint = "/${indices.join(',')}/_refresh"
    performRequest('POST', endpoint)
  }

  public void dropStagingIndex() {
    drop(STAGING_INDEX)
  }

  public void dropSearchIndex() {
    drop(SEARCH_INDEX)
  }

  public void drop(String indexName) {
    try {
      performRequest('DELETE', indexName)
      log.debug "Dropped index [${indexName}]"
    } catch (e) {
      log.warn "Failed to drop index [${indexName}] because it was not found"
    }
  }

  public void moveAliasToIndex(String alias, String index, Boolean dropOldIndices = false) {
    def oldIndices = performRequest('GET', "_alias/$alias").keySet()*.toString()
    oldIndices = oldIndices.findAll({ it.startsWith(alias) })
    def actions = oldIndices.collect { [remove: [index: it, alias: alias]] }
    actions << [add: [index: index, alias: alias]]
    performRequest('POST', '_aliases', [actions: actions])

    if (dropOldIndices) {
      oldIndices.each { drop(it) }
    }
  }

  private Boolean checkAliasExists(String name) {
    def status = restClient.performRequest('HEAD', name).statusLine.statusCode
    return status == 200
  }

  public Map performRequest(String method, String endpoint, Map requestBody) {
    performRequest(method, endpoint, JsonOutput.toJson(requestBody))
  }

  public Map performRequest(String method, String endpoint, String requestBody = null) {
    try {
      log.debug("Performing elasticsearch request: ${method} ${endpoint} ${requestBody}")
      def response = requestBody ?
          restClient.performRequest(method, endpoint, Collections.EMPTY_MAP, new NStringEntity(requestBody, ContentType.APPLICATION_JSON)) :
          restClient.performRequest(method, endpoint)
      log.debug("Got response: $response")
      return parseResponse(response)
    }
    catch (ResponseException e) {
      def response = parseResponse(e.response)
      log.error("Elasticsearch request failed: $response")
      return response
    }
  }

  private static Map parseResponse(Response response) {
    Map result = [
        request   : response.requestLine,
        statusCode: response.statusLine.statusCode
    ]
    if (response.entity) {
      result += new JsonSlurper().parse(response.entity.content) as Map
    }
    return result
  }

}
