package ncei.onestop.api.service

import groovy.json.JsonOutput
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ElasticsearchAdminService {

    private final String index = 'metadata_v1'
    private final String type = 'item'

    private Client client

    @Autowired
    public ElasticsearchAdminService(Client client) {
        this.client = client
    }


    public Map loadDocument(String document) {
        def mappedDoc = MetadataParser.parseXMLMetadataToMap(document)
        def parsedDoc = JsonOutput.toJson(mappedDoc)
        IndexResponse iResponse = client.prepareIndex(index, type, mappedDoc.fileIdentifier)
                .setSource(parsedDoc).execute().actionGet()
        def data = [type: 'collection', id: iResponse.id, attributes: parsedDoc]
        def response = [data: data]
        return response
    }
}
