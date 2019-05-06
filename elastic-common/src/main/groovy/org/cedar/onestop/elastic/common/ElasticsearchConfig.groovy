package org.cedar.onestop.elastic.common

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Slf4j
@Component
class ElasticsearchConfig {

    @Autowired
    private Environment environment

    String PREFIX
    String COLLECTION_SEARCH_INDEX_ALIAS
    String COLLECTION_STAGING_INDEX_ALIAS
    String GRANULE_SEARCH_INDEX_ALIAS
    String GRANULE_STAGING_INDEX_ALIAS
    String FLAT_GRANULE_SEARCH_INDEX_ALIAS
    String SITEMAP_INDEX_ALIAS
    String COLLECTION_PIPELINE
    String GRANULE_PIPELINE
    String TYPE
    Integer MAX_TASKS
    Integer REQUESTS_PER_SECOND
    Integer SITEMAP_SCROLL_SIZE
    Integer SITEMAP_COLLECTIONS_PER_SUBMAP

    static final String TYPE_COLLECTION = "collection"
    static final String TYPE_GRANULE = "granule"
    static final String TYPE_FLATTENED_GRANULE = "flattenedGranule"
    static final String TYPE_SITEMAP = "sitemap"

    private Map<String, String> jsonMappings = [:]
    private Map<String, String> typeMappings = [:]

    @Autowired
    ElasticsearchConfig(
            @Value('${elasticsearch.index.search.collection.name}')
                    String COLLECTION_SEARCH_INDEX_ALIAS,
            @Value('${elasticsearch.index.staging.collection.name}')
                    String COLLECTION_STAGING_INDEX_ALIAS,
            @Value('${elasticsearch.index.search.granule.name}')
                    String GRANULE_SEARCH_INDEX_ALIAS,
            @Value('${elasticsearch.index.staging.granule.name}')
                    String GRANULE_STAGING_INDEX_ALIAS,
            @Value('${elasticsearch.index.search.flattened-granule.name}')
                    String FLAT_GRANULE_SEARCH_INDEX_ALIAS,
            @Value('${elasticsearch.index.sitemap.name}')
                    String SITEMAP_INDEX_ALIAS,
            @Value('${elasticsearch.index.prefix:}')
                    String PREFIX,
            @Value('${elasticsearch.index.search.collection.pipeline-name}')
                    String COLLECTION_PIPELINE,
            @Value('${elasticsearch.index.search.granule.pipeline-name}')
                    String GRANULE_PIPELINE,
            @Value('${elasticsearch.index.universal-type}')
                    String TYPE,
            @Value('${elasticsearch.max-tasks}')
                    Integer MAX_TASKS,
            @Value('${elasticsearch.requests-per-second:}')
                    Integer REQUESTS_PER_SECOND,
            @Value('${etl.sitemap.scroll-size}')
                    Integer SITEMAP_SCROLL_SIZE,
            @Value('${etl.sitemap.collections-per-submap}')
                    Integer SITEMAP_COLLECTIONS_PER_SUBMAP
    ) {
        this.COLLECTION_SEARCH_INDEX_ALIAS = PREFIX + COLLECTION_SEARCH_INDEX_ALIAS
        this.COLLECTION_STAGING_INDEX_ALIAS = PREFIX + COLLECTION_STAGING_INDEX_ALIAS
        this.GRANULE_SEARCH_INDEX_ALIAS = PREFIX + GRANULE_SEARCH_INDEX_ALIAS
        this.GRANULE_STAGING_INDEX_ALIAS = PREFIX + GRANULE_STAGING_INDEX_ALIAS
        this.FLAT_GRANULE_SEARCH_INDEX_ALIAS = PREFIX + FLAT_GRANULE_SEARCH_INDEX_ALIAS
        this.SITEMAP_INDEX_ALIAS = PREFIX + SITEMAP_INDEX_ALIAS
        this.PREFIX = PREFIX
        this.COLLECTION_PIPELINE = COLLECTION_PIPELINE
        this.GRANULE_PIPELINE = GRANULE_PIPELINE
        this.TYPE = TYPE
        this.MAX_TASKS = MAX_TASKS
        this.REQUESTS_PER_SECOND = REQUESTS_PER_SECOND
        this.SITEMAP_SCROLL_SIZE = SITEMAP_SCROLL_SIZE
        this.SITEMAP_COLLECTIONS_PER_SUBMAP = SITEMAP_COLLECTIONS_PER_SUBMAP

        // Elasticsearch alias names are configurable, and this allows a central mapping between
        // the alias names configured (including the prefix) and the JSON mappings
        // https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html
        this.jsonMappings[this.COLLECTION_SEARCH_INDEX_ALIAS] = jsonFromFile("search_collectionIndex.json")
        this.jsonMappings[this.COLLECTION_STAGING_INDEX_ALIAS] = jsonFromFile("staging_collectionIndex.json")
        this.jsonMappings[this.GRANULE_SEARCH_INDEX_ALIAS] = jsonFromFile("search_granuleIndex.json")
        this.jsonMappings[this.GRANULE_STAGING_INDEX_ALIAS] = jsonFromFile("staging_granuleIndex.json")
        this.jsonMappings[this.FLAT_GRANULE_SEARCH_INDEX_ALIAS] = jsonFromFile("search_flattened_granuleIndex.json")
        this.jsonMappings[this.SITEMAP_INDEX_ALIAS] = jsonFromFile("sitemapIndex.json")

        //
        this.typeMappings[this.COLLECTION_SEARCH_INDEX_ALIAS] = TYPE_COLLECTION
        this.typeMappings[this.COLLECTION_STAGING_INDEX_ALIAS] = TYPE_COLLECTION
        this.typeMappings[this.GRANULE_SEARCH_INDEX_ALIAS] = TYPE_GRANULE
        this.typeMappings[this.GRANULE_STAGING_INDEX_ALIAS] = TYPE_GRANULE
        this.typeMappings[this.FLAT_GRANULE_SEARCH_INDEX_ALIAS] = TYPE_FLATTENED_GRANULE
        this.typeMappings[this.SITEMAP_INDEX_ALIAS] = TYPE_SITEMAP
    }

    static String jsonFromFile(String filename) {
        // return file as JSON string
        ClassLoader classLoader = Thread.currentThread().contextClassLoader
        InputStream fileStream = classLoader.getResourceAsStream(filename)
        if(fileStream) {
            return fileStream.text
        }
        return null
    }

    String jsonMapping(String alias) {
        // retrieve mapping for index alias as JSON string
        return this.jsonMappings[alias]
    }

    static String aliasFromIndex(String index) {
        // determine position of postfix timestamp on index name
        def timestampPosition = index.lastIndexOf('-')
        // derive alias for index by cutting off timestamp postfix
        return timestampPosition > 0 ? index.substring(0, timestampPosition) : index
    }

    String typeFromIndex(String index) {
        // derive alias from index and return type for alias
        String alias = aliasFromIndex(index)
        return typeFromAlias(alias)
    }

    String typeFromAlias(String alias) {
        // retrieve type for index alias
        return this.typeMappings[alias]
    }

    Boolean sitemapEnabled() {
        return environment.activeProfiles.contains('sitemap')
    }
}