package org.cedar.onestop.api.admin.service

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import static org.cedar.onestop.elastic.common.DocumentUtil.*

@Slf4j
@Service
class ETLService {

  private ElasticsearchService elasticsearchService
  private MetadataManagementService metadataManagementService
  private ElasticsearchConfig esConfig

  @Autowired
  ETLService(ElasticsearchService elasticsearchService, MetadataManagementService metadataManagementService) {
    this.elasticsearchService = elasticsearchService
    this.metadataManagementService = metadataManagementService
    this.esConfig = elasticsearchService.esConfig
  }

  @Async
  void rebuildSearchIndicesAsync() {
    rebuildSearchIndices()
  }

  @Async
  void updateSearchIndicesAsync() {
    updateSearchIndices()
  }

  /**
   * 1) Create a new search index
   * 2) Reindex the entire staging index into it
   * 3) Move the search alias to point to the new index
   * 4) Drop the old search index
   */
  void rebuildSearchIndices() {
    log.info "Starting search indices rebuilding process"
    def start = System.currentTimeMillis()
    elasticsearchService.ensureIndices()
    elasticsearchService.ensurePipelines()
    elasticsearchService.refresh(esConfig.COLLECTION_STAGING_INDEX_ALIAS, esConfig.GRANULE_STAGING_INDEX_ALIAS)
    def newCollectionSearchIndex = elasticsearchService.createIndex(esConfig.COLLECTION_SEARCH_INDEX_ALIAS)
    def newGranuleSearchIndex = elasticsearchService.createIndex(esConfig.GRANULE_SEARCH_INDEX_ALIAS)
    def newFlatGranuleSearchIndex = elasticsearchService.createIndex(esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)

    try {
      def etlResult = runETL(esConfig.COLLECTION_STAGING_INDEX_ALIAS, newCollectionSearchIndex, esConfig.GRANULE_STAGING_INDEX_ALIAS, newGranuleSearchIndex)
      def flattenResult = runFlatteningETL(newCollectionSearchIndex, newGranuleSearchIndex, newFlatGranuleSearchIndex)
      elasticsearchService.refresh(newCollectionSearchIndex, newGranuleSearchIndex, newFlatGranuleSearchIndex)
      elasticsearchService.moveAliasToIndex(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, newCollectionSearchIndex, true)
      elasticsearchService.moveAliasToIndex(esConfig.GRANULE_SEARCH_INDEX_ALIAS, newGranuleSearchIndex, true)
      elasticsearchService.moveAliasToIndex(esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS, newFlatGranuleSearchIndex, true)
      def end = System.currentTimeMillis()
      log.info "Indexed ${etlResult.updatedCollections + etlResult.createdCollections} of ${etlResult.totalCollectionsInRequest} requested collections, " +
          "${etlResult.updatedGranules + etlResult.createdGranules} of ${etlResult.totalGranulesInRequest} requested granules, and flattened " +
          "${flattenResult.total} granules in ${(end - start) / 1000}s"
    }
    catch (Exception e) {
      log.error "Search reindexing failed because of: " + ExceptionUtils.getRootCauseMessage(e)
      log.error "Root cause stack trace: \n" + ExceptionUtils.getRootCauseStackTrace(e)
      elasticsearchService.dropIndex(newCollectionSearchIndex)
      elasticsearchService.dropIndex(newGranuleSearchIndex)
      elasticsearchService.dropIndex(newFlatGranuleSearchIndex)
    }
  }

