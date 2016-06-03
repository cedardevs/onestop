package ncei.onestop.api.service

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired

import org.elasticsearch.client.Client

@Slf4j
@Service
class SearchService {

    private Client client
    private SearchRequestParserUtil searchRequestParserService


    @Autowired
    public SearchService(Client client,
                         SearchRequestParserUtil searchRequestParserService) {
        this.client = client
        this.searchRequestParserService = searchRequestParserService
    }

    Map search(Map searchParams) {
        def response = queryElasticSearch(searchParams)
        response
    }

    private Map queryElasticSearch(Map params) {
        def geoportalIndex = 'metadata_v1'
        def itemTypeName = 'item'
        def query = searchRequestParserService.parseSearchRequest(params)
        log.debug("ES query:${query} params:${params}")
        def searchResponse = client
            .prepareSearch(geoportalIndex)
            .setTypes(itemTypeName)
            .setQuery(query)
            .execute()
            .actionGet()

        return SearchResponseParserUtil.searchResponseParser(searchResponse)
    }

}
