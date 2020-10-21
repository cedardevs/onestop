package org.cedar.onestop.elastic.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.RequestLine;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentUtil {
  private static final Logger log = LoggerFactory.getLogger(DocumentUtil.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  static Map parseESResponse(Response response) {
    int statusCode = response.getStatusLine().getStatusCode();
    Map result = new HashMap<>();
    result.put("statusCode", statusCode > 0 ? statusCode : 500);
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

  static String getId(Map document) {
    return document.get("_id").toString();
  }

  static Map getSource(Map document) {
    return (Map) document.get("_source");
  }

//  static Map getHits(Map parsedResponse) {
//    return (Map) parsedResponse.get("hits");
//  }

//  static Map getHitsTotal(Map parsedResponse) {
//    return (Map) getHits(parsedResponse).get("total");
//  }
//
//  static Map getAggregations(Map document) {
//    return (Map) document.get("aggregations");
//  }

//  static int getTook(Map parsedResponse) {
//    return Integer.parseInt(parsedResponse.get("took").toString());
//  }
//
//  static int getHitsTotalValue(Map parsedResponse, boolean isES6) {
//    // the structure of the hits in response is different between ES6/ES7
//    // https://www.elastic.co/guide/en/elasticsearch/reference/current/breaking-changes-7.0.html#hits-total-now-object-search-response
//    if(isES6) {
//      return (Integer) getHits(parsedResponse).get("total");
//    } else {
//      return (Integer) getHitsTotal(parsedResponse).get("value");
//    }
//  }
//
//  static String getHitsTotalRelation(Map parsedResponse) {
//    return (String) getHitsTotal(parsedResponse).get("relation");
//  }
//
//  static List<Map> getDocuments(Map parsedResponse) {
//    Map hits = getHits(parsedResponse);
//    return (List<Map>) hits.get("hits");
//  }
//
//  static int getCount(Map parsedResponse) {
//    return Integer.parseInt(parsedResponse.get("count").toString());
//  }
//
//  static String getScrollId(Map parsedResponse) {
//    return parsedResponse.get("_scroll_id").toString();
//  }
//
//  static String getIndex(Map document) {
//    return document.get("_index").toString();
//  }
//
//  static int getVersion(Map document) {
//    return Integer.parseInt(document.get("_version").toString());
//  }
//
//  static String getFileIdentifier(Map document) {
//    return getSource(document).get("fileIdentifier").toString();
//  }
//
//  static String getFileIdentifierFromSource(Map source) {
//    return source.get("fileIdentifier").toString();
//  }
//
//  static String getParentIdentifier(Map document) {
//    return getSource(document).get("parentIdentifier").toString();
//  }
//
//  static String getParentIdentifierFromSource(Map source) {
//    return source.get("parentIdentifier").toString();
//  }
//
//  static String getInternalParentIdentifier(Map document) {
//    return getSource(document).get("internalParentIdentifier").toString();
//  }
//
//  static String getInternalParentIdentifierFromSource(Map source) {
//    return source.get("internalParentIdentifier").toString();
//  }
//
//  static String getDOI(Map document) {
//    return getSource(document).get("doi").toString();
//  }
//
//  static String getDOIFromSource(Map source) {
//    return source.get("doi").toString();
//  }
//
//  static Long getMaxStagedDateValue(Map document) {
//    Map maxStagedDate = (Map) getAggregations(document).get("maxStagedDate");
//    return Long.valueOf(maxStagedDate.get("value").toString());
//  }
//
//  static List<Map> getCollectionBuckets(Map document) {
//    Map collections = (Map) getAggregations(document).get("collections");
//    return (List<Map>) collections.get("buckets");
//  }
//  static List<Map> getCollectionBucketsFromAggregations(Map aggregations) {
//    Map collections = (Map) aggregations.get("collections");
//    return (List<Map>) collections.get("buckets");
//  }

}
