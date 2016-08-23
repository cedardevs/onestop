package ncei.onestop.api.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
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

  @Value('${elasticsearch.index.search.type}')
  private String SEARCH_TYPE

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

  @Scheduled(fixedDelay = 600000L) // 10 minutes after previous run ends
  public void reindex() {
    log.info "starting reindex process"
    def start = System.currentTimeMillis()

    def newSearchIndex = indexAdminService.create(SEARCH_INDEX, [SEARCH_TYPE])
    def bulkRequest = client.prepareBulk()
    def recordCount = 0
    def pageSize = 50
    def scrollTimeout = '5m'
    def addRecordToBulk = { record ->
      def id = record.fileIdentifier as String
      def json = JsonOutput.toJson(record)
      def insertRequest = client.prepareIndex(newSearchIndex, SEARCH_TYPE, id).setSource(json)
      bulkRequest.add(insertRequest)
      recordCount++
      if (bulkRequest.numberOfActions() >= pageSize) {
        bulkRequest.get()
        bulkRequest = client.prepareBulk()
      }
    }

    log.debug("starting initial collection scroll")
    def collectionScroll = client.prepareSearch(STORAGE_INDEX)
        .setTypes(COLLECTION_TYPE)
        .addSort('fileIdentifier', SortOrder.ASC)
        .setScroll(scrollTimeout)
        .setSize(pageSize)
        .execute()
        .actionGet()
    def collectionsRemain = collectionScroll.hits.hits.length > 0
    while (collectionsRemain) {
      collectionScroll.hits.hits.each { collection ->
        def parsedCollection = MetadataParser.parseXMLMetadataToMap(collection.source.isoXml as String)
        addRecordToBulk(parsedCollection) // Add collections whether they have granules or not
        log.debug("starting initial granule scroll for collection ${parsedCollection.fileIdentifier}")
        def granuleScroll = client.prepareSearch(STORAGE_INDEX)
            .setTypes(GRANULE_TYPE)
            .addSort('fileIdentifier', SortOrder.ASC)
            .setScroll(scrollTimeout)
            .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery('parentIdentifier', parsedCollection.fileIdentifier)))
            .setSize(pageSize)
            .execute()
            .actionGet()
        def granulesRemain = granuleScroll.hits.hits.length > 0
/*        if (!granulesRemain) { // insert a synthesized granule record if there is no separate xml for it
          def synthesizedGranule = [parentIdentifier: parsedCollection.fileIdentifier]
          def flattenedSynthesizedRecord = MetadataParser.mergeCollectionAndGranule(parsedCollection, synthesizedGranule)
          addRecordToBulk(flattenedSynthesizedRecord)
        }*/
        while (granulesRemain) {
          granuleScroll.hits.hits.each { granule ->
            def parsedGranule = MetadataParser.parseXMLMetadataToMap(granule.source.isoXml as String)
            def flattenedRecord = MetadataParser.mergeCollectionAndGranule(parsedCollection, parsedGranule)
            addRecordToBulk(flattenedRecord)
          }
          log.debug("starting new granule scroll for collection ${parsedCollection.fileIdentifier}")
          granuleScroll = client.prepareSearchScroll(granuleScroll.scrollId).setScroll(scrollTimeout).execute().actionGet()
          granulesRemain = granuleScroll.hits.hits.length > 0
        }
      }
      log.debug("starting new collection scroll")
      collectionScroll = client.prepareSearchScroll(collectionScroll.scrollId).setScroll(scrollTimeout).execute().actionGet()
      collectionsRemain = collectionScroll.hits.hits.length > 0
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
  }

}
