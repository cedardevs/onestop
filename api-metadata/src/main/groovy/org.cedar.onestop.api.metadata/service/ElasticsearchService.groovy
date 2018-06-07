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

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.collection.name}')
  String COLLECTION_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.collection.name}')
  String COLLECTION_STAGING_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.granule.name}')
  String GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.granule.name}')
  String GRANULE_STAGING_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.flattened-granule.name}')
  private String FLAT_GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}')
  String PREFIX

  @Value('${elasticsearch.index.search.collection.pipeline-name}')
  private String COLLECTION_PIPELINE

  @Value('${elasticsearch.index.search.granule.pipeline-name}')
  private String GRANULE_PIPELINE


  private RestClient restClient

  @Autowired
  ElasticsearchService(RestClient restClient) {
    this.restClient = restClient
  }

  public void ensureIndices() {
    ensureStagingIndices()
    ensureSearchIndices()
  }

  public void ensureStagingIndices() {
    ensureIndex(COLLECTION_STAGING_INDEX)
    ensureIndex(GRANULE_STAGING_INDEX)
  }

  public void ensureSearchIndices() {
    ensureIndex(COLLECTION_SEARCH_INDEX)
    ensureIndex(GRANULE_SEARCH_INDEX)
    ensureIndex(FLAT_GRANULE_SEARCH_INDEX)
  }

  public void ensurePipelines() {
    ensurePipeline(COLLECTION_PIPELINE)
    ensurePipeline(GRANULE_PIPELINE)
  }

  private void ensureIndex(String index) {
    def indexExists = checkAliasExists(index)
    if (!indexExists) {
      def realName = create(index)
      String endPoint = "/${realName}/_alias/${index}"
      performRequest('PUT', endPoint)
    }
  }

  private void ensurePipeline(String pipelineName) {
    def pipelineCheck = performRequest('GET', "_ingest/pipeline/${pipelineName}?filter_path=*.version")
    if(!pipelineCheck[pipelineName]) { // Request is empty response if pipeline doesn't exist
      putPipeline(pipelineName)
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

  public void refreshAllIndices() {
    performRequest('POST', "${COLLECTION_STAGING_INDEX},${GRANULE_STAGING_INDEX},${COLLECTION_SEARCH_INDEX},${GRANULE_SEARCH_INDEX},${FLAT_GRANULE_SEARCH_INDEX}/_refresh")
  }

  public void dropStagingIndices() {
    drop(COLLECTION_STAGING_INDEX)
    drop(GRANULE_STAGING_INDEX)
  }

  public void dropSearchIndices() {
    drop(COLLECTION_SEARCH_INDEX)
    drop(GRANULE_SEARCH_INDEX)
    drop(FLAT_GRANULE_SEARCH_INDEX)
  }

  public void drop(String indexName) {
    try {
      performRequest('DELETE', indexName)
      log.debug "Dropped index [${indexName}]"
    } catch (e) {
      log.warn "Failed to drop index [${indexName}] because it was not found"
    }
  }

  public void putPipeline(String pipelineName) {
    def cl = Thread.currentThread().contextClassLoader
    def jsonFileName = pipelineName + 'Definition.json'
    def pipelineJson = cl.getResourceAsStream(jsonFileName).text
    String pipelineEndpoint = "_ingest/pipeline/${pipelineName}"
    performRequest('PUT', pipelineEndpoint, pipelineJson)

    log.debug("Put pipeline [${pipelineName}]")
  }

  public void moveAliasToIndex(String alias, String index, Boolean dropOldIndices = false) {
    def oldIndices = performRequest('GET', "_alias/${alias}").keySet()*.toString()
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
      log.debug("Got response: ${response}")
      return parseResponse(response)
    }
    catch (ResponseException e) {
      def response = parseResponse(e.response)
      log.error("Elasticsearch request failed: ${response}")
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
