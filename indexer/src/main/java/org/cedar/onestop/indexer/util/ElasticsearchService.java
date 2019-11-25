package org.cedar.onestop.indexer.util;

import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.Version;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class ElasticsearchService {
  private static final Logger log = LoggerFactory.getLogger(ElasticsearchService.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  private final RestHighLevelClient client;
  private final ElasticsearchConfig config;

  public ElasticsearchService(RestHighLevelClient client, ElasticsearchConfig config) {
    this.client = client;
    this.config = config;
    if (config.version.before(Version.V_6_0_0)) {
      throw new IllegalStateException("The indexer service does not work against Elasticsearch prior to version 6, please use the admin service instead");
    }
  }

  public void initializeCluster() throws IOException {
    ensureIndices();
  }

  private void ensureIndices() throws IOException {
    ensureSearchIndices();
  }

  private void ensureSearchIndices() throws IOException {
    ensureAliasWithIndex(config.COLLECTION_SEARCH_INDEX_ALIAS);
    ensureAliasWithIndex(config.GRANULE_SEARCH_INDEX_ALIAS);
    ensureAliasWithIndex(config.FLAT_GRANULE_SEARCH_INDEX_ALIAS);
    if (config.sitemapEnabled()) {
      ensureAliasWithIndex(config.SITEMAP_INDEX_ALIAS);
    }
  }

  private void ensureAliasWithIndex(String alias) throws IOException {
    var aliasExists = checkAliasExists(alias);
    if (aliasExists){
      putMapping(alias);
    } else {
      createIndexWithAlias(alias);
    }
  }

  private boolean checkAliasExists(String alias) throws IOException {
    return client.indices().existsAlias(new GetAliasesRequest(alias), RequestOptions.DEFAULT);
  }

  private boolean putMapping(String indexOrAlias) throws IOException {
    String jsonIndexDef = config.jsonMapping(indexOrAlias);
    Map parsedMapping = mapper.readValue(jsonIndexDef, Map.class);
    String jsonIndexMapping = mapper.writeValueAsString(((Map) parsedMapping.get("mappings")).get("doc"));
    var request = new PutMappingRequest(indexOrAlias).source(jsonIndexMapping, XContentType.JSON);
    var result = client.indices().putMapping(request, RequestOptions.DEFAULT);
    return result.isAcknowledged();
  }

  private CreateIndexResponse createIndexWithAlias(String alias) throws IOException {
    var indexName = alias + "-" +System.currentTimeMillis();
    var indexDefinition = config.jsonMapping(alias);
    Map parsedDefinition = mapper.readValue(indexDefinition, Map.class);
    String jsonIndexMapping = mapper.writeValueAsString(((Map) parsedDefinition.get("mappings")).get("doc"));
    var request = new CreateIndexRequest(indexName)
        .source(jsonIndexMapping, XContentType.JSON)
        .alias(new Alias(alias));
    var result = client.indices().create(request, RequestOptions.DEFAULT);
    log.debug("Created new index [" + indexName + "] with alias [" + alias + "]");
    return result;
  }

}
