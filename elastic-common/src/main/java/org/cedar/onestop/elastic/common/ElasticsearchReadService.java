package org.cedar.onestop.elastic.common;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.cedar.onestop.elastic.common.DocumentUtil.*;
import static org.cedar.onestop.elastic.common.DocumentUtil.getSource;

public class ElasticsearchReadService extends ElasticsearchService {

  public ElasticsearchReadService(RestHighLevelClient restClient, ElasticsearchConfig esConfig) {
    super(restClient, esConfig);
  }

  ////////////////
  // Get Counts //
  ////////////////
  public Map getTotalCounts(String alias) {
    String endpoint = "/" + alias + "/_search";
    var requestMap = new HashMap<>();
    requestMap.put("track_total_hits", true);
    requestMap.put("query", "{'match_all': [], 'size': 0}");

    HttpEntity requestQuery = new NStringEntity(), ContentType.APPLICATION_JSON);
    Request totalCountsRequest = new Request("GET", endpoint);
    totalCountsRequest.setEntity(requestQuery);
    Response totalCountsResponse = llClient.performRequest(totalCountsRequest); // FIXME use wrapper
    Map parsedResponse = parseResponse(totalCountsResponse);

    // FIXME replace with JsonApiResponse:
    Map<String, Object> dataObject = new HashMap<>();
    dataObject.put("type", "count");
    dataObject.put("id", config.typeFromAlias(alias));
    dataObject.put("count", getHitsTotalValue(parsedResponse, isES6));

    // Fixme: deprecation warning to be removed later
    var meta = new HashMap<>();
    meta.put("DEPRECATION_WARNING", "The high-level 'count' field is deprecated and will be removed in a later version. The 'count' field will only be found in 'attributes'.");

    var attributes = new HashMap<>();
    attributes.put("count", getHitsTotalValue(parsedResponse, isES6));

    Map<String, Object> resultMap = new HashMap<>();
    resultMap.put("data", List.of(dataObject));

    return resultMap;
  }

  ///////////////
  // Get By ID //
  ///////////////
  private Map getById(String alias, String id)  {
    String endpoint = "/" + alias + "/_doc/" + id;
    log.debug("Get by ID against endpoint: " + endpoint);
    Request idRequest = new Request("GET", endpoint);
    Map collectionDocument = performRequest(idRequest);
    String type = config.typeFromAlias(alias);

    // FIXME replace with JsonApiResponse:
    Map response = new HashMap();
    if ((Boolean) collectionDocument.get("found")) {
      var dataMap = new HashMap<>();
      dataMap.put("id", getId(collectionDocument));
      dataMap.put("type", config.typeFromAlias(alias));
      dataMap.put("attributes", getSource(collectionDocument));
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
