package org.cedar.onestop.api.admin.service

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import static org.cedar.onestop.elastic.common.DocumentUtil.*

@Slf4j
@Profile("sitemap")
@Service
class SitemapETLService {

  private ElasticsearchService elasticsearchService
  private ElasticsearchConfig esConfig

  @Autowired
  SitemapETLService(ElasticsearchService elasticsearchService) {
    this.elasticsearchService = elasticsearchService
    this.esConfig = elasticsearchService.esConfig
  }

  @Async
  void rebuildSearchIndicesAsync() {
    rebuildSearchIndices()
  }

  @Async
  void updateSearchIndicesAsync() {
    updateSitemap()
  }

  /**
   * 1) Create a new search index
   * 2) Reindex the entire staging index into it
   * 3) Move the search alias to point to the new index
   * 4) Drop the old search index
   */
  void rebuildSearchIndices() {
    log.info "Starting sitemap indices rebuilding process"
    def start = System.currentTimeMillis()
    elasticsearchService.ensureIndices()
    elasticsearchService.ensurePipelines()
    def newSitemapIndex = elasticsearchService.createIndex(esConfig.SITEMAP_INDEX_ALIAS)

    try {
      def sitemapResult = runSitemapEtl(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, newSitemapIndex)
      elasticsearchService.refresh(newSitemapIndex)
      elasticsearchService.moveAliasToIndex(esConfig.SITEMAP_INDEX_ALIAS, newSitemapIndex, true)
      def end = System.currentTimeMillis()
      log.info "Indexed ${sitemapResult.updated + sitemapResult.created} of ${sitemapResult.total} sitemap in ${(end - start) / 1000}s"
    }
    catch (Exception e) {
      log.error "Sitemap reindexing failed because of: " + ExceptionUtils.getRootCauseMessage(e)
      log.error "Root cause stack trace: \n" + ExceptionUtils.getRootCauseStackTrace(e)
      elasticsearchService.dropIndex(newSitemapIndex)
    }
  }

  @Scheduled(initialDelayString='${etl.sitemap.delay.initial}', fixedDelayString = '${etl.sitemap.delay.fixed}')
  void updateSitemap() {
    log.info("starting sitemap update process")
    def start = System.currentTimeMillis()
    def newSitemapIndex =  elasticsearchService.createIndex(esConfig.SITEMAP_INDEX_ALIAS)
    def sitemapResult = runSitemapEtl(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, newSitemapIndex)
    elasticsearchService.moveAliasToIndex(esConfig.SITEMAP_INDEX_ALIAS, newSitemapIndex, true)
    def end = System.currentTimeMillis()
    log.info "Sitemap updated with ${sitemapResult.updated} and created ${sitemapResult.created} in ${(end-start) / 1000}s"
  }

  private Map runSitemapEtl(String collectionIndex, String sitemapIndex) {
    elasticsearchService.ensureSearchIndices()
    elasticsearchService.refresh(collectionIndex, sitemapIndex)

    List<List<String>> collections = []
    List<String> lastSubcollection = []

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
      size: esConfig.SITEMAP_SCROLL_SIZE
      ]

    def collectionResponse = elasticsearchService.performRequest('POST', "${collectionIndex}/_search?scroll=1m", collectionIdRequestBody)

    String scrollId = getScrollId(collectionResponse)

    int collectionsTotal = getHitsTotal(collectionResponse)
    List<Map> collectionDocuments = getDocuments(collectionResponse)
    List<String> collectionIds = collectionDocuments.collect { getId(it) }
    int collectionCount = collectionDocuments.size()
    int currentCount = collectionCount
    lastSubcollection.addAll(collectionIds)

    while(currentCount < collectionsTotal) {
      Map scrollResponse = nextScrollRequest(scrollId)
      List<Map> scrollDocuments = getDocuments(scrollResponse)
      scrollId = getScrollId(scrollResponse)
      List<String> scrollCollectionIds = scrollDocuments.collect { getId(it) }

      currentCount += collectionCount
      if(lastSubcollection.size() < esConfig.SITEMAP_COLLECTIONS_PER_SUBMAP) {
        lastSubcollection.addAll(scrollCollectionIds)
      } else {
        collections.add(lastSubcollection)
        lastSubcollection = []
        lastSubcollection.addAll(scrollCollectionIds)
      }
    }
    if(lastSubcollection.size() > 0) {
      collections.add(lastSubcollection)
    }

    elasticsearchService.performRequest('DELETE', "_search/scroll/${scrollId}")

    List<String> tasksInFlight = []
    Map countSitemappedThings = [
        total: 0,
        updated: 0,
        created: 0
   ]

    collections.each { subcollection ->
      while(tasksInFlight.size() == esConfig.MAX_TASKS) {
        tasksInFlight.removeAll { taskId ->
          def status = elasticsearchService.checkTask(taskId)
          if(status.completed) {
            countSitemappedThings.total += status.totalDocs
            countSitemappedThings.updated += status.updated
            countSitemappedThings.created += status.created
            elasticsearchService.deleteTask(taskId)
          }
          return status.completed
        }
        sleep(1000)
      }

      def task = etlSitemapTask(sitemapIndex, subcollection)
      if (task) {
        tasksInFlight << task
      }
    }

    while(tasksInFlight.size() > 0) {
      tasksInFlight.removeAll { taskId ->
        def status = elasticsearchService.checkTask(taskId)
        if(status.completed) {

          countSitemappedThings.total += status.totalDocs
          countSitemappedThings.updated += status.updated
          countSitemappedThings.created += status.created
          elasticsearchService.deleteTask(taskId)
        }
        return status.completed
      }
      sleep(1000)

    }
    return countSitemappedThings
  }

  private String etlSitemapTask(String sitemapIndex, List<String> collections) {
    log.info "ETL sitemap task: ${collections}"
     String endpoint = "${sitemapIndex}/${esConfig.TYPE}/"
     def body = [
       lastUpdatedDate: System.currentTimeMillis(),
       content: collections
     ]
    def taskId = elasticsearchService.performRequest('POST', endpoint, body).task
    log.debug("Task [ ${taskId} ] started for indexing of flattened granules")
    return taskId
  }

}
