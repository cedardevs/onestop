package org.cedar.onestop.api.metadata.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Slf4j
@Service
class ETLService {

  private String SCROLL_TIMEOUT = '1m'
  private Integer PAGE_SIZE = 10

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.name}')
  private String SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.name}')
  private String STAGING_INDEX

  @Value('${elasticsearch.index.staging.collectionType}')
  private String COLLECTION_TYPE

  @Value('${elasticsearch.index.staging.granuleType}')
  private String GRANULE_TYPE

  private ElasticsearchService elasticsearchService

  @Autowired
  ETLService(ElasticsearchService elasticsearchService) {
    this.elasticsearchService = elasticsearchService
  }

  @Async
  public void rebuildSearchIndexAsync() {
    rebuildSearchIndex()
  }

  @Async
  public void updateSearchIndexAsync() {
    updateSearchIndex()
  }

  /**
   * 1) Create a new search index
   * 2) Reindex the entire staging index into it
   * 3) Move the search alias to point to the new index
   * 4) Drop the old search index
   */
  public void rebuildSearchIndex() {
    log.info "starting rebuilding process"
    def start = System.currentTimeMillis()
    elasticsearchService.refresh(STAGING_INDEX)
    def newSearchIndex = elasticsearchService.create(SEARCH_INDEX)

    try {
      def count = 0L
      def offset = 0
      def collectionEndpoint = "$STAGING_INDEX/$COLLECTION_TYPE/_search"
      def collectionsCount = elasticsearchService.performRequest('GET', collectionEndpoint, [size: 0]).hits.total

      while (offset < collectionsCount) {
        def collectionSearchBody = [sort: [fileIdentifier: 'asc'], from: offset, size: PAGE_SIZE]
        def collections = elasticsearchService.performRequest('GET', collectionEndpoint, collectionSearchBody).hits.hits
        count += etlCollections(collections, STAGING_INDEX, newSearchIndex)
        offset += PAGE_SIZE
      }
      elasticsearchService.refresh(newSearchIndex)

      // TODO - move alias logic down into elasticsearch service?
      def oldIndices = elasticsearchService.performRequest('GET', "_alias/$SEARCH_INDEX").keySet()*.toString()
      oldIndices = oldIndices.findAll({ it.startsWith(SEARCH_INDEX) })
      def actions = oldIndices.collect { [remove: [index: it, alias: SEARCH_INDEX]] }
      actions << [add: [index: newSearchIndex, alias: SEARCH_INDEX]]
      elasticsearchService.performRequest('POST', '_aliases', [actions: actions])
      oldIndices.each { elasticsearchService.drop(it) }

      def end = System.currentTimeMillis()
      log.info "reindexed ${count} records in ${(end - start) / 1000}s"
    }
    catch (Exception e) {
      log.error "Search reindexing failed because of: " + ExceptionUtils.getRootCauseMessage(e)
      log.error "Root cause stack trace: \n" + ExceptionUtils.getRootCauseStackTrace(e)
      elasticsearchService.drop(newSearchIndex)
    }
  }

  /**
   * 1) Identify documents with updates in the staging index which aren't yet represented in the search index
   * 2) Reindex those documents
   */
  @Scheduled(fixedDelay = 600000L) // 10 minutes after previous run ends
  public void updateSearchIndex() {
    // TODO
  }

  /**
   * Bulk index a list of collections, then reindex each collection's granules
   * @param collections The list of collections to be indexed
   * @param from        The index to pull the collections' granules from
   * @param to          The index to send the collections and granules to
   * @return            The total count of documents that were indexed
   */
  private Long etlCollections(List<Map> collections, String from, String to) {
    def bulkRequest = new StringBuffer()
    collections.each {
      bulkRequest << "{\"index\":{\"_index\":\"${to}\",\"_type\":\"${COLLECTION_TYPE}\",\"_id\":\"${it._id}\"}}"
      bulkRequest << "\n"
      bulkRequest << JsonOutput.toJson(it._source)
      bulkRequest << "\n"
    }
    elasticsearchService.performRequest('POST', '_bulk', bulkRequest.toString())
    def count = collections.size()
    collections.each {
      count += etlGranulesForCollection(it, from, to)
    }
    return count
  }

  /**
   * Reindex the granules belonging to a given collection, inheriting any missing fields from the parent collection
   * @param collection      The full map of the collection the granules belong to
   * @param from            The index to pull the granules from
   * @param to              The index to send the granules to
   * @param stagedDateAfter (optional) Only reindex granules which were staged after this date, in millis since the epoch
   * @return                The number of granules indexed
   */
  private Long etlGranulesForCollection(Map collection, String from, String to, Long stagedDateAfter = 0) {
    log.debug("Starting indexing of collection ${collection._id}")

    def parentIds = [collection._source.fileIdentifier, collection._source.doi].findAll()
    def reindexBody = [
        conflicts: "proceed",
        source: [
            index: from,
            type: GRANULE_TYPE,
            query: [
                bool: [
                    filter: [
                        [terms: [parentIdentifier: parentIds]],
                        [range: [stagedDate: [gte: stagedDateAfter]]]
                    ]
                ]
            ]
        ],
        dest: [
            index: to
        ],
        script: [
            lang: "painless",
            params: [defaults: collection._source],
            inline: reindexScript
        ]
    ]
    def results = elasticsearchService.performRequest('POST', '_reindex', reindexBody)
    def reindexed = results.total as Long
    if (reindexed > 0) {
      return reindexed
    }

    def synthesizedGranule = (collection._source as Map) +
        [fileIdentifier: collection._source.fileIdentifier, parentIdentifier: collection._source.fileIdentifier]
    elasticsearchService.performRequest('POST', "$to/$GRANULE_TYPE", synthesizedGranule)
    return 1
  }

  private static String reindexScript = """\
    for (String f : params.defaults.keySet()) {
      if (ctx._source[f] == null) {
        ctx._source[f] = params.defaults[f]
      }
    }""".replaceAll(/\s+/, ' ')

}
