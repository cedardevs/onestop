package org.cedar.onestop.indexer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.indexer.stream.BulkIndexingTransformer;
import org.cedar.onestop.indexer.stream.FlatteningTriggerTransformer;
import org.cedar.onestop.indexer.stream.SitemapIndexer;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksRequest;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

public class ElasticsearchService {
  private static final Logger log = LoggerFactory.getLogger(ElasticsearchService.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  private final RestHighLevelClient client;
  private final ElasticsearchConfig config;

  public ElasticsearchService(RestHighLevelClient client, ElasticsearchConfig config) {
    this.client = client;
    this.config = config;
//    int majorVersion = Integer.parseInt(config.version.getNumber().split("\\.")[0]);
//    int minimumCompatibleMajorVersion = 6;
//    if (majorVersion < minimumCompatibleMajorVersion) {
//      throw new IllegalStateException("The indexer service does not work against Elasticsearch < version " + minimumCompatibleMajorVersion);
//    }
  }

  public RestHighLevelClient getClient() {
    return client;
  }

  public ElasticsearchConfig getConfig() {
    return config;
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
    if (aliasExists) {
      var mapping = getMappingByAlias(alias);
      putMapping(alias, mapping);
    }
    else {
      createIndex(alias, true);
    }
  }

  private boolean checkAliasExists(String alias) throws IOException {
    return client.indices().existsAlias(new GetAliasesRequest(alias), RequestOptions.DEFAULT);
  }

  private boolean putMapping(String indexOrAlias, String mapping) throws IOException {
    var request = new PutMappingRequest(indexOrAlias).source(mapping, XContentType.JSON);
    var result = client.indices().putMapping(request, RequestOptions.DEFAULT);
    return result.isAcknowledged();
  }

  public String createIndex(String aliasName) throws IOException {
    return createIndex(aliasName, false);
  }

  public String createIndex(String aliasName, boolean applyAlias) throws IOException {
    String indexName = newIndexName(aliasName);
    var jsonIndexMapping = getMappingByAlias(aliasName);
    var jsonIndexSettings = getIndexSettingsByAlias(aliasName);
    var request = new CreateIndexRequest(indexName)
        .mapping(jsonIndexMapping, XContentType.JSON);
    if (applyAlias) {
      request.alias(new Alias(aliasName));
    }
    if (!jsonIndexSettings.equals("null")) { // ObjectMapper returns an actual string that says null
      request.settings(jsonIndexSettings, XContentType.JSON);
    }
    var result = client.indices().create(request, RequestOptions.DEFAULT);
    log.debug("Created new index [" + indexName + "] with alias [" + aliasName + "]");
    return result.index();
  }

  public boolean moveAliasToIndex(String alias, String index) throws IOException {
    return moveAliasToIndex(alias, index, false);
  }

  public boolean moveAliasToIndex(String alias, String index, Boolean dropOldIndices) throws IOException {
    var oldIndices = getIndicesForAlias(alias);
    var aliasRequest = new IndicesAliasesRequest();
    oldIndices.forEach(oldName -> aliasRequest.addAliasAction(AliasActions.remove().index(oldName).alias(alias)));
    aliasRequest.addAliasAction(AliasActions.add().index(index).alias(alias));
    var aliasResponse = client.indices().updateAliases(aliasRequest, RequestOptions.DEFAULT);
    if (dropOldIndices && aliasResponse.isAcknowledged()) {
      return dropIndex((String[]) oldIndices.toArray());
    }
    else {
      return aliasResponse.isAcknowledged();
    }
  }

  private Set<String> getIndicesForAlias(String alias) throws IOException {
    return client.indices().getAlias(new GetAliasesRequest(alias), RequestOptions.DEFAULT)
        .getAliases().keySet().stream().filter(it -> it.startsWith(alias)).collect(Collectors.toSet());
  }

  private boolean dropIndex(String index) throws IOException {
    return dropIndex(new String[]{index});
  }

  private boolean dropIndex(String... index) throws IOException {
    return client.indices().delete(new DeleteIndexRequest(index), RequestOptions.DEFAULT).isAcknowledged();
  }

  private String getMappingByAlias(String alias) throws IOException {
    var indexDefinition = config.jsonMapping(alias);
    Map parsedDefinition = mapper.readValue(indexDefinition, Map.class);
    return mapper.writeValueAsString((Map) parsedDefinition.get("mappings"));
  }

  private String getIndexSettingsByAlias(String alias) throws IOException {
    var indexDefinition = config.jsonMapping(alias);
    Map parsedDefinition = mapper.readValue(indexDefinition, Map.class);
    return mapper.writeValueAsString((Map) parsedDefinition.get("settings"));
  }

  private String newIndexName(String alias) {
    return alias + "-" + System.currentTimeMillis();
  }

  public double maxValue(String index, String field) throws IOException {
    var aggName = "max_" + field;
    var searchBuilder = new SearchSourceBuilder()
        .aggregation(AggregationBuilders.max(aggName).field(field))
        .size(0);
    var searchRequest = new SearchRequest(index)
        .source(searchBuilder);

    var response = client.search(searchRequest, RequestOptions.DEFAULT);
    Max max = response.getAggregations().get(aggName);
    return max.getValue();
  }

  public BulkResponse bulk(BulkRequest request) throws IOException {
    return client.bulk(request, RequestOptions.DEFAULT);
  }

  public GetResponse get(String index, String id) throws IOException {
    var request = new GetRequest(index, id);
    return client.get(request, RequestOptions.DEFAULT);
  }

  public void blockUntilTasksAvailable() throws IOException {
    while (client.tasks().list(new ListTasksRequest(), RequestOptions.DEFAULT).getTasks().size() >= config.MAX_TASKS) {
      try {
        sleep(100);
      } catch (InterruptedException e) {
        log.info("blocking for tasks interrupted", e);
      }
    }
  }

  public Cancellable reindexAsync(ReindexRequest request, ActionListener<BulkByScrollResponse> listener) {
    return client.reindexAsync(request, RequestOptions.DEFAULT, listener);
  }

  public BulkIndexingTransformer buildBulkIndexingTransformer(Duration publishInterval, Long bulkMaxBytes) {
    return new BulkIndexingTransformer(this, publishInterval, bulkMaxBytes);
  }

  public FlatteningTriggerTransformer buildFlatteningTriggerTransformer(String keyValueStoreName, String flatteningScript, Duration interval) {
    return new FlatteningTriggerTransformer(keyValueStoreName, this, flatteningScript, interval);
  }

  public void buildSitemap(Long timestamp) {
    SitemapIndexer.buildSitemap(this, timestamp);
  }

}
