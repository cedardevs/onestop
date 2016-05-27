package ncei.onestop.api.service

import ncei.onestop.api.pojo.OneStopSearchRequest
import org.elasticsearch.index.query.QueryBuilder
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired

@Service
class SearchRequestParserService {

    private QueryBuilderService qbService

    @Autowired
    public SearchRequestParserService(QueryBuilderService qbService) {
        this.qbService = qbService
    }

    public QueryBuilder parseSearchRequest(OneStopSearchRequest searchRequest) {
        // FIXME
    }
}

