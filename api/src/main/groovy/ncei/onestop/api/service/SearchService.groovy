package ncei.onestop.api.service

import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired

import org.elasticsearch.client.Client

@Service
class SearchService {

    private Client client
   // private SearchResponseParserService responseService
    private SearchRequestParserUtility searchRequestParserService


    @Autowired
    public SearchService(Client client, /*SearchResponseParserService responseService,*/
                         SearchRequestParserUtility searchRequestParserService) {
        this.client = client
        //this.responseService = responseService
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
        def searchResult = client
            .prepareSearch(geoportalIndex)
            .setTypes(itemTypeName)
            .setQuery(query)
            .execute()
            .actionGet()

        return [items: searchResult.hits.hits*.source]
    }

}
