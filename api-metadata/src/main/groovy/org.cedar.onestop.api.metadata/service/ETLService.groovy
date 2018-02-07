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
    elasticsearchService.ensureIndices()
    elasticsearchService.refresh(STAGING_INDEX)
    def newSearchIndex = elasticsearchService.create(SEARCH_INDEX)

    try {
      def etlResult = etlCollections(STAGING_INDEX, newSearchIndex)
      elasticsearchService.refresh(newSearchIndex)
      elasticsearchService.moveAliasToIndex(SEARCH_INDEX, newSearchIndex, true)
      def end = System.currentTimeMillis()
      log.info "reindexed ${etlResult.total} records in ${(end - start) / 1000}s"
    }
    catch (Exception e) {
      log.error "Search reindexing failed because of: " + ExceptionUtils.getRootCauseMessage(e)
      log.error "Root cause stack trace: \n" + ExceptionUtils.getRootCauseStackTrace(e)
      elasticsearchService.drop(newSearchIndex)
    }
  }

  /**
   * 1) Identify collections with updates in the staging index which aren't yet represented in the search index
   * 2) Reindex those collections and all of their granules
   * 3) Identify collections with granules which:
   *     a) have updates in staging which aren't yet in search
   *     b) have not already been reindexed in (2)
   * 4) Reindex those granules
   */
  @Scheduled(initialDelay = 60000L, fixedDelay = 600000L) // 1 minute after startup then every 10 minutes after previous run ends
  public void updateSearchIndex() {
    log.info "Starting search index update process"
    def start = System.currentTimeMillis()
    elasticsearchService.ensureIndices()
    elasticsearchService.refresh(STAGING_INDEX, SEARCH_INDEX)

    // reindex collections (and their granules) which have been updated
    def maxSearchStagedDate = getMaxSearchStagedMillis()
    def indexedCollections = etlCollections(STAGING_INDEX, SEARCH_INDEX, maxSearchStagedDate)
    def count = indexedCollections.total

    // find collections which have granules which have been updated and have not already been reindexed
    def unindexedCollectionsEndpoint = "$STAGING_INDEX/$GRANULE_TYPE/_search"
    def unindexedCollectionsQuery = [
        size: 0,
        query: [
            bool: [
                must: [
                    [range: [stagedDate: [gt: maxSearchStagedDate]]]
                ],
                must_not: [
                    [terms: [parentIdentifier: indexedCollections.fileIdentifiers + indexedCollections.dois]],
                ]
            ]
        ],
        aggregations: [
            collections: [
                terms: [
                    field: "parentIdentifier"
                ]
            ]
        ]
    ]
    def unindexedCollectionsResponse =
        elasticsearchService.performRequest('GET', unindexedCollectionsEndpoint, unindexedCollectionsQuery)
    def unindexedCollectionIds = unindexedCollectionsResponse.aggregations.collections.buckets*.key
    unindexedCollectionIds.collate(PAGE_SIZE).each { page ->
      def collectionPageEndpoint = "$STAGING_INDEX/$COLLECTION_TYPE/_search"
      def collectionPageBody = [
          query: [
              bool: [
                  should: [
                      [terms: [fileIdentifier: page]],
                      [terms: [doi: page]],
                  ]
              ]
          ]
      ]
      def collectionPageResponse = elasticsearchService.performRequest('GET', collectionPageEndpoint, collectionPageBody)
      collectionPageResponse.hits.hits.each { Map collection ->
        count += etlGranulesForCollection(collection, STAGING_INDEX, SEARCH_INDEX, maxSearchStagedDate)
      }
    }

    elasticsearchService.refresh(STAGING_INDEX, SEARCH_INDEX)
    def end = System.currentTimeMillis()
    log.info "Reindexed ${count} records in ${(end - start) / 1000}s"
  }

  /**
   * Reindex collections and all the granules that belong to them from one index to another,
   * optionally excluding those staged before a given date.
   * @param from        The index to pull the collections' granules from
   * @param to          The index to send the collections and granules to
   * @param stagedAfter (optional) Only reindex collections staged after this date, in millis since the epoch
   * @return            A map shaped like [ids: <list of potential granule parent ids>, total: <total indexed documents>]
   */
  private Map etlCollections(String from, String to, Long stagedAfter = 0) {
    def count = 0
    def offset = 0
    def collectionEndpoint = "$STAGING_INDEX/$COLLECTION_TYPE/_search"
    def fileIdentifiers = []
    def dois = []
    def collectionsCount = null
    while (collectionsCount == null || offset < collectionsCount) {
      def collectionSearchBody = [
          query: [
              bool: [
                  filter: [
                      [range: [stagedDate: [gt: stagedAfter]]]
                  ]
              ]
          ],
          sort: "_uid",
          from: offset,
          size: PAGE_SIZE
      ]
      def collectionsResponse = elasticsearchService.performRequest('GET', collectionEndpoint, collectionSearchBody)
      if (collectionsCount == null) {
        collectionsCount = collectionsResponse.hits.total
      }
      def bulkRequest = new StringBuffer()
      collectionsResponse.hits.hits.each { Map collection ->
        if (collection._source.fileIdentifier) {
          fileIdentifiers << collection._source.fileIdentifier
        }
        if (collection._source.doi) {
          dois << collection._source.doi
        }
        count += etlGranulesForCollection(collection, from, to) // <-- collection changed, so reindex ALL its granules
        bulkRequest << "{\"index\":{\"_index\":\"${to}\",\"_type\":\"${COLLECTION_TYPE}\",\"_id\":\"${collection._id}\"}}"
        bulkRequest << "\n"
        bulkRequest << JsonOutput.toJson(collection._source)
        bulkRequest << "\n"
        count++
      }

      // Don't send empty request -- errors out & not required
      if(bulkRequest) { elasticsearchService.performRequest('POST', '_bulk', bulkRequest.toString()) }
      offset += PAGE_SIZE
    }
    return [fileIdentifiers: fileIdentifiers, dois: dois, total: count]
  }

  /**
   * Reindex the granules belonging to a given collection, inheriting any missing fields from the parent collection
   * @param collection  The full map of the collection the granules belong to
   * @param from        The index to pull the granules from
   * @param to          The index to send the granules to
   * @param stagedAfter (optional) Only reindex granules which were staged after this date, in millis since the epoch
   * @return            The number of granules indexed
   */
  private Long etlGranulesForCollection(Map collection, String from, String to, Long stagedAfter = 0) {
    log.debug("Starting indexing of collection ${collection._id}")

    def reindexScript = """\
        ctx._source['internalParentIdentifier'] = params.parentId;
        for (String f : params.defaults.keySet()) {
          if (ctx._source[f] == null || ctx._source[f] == []) {
            ctx._source[f] = params.defaults[f];
          }
        }""".replaceAll(/\s+/, ' ')
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
                        [range: [stagedDate: [gt: stagedAfter]]]
                    ]
                ]
            ]
        ],
        dest: [
            index: to
        ],
        script: [
            lang: "painless",
            params: [parentId: collection._id, defaults: collection._source],
            inline: reindexScript
        ]
    ]
    def results = elasticsearchService.performRequest('POST', '_reindex', reindexBody)
    def reindexed = results.total as Long
    if (reindexed > 0) {
      return reindexed
    }

    def synthesizedGranule = (collection._source as Map) + [
        fileIdentifier: collection._source.fileIdentifier,
        parentIdentifier: collection._source.fileIdentifier,
        internalParentIdentifier: collection._id
    ]
    elasticsearchService.performRequest('PUT', "$to/$GRANULE_TYPE/$collection._id", synthesizedGranule)
    return 1
  }

  /**
   * Returns the max value of the stagedDate field in the search index
   * If the search index is empty, returns 0
   * Note: This software was written after the epoch, so this should be pretty safe.
   */
  private Long getMaxSearchStagedMillis() {
    def endpoint = "$SEARCH_INDEX/$COLLECTION_TYPE,$GRANULE_TYPE/_search"
    def body = [
        size: 0,
        aggregations: [
            maxStagedDate: [
                max: [
                    field: "stagedDate"
                ]
            ]
        ]
    ]
    def response = elasticsearchService.performRequest('GET', endpoint, body)
    return response.hits.total == 0 ? 0 : response.aggregations.maxStagedDate.value
  }

}
