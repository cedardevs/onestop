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
class ElasticsearchAdminService {

    @Value('${elasticsearch.index}')
    private String INDEX

    @Value('${elasticsearch.type}')
    private String TYPE

    private Client client

    @Autowired
    public ElasticsearchAdminService(Client client) {
        this.client = client
    }


    public Map loadDocument(String document) {
        def mappedDoc = MetadataParser.parseXMLMetadataToMap(document)
        if(!Pattern.matches(".*\\s.*", mappedDoc.fileIdentifier)) {
            def parsedDoc = JsonOutput.toJson(mappedDoc)
            IndexResponse iResponse = client.prepareIndex(INDEX, TYPE, mappedDoc.fileIdentifier)
                    .setSource(parsedDoc).execute().actionGet()
            def attributes = [created: iResponse.created, src: parsedDoc]
            def data = [type: TYPE, id: iResponse.id, attributes: attributes]
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
        def items = client.search(new SearchRequest(INDEX).types(TYPE)).actionGet()
        def ids = items.hits.hits*.id
        def bulkDelete = ids.inject(new BulkRequest()) { bulk, id ->
            bulk.add(new DeleteRequest(INDEX, TYPE, id))
        }
        bulkDelete.refresh(true)
        client.bulk(bulkDelete)
    }

    @PostConstruct
    private configureIndex() {
        def indexExists = client.admin().indices().prepareExists(INDEX).execute().actionGet().exists
        if (!indexExists) {
            // Initialize index:
            def cl = ClassLoader.systemClassLoader
            def indexSettings = cl.getResourceAsStream("config/index-settings.json").text
            client.admin().indices().prepareCreate(INDEX).setSettings(indexSettings).execute().actionGet()
            client.admin().cluster().prepareHealth(INDEX).setWaitForActiveShards(1).execute().actionGet()

            // Initialize mapping:
            def mapping = cl.getResourceAsStream("config/metadata-mapping.json").text
            client.admin().indices().preparePutMapping(INDEX).setSource(mapping).setType(TYPE).execute().actionGet()
        }
    }
}
