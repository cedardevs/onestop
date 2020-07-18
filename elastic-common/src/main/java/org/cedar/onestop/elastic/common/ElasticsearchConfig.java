package org.cedar.onestop.elastic.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cedar.onestop.data.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.*;

public class ElasticsearchConfig {
  private static final Logger log = LoggerFactory.getLogger(ElasticsearchConfig.class);
  private static final ObjectMapper jsonMapper = new ObjectMapper();

  public ElasticsearchVersion version;

  // index aliases
  public final String COLLECTION_SEARCH_INDEX_ALIAS;
  public final String GRANULE_SEARCH_INDEX_ALIAS;
  public final String FLAT_GRANULE_SEARCH_INDEX_ALIAS;
  public final String SITEMAP_INDEX_ALIAS;
  public final String COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS;
  public final String GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS;

  public final String PREFIX;

  public final Integer MAX_TASKS;
  public final Integer REQUESTS_PER_SECOND;

  // sitemap configs
  public final Integer SITEMAP_SCROLL_SIZE;
  public final Integer SITEMAP_COLLECTIONS_PER_SUBMAP;
  public final Boolean SITEMAP_ENABLED;

  public static final String TYPE_COLLECTION = "collection";
  public static final String TYPE_GRANULE = "granule";
  public static final String TYPE_FLATTENED_GRANULE = "flattened-granule";
  public static final String TYPE_SITEMAP = "sitemap";

  private Map<String, String> jsonMappings = new HashMap<>();
  private Map<String, Map<String, Map>> parsedMappings = new HashMap<>();
  private Map<String, String> typeByAlias = new HashMap<>();
  private Map<String, String> searchAliasByType = new HashMap<>();
  private Map<String, String> analysisAndErrorAliasByType = new HashMap<>();

  public ElasticsearchConfig(
      ElasticsearchVersion version,
      // default: null
      String PREFIX,
      // default: 10
      Integer MAX_TASKS,
      // default: null
      Integer REQUESTS_PER_SECOND,
      // optional: feature toggled by 'sitemap' profile, default: empty
      Integer SITEMAP_SCROLL_SIZE,
      // optional: feature toggled by 'sitemap' profile, default: empty
      Integer SITEMAP_COLLECTIONS_PER_SUBMAP,
      // optional: enable sitemap feature, default: false
      Boolean SITEMAP_ENABLED
  ) throws IOException {

    this.version = version;
    this.PREFIX = PREFIX;

    // log prefix if it's not null
    if (PREFIX != null && !PREFIX.isBlank()) {
      log.info("Prefix for Elasticsearch aliases provided by config as " + PREFIX);
    }

    // tack on prefix to aliases so that later logic does not have to concern itself with prefixing at all
    this.COLLECTION_SEARCH_INDEX_ALIAS = PREFIX + "search_collection";
    this.GRANULE_SEARCH_INDEX_ALIAS = PREFIX + "search_granule";
    this.FLAT_GRANULE_SEARCH_INDEX_ALIAS = PREFIX + "search_flattened_granule";
    this.SITEMAP_INDEX_ALIAS = PREFIX + "sitemap";
    this.COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS = PREFIX + "analysis_error_collection";
    this.GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS = PREFIX + "analysis_error_granule";

    // use _doc if it's supported to avoid using an explicit type, which is deprecated
    this.MAX_TASKS = MAX_TASKS;
    this.REQUESTS_PER_SECOND = REQUESTS_PER_SECOND;
    this.SITEMAP_SCROLL_SIZE = SITEMAP_SCROLL_SIZE;
    this.SITEMAP_COLLECTIONS_PER_SUBMAP = SITEMAP_COLLECTIONS_PER_SUBMAP;
    this.SITEMAP_ENABLED = SITEMAP_ENABLED;

    // Elasticsearch alias names are configurable, and this allows a central mapping between
    // the alias names configured (including the prefix) and the JSON mappings
    // https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html
    this.jsonMappings.put(COLLECTION_SEARCH_INDEX_ALIAS, FileUtils.textFromClasspathFile("mappings/search_collectionIndex.json"));
    this.jsonMappings.put(GRANULE_SEARCH_INDEX_ALIAS, FileUtils.textFromClasspathFile("mappings/search_granuleIndex.json"));
    this.jsonMappings.put(FLAT_GRANULE_SEARCH_INDEX_ALIAS, FileUtils.textFromClasspathFile("mappings/search_flattened_granuleIndex.json"));
    this.jsonMappings.put(SITEMAP_INDEX_ALIAS, FileUtils.textFromClasspathFile("mappings/sitemapIndex.json"));
    this.jsonMappings.put(COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS, FileUtils.textFromClasspathFile("mappings/analysis_error_collectionIndex.json"));
    this.jsonMappings.put(GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS, FileUtils.textFromClasspathFile("mappings/analysis_error_granuleIndex.json"));

    this.parsedMappings.put(COLLECTION_SEARCH_INDEX_ALIAS, jsonMapper.readValue(jsonMappings.get(COLLECTION_SEARCH_INDEX_ALIAS), Map.class));
    this.parsedMappings.put(GRANULE_SEARCH_INDEX_ALIAS, jsonMapper.readValue(jsonMappings.get(GRANULE_SEARCH_INDEX_ALIAS), Map.class));
    this.parsedMappings.put(FLAT_GRANULE_SEARCH_INDEX_ALIAS, jsonMapper.readValue(jsonMappings.get(FLAT_GRANULE_SEARCH_INDEX_ALIAS), Map.class));
    this.parsedMappings.put(SITEMAP_INDEX_ALIAS, jsonMapper.readValue(jsonMappings.get(SITEMAP_INDEX_ALIAS), Map.class));
    this.parsedMappings.put(COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS, jsonMapper.readValue(jsonMappings.get(COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS), Map.class));
    this.parsedMappings.put(GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS, jsonMapper.readValue(jsonMappings.get(GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS), Map.class));

    // Associate index aliases directly to their type identifiers for consistency
    this.typeByAlias.put(COLLECTION_SEARCH_INDEX_ALIAS, TYPE_COLLECTION);
    this.typeByAlias.put(COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS, TYPE_COLLECTION);
    this.typeByAlias.put(GRANULE_SEARCH_INDEX_ALIAS, TYPE_GRANULE);
    this.typeByAlias.put(GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS, TYPE_GRANULE);
    this.typeByAlias.put(FLAT_GRANULE_SEARCH_INDEX_ALIAS, TYPE_FLATTENED_GRANULE);
    this.typeByAlias.put(SITEMAP_INDEX_ALIAS, TYPE_SITEMAP);

    // Conversely, associate types directly to their index aliases (search & analysis and errors)
    this.searchAliasByType.put(TYPE_COLLECTION, COLLECTION_SEARCH_INDEX_ALIAS);
    this.searchAliasByType.put(TYPE_GRANULE, GRANULE_SEARCH_INDEX_ALIAS);
    this.searchAliasByType.put(TYPE_FLATTENED_GRANULE, FLAT_GRANULE_SEARCH_INDEX_ALIAS);

    this.analysisAndErrorAliasByType.put(TYPE_COLLECTION, COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS);
    this.analysisAndErrorAliasByType.put(TYPE_GRANULE, GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS);
  }

