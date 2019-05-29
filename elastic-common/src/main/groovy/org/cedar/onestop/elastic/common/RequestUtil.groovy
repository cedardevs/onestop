package org.cedar.onestop.elastic.common

import groovy.util.logging.Slf4j
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient

@Slf4j
class RequestUtil {

  static Response deleteAllIndices(RestClient restClient) {
    log.debug("Deleting all indices...")
    Request deleteRequest = new Request('DELETE', '_all')
    Response deleteResponse = restClient.performRequest(deleteRequest)
    log.debug("Delete all indices response: ${deleteResponse.toString()}")
    return deleteResponse
  }

  static Response refreshAllIndices(RestClient restClient) {
    // The refresh API allows to explicitly refresh one or more index,
    // making all operations performed since the last refresh available for search.
    log.debug("Refreshing all indices...")
    Request refreshRequest = new Request('POST', '_refresh')
    Response refreshResponse = restClient.performRequest(refreshRequest)
    log.debug("Refresh all indices response: ${refreshResponse.toString()}")
    return refreshResponse
  }

  static Response bulk(String alias, String bulkData, RestClient restClient) {
    log.debug("Performing bulk request on alias `${alias}`")
    log.trace("bulkData = ${bulkData}")
    HttpEntity entity = new NStringEntity(bulkData, ContentType.APPLICATION_JSON)
    Request bulkRequest = new Request('POST', '_bulk')
    bulkRequest.entity = entity
    log.debug("Bulk request: ${bulkRequest.toString()}")
    Response bulkResponse = restClient.performRequest(bulkRequest)
    println("Bulk response: ${bulkResponse.toString()}")
    return bulkResponse
  }

  static Response resetIndices(String alias, String jsonMapping, RestClient restClient) {
    log.debug("Resetting alias `${alias}` and corresponding indices with JSON mapping.")
    HttpEntity httpEntity = new NStringEntity(jsonMapping, ContentType.APPLICATION_JSON)
    Request refreshRequest = new Request('PUT', alias)
    refreshRequest.entity = httpEntity
    log.debug("Reset request: ${refreshRequest.toString()}")
    Response refreshResponse = restClient.performRequest(refreshRequest)
    println("Reset response: ${refreshResponse.toString()}")
    return refreshResponse
  }

  static Response resetSearchCollectionsIndices(ElasticsearchConfig esConfig, RestClient restClient) {
    String alias = esConfig.COLLECTION_SEARCH_INDEX_ALIAS
    String jsonMapping = esConfig.jsonMapping(alias)
    return resetIndices(alias, jsonMapping, restClient)
  }

  static Response resetStagedCollectionsIndices(ElasticsearchConfig esConfig, RestClient restClient) {
    String alias = esConfig.COLLECTION_STAGING_INDEX_ALIAS
    String jsonMapping = esConfig.jsonMapping(alias)
    return resetIndices(alias, jsonMapping, restClient)
  }

  static Response resetSearchGranulesIndices(ElasticsearchConfig esConfig, RestClient restClient) {
    String alias = esConfig.GRANULE_SEARCH_INDEX_ALIAS
    String jsonMapping = esConfig.jsonMapping(alias)
    return resetIndices(alias, jsonMapping, restClient)
  }

  static Response resetStagedGranulesIndies(ElasticsearchConfig esConfig, RestClient restClient) {
    String alias = esConfig.GRANULE_STAGING_INDEX_ALIAS
    String jsonMapping = esConfig.jsonMapping(alias)
    return resetIndices(alias, jsonMapping, restClient)
  }

  static Response resetSearchFlattenedGranulesIndices(ElasticsearchConfig esConfig, RestClient restClient) {
    String alias = esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS
    String jsonMapping = esConfig.jsonMapping(alias)
    return resetIndices(alias, jsonMapping, restClient)
  }

  private static Response putMetadataRecord(String alias, String type, String id, String metadata, RestClient restClient) {
    log.debug("Putting new '${alias}' metadata record w/id = '${id}'")
    log.trace("metadata = ${metadata}")
    String endpoint = "/${alias}/${type}/${id}"
    log.debug("endpoint = ${endpoint}")
    HttpEntity httpEntity = new NStringEntity(metadata, ContentType.APPLICATION_JSON)
    Request request = new Request('PUT', endpoint)
    request.entity = httpEntity
    log.debug("new metadata record request: ${request.toString()}")
    Response response = restClient.performRequest(request)
    log.debug("new metadata record response: ${response.toString()}")
    return response
  }

  static Response putSearchCollectionMetadataRecord(String id, String metadata, ElasticsearchConfig esConfig, RestClient restClient) {
    String alias = esConfig.COLLECTION_SEARCH_INDEX_ALIAS
    String type = esConfig.TYPE
    return putMetadataRecord(alias, type, id, metadata, restClient)
  }

  static Response putStagedCollectionMetadataRecord(String id, String metadata, ElasticsearchConfig esConfig, RestClient restClient) {
    String alias = esConfig.COLLECTION_STAGING_INDEX_ALIAS
    String type = esConfig.TYPE
    return putMetadataRecord(alias, type, id, metadata, restClient)
  }

  static Response putSearchGranuleMetadataRecord(String id, String metadata, ElasticsearchConfig esConfig, RestClient restClient) {
    String alias = esConfig.GRANULE_SEARCH_INDEX_ALIAS
    String type = esConfig.TYPE
    return putMetadataRecord(alias, type, id, metadata, restClient)
  }

  static Response putStagedGranulenMetadataRecord(String id, String metadata, ElasticsearchConfig esConfig, RestClient restClient) {
    String alias = esConfig.GRANULE_STAGEING_INDEX_ALIAS
    String type = esConfig.TYPE
    return putMetadataRecord(alias, type, id, metadata, restClient)
  }

  static Response putSearchFlattenedGranuleMetadataRecord(String id, String metadata, ElasticsearchConfig esConfig, RestClient restClient) {
    String alias = esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS
    String type = esConfig.TYPE
    return putMetadataRecord(alias, type, id, metadata, restClient)
  }
}
