package org.cedar.onestop.elastic.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.elasticsearch.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class ElasticsearchService {
  final RestHighLevelClient hlClient;
  final RestClient llClient;
  final ElasticsearchConfig config;
  final ObjectMapper mapper = new ObjectMapper();
  final Logger log = LoggerFactory.getLogger(getClass());


  ElasticsearchService(RestHighLevelClient restClient, ElasticsearchConfig esConfig) {
    this.hlClient = restClient;
    this.llClient = restClient.getLowLevelClient();
    this.config = esConfig;
  }

  public Map<String, Object> performRequest(Request request) {
    log.debug("Sending Elasticsearch request: " + request.toString());
    try {
      var response = llClient.performRequest(request);
      log.debug("Got response: " + response);
      return parseResponse(response);
    }
    catch (ResponseException e) {
      var response = parseResponse(e.getResponse());
      log.debug("Got response: " + response);
      return response;
    }
    catch (IOException e) {
      Map<String, Object> response = new HashMap<>();
      response.put("statusCode", HttpStatus.SC_SERVICE_UNAVAILABLE);
      // TODO -- more content in response here?
      log.error("Elasticsearch request [ " + request.getMethod() + " " + request.getEndpoint() + " ] failed: " + e.getMessage());
      return response;
    }
  }

  public Map<String, Object> performRequest(String method, String endpoint, String requestBody) {
    log.debug("Sending elasticsearch request: " + method + " " + endpoint + " " + requestBody);
    var request = new Request(method, endpoint);
    request.setJsonEntity(requestBody);
    return performRequest(request);
  }

  public Map<String, Object> parseResponse(Response response) {
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
      result.put("statusCode", HttpStatus.SC_SERVICE_UNAVAILABLE);
      // TODO -- correct status code? need more info?
      log.error("Failed to parse Elasticsearch response as JSON", e);
    }
    return result;
  }
}
