package ncei.onestop.api.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.elasticsearch.common.geo.ShapeRelation
import org.elasticsearch.common.geo.builders.ShapeBuilder
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.script.Script
import org.elasticsearch.script.ScriptService
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.springframework.stereotype.Service

@Slf4j
@Service
class SearchRequestParserService {

  public SearchRequestParserService() {}


  public Map parseSearchRequest(Map params) {

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

  public List<AggregationBuilder> createGCMDAggregations() {

    def aggregations = [
        AggregationBuilders.terms('science').field('gcmdScience').order(Terms.Order.term(true)),
        AggregationBuilders.terms('locations').field('gcmdLocations').order(Terms.Order.term(true)),
        AggregationBuilders.terms('instruments').field('gcmdInstruments').order(Terms.Order.term(true)),
        AggregationBuilders.terms('platforms').field('gcmdPlatforms').order(Terms.Order.term(true)),
        AggregationBuilders.terms('projects').field('gcmdProjects').order(Terms.Order.term(true)),
        AggregationBuilders.terms('dataCenters').field('gcmdDataCenters').order(Terms.Order.term(true)),
        AggregationBuilders.terms('dataResolution').field('gcmdDataResolution').order(Terms.Order.term(true))
    ]

    return aggregations
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

  private Map assembleFilters(List<Map> filters) {

    /*For filters:
         * union: A | B | A & B; intersection: A & B
         - union with bool > must > bool > should [] for multiple selections on same term
         - union of multiple unions is  bool > must >> bool > should []
                -- (does this mean a match must come from each nested filter?)
         - intersection probably bool > must > bool > must (single term)
    */

    def postFilters = false
    def collections = true

    def preBuilder = QueryBuilders.boolQuery()
    def postBuilder = QueryBuilders.boolQuery()
    if (!filters) {
      return [
          pre: preBuilder,
          post: null,
          collections: collections
      ]
    }

    def groupedFilters = filters.groupBy { it.type }

    // Temporal filters:
    groupedFilters.datetime.each {
      // TODO post filters for datetime from timeline view?
      if (it.before) {
        preBuilder.must(QueryBuilders.rangeQuery('temporalBounding.beginDate').lte(it.before))
      }
      if (it.after) {
        preBuilder.must(QueryBuilders.rangeQuery('temporalBounding.endDate').gte(it.after))
      }
    }

    // Spatial filters:
    groupedFilters.geometry.each {
      // TODO post filters for geometry from map view?
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

    // Collection filter -- force a union since an intersection on multiple parentIds will return nothing
    def parentIds = [] as Set
    groupedFilters.collection.each {
      collections = false
      parentIds.addAll(it.values)
    }
    if(parentIds) { preBuilder.must(QueryBuilders.termsQuery('parentIdentifier', parentIds)) }

    postBuilder = postFilters ? postBuilder : null
    return [
        pre: preBuilder,
        post: postBuilder
    ]
  }

  private AggregationBuilder createCollectionsAggregation() {

    def scoreScript = new Script('_score', ScriptService.ScriptType.INLINE, 'expression', null)

    def collections = AggregationBuilders
        .terms('parentIdentifier').order(Terms.Order.aggregation('score_agg', 'sum', false))
        .subAggregation(AggregationBuilders.stats('score_agg').script(scoreScript))

    return collections
  }
}

