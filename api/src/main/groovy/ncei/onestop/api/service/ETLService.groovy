package ncei.onestop.api.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Slf4j
@Service
class ETLService {

  @Value('${elasticsearch.index.search.name}')
  private String SEARCH_INDEX

  @Value('${elasticsearch.index.staging.name}')
  private String STAGING_INDEX

  @Value('${elasticsearch.index.staging.collectionType}')
  private String COLLECTION_TYPE

  @Value('${elasticsearch.index.staging.granuleType}')
  private String GRANULE_TYPE

  private Client client
  private IndexAdminService indexAdminService

  @Autowired
  ETLService(Client client, IndexAdminService indexAdminService) {
    this.client = client
    this.indexAdminService = indexAdminService
  }

  @Async
  public void reindexAsync() {
    reindex()
  }

//  @Scheduled(fixedDelay = 600000L) // 10 minutes after previous run ends
  public void reindex() {
    log.info "starting reindex process"
    def start = System.currentTimeMillis()
    indexAdminService.refresh(STAGING_INDEX)
    def newSearchIndex = indexAdminService.create(SEARCH_INDEX, [GRANULE_TYPE, COLLECTION_TYPE])

    try {
      def bulkRequest = client.prepareBulk()
      def recordCount = 0
      def granuleScrollTimeout = '1m'
      def granulePageSize = 10

      def offset = 0
      def increment = 10
      def collectionsCount = client.prepareSearch(STAGING_INDEX).setTypes(COLLECTION_TYPE)
          .setSize(0).execute().actionGet().hits.totalHits

      def addRecordToBulk = { record, type ->
        def id = record.fileIdentifier as String
        def json = JsonOutput.toJson(record)
        def insertRequest = client.prepareIndex(newSearchIndex, type, id).setSource(json)
        bulkRequest.add(insertRequest)
        recordCount++
        if (bulkRequest.numberOfActions() >= 1000) {
          bulkRequest.get()
          bulkRequest = client.prepareBulk()
        }
      }


      while (offset < collectionsCount) {
        def collections = client.prepareSearch(STAGING_INDEX).setTypes(COLLECTION_TYPE)
            .addSort("fileIdentifier", SortOrder.ASC).setFrom(offset).setSize(increment).execute().actionGet().hits.hits
        collections.each { collection ->
          def collectionDoc = collection.source.findAll { it.key != 'isoXml' }
          log.debug('Starting indexing of collection ' + collectionDoc.fileIdentifier) //fixme delete later
          addRecordToBulk(collectionDoc, COLLECTION_TYPE) // Add collections whether they have granules or not
          def granuleScroll = client.prepareSearch(STAGING_INDEX)
              .setTypes(GRANULE_TYPE)
              .addSort('fileIdentifier', SortOrder.ASC)
              .setScroll(granuleScrollTimeout)
              .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery('parentIdentifier', collectionDoc.fileIdentifier)))
              .setSize(granulePageSize)
              .execute()
              .actionGet()
          def granulesRemain = granuleScroll.hits.hits.length > 0
          if (!granulesRemain) { // insert a synthesized granule record if there are no found granules
            log.debug('Inserting synthesized granule for collection ' + collectionDoc.fileIdentifier) // fixme delete later
            def synthesizedGranule = [fileIdentifier: collectionDoc.fileIdentifier, parentIdentifier: collectionDoc.fileIdentifier]
            def flattenedSynthesizedRecord = MetadataParser.mergeCollectionAndGranule(collectionDoc, synthesizedGranule)
            addRecordToBulk(flattenedSynthesizedRecord, GRANULE_TYPE)
          }

          while (granulesRemain) {
            granuleScroll.hits.hits.each { granule ->
              def granuleDoc = granule.source
              def flattenedRecord = MetadataParser.mergeCollectionAndGranule(collectionDoc, granuleDoc)
              addRecordToBulk(flattenedRecord, GRANULE_TYPE)
            }
            granuleScroll = client.prepareSearchScroll(granuleScroll.scrollId).setScroll(granuleScrollTimeout).execute().actionGet()
            granulesRemain = granuleScroll.hits.hits.length > 0
          }
          log.debug('Finished indexing for collection ' + collectionDoc.fileIdentifier) // fixme delete later
        }

        offset += increment
      }

      if (bulkRequest.numberOfActions() > 0) {
        bulkRequest.get()
      }

      indexAdminService.refresh(newSearchIndex)

      def aliasBuilder = client.admin().indices().prepareAliases()
      def oldIndices = client.admin().indices().prepareGetAliases(SEARCH_INDEX).get().aliases.keysIt()*.toString()
      oldIndices.each {
        aliasBuilder.removeAlias(it, SEARCH_INDEX)
      }
      aliasBuilder.addAlias(newSearchIndex, SEARCH_INDEX)
      aliasBuilder.execute().actionGet()
      oldIndices.each { indexAdminService.drop(it) }

      def end = System.currentTimeMillis()
      log.info "reindexed ${recordCount} records in ${(end - start) / 1000}s"

    } catch (Exception e) {
      log.error "Search reindexing failed because of: " + ExceptionUtils.getRootCauseMessage(e)
      log.error "Root cause stack trace: \n" + ExceptionUtils.getRootCauseStackTrace(e)
      indexAdminService.drop(newSearchIndex)
    }
  }

}