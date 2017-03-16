package ncei.onestop.api.etl.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.metrics.max.Max
import org.elasticsearch.search.sort.SortOrder
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

  private Client adminClient
  private IndexAdminService indexAdminService

  @Autowired
  ETLService(Client adminClient, IndexAdminService indexAdminService) {
    this.adminClient = adminClient
    this.indexAdminService = indexAdminService
  }

  @Async
  public void rebuildSearchIndexAsync() {
    rebuildSearchIndex()
  }

  @Async
  public void updateSearchIndexAsync() {
    updateSearchIndex()
  }

  public void rebuildSearchIndex() {
    log.info "starting rebuilding process"
    def start = System.currentTimeMillis()
    indexAdminService.refresh(STAGING_INDEX)
    def newSearchIndex = indexAdminService.create(SEARCH_INDEX, [GRANULE_TYPE, COLLECTION_TYPE])

    try {
      def offset = 0
      def bulkIndexer = new BulkIndexer(adminClient, newSearchIndex)
      def collectionsCount = adminClient.prepareSearch(STAGING_INDEX).setTypes(COLLECTION_TYPE).setSize(0)
          .execute().actionGet().hits.totalHits

      while (offset < collectionsCount) {
        def collections = adminClient.prepareSearch(STAGING_INDEX).setTypes(COLLECTION_TYPE)
            .addSort("fileIdentifier", SortOrder.ASC).setFrom(offset).setSize(PAGE_SIZE)
            .execute().actionGet().hits.hits
        collections.each {
          etlCollectionAndGranules(bulkIndexer, it.source)
        }
        offset += PAGE_SIZE
      }

      bulkIndexer.flush()
      indexAdminService.refresh(newSearchIndex)

      def aliasBuilder = adminClient.admin().indices().prepareAliases()
      def oldIndices = adminClient.admin().indices().prepareGetAliases(SEARCH_INDEX).get().aliases.keysIt()*.toString()
      oldIndices.each {
        aliasBuilder.removeAlias(it, SEARCH_INDEX)
      }
      aliasBuilder.addAlias(newSearchIndex, SEARCH_INDEX)
      aliasBuilder.execute().actionGet()
      oldIndices.each { indexAdminService.drop(it) }

      def end = System.currentTimeMillis()
      log.info "reindexed ${bulkIndexer.recordCount} records in ${(end - start) / 1000}s"

    } catch (Exception e) {
      log.error "Search reindexing failed because of: " + ExceptionUtils.getRootCauseMessage(e)
      log.error "Root cause stack trace: \n" + ExceptionUtils.getRootCauseStackTrace(e)
      indexAdminService.drop(newSearchIndex)
    }
  }

  @Scheduled(fixedDelay = 600000L) // 10 minutes after previous run ends
  public void updateSearchIndex() {
    log.info "Starting search index update process"
    def start = System.currentTimeMillis()
    indexAdminService.refresh(STAGING_INDEX, SEARCH_INDEX)

    def offset = 0
    def bulkIndexer = new BulkIndexer(adminClient, SEARCH_INDEX)
    def maxSearchStagedDate = getMaxSearchStagedMillis()

    // Get all collection IDs where stagedDate > maxSearchStagedDate:
    def sr = adminClient.prepareSearch(STAGING_INDEX)
        .setTypes(COLLECTION_TYPE)
        .setSize(0)
        .setQuery(QueryBuilders.rangeQuery('stagedDate').gt(maxSearchStagedDate))
        .addAggregation(AggregationBuilders.terms('collections').field('_uid').size(0))
        .execute().actionGet()
    def collectionIds = sr.aggregations.get('collections').getBuckets().stream().map( {i -> i.keyAsString} ).map( {i -> i.substring(i.indexOf('#') + 1)}).collect()

    // If any collections returned, need to reindex ENTIRE collection:
    if (collectionIds) {
      while (offset < collectionIds.size()) {
        def collectionsToRetrieve = collectionIds.stream().skip(offset).limit(PAGE_SIZE).collect()
        def collectionDocs = adminClient.prepareMultiGet().add(STAGING_INDEX, COLLECTION_TYPE, collectionsToRetrieve).get().responses
        collectionDocs.each {
          etlCollectionAndGranules(bulkIndexer, it.response.source)
        }
        offset += PAGE_SIZE
      }
    }

    // Get any granules that are not part of the already covered collections; gather collection IDs first:
    sr = adminClient.prepareSearch(STAGING_INDEX)
        .setTypes(GRANULE_TYPE)
        .setSize(0)
        .setQuery(QueryBuilders.boolQuery()
          .must(QueryBuilders.rangeQuery('stagedDate').gt(maxSearchStagedDate))
          .mustNot(QueryBuilders.termsQuery('parentIdentifier', collectionIds)))
        .addAggregation(AggregationBuilders.terms('collections').field('parentIdentifier').size(0))
        .execute().actionGet()
    collectionIds = sr.aggregations.get('collections').getBuckets().stream().map( {i -> i.keyAsString} ).collect()

    if (collectionIds) {
      offset = 0 // Reset to zero
      while (offset < collectionIds.size()) {
        def collectionsToRetrieve = collectionIds.stream().skip(offset).limit(PAGE_SIZE).collect()
        def collectionDocs = adminClient.prepareMultiGet().add(STAGING_INDEX, COLLECTION_TYPE, collectionsToRetrieve).get().responses
        collectionDocs.each { collection ->
          if (collection.response.exists) { // Check in case a granule has been inserted without an existing collection
            etlCollectionAndGranules(bulkIndexer, collection.response.source, maxSearchStagedDate)
          }
        }
        offset += PAGE_SIZE
      }
    }

    bulkIndexer.flush()
    indexAdminService.refresh(STAGING_INDEX, SEARCH_INDEX)
    def end = System.currentTimeMillis()
    log.info "Reindexed ${bulkIndexer.recordCount} records in ${(end - start) / 1000}s"

  }

  // Returns the max value of the stagedDate field in the search index
  // If the search index is empty, returns 0
  // Note: This software was written after the epoch, so this should be pretty safe.
  private long getMaxSearchStagedMillis() {
    def searchIndexCount = adminClient
        .prepareSearch(SEARCH_INDEX).setTypes(COLLECTION_TYPE, GRANULE_TYPE).setSize(0)
        .execute().actionGet().hits.totalHits

    if (searchIndexCount == 0L) {
      return 0
    }

    def maxDateSearch = adminClient.prepareSearch(SEARCH_INDEX)
        .setTypes(COLLECTION_TYPE, GRANULE_TYPE)
        .setSize(0)
        .addAggregation(AggregationBuilders.max('maxStagedDate').field('stagedDate'))
        .execute().actionGet()
    def maxDateAgg = maxDateSearch.aggregations.get('maxStagedDate') as Max
    return maxDateAgg.value as long
  }

  private etlCollectionAndGranules(BulkIndexer indexer, Map collection, Long stagedDateAfter = 0) {
    def collectionDoc = collection.findAll { it.key != 'isoXml' }
    log.debug("Starting indexing of collection ${collectionDoc.fileIdentifier}")
    if (collectionDoc.stagedDate > stagedDateAfter) {
      indexer.addRecord(collectionDoc, COLLECTION_TYPE)
    }

    def granuleQuery = QueryBuilders.boolQuery()
        .must(QueryBuilders.termsQuery('parentIdentifier', collectionDoc.fileIdentifier))
        .must(QueryBuilders.rangeQuery('stagedDate').gt(stagedDateAfter))
    def granuleScroll = adminClient.prepareSearch(STAGING_INDEX).setTypes(GRANULE_TYPE).setQuery(granuleQuery)
        .addSort('_doc', SortOrder.ASC).setSize(PAGE_SIZE).setScroll(SCROLL_TIMEOUT)
        .execute().actionGet()

    def granulesRemain = granuleScroll.hits.hits.length > 0
    if (!granulesRemain) { // insert a synthesized granule record if there are no found granules
      log.debug("Inserting synthesized granule for collection ${collectionDoc.fileIdentifier}")
      def synthesizedGranule = [fileIdentifier: collectionDoc.fileIdentifier, parentIdentifier: collectionDoc.fileIdentifier]
      def flattenedSynthesizedRecord = collectionDoc + synthesizedGranule
      indexer.addRecord(flattenedSynthesizedRecord, GRANULE_TYPE)
    }
    while (granulesRemain) {
      granuleScroll.hits.hits.each { granule ->
        def granuleDoc = granule.source
        def flattenedRecord = collectionDoc + granuleDoc
        indexer.addRecord(flattenedRecord, GRANULE_TYPE)
      }
      granuleScroll = adminClient.prepareSearchScroll(granuleScroll.scrollId).setScroll(SCROLL_TIMEOUT).execute().actionGet()
      granulesRemain = granuleScroll.hits.hits.length > 0
    }
    log.debug("Finished indexing for collection ${collectionDoc.fileIdentifier}")
  }

  // a helper class to facilitate bulk indexing
  private static class BulkIndexer {
    private client, index, bulkRequest, recordCount
    public getRecordCount() { recordCount }

    BulkIndexer(Client esClient, String esIndex) {
      client = esClient
      index = esIndex
      bulkRequest = client.prepareBulk()
      recordCount = 0
    }

    def addRecord(record, type) {
      def id = record.fileIdentifier as String
      def json = JsonOutput.toJson(record)
      def insertRequest = client.prepareIndex(index, type, id).setSource(json)
      bulkRequest.add(insertRequest)
      recordCount++
      if (bulkRequest.numberOfActions() >= 1000) {
        bulkRequest.get()
        bulkRequest = client.prepareBulk()
      }
    }

    def flush() {
      if (bulkRequest.numberOfActions() > 0) {
        bulkRequest.get()
      }
    }

  }
}