package org.cedar.onestop.api.metadata.service

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.elasticsearch.Version
import org.elasticsearch.client.Response
import org.elasticsearch.client.ResponseException
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import static org.cedar.onestop.elastic.common.DocumentUtil.*

@Slf4j
@Service
class ElasticsearchService {

  private RestClient restClient
  Version version
  ElasticsearchConfig esConfig

  @Autowired
  ElasticsearchService(RestClient restClient, Version version, ElasticsearchConfig elasticsearchConfig) {
    this.version = version
    log.info("Elasticsearch found with version: ${this.version.toString()}" )
    boolean supported = version.onOrAfter(Version.V_5_6_0)
    if(!supported) {
      throw new RuntimeException("Admin API does not support version ${version.toString()} of Elasticsearch")
    }
    this.restClient = restClient
    this.esConfig = elasticsearchConfig
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
    if (!aliasExists) {
      def index = createIndex(alias)
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
    dropAlias(esConfig.COLLECTION_STAGING_INDEX_ALIAS)
    dropAlias(esConfig.GRANULE_STAGING_INDEX_ALIAS)
  }

  void dropSearchIndices() {
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
    def cl = Thread.currentThread().contextClassLoader
    def jsonFileName = pipelineName + 'Definition.json'
    def pipelineJson = cl.getResourceAsStream(jsonFileName).text
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
    def status = restClient.performRequest('HEAD', alias).statusLine.statusCode
    return status == 200
  }

  Map performRequest(String method, String endpoint, Map requestBody) {
    performRequest(method, endpoint, JsonOutput.toJson(requestBody))
  }

  Map performRequest(String method, String endpoint, String requestBody = null) {
    try {
      log.debug("Performing Elasticsearch request: ${method} ${endpoint} ${requestBody}")
      Response response = requestBody ?
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

  boolean deleteTask(String taskId) {
    def result = performRequest('DELETE', ".tasks/task/${taskId}")
    log.debug("Deleted task [ ${taskId} ]: ${result.found}")
    return result.found
  }

  Map checkTask(String taskId) {
    Map result = performRequest('GET', "_tasks/${taskId}")

    // TODO: Analyze this log output to determine what's going on with the result of checking taskIds
    JsonBuilder jsonResultBuilder = new JsonBuilder(result)
    log.info(":::AdminService::checkTask::result:\n" + jsonResultBuilder.toPrettyString())

    // TODO: 1) evaluate how we are distinguishing between a real request exception and a task that is simply complete
    // TODO: 2) does this
    // structure of result when task is gone (is caught as ResponseException try/catch of `performRequest`)
    /*{
      "error": {
        "root_cause": [
                {
                  "type": "resource_not_found_exception",
                  "reason": "task [IziaxPTxS_iEa-kkvW-rtQ:21463900] isn't running and hasn't stored its results"
                }
        ],
        "type": "resource_not_found_exception",
        "reason": "task [IziaxPTxS_iEa-kkvW-rtQ:21463900] isn't running and hasn't stored its results"
      },
        "status": 404
    }*/

    // structure of result when task is running (seems to be unlikely to catch this condition when tasks finish quickly)
    // but since this condition doesn't trigger the try/catch for a ResponseException, our attempt to retrieve
    // non-existent keys in the return of `checkTask` resulted in null pointer exceptions in the rare case that the
    // task was "caught in the act"
    /*{
      "completed": false,
      "task": {
        "node": "IziaxPTxS_iEa-kkvW-rtQ",
        "id": 21463900,
        "type": "netty",
        "action": "cluster:monitor/task/get",
        "description": "",
        "start_time_in_millis": 1556909931975,
        "running_time_in_nanos": 23208,
        "cancellable": false,
        "parent_task_id": "zZnt4tEQSI-4EsaFseceEA:19801870"
      }
    }*/
    def completed = result.completed
    Map task = result.task as Map
    Map status = task.status as Map
    return [
            completed: completed,
            totalDocs: status.total,
            updated: status.updated,
            created: status.created,
            /* took: completed ? result.response.took : null,      // REMOVE as task ID responses never have this
            failures: completed ? result.response.failures : [] */ // REMOVE as task ID responses never have this
    ]
  }
}
