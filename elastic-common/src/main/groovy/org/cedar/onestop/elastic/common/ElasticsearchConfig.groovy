package org.cedar.onestop.elastic.common

import groovy.util.logging.Slf4j
import org.elasticsearch.Version
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Slf4j
@Component
class ElasticsearchConfig {

  @Autowired
  private Environment environment

  // index aliases
  String COLLECTION_SEARCH_INDEX_ALIAS = "search_collection"
  String COLLECTION_STAGING_INDEX_ALIAS = "staging_collection"
  String GRANULE_SEARCH_INDEX_ALIAS = "search_granule"
  String GRANULE_STAGING_INDEX_ALIAS = "staging_granule"
  String FLAT_GRANULE_SEARCH_INDEX_ALIAS = "search_flattened_granule"
  String SITEMAP_INDEX_ALIAS = "sitemap"

  // pipeline names
  static final String COLLECTION_PIPELINE = "collection_pipeline"
  static final String GRANULE_PIPELINE = "granule_pipeline"

  // type (e.g. 'doc')
  String TYPE

  //
  Integer MAX_TASKS
  Integer REQUESTS_PER_SECOND

  // sitemap configs
  Integer SITEMAP_SCROLL_SIZE
  Integer SITEMAP_COLLECTIONS_PER_SUBMAP

  // Elasticsearch Version
  Version version

  static final String TYPE_COLLECTION = "collection"
  static final String TYPE_GRANULE = "granule"
  static final String TYPE_FLATTENED_GRANULE = "flattened-granule"
  static final String TYPE_SITEMAP = "sitemap"

  private Map<String, String> jsonPipelines = [:]
  private Map<String, String> jsonMappings = [:]
  private Map<String, String> typesByAlias = [:]

