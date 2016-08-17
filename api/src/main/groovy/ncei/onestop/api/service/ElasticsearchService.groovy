package ncei.onestop.api.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.elasticsearch.action.WriteConsistencyLevel
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import java.util.regex.Pattern

@Slf4j
@Service
class ElasticsearchService {

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
  private SearchRequestParserService searchRequestParserService
  private SearchResponseParserService searchResponseParserService
  private IndexAdminService indexAdminService

  @Autowired
  public ElasticsearchService(Client client,
                              SearchRequestParserService searchRequestParserService,
                              SearchResponseParserService searchResponseParserService,
                              IndexAdminService indexAdminService) {
    this.client = client
    this.searchRequestParserService = searchRequestParserService
    this.searchResponseParserService = searchResponseParserService
    this.indexAdminService = indexAdminService
  }


  Map search(Map searchParams) {
    def response = queryElasticSearch(searchParams)
    response
  }

  private Map queryElasticSearch(Map params) {
    def parsedRequest = searchRequestParserService.parseSearchRequest(params)
    def query = parsedRequest.query
    def postFilters = parsedRequest.postFilters
    def aggregations = searchRequestParserService.createDefaultAggregations()

    log.debug("ES query:${query} params:${params}")

    // Assemble the search request:
    def srb = client.prepareSearch(SEARCH_INDEX)
    srb = srb.setTypes(SEARCH_TYPE).setQuery(query)
    if(postFilters) { srb = srb.setPostFilter(postFilters) }
    aggregations.each { a -> srb = srb.addAggregation(a) }

    if(params.page) {
      srb = srb.setFrom(params.page.offset).setSize(params.page.max)
    } else {
      srb = srb.setFrom(0).setSize(100)
    }

    def searchResponse = srb.execute().actionGet()
    return searchResponseParserService.searchResponseParser(searchResponse)
  }

  @Async
  public void reindexAsync() {
    reindex()
  }

  @Scheduled(fixedDelay = 300000L) // 5 minutes after previous run ends
  public void reindex() {
    log.info "starting reindex process"
    def start = System.currentTimeMillis()

    def newSearchIndex = indexAdminService.create(SEARCH_INDEX, [SEARCH_TYPE])
    def bulkRequest = client.prepareBulk()
    def recordCount = 0
    def pageSize = 100
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

    def collectionScroll = client.prepareSearch(STORAGE_INDEX)
        .setTypes(COLLECTION_TYPE)
        .addSort('fileIdentifier', SortOrder.ASC)
        .setScroll('1m')
        .setSize(pageSize)
        .execute()
        .actionGet()
    def collectionsRemain = collectionScroll.hits.hits.length > 0
    while (collectionsRemain) {
      collectionScroll.hits.hits.each { collection ->
        def parsedCollection = MetadataParser.parseXMLMetadataToMap(collection.source.isoXml as String)
        def granuleScroll = client.prepareSearch(STORAGE_INDEX)
            .setTypes(GRANULE_TYPE)
            .addSort('fileIdentifier', SortOrder.ASC)
            .setScroll('1m')
            .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery('parentIdentifier', parsedCollection.fileIdentifier)))
            .setSize(pageSize)
            .execute()
            .actionGet()
        def granulesRemain = granuleScroll.hits.hits.length > 0
        if (!granulesRemain) {
          addRecordToBulk(parsedCollection)
        }
        while (granulesRemain) {
          granuleScroll.hits.hits.each { granule ->
            def parsedGranule = MetadataParser.parseXMLMetadataToMap(granule.source.isoXml as String)
            def flattenedRecord = MetadataParser.mergeCollectionAndGranule(parsedCollection, parsedGranule)
            addRecordToBulk(flattenedRecord)
          }
          granuleScroll = client.prepareSearchScroll(granuleScroll.scrollId).setScroll('1m').execute().actionGet()
          granulesRemain = granuleScroll.hits.hits.length > 0
        }
      }
      collectionScroll = client.prepareSearchScroll(collectionScroll.scrollId).setScroll('1m').execute().actionGet()
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

  public void refresh() {
    indexAdminService.refresh(SEARCH_INDEX, STORAGE_INDEX)
  }

  public void drop() {
    indexAdminService.drop(SEARCH_INDEX)
  }

  @PostConstruct
  public void ensure() {
    def searchExists = client.admin().indices().prepareAliasesExist(SEARCH_INDEX).execute().actionGet().exists
    if (!searchExists) {
      def realName = indexAdminService.create(SEARCH_INDEX, [SEARCH_TYPE])
      client.admin().indices().prepareAliases().addAlias(realName, SEARCH_INDEX).execute().actionGet()
    }
  }

  public void recreate() {
    drop()
    ensure()
  }
}
