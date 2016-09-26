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

  @Value('${elasticsearch.index.storage.name}')
  private String STORAGE_INDEX

  @Value('${elasticsearch.index.storage.collectionType}')
  private String COLLECTION_TYPE

  @Value('${elasticsearch.index.storage.granuleType}')
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
    def newSearchIndex = indexAdminService.create(SEARCH_INDEX, [GRANULE_TYPE, COLLECTION_TYPE])

    try {
      def bulkRequest = client.prepareBulk()
      def recordCount = 0
      def granuleScrollTimeout = '1m'
      def granulePageSize = 10

      def offset = 0
      def increment = 10
      def collectionsCount = client.prepareSearch(STORAGE_INDEX).setTypes(COLLECTION_TYPE)
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
        def collections = client.prepareSearch(STORAGE_INDEX).setTypes(COLLECTION_TYPE)
            .addSort("fileIdentifier", SortOrder.ASC).setFrom(offset).setSize(increment).execute().actionGet().hits.hits
        collections.each { collection ->
          def parsedCollection = MetadataParser.parseXMLMetadataToMap(collection.source.isoXml as String)
          log.debug('Starting indexing of collection ' + parsedCollection.fileIdentifier) //fixme delete later
          addRecordToBulk(parsedCollection, COLLECTION_TYPE) // Add collections whether they have granules or not
          def granuleScroll = client.prepareSearch(STORAGE_INDEX)
              .setTypes(GRANULE_TYPE)
              .addSort('fileIdentifier', SortOrder.ASC)
              .setScroll(granuleScrollTimeout)
              .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery('parentIdentifier', parsedCollection.fileIdentifier)))
              .setSize(granulePageSize)
              .execute()
              .actionGet()
          def granulesRemain = granuleScroll.hits.hits.length > 0
          if (!granulesRemain) { // insert a synthesized granule record if there is no separate xml for it
            log.debug('Inserting synthesized granule for collection ' + parsedCollection.fileIdentifier) // fixme delete later
            def synthesizedGranule = [fileIdentifier: parsedCollection.fileIdentifier, parentIdentifier: parsedCollection.fileIdentifier]
            def flattenedSynthesizedRecord = MetadataParser.mergeCollectionAndGranule(parsedCollection, synthesizedGranule)
            addRecordToBulk(flattenedSynthesizedRecord, GRANULE_TYPE)
          }

          while (granulesRemain) {
            granuleScroll.hits.hits.each { granule ->
              def parsedGranule = MetadataParser.parseXMLMetadataToMap(granule.source.isoXml as String)
              def flattenedRecord = MetadataParser.mergeCollectionAndGranule(parsedCollection, parsedGranule)
              flattenedRecord.parentIdentifier = parsedCollection.parentIdentifier // should always be true, but may not be if granule xml has a bad PID
              addRecordToBulk(flattenedRecord, GRANULE_TYPE)
            }
            granuleScroll = client.prepareSearchScroll(granuleScroll.scrollId).setScroll(granuleScrollTimeout).execute().actionGet()
            granulesRemain = granuleScroll.hits.hits.length > 0
          }
          log.debug('Finished indexing for collection ' + parsedCollection.fileIdentifier) // fixme delete later
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