  @Scheduled(initialDelay = 60000L, fixedDelay = 600000L) // 1 minute after startup then every 10 minutes after previous run ends
  void updateSearchIndices() {
    // Update collections & granules
    log.info("Starting search indices update process")
    def start = System.currentTimeMillis()
    def etlResult =  runETL(esConfig.COLLECTION_STAGING_INDEX_ALIAS, esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_STAGING_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS)
    def end = System.currentTimeMillis()
    log.info "Reindexed ${etlResult.updatedCollections + etlResult.createdCollections} of ${etlResult.totalCollectionsInRequest} requested collections and " +
        "${etlResult.updatedGranules + etlResult.createdGranules} of ${etlResult.totalGranulesInRequest} requested granules in ${(end - start) / 1000}s"

    // Update flattened granules
    log.info("Starting flattened granules search index update process")
    start = System.currentTimeMillis()
    def flatResult = runFlatteningETL(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
    end = System.currentTimeMillis()
    log.info "Updated ${flatResult.updated} and created ${flatResult.created} flattened granules in ${(end - start) / 1000}s"
  }

  /**
   * 1) Identify collections with updates in the staging_collection index which aren't yet represented in the search_collection index
   * 2) Reindex those collections
   * 3) Identify granules with updates in the staging_granule index which aren't yet represented in the search_granule index
   * 4) Find ES ids of associated collections for those granules
   * 5) Reindex those granules with internalParentIdentifier
   */
  private Map runETL(String sourceCollection, String destCollection, String sourceGranule, String destGranule) {
    elasticsearchService.ensureIndices()
    elasticsearchService.ensurePipelines()
    elasticsearchService.refresh(sourceCollection, sourceGranule, destCollection, destGranule)

    // Identify & reindex new/updated collections from staging -> search
    def maxCollectionStagedDate = getMaxSearchStagedMillis(destCollection)
    def collectionTask = etlCollections(sourceCollection, destCollection, maxCollectionStagedDate)

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
                    field: "parentIdentifier",
                    size : Integer.MAX_VALUE
                ]
            ]
        ]
    ]

    log.debug("Running ETL from ${sourceCollection} to ${destCollection} & from ${sourceGranule} to ${destGranule}")
    log.debug("maxCollectionStagedDate: ${maxCollectionStagedDate.toString()}")
    log.debug("maxGranuleStagedDate: ${maxGranuleStagedDate.toString()}")

    def parentIdsResponse = elasticsearchService.performRequest('GET', "${esConfig.GRANULE_STAGING_INDEX_ALIAS}/_search", findParentIdsQuery)
    List<Map> collectionBuckets = getCollectionBuckets(parentIdsResponse)
    List<String> parentIds = collectionBuckets.collect { it.key as String }

    log.trace("parentIds: [ ${parentIds.join(", ")} ]")

    def countGranules = [
        total: 0,
        updated: 0,
        created: 0
    ]
    def granuleTasksInFlight = []
    parentIds.each { parentId ->
      // parentId could be a collection's: fileIdentifier, doi, or id; since GET ES request is less taxing than a search
      // query, attempt to locate by id first
      def foundInternalParentId
      List<Map> internalIdData = metadataManagementService.getMetadata(parentId as String, true).data
      if(internalIdData && internalIdData.size() == 1 && internalIdData[0].type == ElasticsearchConfig.TYPE_COLLECTION) {
        foundInternalParentId = parentId
      }
      else {
        List<Map> externalIdData = metadataManagementService.findMetadata(parentId as String, parentId as String, true).data
        if(externalIdData && externalIdData.size() == 1 && externalIdData[0].type == ElasticsearchConfig.TYPE_COLLECTION) {
          foundInternalParentId = externalIdData[0].id
        }
      }

      // Only push to Search granules which can be definitively linked to a single, existing collection
      if(foundInternalParentId) {
        while(granuleTasksInFlight.size() == esConfig.MAX_TASKS) {
          granuleTasksInFlight.removeAll { taskId ->
            def status = elasticsearchService.checkTask(taskId as String)
            if(status.completed) {
              countGranules.total += status.totalDocs
              countGranules.updated += status.updated
              countGranules.created += status.created
              elasticsearchService.deleteTask(taskId as String)
            }
            return status.completed
          }
          log.debug("waiting 1 second for granule task to complete")
          sleep(1000)
        }

        def granuleTask = etlGranules(sourceGranule, destGranule, parentId as String, foundInternalParentId, maxGranuleStagedDate)
        granuleTasksInFlight << granuleTask
      }
    }

    // Wait for any remaining tasks to complete
    while(granuleTasksInFlight.size() > 0) {
      granuleTasksInFlight.removeAll { taskId ->
         def status = elasticsearchService.checkTask(taskId as String)
         if(status.completed) {
          countGranules.total += status.totalDocs
          countGranules.updated += status.updated
          countGranules.created += status.created
          elasticsearchService.deleteTask(taskId as String)
        }
        return status.completed
      }
      log.debug("waiting 1 second for remaining granule task to complete")
      sleep(1000)
    }

    def collectionTaskStatus = elasticsearchService.checkTask(collectionTask)
    while(!collectionTaskStatus.completed) {
      // Polling until the task completes
      log.debug("waiting 1 second for collection task to complete")
      sleep(1000)
      collectionTaskStatus = elasticsearchService.checkTask(collectionTask)
    }
    elasticsearchService.deleteTask(collectionTask)

    elasticsearchService.refresh(esConfig.COLLECTION_STAGING_INDEX_ALIAS, esConfig.GRANULE_STAGING_INDEX_ALIAS, esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS)
    return [
        totalCollectionsInRequest: collectionTaskStatus.totalDocs,
        updatedCollections: collectionTaskStatus.updated,
        createdCollections: collectionTaskStatus.created,
        totalGranulesInRequest: countGranules.total,
        updatedGranules: countGranules.updated,
        createdGranules: countGranules.created
    ]
  }

  private Map runFlatteningETL(String collectionIndex, String source, String destination) {
    elasticsearchService.ensureSearchIndices()
    elasticsearchService.refresh(collectionIndex, source, destination)

    def wholeCollections
    def wholeCollectionsTotal
    def strayGranuleCollections
    def maxFlatGranulesStagedDate = getMaxSearchStagedMillis(destination)

    def nextScrollRequest = { String scroll_id ->
      def requestBody = [
          scroll: '1m',
          scroll_id: scroll_id
      ]
      return elasticsearchService.performRequest('POST', '_search/scroll', requestBody)
    }

    // Get all collections newer than maxFlatGranulesStagedDate
    def collectionIdRequestBody = [
        _source: false,
        sort: "_doc", // Non-scoring query, so this will be most efficient ordering
        size: 500,
        query: [
            bool: [
                filter: [
                    range: [
                        stagedDate: [
                            gt: maxFlatGranulesStagedDate
                        ]
                    ]
                ]
            ]
        ]
    ]
    def collectionResponse = elasticsearchService.performRequest('POST', "${collectionIndex}/_search?scroll=1m", collectionIdRequestBody)
    def scrollId = collectionResponse._scroll_id
    wholeCollectionsTotal = collectionResponse.hits.total
    wholeCollections = collectionResponse.hits.hits*._id

    while (wholeCollections.size() < wholeCollectionsTotal) {
      def scrollResponse = nextScrollRequest(scrollId as String)
      scrollId = scrollResponse._scroll_id
      wholeCollections.addAll(scrollResponse.hits.hits*._id)
    }

    // Close the search context & free resources
    elasticsearchService.performRequest('DELETE', "_search/scroll/${scrollId}")

    // Get all collections not already found (if any) from stray granules
    def boolQuery = [
        must: [
            [range: [stagedDate: [gt: maxFlatGranulesStagedDate]]]
        ]
    ]
    if(wholeCollections) {
        boolQuery << [must_not: [terms: [internalParentIdentifier: wholeCollections]]]

    }
    def strayCollectionIdRequestBody = [
        size: 0,
        query: [
            bool: boolQuery
        ],
        aggregations: [
            internalParentIdentifiers: [
                terms: [
                    field: "internalParentIdentifier",
                    size: Integer.MAX_VALUE
                ]
            ]
        ]
    ]
    def granuleResponse = elasticsearchService.performRequest('POST', "${source}/_search", strayCollectionIdRequestBody)
    strayGranuleCollections = granuleResponse.aggregations.internalParentIdentifiers.buckets*.key

    // Iterate through all gathered collections for flattening
    def tasksInFlight = []
    def countFlatGranules = [
        total: 0,
        updated: 0,
        created: 0
    ]

    wholeCollections.each { id ->
      while(tasksInFlight.size() == esConfig.MAX_TASKS) {
        tasksInFlight.removeAll { taskId ->
          def status = elasticsearchService.checkTask(taskId as String)
          if(status.completed) {
            countFlatGranules.total += status.totalDocs
            countFlatGranules.updated += status.updated
            countFlatGranules.created += status.created
            elasticsearchService.deleteTask(taskId as String)
          }
          return status.completed
        }
        sleep(1000)
      }

      def flattenTask = etlFlattenedGranules(collectionIndex, source, destination, id as String) // Flattening ALL granules (collection has changed)
      if(flattenTask) { // Could be null if collection wasn't found in the search index for some reason
        tasksInFlight << flattenTask
      }
    }

    strayGranuleCollections.each { id ->
      while(tasksInFlight.size() == esConfig.MAX_TASKS) {
        tasksInFlight.removeAll { taskId ->
          def status = elasticsearchService.checkTask(taskId as String)
          if(status.completed) {
            countFlatGranules.total += status.totalDocs
            countFlatGranules.updated += status.updated
            countFlatGranules.created += status.created
            elasticsearchService.deleteTask(taskId as String)
          }
          return status.completed
        }
        sleep(1000)
      }

      def flattenTask = etlFlattenedGranules(collectionIndex, source, destination, id as String, maxFlatGranulesStagedDate) // Only flatten new/updated granules
      if(flattenTask) { // Could be null if collection wasn't found in the search index for some reason
        tasksInFlight << flattenTask
      }
    }

    // Wait for any remaining tasks to complete
    while(tasksInFlight.size() > 0) {
      tasksInFlight.removeAll { taskId ->
        def status = elasticsearchService.checkTask(taskId as String)
        if(status.completed) {
          countFlatGranules.total += status.totalDocs
          countFlatGranules.updated += status.updated
          countFlatGranules.created += status.created
          elasticsearchService.deleteTask(taskId as String)
        }
        return status.completed
      }
      sleep(1000)
    }

    return countFlatGranules
  }

  /**
   * @param sourceIndex
   * @param destIndex
   * @param stagedAfter (optional) Only reindex collections staged after this date, in millis since the epoch
   * @return ES Task API task ID for this reindex task
   */
  private String etlCollections(String sourceIndex, String destIndex, Long stagedAfter = 0) {
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
            pipeline: esConfig.COLLECTION_PIPELINE
        ]
    ]

    def endpoint = "_reindex?wait_for_completion=false${esConfig.REQUESTS_PER_SECOND ? ";requests_per_second=${esConfig.REQUESTS_PER_SECOND}" : ""}"
    def taskId = elasticsearchService.performRequest('POST', endpoint, reindexBody).task
    log.debug("Task [ ${taskId} ] started for indexing of collections")
    return taskId
  }

  /**
   * @param sourceIndex
   * @param destIndex
   * @param parentIds parentIdentifier identified in granule records
   * @param internalParentId ES id of associated collection
   * @param stagedAfter (optional) Only reindex granules staged after this date, in millis since the epoch
   * @return ES Task API task ID for this reindex task
   */
  private String etlGranules(String sourceIndex, String destIndex, String parentIdentifier, String internalParentId, Long stagedAfter = 0) {
    log.debug("Starting granule indexing of collection ${internalParentId}")

    def reindexScript = "ctx._source.internalParentIdentifier = params.internalParentId"
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
            pipeline: esConfig.GRANULE_PIPELINE
        ],
        script: [
            lang: "painless",
            source: reindexScript,
            params: [internalParentId: internalParentId]
        ]
    ]

    def endpoint = "_reindex?wait_for_completion=false${esConfig.REQUESTS_PER_SECOND ? ";requests_per_second=${esConfig.REQUESTS_PER_SECOND}" : ""}"
    def taskId = elasticsearchService.performRequest('POST', endpoint, reindexBody).task
    log.debug("Task [ ${taskId} ] started for indexing of granules")
    return taskId
  }

  private String etlFlattenedGranules(String collectionIndex, String source, String dest, String internalParentIdentifier, Long stagedAfter = 0) {
    log.debug("Starting flattened-granule indexing of collection [ ${internalParentIdentifier} ]")

    String collectionEndpoint = "${collectionIndex}/${esConfig.TYPE}/${internalParentIdentifier}"
    def collectionResponse = elasticsearchService.performRequest('GET', collectionEndpoint)
    def collectionBody
    if(collectionResponse.found) {
      collectionBody = collectionResponse._source
      // Notes: Can't use Math.max because it returns a double. Also, painless has strange behavior with if/elseif --
      //        need to include last else or will run into obscured errors
      def reindexScript = """\
        for (String f : params.defaults.keySet()) {
          if (f == params.stagedDate) {
            def collectionDate = params.defaults[f];
            def granuleDate = ctx._source[f];
            if (collectionDate > granuleDate) {
              ctx._source[f] = collectionDate;
            }
            else {
              ctx._source[f] = granuleDate;
            }
          }
          else if (ctx._source[f] == null || ctx._source[f] == []) {
            ctx._source[f] = params.defaults[f];
          }
          else {
            ctx._source[f] = ctx._source[f];
          }
        }""".replaceAll(/\s+/, ' ')
      def reindexBody = [
          conflicts: "proceed",
          source: [
              index: source,
              query: [
                  bool: [
                      filter: [
                          [term: [internalParentIdentifier: internalParentIdentifier]],
                          [range: [stagedDate: [gt: stagedAfter]]]
                      ]
                  ]
              ]
          ],
          dest: [
              index: dest
          ],
          script: [
              lang: "painless",
              source: reindexScript,
              params: [defaults: collectionBody, stagedDate: 'stagedDate']
          ]
      ]

      String endpoint = "_reindex?wait_for_completion=false${esConfig.REQUESTS_PER_SECOND ? ";requests_per_second=${esConfig.REQUESTS_PER_SECOND}" : ""}"
      def taskId = elasticsearchService.performRequest('POST', endpoint, reindexBody).task
      log.debug("Task [ ${taskId} ] started for indexing of flattened granules")
      return taskId
    }
    else {
      log.error("Collection ${internalParentIdentifier} not found in search index. No flattened granules indexed for this collection.")
      return null
    }
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
    Map response = elasticsearchService.performRequest('GET', endpoint, body)
    return getHitsTotal(response) == 0 ? 0 : getMaxStagedDateValue(response)
  }

}
