package ncei.onestop.api.service

import org.springframework.stereotype.Service

import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders

/**
 * Class of methods returning QueryBuilder objects
 */
@Service
class QueryBuilderService {

    /**
     * Query all fields for the supplied text
     */
    public QueryBuilder allTextQuery(String searchParam) {
        QueryBuilders.matchQuery("_all", searchParam)
    }

}
