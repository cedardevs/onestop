package org.cedar.onestop.elastic.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ElasticsearchConfig {
  private static final Logger log = LoggerFactory.getLogger(ElasticsearchConfig.class);

  // index aliases
  public String COLLECTION_SEARCH_INDEX_ALIAS = "search_collection";
  public String COLLECTION_STAGING_INDEX_ALIAS = "staging_collection";
  public String GRANULE_SEARCH_INDEX_ALIAS = "search_granule";
  public String GRANULE_STAGING_INDEX_ALIAS = "staging_granule";
  public String FLAT_GRANULE_SEARCH_INDEX_ALIAS = "search_flattened_granule";
  public String SITEMAP_INDEX_ALIAS = "sitemap";

  // type (e.g. 'doc')
  public final String TYPE;

  //
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
  private Map<String, String> typesByAlias = new HashMap<>();

  public ElasticsearchConfig(
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
    // log prefix if it's not null
    if (PREFIX != null && !PREFIX.isBlank()) {
      log.info("Prefix for Elasticsearch aliases provided by config as " + PREFIX);
    }

    // tack on prefix to aliases so that later logic does not have to concern itself with prefixing at all
    this.COLLECTION_SEARCH_INDEX_ALIAS = PREFIX + this.COLLECTION_SEARCH_INDEX_ALIAS;
    this.COLLECTION_STAGING_INDEX_ALIAS = PREFIX + this.COLLECTION_STAGING_INDEX_ALIAS;
    this.GRANULE_SEARCH_INDEX_ALIAS = PREFIX + this.GRANULE_SEARCH_INDEX_ALIAS;
    this.GRANULE_STAGING_INDEX_ALIAS = PREFIX + this.GRANULE_STAGING_INDEX_ALIAS;
    this.FLAT_GRANULE_SEARCH_INDEX_ALIAS = PREFIX + this.FLAT_GRANULE_SEARCH_INDEX_ALIAS;
    this.SITEMAP_INDEX_ALIAS = PREFIX + this.SITEMAP_INDEX_ALIAS;

    // use _doc if it's supported to avoid using an explicit type, which is deprecated
    this.TYPE = "_doc";
    this.MAX_TASKS = MAX_TASKS;
    this.REQUESTS_PER_SECOND = REQUESTS_PER_SECOND;
    this.SITEMAP_SCROLL_SIZE = SITEMAP_SCROLL_SIZE;
    this.SITEMAP_COLLECTIONS_PER_SUBMAP = SITEMAP_COLLECTIONS_PER_SUBMAP;
    this.SITEMAP_ENABLED = SITEMAP_ENABLED;

    // Elasticsearch alias names are configurable, and this allows a central mapping between
    // the alias names configured (including the prefix) and the JSON mappings
    // https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html
    this.jsonMappings.put(COLLECTION_SEARCH_INDEX_ALIAS, FileUtil.textFromClasspathFile("mappings/search_collectionIndex.json"));
    this.jsonMappings.put(COLLECTION_STAGING_INDEX_ALIAS, FileUtil.textFromClasspathFile("mappings/staging_collectionIndex.json"));
    this.jsonMappings.put(GRANULE_SEARCH_INDEX_ALIAS, FileUtil.textFromClasspathFile("mappings/search_granuleIndex.json"));
    this.jsonMappings.put(GRANULE_STAGING_INDEX_ALIAS, FileUtil.textFromClasspathFile("mappings/staging_granuleIndex.json"));
    this.jsonMappings.put(FLAT_GRANULE_SEARCH_INDEX_ALIAS, FileUtil.textFromClasspathFile("mappings/search_flattened_granuleIndex.json"));
    this.jsonMappings.put(SITEMAP_INDEX_ALIAS, FileUtil.textFromClasspathFile("mappings/sitemapIndex.json"));

    // Associate index aliases directly to their type identifiers for consistency
    this.typesByAlias.put(COLLECTION_SEARCH_INDEX_ALIAS, TYPE_COLLECTION);
    this.typesByAlias.put(COLLECTION_STAGING_INDEX_ALIAS, TYPE_COLLECTION);
    this.typesByAlias.put(GRANULE_SEARCH_INDEX_ALIAS, TYPE_GRANULE);
    this.typesByAlias.put(GRANULE_STAGING_INDEX_ALIAS, TYPE_GRANULE);
    this.typesByAlias.put(FLAT_GRANULE_SEARCH_INDEX_ALIAS, TYPE_FLATTENED_GRANULE);
    this.typesByAlias.put(SITEMAP_INDEX_ALIAS, TYPE_SITEMAP);
  }

  public String jsonMapping(String alias) {
    // retrieve JSON mapping for index alias
    return this.jsonMappings.get(alias);
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
    return this.typesByAlias.get(alias);
  }

  public Boolean sitemapEnabled() {
    return SITEMAP_ENABLED;
  }
}
