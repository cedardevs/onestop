package org.cedar.onestop.elastic.common;

import org.apache.http.HttpStatus;
import org.cedar.onestop.data.util.JsonUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cedar.onestop.elastic.common.DocumentUtil.*;
import static org.cedar.onestop.elastic.common.DocumentUtil.getSource;

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
    String query = "{\"match_all\": []}";

    Request totalCountsRequest = assembleTotalCountRequest(endpoint, query);
    Map<String, Object> marshalledResponse = performRequest(totalCountsRequest);

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("count", extractHitsValue(marshalledResponse));

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
    String query = "{\"term\": {\"" + termField + "\": \"" + termValue +"\"}}";

    Request totalCountsRequest = assembleTotalCountRequest(endpoint, query);
    Map<String, Object> marshalledResponse = performRequest(totalCountsRequest);

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("count", extractHitsValue(marshalledResponse));
    attributes.put("termField", termField);
    attributes.put("termValue", termValue);

    Map<String, Object> dataObject = new HashMap<>();
    dataObject.put("type", "countByTerm");
    dataObject.put("id", config.typeFromAlias(indexAlias));
    dataObject.put("attributes", attributes);

    Map<String, Object> resultMap = new HashMap<>();
    resultMap.put("data", List.of(dataObject));

    return resultMap;
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
  private boolean exactHitCount(Map<String, Object> marshalledResponse) {
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
  private Request assembleTotalCountRequest(String endpoint, String query) {
    var requestMap = new HashMap<String, Object>();
    requestMap.put("query", query);
    requestMap.put("track_total_hits", true);
    requestMap.put("size", 0);

    Request countRequest = new Request("GET", endpoint);
    countRequest.setJsonEntity(JsonUtils.toJson(requestMap));

    return countRequest;
  }


  ////////////////////////////////////////////////////////////
  //                       Get By ID                        //
  ////////////////////////////////////////////////////////////
  public Map<String, Object> getById(String alias, String id)  {
    String endpoint = "/" + alias + "/_doc/" + id;
    log.debug("Get by ID against endpoint: " + endpoint);
    Request idRequest = new Request("GET", endpoint);
    Map<String, Object> marshalledResponse = performRequest(idRequest);
    String type = config.typeFromAlias(alias);

    Map<String, Object> response = new HashMap<>();
    if ((Boolean) marshalledResponse.get("found")) {
      var dataMap = new HashMap<>();
      dataMap.put("id", getId(marshalledResponse));
      dataMap.put("type", config.typeFromAlias(alias));
      dataMap.put("attributes", getSource(marshalledResponse));
      response.put("data", List.of(dataMap));
    }
    else {
      response.put("status", HttpStatus.SC_NOT_FOUND);
      response.put("title", "No such document");
      response.put("detail", "Record type [ " + type + " ] with ID [ " + id + " ] does not exist.");
    }

    return response;
  }


  //////////////////
  // Get Mappings //
  //////////////////



  //////////////
  // Searches //
  //////////////

}