  public String jsonMapping(String alias) {
    // retrieve JSON mapping for index alias
    return this.jsonMappings.getOrDefault(alias, null);
  }

  public Map<String, Map> parsedMapping(String alias) {
    // retrieve JSON mapping for index alias
    return this.parsedMappings.getOrDefault(alias, Collections.emptyMap());
  }

  public Map<String, Map> indexedProperties(String alias) {
    var parsed = (Map<String, Map>) parsedMapping(alias);
    var mappings = (Map<String, Map>) parsed.getOrDefault("mappings", Collections.emptyMap());
    return (Map<String, Map>) mappings.getOrDefault("properties", Collections.emptyMap());
  }

  public static String aliasFromIndex(String index) {
    // Determine position of postfix timestamp on index name.
    // Index names are just the alias with an optional prefix
    // AND suffix => '-' + System.currentTimeMillis()
    // Regular Expression Explanation
    // \d+ -> Indicates 1 or more numeric characters.
    //        System.currentTimeMillis() is generally 13 digits in modern day,
    //        but in theory could be less (past) or rollover (future) to >13 digits
    // $ -> Indicates the end of the string (there should be nothing after the timestamp in the index name)
    // This allows us to accommodate a test which used to use this function equivalent elsewhere by assuming
    // it could be passed either an index or an alias, but in reality now, there's no reason why this function
    // in the code would be called using an alias or prefixed alias alone.
    return index.replaceFirst("-\\d+$", "");
  }

  public String typeFromIndex(String index) {
    // derive alias from index and return type for alias
    String alias = aliasFromIndex(index);
    return typeFromAlias(alias);
  }

  public String typeFromAlias(String alias) {
    // retrieve type for index alias
    return this.typeByAlias.get(alias);
  }

  public String searchAliasFromType(String type) {
    return this.searchAliasByType.get(type);
  }

  public String analysisAndErrorsAliasFromType(String type) {
    return this.analysisAndErrorAliasByType.get(type);
  }

  public Boolean sitemapEnabled() {
    return SITEMAP_ENABLED;
  }

}
