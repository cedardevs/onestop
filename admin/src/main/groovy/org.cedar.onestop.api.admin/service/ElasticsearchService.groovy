package org.cedar.onestop.api.admin.service

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.elasticsearch.Version
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.ResponseException
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static org.cedar.onestop.elastic.common.DocumentUtil.parseAdminResponse

@Slf4j
@Service
class ElasticsearchService {

  private RestClient restClient
  Version version
  ElasticsearchConfig esConfig

  @Autowired
  ElasticsearchService(RestClient restClient, ElasticsearchConfig elasticsearchConfig) {
    this.version = elasticsearchConfig.version
    log.info("Elasticsearch found with version: ${this.version.toString()}" )
    boolean supported = version.onOrAfter(Version.V_5_6_0)
    if(!supported) {
      throw new RuntimeException("Admin API does not support version ${version.toString()} of Elasticsearch")
    }
    this.restClient = restClient
    this.esConfig = elasticsearchConfig
    putPipeline(esConfig.COLLECTION_PIPELINE)
    putPipeline(esConfig.GRANULE_PIPELINE)
  }

  void ensureIndices() {
    ensureStagingIndices()
    ensureSearchIndices()
  }

  void ensureStagingIndices() {
    ensureAliasWithIndex(esConfig.COLLECTION_STAGING_INDEX_ALIAS)
    ensureAliasWithIndex(esConfig.GRANULE_STAGING_INDEX_ALIAS)
  }

