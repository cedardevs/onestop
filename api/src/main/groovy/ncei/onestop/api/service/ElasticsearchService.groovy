package ncei.onestop.api.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.Client
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
        def mappedDoc = MetadataParser.parseXMLMetadataToMap(document)
        if(!Pattern.matches(".*\\s.*", mappedDoc.fileIdentifier)) {
            def parsedDoc = JsonOutput.toJson(mappedDoc)
            IndexResponse iResponse = client.prepareIndex(SEARCH_INDEX, SEARCH_TYPE, mappedDoc.fileIdentifier)
                    .setSource(parsedDoc).execute().actionGet()
            def attributes = [created: iResponse.created, src: parsedDoc]
            def data = [type: SEARCH_TYPE, id: iResponse.id, attributes: attributes]
            def response = [data: data]
            return response
        } else {
            def errors = [
                    status: 400,
                    title: 'Load request failed due to bad fileIdentifier value',
                    detail: mappedDoc.fileIdentifier
            ]
            return [errors: errors]
        }
    }

    public Map loadDocumentToTest(String document) {
        def mappedDoc = MetadataParser.parseKeywords(document)
        if(!Pattern.matches(".*\\s.*", mappedDoc.fileIdentifier)) {
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
                title: 'Load request failed due to bad fileIdentifier value',
                detail: mappedDoc.fileIdentifier
            ]
            return [errors: errors]
        }
    }

    public void purgeIndex() {
        def items = client.search(new SearchRequest(SEARCH_INDEX).types(SEARCH_TYPE)).actionGet()
        def ids = items.hits.hits*.id
        def bulkDelete = ids.inject(new BulkRequest()) { bulk, id ->
            bulk.add(new DeleteRequest(SEARCH_INDEX, SEARCH_TYPE, id))
        }
        bulkDelete.refresh(true)
        client.bulk(bulkDelete)
    }

    public void refreshIndex() {
        client.admin().indices().prepareRefresh(SEARCH_INDEX).execute().actionGet()
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
