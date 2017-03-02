package ncei.onestop.api.service

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.elasticsearch.common.geo.ShapeRelation
import org.elasticsearch.common.geo.builders.ShapeBuilder
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders
import org.elasticsearch.script.Script
import org.elasticsearch.script.ScriptService
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class SearchRequestParserService {

  private SearchConfig config

  @Autowired
  SearchRequestParserService(SearchConfig config) {
    this.config = config
  }


  public static final Map<String, String> facetNameMappings = [
      //'parentIdentifier': 'parentIdentifier',
      'science': 'gcmdScience',
      'instruments': 'gcmdInstruments.raw',
      'platforms': 'gcmdPlatforms.raw',
      'projects': 'gcmdProjects.raw',
      'dataCenters': 'gcmdDataCenters.raw',
      'dataResolution': 'gcmdDataResolution.raw',
  ]


  QueryBuilder parseSearchQuery(Map params) {
    log.debug("Queries: ${params.queries}")
    log.debug("Filters: ${params.filters}")

    /*
    query
        bool
            filter
                {filters}
            must
                {queries}
     */
    def query = assembleQuery(params.queries)
    def filters = assembleFilters(params.filters)
    return QueryBuilders.boolQuery().filter(filters).must(query)
  }

  AggregationBuilder createCollectionsAggregation() {

    def scoreScript = new Script('_score', ScriptService.ScriptType.INLINE, 'expression', null)

    def collections = AggregationBuilders
        .terms('collections').field('parentIdentifier').order(Terms.Order.aggregation('score_agg', 'max', false)).size(0)
        .subAggregation(AggregationBuilders.stats('score_agg').script(scoreScript))

    return collections
  }

  List<AggregationBuilder> createGCMDAggregations(boolean forCollections) {

    def aggregations = facetNameMappings.collect { name, field ->
      AggregationBuilders.terms(name).field(field).order(Terms.Order.term(true)).size(0)
    }

    if(forCollections) {
      aggregations.each { a ->
        a.subAggregation(AggregationBuilders.terms('byCollection').field('parentIdentifier').size(0))
      }
    }

    return aggregations
  }

  Boolean shouldReturnCollections(Map params) {
    !params.filters.any { it.type == 'collection' }
  }

  private QueryBuilder assembleQuery(List<Map> queries) {
    def allTextQueries = QueryBuilders.boolQuery()
    if (!queries) {
      return allTextQueries
    }

    def groupedQueries = queries.groupBy { it.type }

    groupedQueries.queryText.each {
      def query = QueryBuilders.queryStringQuery(it.value as String)
      config?.boosts?.each { field, boost ->
        query.field(field, boost ?: 1f)
      }
      if (config?.minimumShouldMatch) {
        query.minimumShouldMatch(config.minimumShouldMatch)
      }
      if (config?.phraseSlop) {
        query.phraseSlop(config.phraseSlop)
      }
      if (config?.tieBreaker) {
        query.tieBreaker(config.tieBreaker)
      }
      query.lenient(true)
      allTextQueries.must(query)
    }

    if (config?.dsmm?.factor || config?.dsmm?.modifier) {
      def scoreFunction = ScoreFunctionBuilders.fieldValueFactorFunction('dsmmAverage')
          .modifier(FieldValueFactorFunction.Modifier.valueOf((config.dsmm.modifier ?: 'log1p').toUpperCase()))
          .factor(config.dsmm.factor ?: 1f)
          .missing(0)
      def dsmmQuery = QueryBuilders.functionScoreQuery(allTextQueries)
          .add(scoreFunction)
          .boostMode('sum')
      return dsmmQuery
    }

    return allTextQueries
  }

  private QueryBuilder assembleFilters(List<Map> filters) {

    /*For filters:
         * union: A | B | A & B; intersection: A & B
         - union with bool > must > bool > should [] for multiple selections on same term
         - union of multiple unions is  bool > must >> bool > should []
                -- (does this mean a match must come from each nested filter?)
         - intersection probably bool > must > bool > must (single term)
    */

    def builder = QueryBuilders.boolQuery()
    if (!filters) {
      return builder
    }

    def groupedFilters = filters.groupBy { it.type }

    // Temporal filters:
    groupedFilters.datetime.each {
      // TODO post filters for datetime from timeline view?
      if (it.before) {
        builder.must(QueryBuilders.rangeQuery('temporalBounding.beginDate').lte(it.before))
      }
      if (it.after) {
        builder.must(QueryBuilders.rangeQuery('temporalBounding.endDate').gte(it.after))
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

      builder.must(QueryBuilders.geoShapeQuery("spatialBounding", shape, relation))
    }

    // Facet filters:
    groupedFilters.facet.each {
      def fieldName = facetNameMappings[it.name] ?: it.name
      builder.must(QueryBuilders.termsQuery(fieldName, it.values))
    }

    // Collection filter -- force a union since an intersection on multiple parentIds will return nothing
    def parentIds = [] as Set
    groupedFilters.collection.each {
      parentIds.addAll(it.values)
    }
    if(parentIds) {
      builder.must(QueryBuilders.termsQuery('parentIdentifier', parentIds))
    }

    return builder
  }
}

