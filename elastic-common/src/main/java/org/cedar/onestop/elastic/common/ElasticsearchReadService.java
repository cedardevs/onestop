package org.cedar.onestop.elastic.common;

import org.apache.http.HttpStatus;
import org.cedar.onestop.data.util.JsonUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// FIXME Replace generic Map response with JsonApiResponse
public class ElasticsearchReadService extends ElasticsearchService {

  public ElasticsearchReadService(RestHighLevelClient restHighLevelClient, ElasticsearchConfig esConfig) {
    super(restHighLevelClient, esConfig);
  }

  ////////////////////////////////////////////////////////////
  //                      Get Counts                        //
  ////////////////////////////////////////////////////////////

  /**
   *
   * @param indexAlias
   * @return
   */
  public Map<String, Object> getTotalCountInIndex(String indexAlias) {
    String endpoint = "/" + indexAlias + "/_search";
    Map<String, Object> query = new HashMap<>();
    query.put("match_all", new HashMap<>());

    Request totalCountsRequest = assembleTotalCountRequest(endpoint, query);
    Map<String, Object> marshalledResponse = performRequest(totalCountsRequest);

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("count", extractHitsValue(marshalledResponse));
    attributes.put("exactCount", isCountExact(marshalledResponse));

    Map<String, Object> dataObject = new HashMap<>();
    dataObject.put("type", "count");
    dataObject.put("id", config.typeFromAlias(indexAlias));
    dataObject.put("count", extractHitsValue(marshalledResponse)); // FIXME -- Remove this in OSIMv3.1 release
    dataObject.put("attributes", attributes);

    // FIXME -- deprecation warning to be removed in OSIMv3.1 release
    Map<String, Object> meta = new HashMap<>();
    meta.put("DEPRECATION_WARNING", "The high-level 'count' field is deprecated and will be removed in OSIM v3.1.0. The 'count' field will only be found in 'attributes'.");

    Map<String, Object> resultMap = new HashMap<>();
    resultMap.put("data", List.of(dataObject));
    resultMap.put("meta", meta);

    return resultMap;
    // FIXME errors?
  }

  /**
   *
   * @param indexAlias
   * @param termField
   * @param termValue
   * @return
   */
  public Map<String, Object> getTotalCountInIndexByTerm(String indexAlias, String termField, String termValue) {
    String endpoint = "/" + indexAlias + "/_search";
    Map<String, Object> query = new HashMap<>();
    Map<String, Object> term = new HashMap<>();
    term.put(termField, termValue);
    query.put("term", term);

    Request totalCountsRequest = assembleTotalCountRequest(endpoint, query);
    Map<String, Object> marshalledResponse = performRequest(totalCountsRequest);

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("count", extractHitsValue(marshalledResponse));
    attributes.put("exactCount", isCountExact(marshalledResponse));
    attributes.put("termField", termField);
    attributes.put("termValue", termValue);

    Map<String, Object> dataObject = new HashMap<>();
    dataObject.put("type", "countByTerm");
    dataObject.put("id", config.typeFromAlias(indexAlias));
    dataObject.put("attributes", attributes);

    Map<String, Object> resultMap = new HashMap<>();
    resultMap.put("data", List.of(dataObject));

    return resultMap;
    // FIXME errors?
  }

               ///////////////////////////////
               //       Count Helpers       //
               ///////////////////////////////

  /**
   *
   * @param marshalledResponse
   * @return
   */
  private int extractHitsValue(Map<String, Object> marshalledResponse) {
    var hits = (Map) marshalledResponse.get("hits");
    var totalHits = (Map) hits.get("total");
    return (Integer) totalHits.get("value");
  }

  /**
   *
   * @param marshalledResponse
   * @return
   */
  private boolean isCountExact(Map<String, Object> marshalledResponse) {
    var hits = (Map) marshalledResponse.get("hits");
    var totalHits = (Map) hits.get("total");
    return totalHits.get("relation").toString().equals("eq");
  }

  /**
   *
   * @param endpoint
   * @param query
   * @return
   */
  private Request assembleTotalCountRequest(String endpoint, Object query) {
    var requestMap = new HashMap<String, Object>();
    requestMap.put("query", query);
    requestMap.put("track_total_hits", true);
    requestMap.put("size", 0);

    String requestBody = JsonUtils.toJson(requestMap);
    log.debug("Request body: " + requestBody);

    Request countRequest = new Request("GET", endpoint);
    countRequest.setJsonEntity(requestBody);

    return countRequest;
  }


  ////////////////////////////////////////////////////////////
  //                       Get By ID                        //
  ////////////////////////////////////////////////////////////

