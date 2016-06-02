package ncei.onestop.api.service

import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.stereotype.Service

@Service
class SearchRequestParserUtility {

    public SearchRequestParserUtility() {}


    public QueryBuilder parseSearchRequest(Map params) {

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
        def groupedFilters = filters.groupBy { it.type }

        // Temporal filters:
        groupedFilters.dateTime.each {
            // TODO date field name in ES document unknown -- calling it creationDate for now
            builder.must(QueryBuilders.rangeQuery("creationDate").gte(it.start).lte(it.end)/*.format("")*/)
        }

        // Spatial filters:
        groupedFilters.point.each {
            // TODO
        }

        // Facet filters:
        groupedFilters.facet.each {
            def groupedFacets = it.groupEntriesBy { it.name }

            groupedFacets.each {
                builder.must(QueryBuilders.termsQuery(it.name, it.values))
            }
        }

        return builder
    }


    private QueryBuilder assembleQuery(List<Map> queries) {
        def builder = QueryBuilders.boolQuery()
        def groupedQueries = queries.groupBy { it.type }

        groupedQueries.queryText.each {
            // TODO check string for double quotes -- term query for exact match? or is ES already doing this?
            builder.must(QueryBuilders.matchQuery('_all', it.value))
        }

        return builder
    }

}

