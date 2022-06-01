package org.cedar.onestop.indexer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cedar.onestop.data.util.JsonUtils;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.elastic.common.ElasticsearchReadService;
import org.cedar.onestop.indexer.stream.BulkIndexingConfig;
import org.cedar.onestop.indexer.stream.BulkIndexingTransformer;
import org.cedar.onestop.indexer.stream.FlatteningConfig;
import org.cedar.onestop.indexer.stream.FlatteningTransformer;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksRequest;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

public class ElasticsearchService {
  private static final Logger log = LoggerFactory.getLogger(ElasticsearchService.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  private final RestHighLevelClient client;
  private final ElasticsearchConfig config;

  private final ElasticsearchReadService esReadService;

  public ElasticsearchService(RestHighLevelClient client, ElasticsearchConfig config) {
    this.client = client;
    this.config = config;

    esReadService = new ElasticsearchReadService(client, config);
  }

  public RestHighLevelClient getClient() {
    return client;
  }

  public ElasticsearchConfig getConfig() {
    return config;
  }

  public void initializeCluster() throws Exception {
    ensureIndices();
  }

  private void ensureIndices() throws Exception {
    ensureSearchIndices();
    ensureAnalysisAndErrorsIndices();
  }

  private void ensureSearchIndices() throws Exception {
    ensureAliasWithIndex(config.COLLECTION_SEARCH_INDEX_ALIAS);
    ensureAliasWithIndex(config.GRANULE_SEARCH_INDEX_ALIAS);
    ensureAliasWithIndex(config.FLAT_GRANULE_SEARCH_INDEX_ALIAS);
    if (config.sitemapEnabled()) {
      ensureAliasWithIndex(config.SITEMAP_INDEX_ALIAS);
    }
  }

  private void ensureAnalysisAndErrorsIndices() throws Exception {
    ensureAliasWithIndex(config.COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS);
    ensureAliasWithIndex(config.GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS);
  }

  private void ensureAliasWithIndex(String alias) throws Exception {
    var aliasExists = checkAliasExists(alias);
    if (aliasExists) {
      String existingMapping = getDeployedMappingByAlias(alias);
      log.debug("EXISTING " + alias + " MAPPING: " + existingMapping);
      String expectedMapping = getExpectedMappingByAlias(alias);
      log.debug("EXPECTED " + alias + " MAPPING: " + expectedMapping );

      String existingAnalysis = getDeployedIndexAnalyzersByAlias(alias);
      log.debug("EXISTING " + alias + " ANALYSIS: " + existingAnalysis);
      String expectedAnalysis = getExpectedIndexAnalyzersByAlias(alias);
      log.debug("EXPECTED " + alias + " ANALYSIS: " + expectedAnalysis);

      if(existingMapping != null) {
        List<Map<String, Object>> mappingDiffs = JsonUtils.getJsonDiffList(existingMapping, expectedMapping);

        mappingDiffs.stream().forEach( e -> {
          if (e.get("op") == "remove") {
            log.warn("MAPPING FOR " + alias + " removes " + e.get("path"));
          } else if (e.get("op") == "add") {
            log.warn("MAPPING FOR " + alias + " adds " + e.get("path"));
          } else {
            log.error("MAPPING FOR " + alias + " " + e.get("op") + " " + e.get("path") + " - this operation cannot be done without reindexing.");
          }
        });

        if (existingAnalysis != null) {

          List<Map<String, Object>> settingDiffs = JsonUtils.getJsonDiffList(existingAnalysis, expectedAnalysis);
          settingDiffs.stream().forEach( e -> {
            if (e.get("op") == "remove") {
              log.warn("ANALYSIS FOR " + alias + " removes " + e.get("path"));
            } else if (e.get("op") == "add") {
              log.warn("ANALYSIS FOR " + alias + " adds " + e.get("path"));
            } else {
              log.error("ANALYSIS FOR " + alias + " " + e.get("op") + " " + e.get("path") + " - this operation cannot be done without reindexing.");
            }
          });

          List<String> settingsOps = settingDiffs.stream().map(e -> (String) e.get("op")).collect(Collectors.toList());

          if (!settingsOps.isEmpty() && settingsOps.contains("replace")) {
            System.exit(1);
          }
        }

        List<String> ops = mappingDiffs.stream().map(e -> (String) e.get("op")).collect(Collectors.toList());
        if (!ops.isEmpty() && ops.contains("replace")) {
          System.exit(1);
        }
        // FIXME -- Strict mappings double check?

        putMapping(alias, expectedMapping);
      }
    }
    else {
      log.debug("CREATING INDEX " + alias);
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

  private boolean putSettings(String indexOrAlias, String settings) throws IOException {
    if (settings == null || settings.isBlank() || settings.equals("null")) {
      return true;
    }
    var request = new UpdateSettingsRequest(indexOrAlias).settings(settings, XContentType.JSON);
    var result = client.indices().putSettings(request, RequestOptions.DEFAULT);
    return result.isAcknowledged();
  }

  public String createIndex(String aliasName) throws IOException {
    return createIndex(aliasName, false);
  }

  public String createIndex(String aliasName, boolean applyAlias) throws IOException {
    String indexName = newIndexName(aliasName);
    var jsonIndexMapping = getExpectedMappingByAlias(aliasName);
    var jsonIndexSettings = getExpectedIndexSettingsByAlias(aliasName);
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

  private String getExpectedMappingByAlias(String alias) throws IOException {
    var indexDefinition = config.jsonMapping(alias);
    Map parsedDefinition = mapper.readValue(indexDefinition, Map.class);
    return mapper.writeValueAsString((Map) parsedDefinition.get("mappings"));
  }

  private String getDeployedMappingByAlias(String alias) throws Exception {
    Map<String, Object> response = esReadService.getIndexMapping(alias);
    List data = (List) response.get("data");
    String mapping = null;
    if(data != null) {
      Map attributes = (Map)((Map) data.get(0)).get("attributes");
      mapping = JsonUtils.toJson((Map) attributes.get("mappings"));
    }
    return mapping;
  }

  private String getExpectedIndexSettingsByAlias(String alias) throws IOException {
    var indexDefinition = config.jsonMapping(alias);
    Map parsedDefinition = mapper.readValue(indexDefinition, Map.class);
    return mapper.writeValueAsString((Map) parsedDefinition.get("settings"));
  }

  private String getExpectedIndexAnalyzersByAlias(String alias) throws IOException {
    var indexDefinition = config.jsonMapping(alias);
    Map parsedDefinition = mapper.readValue(indexDefinition, Map.class);
    Map settings = (Map) parsedDefinition.get("settings");
    if (settings == null ) {
      return null;
    }
    return mapper.writeValueAsString((Map)(settings).get("analysis"));
  }

  private String getDeployedIndexAnalyzersByAlias(String alias) throws Exception {
    Map<String, Object> response = esReadService.getIndexSettings(alias);
    List data = (List) response.get("data");
    String mapping = null;
    if(data != null) {
      Map entry = (Map) data.get(0);
      if(entry != null) {
        Map attrs = (Map) entry.get("attributes");
        if(attrs != null) {
          Map settings = (Map) attrs.get("settings");
          if(settings != null) {
            Map index = (Map) settings.get("index");
            if(index != null) {
              mapping = JsonUtils.toJson((Map)index.get("analysis"));
            }
          }
        }
      }
    }
    return mapping;
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
    while (currentRunningTasks() >= config.MAX_TASKS) {
      try {
        sleep(100);
      } catch (InterruptedException e) {
        log.info("blocking for tasks interrupted", e);
      }
    }
  }

  public Integer currentRunningTasks() throws IOException {
    return client.tasks().list(new ListTasksRequest(), RequestOptions.DEFAULT).getTasks().size();
  }

  public BulkByScrollResponse reindex(ReindexRequest request) throws IOException {
    return client.reindex(request, RequestOptions.DEFAULT);
  }

  public Cancellable reindexAsync(ReindexRequest request, ActionListener<BulkByScrollResponse> listener) {
    return client.reindexAsync(request, RequestOptions.DEFAULT, listener);
  }

  public BulkIndexingTransformer buildBulkIndexingTransformer(BulkIndexingConfig config) {
    return new BulkIndexingTransformer(this, config);
  }

  public FlatteningTransformer buildFlatteningTransformer(FlatteningConfig config) {
    return new FlatteningTransformer(this, config);
  }

  public void buildSitemap(Long timestamp) {
    SitemapIndexingHelpers.buildSitemap(this, timestamp);
  }

}
