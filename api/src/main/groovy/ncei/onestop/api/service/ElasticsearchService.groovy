package ncei.onestop.api.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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

  @Autowired
  public ElasticsearchService(Client client,
                              SearchRequestParserService searchRequestParserService,
                              SearchResponseParserService searchResponseParserService) {
    this.client = client
    this.searchRequestParserService = searchRequestParserService
    this.searchResponseParserService = searchResponseParserService
  }


  Map search(Map searchParams) {
    def response = queryElasticSearch(searchParams)
    response
  }

  private Map queryElasticSearch(Map params) {
    def query = searchRequestParserService.parseSearchRequest(params)
    log.debug("ES query:${query} params:${params}")
    def searchResponse = client
        .prepareSearch(SEARCH_INDEX)
        .setTypes(SEARCH_TYPE)
        .setQuery(query)
        .setFrom(0).setSize(100) // TODO - expose these as API parameters
        .execute()
        .actionGet()

    return searchResponseParserService.searchResponseParser(searchResponse)
  }

  public Map loadDocument(String document) {
    def storageInfo = MetadataParser.parseStorageInfo(document)
    def id = storageInfo.id
    if (Pattern.matches(/.*\s.*/, id)) {
      return [
          errors: [
              status: 400,
              title : 'Load request failed due to bad fileIdentifier value',
              detail: id
          ]
      ]
    } else {
      def type = storageInfo.parentId ? GRANULE_TYPE : COLLECTION_TYPE
      def source = [isoXml: document, fileIdentifier: id]
      if (type == GRANULE_TYPE) {
        source.parentIdentifier = storageInfo.parentId
      }
      source = JsonOutput.toJson(source)
      def response = client.prepareIndex(STORAGE_INDEX, type, id).setSource(source).execute().actionGet()
      return [
          data: [
              id        : id,
              type      : type,
              attributes: [
                  created: response.created
              ]
          ]
      ]
    }
  }

  // FIXME Delete this once no longer testing
  public Map loadDocumentToTest(String document) {
    def mappedDoc = MetadataParser.parseXMLMetadataToMap(document)
    if (!Pattern.matches(".*\\s.*", mappedDoc.fileIdentifier)) {
      def parsedDoc = JsonOutput.toJson(mappedDoc)
      IndexResponse iResponse = client.prepareIndex("testing", SEARCH_TYPE, mappedDoc.fileIdentifier)
          .setSource(parsedDoc).execute().actionGet()
      def attributes = [created: iResponse.created, src: parsedDoc]
      def data = [type: SEARCH_TYPE, id: iResponse.id, attributes: attributes]
      def response = [data: data]
      return response
    } else {
      def errors = [
          status: 400,
          title : 'Load request failed due to bad fileIdentifier value',
          detail: mappedDoc.fileIdentifier
      ]
      return [errors: errors]
    }
  }

  public void purgeIndex() {
    def items = client.search(new SearchRequest(SEARCH_INDEX).types(SEARCH_TYPE)).actionGet()
    def ids = items.hits.hits*.id
    def bulkDelete = ids.inject(new BulkRequest()) {bulk, id ->
      bulk.add(new DeleteRequest(SEARCH_INDEX, SEARCH_TYPE, id))
    }
    bulkDelete.refresh(true)
    client.bulk(bulkDelete)
  }

  public void refresh() {
    client.admin().indices().prepareRefresh(SEARCH_INDEX).execute().actionGet()
    client.admin().indices().prepareRefresh(STORAGE_INDEX).execute().actionGet()
  }

  public void reindex() {
    log.info "starting reindex process"
    def start = System.currentTimeMillis()

    def bulkRequest = client.prepareBulk()
    def bulkCount = 0
    def bulkSize = 100
    def recordCount = 0
    def addRecordToBulk = {record ->
      def id = record.fileIdentifier
      def json = JsonOutput.toJson(record)
      def insertRequest = client.prepareIndex(SEARCH_INDEX, SEARCH_TYPE, id).setSource(json)
      bulkRequest.add(insertRequest)
      bulkCount++
      recordCount++
      if (bulkCount >= bulkSize) {
        bulkRequest.get()
        bulkRequest = client.prepareBulk()
        bulkCount = 0
      }
    }

    def scrollSize = 100
    def collectionScroll = client.prepareSearch(STORAGE_INDEX)
        .setTypes(COLLECTION_TYPE)
        .addSort('fileIdentifier', SortOrder.ASC)
        .setScroll('1m')
        .setSize(scrollSize)
        .execute()
        .actionGet()
    def collectionsRemain = collectionScroll.hits.hits.length > 0
    while (collectionsRemain) {
      collectionScroll.hits.hits.each {collection ->
        def parsedCollection = MetadataParser.parseXMLMetadataToMap(collection.source.isoXml as String)
        def granuleScroll = client.prepareSearch(STORAGE_INDEX)
            .setTypes(GRANULE_TYPE)
            .addSort('fileIdentifier', SortOrder.ASC)
            .setScroll('1m')
            .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery('parentIdentifier', parsedCollection.fileIdentifier)))
            .setSize(scrollSize)
            .execute()
            .actionGet()
        def granulesRemain = granuleScroll.hits.hits.length > 0
        if (!granulesRemain) {
          addRecordToBulk(parsedCollection)
        }
        while (granulesRemain) {
          granuleScroll.hits.hits.each {granule ->
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

    bulkRequest.get()
    def end = System.currentTimeMillis()
    log.info "reindexed ${recordCount} records in ${(end - start) / 1000}s"
  }

  @PostConstruct
  private configureSearchIndex() {
    def indexExists = client.admin().indices().prepareExists(SEARCH_INDEX).execute().actionGet().exists
    if (!indexExists) {
      // Initialize index:
      def cl = ClassLoader.systemClassLoader
      def indexSettings = cl.getResourceAsStream("config/${SEARCH_INDEX}-settings.json").text
      client.admin().indices().prepareCreate(SEARCH_INDEX).setSettings(indexSettings).execute().actionGet()
      client.admin().cluster().prepareHealth(SEARCH_INDEX).setWaitForActiveShards(1).execute().actionGet()

      // Initialize mapping:
      def mapping = cl.getResourceAsStream("config/${SEARCH_INDEX}-mapping-${SEARCH_TYPE}.json").text
      client.admin().indices().preparePutMapping(SEARCH_INDEX).setSource(mapping).setType(SEARCH_TYPE).execute().actionGet()
    }
  }

  @PostConstruct
  private configureStorageIndex() {
    def indexExists = client.admin().indices().prepareExists(STORAGE_INDEX).execute().actionGet().exists
    if (!indexExists) {
      // Initialize index:
      def cl = ClassLoader.systemClassLoader
      def indexSettings = cl.getResourceAsStream("config/${STORAGE_INDEX}-settings.json").text
      client.admin().indices().prepareCreate(STORAGE_INDEX).setSettings(indexSettings).execute().actionGet()
      client.admin().cluster().prepareHealth(STORAGE_INDEX).setWaitForActiveShards(1).execute().actionGet()

      // Initialize mapping:
      def collectionMapping = cl.getResourceAsStream("config/${STORAGE_INDEX}-mapping-${COLLECTION_TYPE}.json").text
      client.admin().indices().preparePutMapping(STORAGE_INDEX).setSource(collectionMapping).setType(COLLECTION_TYPE).execute().actionGet()
      def granuleMapping = cl.getResourceAsStream("config/${STORAGE_INDEX}-mapping-${GRANULE_TYPE}.json").text
      client.admin().indices().preparePutMapping(STORAGE_INDEX).setSource(granuleMapping).setType(GRANULE_TYPE).execute().actionGet()
    }
  }
}
