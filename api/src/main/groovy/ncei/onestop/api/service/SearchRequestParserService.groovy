package ncei.onestop.api.service

import groovy.util.logging.Slf4j
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.WrapperQueryBuilder
import org.springframework.stereotype.Service

@Slf4j
@Service
class SearchRequestParserService {

    public SearchRequestParserService() {}


    public QueryBuilder parseSearchRequest(Map params) {

        log.debug("Queries: ${params.queries}")
        log.debug("Filters: ${params.filters}")

        def query = assembleQuery(params.queries)
        def filter = assembleFilter(params.filters)

        /*
        query
            bool
                filter
                    {filters}
                must
                    {queries}
         */
        def completeQuery = QueryBuilders.boolQuery().filter(filter).must(query)
        completeQuery
    }


    private QueryBuilder assembleFilter(List<Map> filters) {

        /*For filters:
             * union: A | B | A & B; intersection: A & B
             - union with bool > must > bool > should [] for multiple selections on same term
             - union of multiple unions is  bool > must >> bool > should []
                    -- (does this mean a match must come from each nested filter?)
             - intersection probably bool > must > bool > must (single term)
        */

        def builder = QueryBuilders.boolQuery()
        if (!filters) { return builder }

        def groupedFilters = filters.groupBy { it.type }

        // Temporal filters:
        groupedFilters.datetime.each {
            def dataEndsInRange = QueryBuilders.boolQuery()
            dataEndsInRange.must(QueryBuilders.rangeQuery("temporalBounding.beginDate").gte(it.after).lte(it.before))
            dataEndsInRange.must(QueryBuilders.rangeQuery("temporalBounding.endDate").gte(it.before))

            def dataContainedInRange = QueryBuilders.boolQuery()
            dataContainedInRange.must(QueryBuilders.rangeQuery("temporalBounding.beginDate").lte(it.after))
            dataContainedInRange.must(QueryBuilders.rangeQuery("temporalBounding.endDate").gte(it.before))

            def dataStartsInRange = QueryBuilders.boolQuery()
            dataStartsInRange.must(QueryBuilders.rangeQuery("temporalBounding.beginDate").lte(it.after))
            dataStartsInRange.must(QueryBuilders.rangeQuery("temporalBounding.endDate").gte(it.after).lte(it.before))

            // At least one date range match should be true for data set to pass filter:
            builder.should(dataEndsInRange).should(dataContainedInRange).should(dataStartsInRange)
        }

        // Spatial filters:
        groupedFilters.point.each {
            // TODO
        }

        // Facet filters:
        groupedFilters.facet.each {
            // TODO
            builder.must(QueryBuilders.termsQuery(it.name, it.values))
        }

        return builder
    }


    private QueryBuilder assembleQuery(List<Map> queries) {
        def builder = QueryBuilders.boolQuery()
        if (!queries) { return builder }

        def groupedQueries = queries.groupBy { it.type }

        groupedQueries.queryText.each {
            builder.must(QueryBuilders.queryStringQuery(it.value))
        }

        return builder
    }

}