  void ensureSearchIndices() {
    ensureAliasWithIndex(esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
    ensureAliasWithIndex(esConfig.GRANULE_SEARCH_INDEX_ALIAS)
    ensureAliasWithIndex(esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
    if(esConfig.sitemapEnabled()) {
      ensureAliasWithIndex(esConfig.SITEMAP_INDEX_ALIAS)
    }
  }

  void ensurePipelines() {
    ensurePipeline(esConfig.COLLECTION_PIPELINE)
    ensurePipeline(esConfig.GRANULE_PIPELINE)
  }

  private void ensureAliasWithIndex(String alias) {
    def aliasExists = checkAliasExists(alias)
    if (aliasExists){
      String jsonIndexDef = esConfig.jsonMapping(alias)
      String endPoint = "/${alias}/_mapping/${esConfig.TYPE}"
      String jsonIndexMapping = JsonOutput.toJson((new JsonSlurper().parseText(jsonIndexDef)).mappings.doc)
      performRequest('PUT', endPoint, jsonIndexMapping)
    }else{
      def index = createIndex(alias)
      log.debug("Creating alias `${alias}` for index `${index}`")
      String endPoint = "/${index}/_alias/${alias}"
      performRequest('PUT', endPoint)
    }
  }

  private void ensurePipeline(String pipelineName) {
    def pipelineCheck = performRequest('GET', "_ingest/pipeline/${pipelineName}?filter_path=*.version")
    if(!pipelineCheck[pipelineName]) { // Request is empty response if pipeline doesn't exist
      putPipeline(pipelineName)
    }
  }

  String createIndex(String alias) {
    String index = "${alias}-${System.currentTimeMillis()}"
    String jsonMapping = esConfig.jsonMapping(alias)
    performRequest('PUT', index, jsonMapping)
    log.debug "Created new index [${index}]"
    return index
  }

  void refresh(String... indices) {
    String endpoint = "/${indices.join(',')}/_refresh"
    performRequest('POST', endpoint)
  }

  void refreshAllIndices() {
    def allIndices = [
      esConfig.COLLECTION_STAGING_INDEX_ALIAS,
      esConfig.GRANULE_STAGING_INDEX_ALIAS,
      esConfig.COLLECTION_SEARCH_INDEX_ALIAS,
      esConfig.GRANULE_SEARCH_INDEX_ALIAS,
      esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS
    ]
    performRequest('POST', "${allIndices.join(',')}/_refresh")
  }

  void dropStagingIndices() {
    log.debug("Dropping staging indices...")
    dropAlias(esConfig.COLLECTION_STAGING_INDEX_ALIAS)
    dropAlias(esConfig.GRANULE_STAGING_INDEX_ALIAS)
  }

  void dropSearchIndices() {
    log.debug("Dropping search indices...")
    dropAlias(esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
    dropAlias(esConfig.GRANULE_SEARCH_INDEX_ALIAS)
    dropAlias(esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
    if(esConfig.sitemapEnabled()) {
      dropAlias(esConfig.SITEMAP_INDEX_ALIAS)
    }
  }

  Set<String> getIndicesForAlias(String alias) {
    try {
      // find all indices for this alias
      def response = performRequest('GET',"_alias/${alias}")
      return response.keySet().findAll({ it.startsWith(alias) })
    } catch (e) {
      log.warn "Failed to retrieve indices for alias \'${alias}\' due to: ${e.message}"
      return []
    }
  }

  void dropAlias(String alias) {
    // In Elasticsearch, once all indices associated with an alias are deleted,
    // the alias is also removed. In ES6+, deleting an alias directly no longer
    // deletes the underlying indices implicitly, so this approach should work
    // for all versions.
    log.debug("Dropping indices for alias: ${alias}")
    getIndicesForAlias(alias).each { dropIndex(it) }
  }

  void dropIndex(String index) {
    try {
      performRequest('DELETE', index)
      log.debug "Dropped index [${index}]"
    } catch (e) {
      log.warn "Failed to drop index [${index}] because it was not found: ${e.message}"
    }
  }

  void putPipeline(String pipelineName) {
    String pipelineJson = esConfig.jsonPipeline(pipelineName)
    String pipelineEndpoint = "_ingest/pipeline/${pipelineName}"
    performRequest('PUT', pipelineEndpoint, pipelineJson)
    log.debug("Put pipeline [${pipelineName}]")
  }

  void moveAliasToIndex(String alias, String index, Boolean dropOldIndices = false) {
    def oldIndices = getIndicesForAlias(alias)
    if(!oldIndices.isEmpty()) {
      def actions = oldIndices.collect { [remove: [index: it, alias: alias]] }
      actions << [add: [index: index, alias: alias]]
      performRequest('POST', '_aliases', [actions: actions])
    }
    if (dropOldIndices) {
      oldIndices.each { dropIndex(it) }
    }
  }

  private Boolean checkAliasExists(String alias) {
    Request aliasExistsRequest = new Request('HEAD', alias)
    int status = restClient.performRequest(aliasExistsRequest).statusLine.statusCode
    Boolean exists = status == 200
    log.debug("Alias `${alias}` ${exists ? 'exists' : 'does NOT exist'}")
    return exists
  }

  Map performRequest(String method, String endpoint, Map requestBody) {
    performRequest(method, endpoint, JsonOutput.toJson(requestBody))
  }

  Map performRequest(String method, String endpoint, String requestBody = null) {
    String requestBodyOneLiner = requestBody ? requestBody.replace("\r\n", " ").replace("\n", " ") : null
    try {
      log.debug("Performing Elasticsearch request: ${method} ${endpoint} ${requestBodyOneLiner}")

      Response response
      if(requestBody) {
        Request requestWithBody = new Request(method, endpoint)
        requestWithBody.entity = new NStringEntity(requestBody, ContentType.APPLICATION_JSON)
        response = restClient.performRequest(requestWithBody)
      } else {
        Request requestWithoutBody = new Request(method, endpoint)
        response = restClient.performRequest(requestWithoutBody)
      }
      log.debug("Got response: ${response}")
      return parseAdminResponse(response)
    }
    catch (ResponseException e) {
      def response = parseAdminResponse(e.response)
      log.error("Elasticsearch request failed: ${response}")
      return response
    }
  }

  boolean deleteTask(String taskId) {
    def result = performRequest('DELETE', ".tasks/task/${taskId}")
    log.debug("Deleted task [ ${taskId} ]: ${result.found}")
    return result.found
  }

  Map checkTask(String taskId) {
    Map result = performRequest('GET', "_tasks/${taskId}")

    def completed = result.completed
    Map task = result.task as Map
    String taskNode = task.node as String
    int taskIdResponse = task.id as int
    Map status = task.status as Map

    // instead of printing out all possible responses, selectively output failure reasons to simplify debugging
    Map response = result.response as Map
    List failures = response ? response.failures as List : []
    int numFailures = failures ? failures.size() : 0
    if(numFailures > 0) {
      log.error("Task (node: ${taskNode}, id: ${taskIdResponse.toString()}) encountered ${numFailures} failure(s):")
      failures.eachWithIndex { Map failure, int nthFailure ->
        String failureIndex = failure.index as String
        String failureId = failure.id as String
        Map failureCause = failure.cause as Map
        String failureReason = failureCause.reason as String
        log.error("Failure ${nthFailure} occurred on index: '${failureIndex}' and id: '${failureId}'. Reason: ${failureReason}")
      }
    }

    return [
            completed: completed,
            totalDocs: status.total,
            updated: status.updated,
            created: status.created
    ]
  }
}
