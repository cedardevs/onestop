package ncei.onestop.api.service

import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired

import org.elasticsearch.client.Client

@Service
class SearchService {

    private Client client
    private SearchResponseParserService responseService
    private SearchRequestParserService searchRequestParserService


    @Autowired
    public SearchService(Client client, SearchResponseParserService responseService,
        SearchRequestParserService searchRequestParserService) {
        this.client = client
        this.responseService = responseService
        this.searchRequestParserService = searchRequestParserService
    }

    Map search(Map searchParams) {
        def response = queryElasticSearch(searchParams)
        response
    }

    private Map queryElasticSearch(Map params) {
        def geoportalIndex = 'metadata_v1'
        def itemTypeName = 'item'
        def query = parseSearchRequest(params)
        def searchResponse = client
            .prepareSearch(geoportalIndex)
            .setTypes(itemTypeName)
            .setQuery(query)
            .execute()
            .actionGet()

        return responseService.searchResponseParser(searchResponse)
    }

    private static QueryBuilder parseSearchRequest(Map params) {
        if (params.searchText instanceof String) {
            return QueryBuilders.matchQuery('_all', params.searchText)
        }
        else {
            return QueryBuilders.matchAllQuery()
        }
    }

}
