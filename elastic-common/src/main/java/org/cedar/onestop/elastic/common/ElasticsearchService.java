package org.cedar.onestop.elastic.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.cedar.onestop.data.util.JsonUtils;
import org.elasticsearch.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ElasticsearchService {
  final RestHighLevelClient hlClient;
  final RestClient llClient;
  final ElasticsearchConfig config;
  final ObjectMapper mapper = new ObjectMapper();
  final Logger log = LoggerFactory.getLogger(getClass());

  final boolean isES6;


  ElasticsearchService(RestHighLevelClient restClient, ElasticsearchConfig esConfig) {
    this.hlClient = restClient;
    this.llClient = restClient.getLowLevelClient();
    this.config = esConfig;
    this.isES6 = esConfig.version.isMajorVersion(6);
  }

  Map<String, Object> performRequest(Request request) {
    try {
      var response = llClient.performRequest(request);
      log.debug("Got response: " + response);
      return parseResponse(response);
    }
    catch (ResponseException e) {
      var response = parseResponse(e.getResponse());
      log.error("Elasticsearch request failed: " + response);
      return response;
    }
    catch (IOException e) {
      Map<String, Object> response = new HashMap<>();
      response.put("statusCode", HttpStatus.SC_SERVICE_UNAVAILABLE);
      response.put("");
    }
  }

  Map<String, Object> performRequest(String method, String endpoint, String requestBody) {
    log.debug("Performing elasticsearch request: " + method + " " + endpoint + " " + requestBody);
    var request = new Request(method, endpoint);
    request.setJsonEntity(requestBody);
    return performRequest(request);
  }

  Map<String, Object> parseResponse(Response response) {
    int statusCode = response.getStatusLine().getStatusCode();
    Map<String, Object> result = new HashMap<>();
    // Negative status codes can occur with network/connection problems so replace them with 500-level error
    result.put("statusCode", statusCode > 0 ? statusCode : HttpStatus.SC_INTERNAL_SERVER_ERROR);
    try {
      if (response.getEntity() != null) {
        HttpEntity entity = response.getEntity();
        InputStream content = entity.getContent();
        result.putAll(mapper.readValue(content, Map.class));
      }
    }
    catch (Exception e) {
      log.warn("Failed to parse Elasticsearch response as JSON", e);
    }
    return result;
  }
}
