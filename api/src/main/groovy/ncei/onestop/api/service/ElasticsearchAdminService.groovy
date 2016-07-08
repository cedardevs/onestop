package ncei.onestop.api.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j

import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

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
            def data = [type: 'collection', id: iResponse.id, attributes: attributes]
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
}
