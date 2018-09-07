package org.cedar.onestop.api.metadata.service

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Slf4j
@ConditionalOnProperty("features.sitemap")
@Service
class SitemapETLService {

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.collection.name}')
  private String COLLECTION_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.sitemap.name}')
  private String SITEMAP_INDEX

  @Value('${elasticsearch.index.universal-type}')
  String TYPE

  @Value('${etl.sitemap.scroll-size}')
  Integer SITEMAP_SCROLL_SIZE

  @Value('${etl.sitemap.collections-per-submap}')
  Integer SITEMAP_COLLECTIONS_PER_SUBMAP

  @Value('${elasticsearch.max-tasks}')
  private Integer MAX_TASKS

  private ElasticsearchService elasticsearchService

  @Autowired
  SitemapETLService(ElasticsearchService elasticsearchService) {
    this.elasticsearchService = elasticsearchService
  }

  @Async
  public void rebuildSearchIndicesAsync() {
    rebuildSearchIndices()
  }

  @Async
  public void updateSearchIndicesAsync() {
    updateSitemap()
  }

  /**
   * 1) Create a new search index
   * 2) Reindex the entire staging index into it
   * 3) Move the search alias to point to the new index
   * 4) Drop the old search index
   */
  public void rebuildSearchIndices() {
    log.info "Starting sitemap indices rebuilding process"
    def start = System.currentTimeMillis()
    elasticsearchService.ensureIndices()
    elasticsearchService.ensurePipelines()
    def newSitemapIndex = elasticsearchService.create(SITEMAP_INDEX)

    try {
      def sitemapResult = runSitemapEtl(newCollectionSearchIndex, newSitemapIndex)
      elasticsearchService.refresh(newSitemapIndex)
      elasticsearchService.moveAliasToIndex(SITEMAP_INDEX, newSitemapIndex, true)
      def end = System.currentTimeMillis()
      log.info "Indexed ${sitemapResult.updated + sitemapResult.created} of ${sitemapResult.total} sitemap in ${(end - start) / 1000}s"
    }
    catch (Exception e) {
      log.error "Sitemap reindexing failed because of: " + ExceptionUtils.getRootCauseMessage(e)
      log.error "Root cause stack trace: \n" + ExceptionUtils.getRootCauseStackTrace(e)
      elasticsearchService.drop(newSitemapIndex)
    }
  }

  @Scheduled(initialDelayString='${etl.sitemap.delay.initial}', fixedDelayString = '${etl.sitemap.delay.fixed}')
  public void updateSitemap() {
    log.info("starting sitemap update process")
    def start = System.currentTimeMillis()
    def newSitemapIndex =  elasticsearchService.create(SITEMAP_INDEX)
    def sitemapResult = runSitemapEtl(COLLECTION_SEARCH_INDEX, newSitemapIndex)
    elasticsearchService.moveAliasToIndex(SITEMAP_INDEX, newSitemapIndex, true)
    def end = System.currentTimeMillis()
    log.info "Sitemap updated with ${sitemapResult.updated} and created ${sitemapResult.created} in ${(end-start) / 1000}s"
  }

  private Map runSitemapEtl(String collectionIndex, String destination ) {
    elasticsearchService.ensureSearchIndices()
    elasticsearchService.refresh(collectionIndex, destination)

    def collections = []
    def lastSubcollection = []
    def collectionsTotal
    def currentCount

    def nextScrollRequest = { String scroll_id ->
      def requestBody = [
      scroll: '1m',
      scroll_id: scroll_id
      ]
      return elasticsearchService.performRequest('POST', '_search/scroll', requestBody)
    }

    def collectionIdRequestBody = [
      _source: false,
      sort: "_doc", // Non-scoring query, so this will be most efficient ordering
      size: SITEMAP_SCROLL_SIZE
      ]

    def collectionResponse = elasticsearchService.performRequest('POST', "${collectionIndex}/_search?scroll=1m", collectionIdRequestBody)

    def scrollId = collectionResponse._scroll_id

    collectionsTotal = collectionResponse.hits.total
    currentCount = collectionResponse.hits.hits*._id.size()
    lastSubcollection.addAll(collectionResponse.hits.hits*._id)

    while(currentCount < collectionsTotal) {
      def scrollResponse = nextScrollRequest(scrollId)
      scrollId = scrollResponse._scroll_id

      currentCount += collectionResponse.hits.hits*._id.size()
      if(lastSubcollection.size() < SITEMAP_COLLECTIONS_PER_SUBMAP) {
        lastSubcollection.addAll(scrollResponse.hits.hits*._id)
      } else {
        collections.add(lastSubcollection)
        lastSubcollection = []
        lastSubcollection.addAll(scrollResponse.hits.hits*._id)
      }
    }
    if(lastSubcollection.size() > 0) {
      collections.add(lastSubcollection)
    }

    elasticsearchService.performRequest('DELETE', "_search/scroll/${scrollId}")

    def tasksInFlight = []
    def countSitemappedThings = [
        total: 0,
        updated: 0,
        created: 0
   ]

    collections.each { subcollection ->
      while(tasksInFlight.size() == MAX_TASKS) {
        tasksInFlight.removeAll { taskId ->
          def status = checkTask(taskId)
          if(status.completed) {
            countSitemappedThings.total += status.totalDocs
            countSitemappedThings.updated += status.updated
            countSitemappedThings.created += status.created
            deleteTask(taskId)
          }
          return status.completed
        }
        sleep(1000)
      }

      def task = etlSitemapTask(collectionIndex, destination, subcollection)
      if (task) {
        tasksInFlight << task
      }
    }

    while(tasksInFlight.size() >0) {
      tasksInFlight.removeAll { taskId ->
        def status = checkTask(taskId)
        if(status.completed) {

          countSitemappedThings.total += status.totalDocs
          countSitemappedThings.updated += status.updated
          countSitemappedThings.created += status.created
          deleteTask(taskId)
        }
        return status.completed
      }
      sleep(1000)

    }
    return countSitemappedThings
  }

  private String etlSitemapTask(String collectionIndex, String dest, def collections) {
    log.info "etl sitemap task: ${collections}"

     String endpoint = "${dest}/${TYPE}/"
     def body = [
       lastUpdatedDate: System.currentTimeMillis(),
       content: collections
     ]
    def taskId = elasticsearchService.performRequest('POST', endpoint, body).task
    log.debug("Task [ ${taskId} ] started for indexing of flattened granules")
    return taskId
  }

  // TODO refactor out common helpers
  private boolean deleteTask(String taskId) {
    def result = elasticsearchService.performRequest('DELETE', ".tasks/task/${taskId}")
    log.debug("Deleted task [ ${taskId} ]: ${result.found}")
    return result.found
  }

  private Map checkTask(String taskId) {
    def result = elasticsearchService.performRequest('GET', "_tasks/${taskId}")
    def completed = result.completed
    return [
        completed: completed,
        totalDocs: result.task.status.total,
        updated: result.task.status.updated,
        created: result.task.status.created,
        took: completed ? result.response.took : null,
        failures: completed ? result.response.failures : []
    ]
  }

}
