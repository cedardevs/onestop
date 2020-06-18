package org.cedar.onestop.elastic.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.cedar.onestop.data.util.JsonUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.cedar.onestop.elastic.common.DocumentUtil.*;
import static org.cedar.onestop.elastic.common.DocumentUtil.getSource;

public class ElasticsearchReadService {

  private static final Logger log = LoggerFactory.getLogger(ElasticsearchReadService.class);

  private RestClient client;
  private ElasticsearchConfig config;
  private ObjectMapper mapper = new ObjectMapper();

  public ElasticsearchReadService(RestClient restClient, ElasticsearchConfig esConfig) {
    this.client = restClient;
    this.config = esConfig;
  }

  public Map getTotalCounts(String alias) throws IOException {
    String endpoint = "/" + alias + "/_search";
    var requestMap = new HashMap<>();
    requestMap.put("track_total_hits", true);
    requestMap.put("query", "{'match_all': [], 'size': 0}");

    HttpEntity requestQuery = new NStringEntity(), ContentType.APPLICATION_JSON);
    Request totalCountsRequest = new Request("GET", endpoint);
    totalCountsRequest.setEntity(requestQuery);
    Response totalCountsResponse = client.performRequest(totalCountsRequest);
    Map parsedResponse = parseSearchResponse(totalCountsResponse);

//    [
//    data: [
//            [
//    type : "count",
//        id   : esConfig.typeFromAlias(alias),
//        count: getHitsTotalValue(parsedResponse, isES6)
//            ]
//        ]
//    ]

    return resultMap;
  }

  private Map getById(String alias, String id) {
    String endpoint = "/${alias}/_doc/${id}"
    log.debug("Get by ID against endpoint: ${endpoint}")
    Request idRequest = new Request('GET', endpoint)
    Response idResponse = restClient.performRequest(idRequest)
    Map collectionDocument = parseSearchResponse(idResponse)
    String type = esConfig.typeFromAlias(alias)
    if (collectionDocument.found) {
      return [
      data: [[
      id        : getId(collectionDocument),
          type      : esConfig.typeFromAlias(alias),
          attributes: getSource(collectionDocument)
                 ]]
      ]
    }
    else {
      return [
      status: HttpStatus.NOT_FOUND.value(),
          title : 'No such document',
          detail: "Record type ${type} with Elasticsearch ID [ ${id} ] does not exist."
      ]
    }
  }

}