  @Autowired
  ElasticsearchConfig(
      // default: null
      @Value('${elasticsearch.index.prefix:}')
          String PREFIX,
      // default: 10
      @Value('${elasticsearch.max-tasks:10}')
          Integer MAX_TASKS,
      // default: null
      @Value('${elasticsearch.requests-per-second:}')
          Integer REQUESTS_PER_SECOND,
      // optional: feature toggled by 'sitemap' profile, default: empty
      @Value('${etl.sitemap.scroll-size:}')
          Integer SITEMAP_SCROLL_SIZE,
      // optional: feature toggled by 'sitemap' profile, default: empty
      @Value('${etl.sitemap.collections-per-submap:}')
          Integer SITEMAP_COLLECTIONS_PER_SUBMAP,

      // Elasticsearch Version
      Version version
  ) {

    // log prefix if it's not null
    if(PREFIX) {
      log.info("Prefix for Elasticsearch aliases provided by config as '${PREFIX}'")
    }

    // tack on prefix to aliases so that later logic does not have to concern itself with prefixing at all
    this.COLLECTION_SEARCH_INDEX_ALIAS = PREFIX + this.COLLECTION_SEARCH_INDEX_ALIAS
    this.COLLECTION_STAGING_INDEX_ALIAS = PREFIX + this.COLLECTION_STAGING_INDEX_ALIAS
    this.GRANULE_SEARCH_INDEX_ALIAS = PREFIX + this.GRANULE_SEARCH_INDEX_ALIAS
    this.GRANULE_STAGING_INDEX_ALIAS = PREFIX + this.GRANULE_STAGING_INDEX_ALIAS
    this.FLAT_GRANULE_SEARCH_INDEX_ALIAS = PREFIX + this.FLAT_GRANULE_SEARCH_INDEX_ALIAS
    this.SITEMAP_INDEX_ALIAS = PREFIX + this.SITEMAP_INDEX_ALIAS

    // type is no longer configurable and based entirely on the migration path from ES5 to ES6+
    this.TYPE = version.onOrAfter(Version.V_6_2_0) ? '_doc' : 'doc'
    this.MAX_TASKS = MAX_TASKS
    this.REQUESTS_PER_SECOND = REQUESTS_PER_SECOND
    this.SITEMAP_SCROLL_SIZE = SITEMAP_SCROLL_SIZE
    this.SITEMAP_COLLECTIONS_PER_SUBMAP = SITEMAP_COLLECTIONS_PER_SUBMAP

    this.version = version

    // Associate pipeline names directly to their JSON definitions as strings as a memoization
    if (version.onOrAfter(Version.V_6_4_0)) {
      // ES6.4+ can make use of more efficient pipeline definitions (e.g. - removing array of fields, etc.)
      this.jsonPipelines[COLLECTION_PIPELINE] = FileUtil.textFromFile("pipelines/${COLLECTION_PIPELINE}_ES5-6_Definition.json")
      this.jsonPipelines[GRANULE_PIPELINE] = FileUtil.textFromFile("pipelines/${GRANULE_PIPELINE}_ES5-6_Definition.json")
    } else {
      this.jsonPipelines[COLLECTION_PIPELINE] = FileUtil.textFromFile("pipelines/${COLLECTION_PIPELINE}Definition.json")
      this.jsonPipelines[GRANULE_PIPELINE] = FileUtil.textFromFile("pipelines/${GRANULE_PIPELINE}Definition.json")
    }

    // Elasticsearch alias names are configurable, and this allows a central mapping between
    // the alias names configured (including the prefix) and the JSON mappings
    // https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html
    if (version.onOrAfter(Version.V_6_0_0)) {
      log.debug("Elasticsearch version ${version.toString()} found. Using mappings without `_all`.")
      // [_all] is deprecated in 6.0+ and will be removed in 7.0.
      // -> It is now disabled by default because it requires extra CPU cycles and disk space
      // https://www.elastic.co/guide/en/elasticsearch/reference/6.4/mapping-all-field.html
      this.jsonMappings[this.COLLECTION_SEARCH_INDEX_ALIAS] = FileUtil.textFromFile("mappings/ES6/search_collectionIndex.json")
      this.jsonMappings[this.COLLECTION_STAGING_INDEX_ALIAS] = FileUtil.textFromFile("mappings/ES6/staging_collectionIndex.json")
      this.jsonMappings[this.GRANULE_SEARCH_INDEX_ALIAS] = FileUtil.textFromFile("mappings/ES6/search_granuleIndex.json")
      this.jsonMappings[this.GRANULE_STAGING_INDEX_ALIAS] = FileUtil.textFromFile("mappings/ES6/staging_granuleIndex.json")
      this.jsonMappings[this.FLAT_GRANULE_SEARCH_INDEX_ALIAS] = FileUtil.textFromFile("mappings/ES6/search_flattened_granuleIndex.json")
      this.jsonMappings[this.SITEMAP_INDEX_ALIAS] = FileUtil.textFromFile("mappings/ES6/sitemapIndex.json")
    } else {
      log.debug("Elasticsearch version ${version.toString()} found. Using mappings with `_all` disabled.")
      // ES 5 did not disable [_all] by default and so the mappings to support < 6.0 explicitly disable it
      // https://www.elastic.co/guide/en/elasticsearch/reference/5.6/mapping-all-field.html
      this.jsonMappings[this.COLLECTION_SEARCH_INDEX_ALIAS] = FileUtil.textFromFile("mappings/ES5/search_collectionIndex.json")
      this.jsonMappings[this.COLLECTION_STAGING_INDEX_ALIAS] = FileUtil.textFromFile("mappings/ES5/staging_collectionIndex.json")
      this.jsonMappings[this.GRANULE_SEARCH_INDEX_ALIAS] = FileUtil.textFromFile("mappings/ES5/search_granuleIndex.json")
      this.jsonMappings[this.GRANULE_STAGING_INDEX_ALIAS] = FileUtil.textFromFile("mappings/ES5/staging_granuleIndex.json")
      this.jsonMappings[this.FLAT_GRANULE_SEARCH_INDEX_ALIAS] = FileUtil.textFromFile("mappings/ES5/search_flattened_granuleIndex.json")
      this.jsonMappings[this.SITEMAP_INDEX_ALIAS] = FileUtil.textFromFile("mappings/ES5/sitemapIndex.json")
    }

    // Associate index aliases directly to their type identifiers for consistency
    this.typesByAlias[this.COLLECTION_SEARCH_INDEX_ALIAS] = TYPE_COLLECTION
    this.typesByAlias[this.COLLECTION_STAGING_INDEX_ALIAS] = TYPE_COLLECTION
    this.typesByAlias[this.GRANULE_SEARCH_INDEX_ALIAS] = TYPE_GRANULE
    this.typesByAlias[this.GRANULE_STAGING_INDEX_ALIAS] = TYPE_GRANULE
    this.typesByAlias[this.FLAT_GRANULE_SEARCH_INDEX_ALIAS] = TYPE_FLATTENED_GRANULE
    this.typesByAlias[this.SITEMAP_INDEX_ALIAS] = TYPE_SITEMAP
  }

  String jsonPipeline(String pipeline) {
    // retrieve JSON pipeline for pipeline name
    return this.jsonPipelines[pipeline]
  }

  String jsonMapping(String alias) {
    // retrieve JSON mapping for index alias
    return this.jsonMappings[alias]
  }

  static String aliasFromIndex(String index) {
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
    return index.replaceFirst(/-\d+$/, "")
  }

  String typeFromIndex(String index) {
    // derive alias from index and return type for alias
    String alias = aliasFromIndex(index)
    return typeFromAlias(alias)
  }

  String typeFromAlias(String alias) {
    // retrieve type for index alias
    return this.typesByAlias[alias]
  }

  Boolean sitemapEnabled() {
    return environment.activeProfiles.contains('sitemap')
  }
}