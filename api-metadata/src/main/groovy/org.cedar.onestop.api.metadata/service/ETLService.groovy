package org.cedar.onestop.api.metadata.service

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

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.collection.name}')
  private String COLLECTION_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.collection.name}')
  private String COLLECTION_STAGING_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.granule.name}')
  private String GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.granule.name}')
  private String GRANULE_STAGING_INDEX

  @Value('${elasticsearch.index.search.collection.pipeline-name}')
  private String COLLECTION_PIPELINE

  @Value('${elasticsearch.index.search.granule.pipeline-name}')
  private String GRANULE_PIPELINE


  private ElasticsearchService elasticsearchService
  private MetadataManagementService metadataManagementService

  @Autowired
  ETLService(ElasticsearchService elasticsearchService, MetadataManagementService metadataManagementService) {
    this.elasticsearchService = elasticsearchService
    this.metadataManagementService = metadataManagementService
  }

  @Async
  public void rebuildSearchIndicesAsync() {
    rebuildSearchIndices()
  }

  @Async
  public void updateSearchIndicesAsync() {
    updateSearchIndices()
  }

  /**
   * 1) Create a new search index
   * 2) Reindex the entire staging index into it
   * 3) Move the search alias to point to the new index
   * 4) Drop the old search index
   */
  public void rebuildSearchIndices() {
    log.info "Starting search indices rebuilding process"
    def start = System.currentTimeMillis()
    elasticsearchService.ensureIndices()
    elasticsearchService.ensurePipelines()
    elasticsearchService.refresh(COLLECTION_STAGING_INDEX, GRANULE_STAGING_INDEX)
    def newCollectionSearchIndex = elasticsearchService.create(COLLECTION_SEARCH_INDEX)
    def newGranuleSearchIndex = elasticsearchService.create(GRANULE_SEARCH_INDEX)

    try {
      def etlResult = runETL(COLLECTION_STAGING_INDEX, newCollectionSearchIndex, GRANULE_STAGING_INDEX, newGranuleSearchIndex)
      elasticsearchService.refresh(newCollectionSearchIndex, newGranuleSearchIndex)
      elasticsearchService.moveAliasToIndex(COLLECTION_SEARCH_INDEX, newCollectionSearchIndex, true)
      elasticsearchService.moveAliasToIndex(GRANULE_SEARCH_INDEX, newGranuleSearchIndex, true)
      def end = System.currentTimeMillis()
      log.info "Reindexed ${etlResult.collections} collection and ${etlResult.granules} granule records in ${(end - start) / 1000}s"
    }
    catch (Exception e) {
      log.error "Search reindexing failed because of: " + ExceptionUtils.getRootCauseMessage(e)
      log.error "Root cause stack trace: \n" + ExceptionUtils.getRootCauseStackTrace(e)
      elasticsearchService.drop(newCollectionSearchIndex)
      elasticsearchService.drop(newGranuleSearchIndex)
    }
  }

  @Scheduled(initialDelay = 60000L, fixedDelay = 600000L) // 1 minute after startup then every 10 minutes after previous run ends
  public Map updateSearchIndices() {
    log.info("Starting search indices update process")
    def startTime = System.currentTimeMillis()
    def result =  runETL(COLLECTION_STAGING_INDEX, COLLECTION_SEARCH_INDEX, GRANULE_STAGING_INDEX, GRANULE_SEARCH_INDEX)
    def endTime = System.currentTimeMillis()
    log.info "Reindexed ${result.collections} collection and ${result.granules} granule records in ${(endTime - startTime) / 1000}s"
  }

  /**
   * 1) Identify collections with updates in the staging_collection index which aren't yet represented in the search_collection index
   * 2) Reindex those collections
   * 3) Identify granules with updates in the staging_granule index which aren't yet represented in the search_granule index
   * 4) Find ES ids of associated collections for those granules
   * 5) Reindex those granules with internalParentIdentifier
   */
  private Map runETL(String sourceCollection, String destCollection, String sourceGranule, String destGranule) {
    def countCollections = 0
    def countGranules = 0
    elasticsearchService.ensureIndices()
    elasticsearchService.ensurePipelines()
    elasticsearchService.refresh(sourceCollection, sourceGranule, destCollection, destGranule)

    // Identify & reindex new/updated collections from staging -> search
    def maxCollectionStagedDate = getMaxSearchStagedMillis(destCollection)
    countCollections = etlCollections(sourceCollection, destCollection, maxCollectionStagedDate)

    // Get parent ids for new/updated granules. Append collection id to granules on their way from staging -> search
    def maxGranuleStagedDate = getMaxSearchStagedMillis(destGranule)
    def findParentIdsQuery = [
        size: 0,
        query: [
            bool: [
                filter: [
                    [range: [stagedDate: [gt: maxGranuleStagedDate]]]
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
    def parentIdsResponse = elasticsearchService.performRequest('GET', "$GRANULE_STAGING_INDEX/_search", findParentIdsQuery)
    def parentIds = parentIdsResponse.aggregations?.collections?.buckets*.key
    parentIds.each { parentId ->
      def internalIdResponse = metadataManagementService.findMetadata(parentId, parentId, true)
      // Only push to Search granules which can be definitively linked to a single, existing collection
      if(internalIdResponse.data && internalIdResponse.data.size() == 1 && internalIdResponse.data[0].type == 'collection') {
        countGranules += etlGranules(sourceGranule, destGranule, parentId, internalIdResponse.data[0].id, maxGranuleStagedDate)
      }
    }

    elasticsearchService.refresh(COLLECTION_STAGING_INDEX, GRANULE_STAGING_INDEX, COLLECTION_SEARCH_INDEX, GRANULE_SEARCH_INDEX)
    return [
        collections: countCollections,
        granules: countGranules
    ]
  }

  /**
   * @param sourceIndex
   * @param destIndex
   * @param stagedAfter (optional) Only reindex collections staged after this date, in millis since the epoch
   * @return Number of collections reindexed (updated + created)
   */
  private Long etlCollections(String sourceIndex, String destIndex, Long stagedAfter = 0) {
    log.debug("Starting indexing of collections")

    def reindexBody = [
        conflicts: "proceed",
        source: [
            index: sourceIndex,
            query: [
                bool: [
                    filter: [
                        [range: [stagedDate: [gt: stagedAfter]]]
                    ]
                ]
            ]
        ],
        dest: [
            index: destIndex,
            pipeline: COLLECTION_PIPELINE
        ]
    ]

    def results = elasticsearchService.performRequest('POST', '_reindex', reindexBody)
    log.debug("Updated ${results.updated} and created ${results.created} collections in search index. \nFailures: ${results.failures}. \nTook ${results.took} ms.")
    return results.updated + results.created
  }

  /**
   * @param sourceIndex
   * @param destIndex
   * @param parentIds parentIdentifier identified in granule records
   * @param internalParentId ES id of associated collection
   * @param stagedAfter (optional) Only reindex granules staged after this date, in millis since the epoch
   * @return Number of granules reindexed (updated + created)
   */
  private Long etlGranules(String sourceIndex, String destIndex, String parentIdentifier, String internalParentId, Long stagedAfter = 0) {
    log.debug("Starting granule indexing of collection ${internalParentId}")

    def reindexScript = "ctx._source['internalParentIdentifier'] = params.internalParentId;"
    def reindexBody = [
        conflicts: "proceed",
        source: [
            index: sourceIndex,
            query: [
                bool: [
                    filter: [
                        [term: [parentIdentifier: parentIdentifier]],
                        [range: [stagedDate: [gt: stagedAfter]]]
                    ]
                ]
            ]
        ],
        dest: [
            index: destIndex,
            pipeline: GRANULE_PIPELINE
        ],
        script: [
            lang: "painless",
            params: [internalParentId: internalParentId],
            inline: reindexScript
        ]
    ]

    def results = elasticsearchService.performRequest('POST', '_reindex', reindexBody)
    log.debug("Updated ${results.updated} and created ${results.created} granules in search index. \nFailures: ${results.failures}. \nTook ${results.took} ms.")
    return results.updated + results.created
  }

  /**
   * Returns the max value of the stagedDate field in the specified search index
   * If the search index is empty, returns 0
   * Note: This software was written after the epoch, so this should be pretty safe.
   */
  private Long getMaxSearchStagedMillis(String index) {
    String endpoint = "$index/_search"
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
