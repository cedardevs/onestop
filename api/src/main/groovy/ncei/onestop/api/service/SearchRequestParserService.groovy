package ncei.onestop.api.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.elasticsearch.common.geo.ShapeRelation
import org.elasticsearch.common.geo.builders.ShapeBuilder
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.springframework.stereotype.Service

@Slf4j
@Service
class SearchRequestParserService {

  public SearchRequestParserService() {}


  public Map<String, QueryBuilder> parseSearchRequest(Map params) {

    log.debug("Queries: ${params.queries}")
    log.debug("Filters: ${params.filters}")

    def query = assembleQuery(params.queries)
    def filters = assembleFilters(params.filters)

    /*
    query
        bool
            filter
                {filters}
            must
                {queries}
     */
    def completeQuery = QueryBuilders.boolQuery().filter(filters.pre).must(query)

    return [
        query: completeQuery,
        postFilters: filters.post
    ]
  }


  public List<AggregationBuilder> createDefaultAggregations() {

    def aggregations = [
        AggregationBuilders.terms('science').field('gcmdScience'),
        AggregationBuilders.terms('locations').field('gcmdLocations'),
        AggregationBuilders.terms('instruments').field('gcmdInstruments'),
        AggregationBuilders.terms('platforms').field('gcmdPlatforms'),
        AggregationBuilders.terms('projects').field('gcmdProjects'),
        AggregationBuilders.terms('dataCenters').field('gcmdDataCenters'),
        AggregationBuilders.terms('dataResolution').field('gcmdDataResolution')
    ]

    return aggregations
  }


  private Map<String, QueryBuilder> assembleFilters(List<Map> filters) {

    /*For filters:
         * union: A | B | A & B; intersection: A & B
         - union with bool > must > bool > should [] for multiple selections on same term
         - union of multiple unions is  bool > must >> bool > should []
                -- (does this mean a match must come from each nested filter?)
         - intersection probably bool > must > bool > must (single term)
    */

    def postFilters = false // Use a flag

    def preBuilder = QueryBuilders.boolQuery()
    def postBuilder = QueryBuilders.boolQuery()
    if (!filters) {
      return [
          pre: preBuilder,
          post: null
      ]
    }

    def groupedFilters = filters.groupBy { it.type }

    // Temporal filters:
    groupedFilters.datetime.each {
      // TODO post views for datetime from timeline
      if (it.before) {
        preBuilder.must(QueryBuilders.rangeQuery('temporalBounding.beginDate').lte(it.before))
      }
      if (it.after) {
        preBuilder.must(QueryBuilders.rangeQuery('temporalBounding.endDate').gte(it.after))
      }
    }

    // Spatial filters:
    groupedFilters.geometry.each {
      // TODO post filters for geometry from map view
      // it's a little silly to convert back to json string, but it's the easiest
      // and most flexible way for the elasticsearch api to parse a request from it
      def json = JsonOutput.toJson(it.geometry)
      def parser = XContentType.JSON.xContent().createParser(json)
      parser.nextToken() // need to advance to the first token before parsing for some reason...
      def shape = ShapeBuilder.parse(parser)
      def relation = ShapeRelation.getRelationByName(it.relation ?: 'intersects')

      preBuilder.must(QueryBuilders.geoShapeQuery("spatialBounding", shape, relation))
    }

    // Facet filters:
    groupedFilters.facet.each {
      // Facets are applied as post_filters so that counts on the facet menu don't change but displayed results do
      postFilters = true
      postBuilder.must(QueryBuilders.termsQuery(it.name, it.values))
    }

    postBuilder = postFilters ? postBuilder : null
    return [
        pre: preBuilder,
        post: postBuilder
    ]
  }


  private QueryBuilder assembleQuery(List<Map> queries) {
    def builder = QueryBuilders.boolQuery()
    if (!queries) {
      return builder
    }

    def groupedQueries = queries.groupBy { it.type }

    groupedQueries.queryText.each {
      builder.must(QueryBuilders.queryStringQuery(it.value))
    }

    return builder
  }
}