  /**
   *
   * @param alias
   * @param id
   * @return
   */
  public Map<String, Object> getById(String alias, String id)  {
    String endpoint = "/" + alias + "/_doc/" + id;
    log.debug("Get by ID against endpoint: " + endpoint);
    Request idRequest = new Request("GET", endpoint);
    Map<String, Object> marshalledResponse = performRequest(idRequest);
    String type = config.typeFromAlias(alias);

    Map<String, Object> response = new HashMap<>();
    if ((Boolean) marshalledResponse.get("found")) {
      var dataMap = new HashMap<>();
      dataMap.put("id", marshalledResponse.get("_id"));
      dataMap.put("type", config.typeFromAlias(alias));
      dataMap.put("attributes", marshalledResponse.get("_source"));
      response.put("data", List.of(dataMap));
    }
    else {
      var error = new HashMap<String, Object>();
      error.put("status", HttpStatus.SC_NOT_FOUND);
      error.put("title", "No such document");
      error.put("detail", "Record type [ " + type + " ] with ID [ " + id + " ] does not exist.");
      response.put("errors", List.of(error));
    }

    return response;
  }


  ////////////////////////////////////////////////////////////
  //                    Get Mappings                        //
  ////////////////////////////////////////////////////////////

  /**
   *
   * @param alias
   * @return
   */
  public Map<String, Object> getIndexMapping(String alias) {
    String endpoint = "/" + alias + "/_mapping";
    log.debug("GET mapping for [ " + alias + " ]");

    var request = new Request("GET", endpoint);
    var marshalledResponse = performRequest(request);

    var response = new HashMap<String, Object>();

    if(!marshalledResponse.containsKey("error")) {
      // Actual timestamped name is used in the response, not the alias, but need to drop the "statusCode"
      List<String> keys = marshalledResponse.keySet().stream()
          .filter(key -> !key.equals("statusCode"))
          .collect(Collectors.toList());
      String indexName = keys.get(0); // Only one key left in response

      var data = new HashMap<String, Object>();
      data.put("id", alias);
      data.put("type", "index-map");
      data.put("attributes", marshalledResponse.get(indexName));

      response.put("data", List.of(data));
    }
    else {
      // FIXME granularity in error response -- could be a variety of errors here, but assuming always "not found" right now
      var error = new HashMap<String, Object>();
      error.put("status", HttpStatus.SC_NOT_FOUND);
      error.put("title", "Index not found");
      error.put("detail", "Index with alias [ " + alias + " ] does not exist.");

      // FIXME -- put ES-specific info in a meta block?

      response.put("errors", List.of(error));
    }

    return response;
  }


  ////////////////////////////////////////////////////////////
  //                      Searches                          //
  ////////////////////////////////////////////////////////////

  /**
   *
   * @param alias
   * @param requestBodyJson
   * @return
   */
  public Map<String, Object> getSearchResults(String alias, String requestBodyJson) {
    var response = new HashMap<String, Object>();

    if(requestBodyJson == null) {
      var error = new HashMap<String, Object>();
      error.put("status", HttpStatus.SC_BAD_REQUEST);
      error.put("title", "Missing expected Elasticsearch request body");
      error.put("detail", "Search request body for Elasticsearch request is empty.");

      response.put("errors", List.of(error));
    }

    else {
      String endpoint = alias + "/_search";
      var marshalledResponse = performRequest("GET", endpoint, requestBodyJson);
      return constructSearchResponse(marshalledResponse);
    }

    return response;

  }

  /**
   *
   * @param alias
   * @param requestBodyMap
   * @return
   */
  public Map<String, Object> getSearchResults(String alias, Map<String, Object> requestBodyMap) {
    var requestBodyJson = JsonUtils.toJson(requestBodyMap);

    if(requestBodyJson == null) {
      var response = new HashMap<String, Object>();
      var error = new HashMap<String, Object>();
      error.put("status", HttpStatus.SC_BAD_REQUEST);
      error.put("title", "Could not parse Elasticsearch request body");
      error.put("detail", "Search request body for Elasticsearch request did not parse as valid JSON.");

      response.put("errors", List.of(error));
      return response;
    }

    return getSearchResults(alias, requestBodyJson);
  }

  /**
   *
   * @param marshalledResponse
   * @return
   */
  public Map<String, Object> constructSearchResponse(Map<String, Object> marshalledResponse) {
    // FIXME what happens in event of error response from ES?

    var dataList = new ArrayList<Map<String, Object>>();
    var docs = getDocuments(marshalledResponse);
    docs.forEach( m -> {
      dataList.add(constructDataElement(m));
    });

    var meta = new HashMap<String, Object>();
    meta.put("took", marshalledResponse.get("took"));
    meta.put("total", extractHitsValue(marshalledResponse));
    meta.put("exactCount", isCountExact(marshalledResponse));

    var searchResponse = new HashMap<String, Object>();
    searchResponse.put("data", dataList);
    searchResponse.put("meta", meta);

    return searchResponse;
  }

                ///////////////////////////////
                //       Search Helpers      //
                ///////////////////////////////

  private List<Map<String, Object>> getDocuments(Map<String, Object> marshalledResponse) {
    var hits = (Map) marshalledResponse.get("hits");
    var documents = (List) hits.get("hits");
    return documents;
  }

  private Map<String, Object> constructDataElement(Map<String, Object> elasticDocument) {
    var dataElement = new HashMap<String, Object>();
    dataElement.put("id", elasticDocument.get("_id"));
    dataElement.put("type", config.typeFromIndex((String) elasticDocument.get("_index")));
    dataElement.put("attributes", elasticDocument.get("_source"));

    return dataElement;
  }
}
