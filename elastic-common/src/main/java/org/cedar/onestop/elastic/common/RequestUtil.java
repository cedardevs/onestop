package org.cedar.onestop.elastic.common;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RequestUtil {
  private static final Logger log = LoggerFactory.getLogger(RequestUtil.class);

  public static Response deleteAllIndices(RestClient restClient) throws IOException {
    log.debug("Deleting all indices...");
    Request deleteRequest = new Request("DELETE", "_all");
    Response deleteResponse = restClient.performRequest(deleteRequest);
    log.debug("Delete all indices response: " + deleteResponse);
    return deleteResponse;
  }

  public static Response refreshAllIndices(RestClient restClient) throws IOException {
    // The refresh API allows to explicitly refresh one or more index,
    // making all operations performed since the last refresh available for search.
    log.debug("Refreshing all indices...");
    Request refreshRequest = new Request("POST", "_refresh");
    Response refreshResponse = restClient.performRequest(refreshRequest);
    log.debug("Refresh all indices response: " + refreshResponse);
    return refreshResponse;
  }

  public static Response bulk(String alias, String bulkData, RestClient restClient) throws IOException {
    log.debug("Performing bulk request on alias `" + alias + "`");
    log.trace("bulkData = " + bulkData);
    HttpEntity entity = new NStringEntity(bulkData, ContentType.APPLICATION_JSON);
    Request bulkRequest = new Request("POST", "_bulk");
    bulkRequest.setEntity(entity);
    log.debug("Bulk request: " + bulkRequest);
    Response bulkResponse = restClient.performRequest(bulkRequest);
    log.trace("Bulk response: " + bulkResponse);
    return bulkResponse;
  }

  public static Response resetIndices(String alias, String jsonMapping, RestClient restClient) throws IOException {
    log.debug("Resetting alias `" + alias + "` and corresponding indices with JSON mapping.");
    HttpEntity httpEntity = new NStringEntity(jsonMapping, ContentType.APPLICATION_JSON);
    // the `include_type_name=false` param is allowing ES6/ES7 cross compatibility:
    // ES7 defaults to false, but ES6 defaults to true, so we have to explicitly set it
    Request refreshRequest = new Request("PUT", alias + "?include_type_name=false");
    refreshRequest.setEntity(httpEntity);
    log.debug("Reset request: " + refreshRequest);
    Response refreshResponse = restClient.performRequest(refreshRequest);
    log.trace("Reset response: " + refreshResponse);
    return refreshResponse;
  }

  public static Response resetSearchCollectionsIndices(ElasticsearchConfig esConfig, RestClient restClient) throws IOException {
    String alias = esConfig.COLLECTION_SEARCH_INDEX_ALIAS;
    String jsonMapping = esConfig.jsonMapping(alias);
    return resetIndices(alias, jsonMapping, restClient);
  }

  public static Response resetSearchGranulesIndices(ElasticsearchConfig esConfig, RestClient restClient) throws IOException {
    String alias = esConfig.GRANULE_SEARCH_INDEX_ALIAS;
    String jsonMapping = esConfig.jsonMapping(alias);
    return resetIndices(alias, jsonMapping, restClient);
  }

  public static Response resetSearchFlattenedGranulesIndices(ElasticsearchConfig esConfig, RestClient restClient) throws IOException {
    String alias = esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS;
    String jsonMapping = esConfig.jsonMapping(alias);
    return resetIndices(alias, jsonMapping, restClient);
  }

  private static Response putMetadataRecord(String alias, String id, String metadata, RestClient restClient) throws IOException {
    log.debug("Putting new '" + alias + "' metadata record w/id = '" + id + "'");
    log.trace("metadata = " + metadata);
    String endpoint = "/" + alias + "/_doc/" + id;
    log.debug("endpoint = " + endpoint);
    HttpEntity httpEntity = new NStringEntity(metadata, ContentType.APPLICATION_JSON);
    Request request = new Request("PUT", endpoint);
    request.setEntity(httpEntity);
    log.debug("new metadata record request: " + request);
    Response response = restClient.performRequest(request);
    log.debug("new metadata record response: " + response);
    return response;
  }

  public static Response putSearchCollectionMetadataRecord(String id, String metadata, ElasticsearchConfig esConfig, RestClient restClient) throws IOException {
    String alias = esConfig.COLLECTION_SEARCH_INDEX_ALIAS;
    return putMetadataRecord(alias, id, metadata, restClient);
  }

  public static Response putSearchGranuleMetadataRecord(String id, String metadata, ElasticsearchConfig esConfig, RestClient restClient) throws IOException {
    String alias = esConfig.GRANULE_SEARCH_INDEX_ALIAS;
    return putMetadataRecord(alias, id, metadata, restClient);
  }

  public static Response putSearchFlattenedGranuleMetadataRecord(String id, String metadata, ElasticsearchConfig esConfig, RestClient restClient) throws IOException {
    String alias = esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS;
    return putMetadataRecord(alias, id, metadata, restClient);
  }
}
