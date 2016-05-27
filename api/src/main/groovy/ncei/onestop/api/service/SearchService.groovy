package ncei.onestop.api.service

import ncei.onestop.api.pojo.OneStopSearchRequest
import ncei.onestop.api.pojo.OneStopSearchResponse
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
        SearchRequestParserService searchRequestParserService
    ) {
        this.client = client
        this.responseService = responseService
        this.searchRequestParserService = searchRequestParserService
    }

    public OneStopSearchResponse queryElasticSearch() {}

    def search(OneStopSearchRequest oneStopSearchRequest) {
        def elasticRequest = searchRequestParserService.parseSearchRequest(oneStopSearchRequest)
        def response = queryElasticSearch(elasticRequest)
        response
    }
}
