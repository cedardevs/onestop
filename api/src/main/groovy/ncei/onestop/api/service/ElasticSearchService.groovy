package ncei.onestop.api.service

import ncei.onestop.api.pojo.OneStopSearchResponse
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired

import org.elasticsearch.client.Client

@Service
class ElasticSearchService {

    private Client client
    private SearchResponseParserService responseService

    @Autowired
    public ElasticSearchService(Client client, SearchResponseParserService responseService) {
        this.client = client
        this.responseService = responseService
    }

    public OneStopSearchResponse queryElasticSearch() {}

}
