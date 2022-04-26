package org.cedar.onestop.indexer.util;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;


public class SitemapIndexingHelpers {
  private static final Logger log = LoggerFactory.getLogger(SitemapIndexingHelpers.class);

  public static void buildSitemap(ElasticsearchService esService, long timestamp) {
    log.info("starting sitemap update process for timestamp " + timestamp);
    try {
      var start = System.currentTimeMillis();
      var params = new SitemapParams(
          esService.getConfig().COLLECTION_SEARCH_INDEX_ALIAS,
          esService.getConfig().SITEMAP_INDEX_ALIAS,
          esService.getConfig().SITEMAP_SCROLL_SIZE,
          timestamp
      );
      if (esService.maxValue(params.to, "lastUpdatedDate") >= timestamp) {
        log.info("sitemap has already been updated beyond timestamp " + timestamp);
        return;
      }
      var sitemapResult = runSitemapEtl(esService.getClient(), params);
      var end = System.currentTimeMillis();
      log.info("sitemap updated with " + sitemapResult + " collections in " + (end - start) / 1000 + "s");
    } catch (IOException e) {
      log.error("updating sitemap failed", e);
    }
  }

  private static int runSitemapEtl(RestHighLevelClient client, SitemapParams params) throws IOException {
    var sourceCount = client.count(new CountRequest(params.from), RequestOptions.DEFAULT).getCount();
    var pagesNeeded = sourceCount / params.pageSize;
    if (sourceCount % params.pageSize != 0) {
      pagesNeeded++;
    }
    var currPageCount = client.count(new CountRequest(params.to), RequestOptions.DEFAULT).getCount();
    if (pagesNeeded < currPageCount) {
      log.info("number of sitemap pages is shrinking, rebuilding from scratch");
      var deleteAllRequest = new DeleteByQueryRequest(params.to).setQuery(QueryBuilders.matchAllQuery());
      client.deleteByQuery(deleteAllRequest, RequestOptions.DEFAULT);
    }

    final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
    var searchSourceBuilder = new SearchSourceBuilder()
        .size(params.pageSize)
        .query(matchAllQuery())
        .fetchSource(false)
        .sort("_doc");
    var searchRequest = new SearchRequest(params.from)
        .scroll(scroll)
        .source(searchSourceBuilder);

    var searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    var scrollId = searchResponse.getScrollId();
    var searchHits = searchResponse.getHits().getHits();

    var totalIndexed = 0;
    for (int i = 0; searchHits != null && searchHits.length > 0; i++) {
      totalIndexed += indexSitemapChunk(client, params.to, searchHits, params.timestamp, i);

      var nextScrollRequest = new SearchScrollRequest(scrollId).scroll(scroll);
      searchResponse = client.scroll(nextScrollRequest, RequestOptions.DEFAULT);
      scrollId = searchResponse.getScrollId();
      searchHits = searchResponse.getHits().getHits();
    }

    ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
    clearScrollRequest.addScrollId(scrollId);
    ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
    if (!clearScrollResponse.isSucceeded()) {
      log.error("cleaning up scroll with id [" + scrollId + "] did not succeed: " + clearScrollResponse);
    }
    return totalIndexed;
  }

  private static int indexSitemapChunk(RestHighLevelClient client, String index, SearchHit[] hits, long timestamp, int chunkNum) throws IOException {
    log.info("indexing sitemap chunk #" + chunkNum + " with " + hits.length + " collection ids");
    var ids = Arrays.stream(hits).map(SearchHit::getId).collect(Collectors.toList());
    Map<String, Object> jsonMap = new HashMap<>();
    jsonMap.put("lastUpdatedDate", timestamp);
    jsonMap.put("content", ids);
    var request = new IndexRequest(index).id(String.valueOf(chunkNum)).source(jsonMap);
    var response = client.index(request, RequestOptions.DEFAULT);
    log.info("indexed sitemap chunk #" + chunkNum + " with result status " + response.status());
    return hits.length;
  }

  private static class SitemapParams {
    final String from;
    final String to;
    final int pageSize;
    final long timestamp;

    SitemapParams(String from, String to, int pageSize, long timestamp) {
      this.from = from;
      this.to = to;
      this.pageSize = pageSize;
      this.timestamp = timestamp;
    }
  }

}
