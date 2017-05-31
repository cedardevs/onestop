package ncei.onestop.api.service

import groovy.util.logging.Slf4j
import org.elasticsearch.action.get.MultiGetResponse
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Slf4j
@Service
class SearchIndexService {

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.name}')
  private String SEARCH_INDEX

  @Value('${elasticsearch.index.search.collectionType}')
  private String COLLECTION_TYPE

  @Value('${elasticsearch.index.search.granuleType}')
  private String GRANULE_TYPE

  private Client searchClient
  private SearchRequestParserService searchRequestParserService
  private IndexAdminService indexAdminService

  @Autowired
  public SearchIndexService(Client searchClient,
                            SearchRequestParserService searchRequestParserService,
                            IndexAdminService indexAdminService) {
    this.searchClient = searchClient
    this.searchRequestParserService = searchRequestParserService
    this.indexAdminService = indexAdminService
  }

  public void refresh() {
    indexAdminService.refresh(SEARCH_INDEX)
  }

  public void drop() {
    indexAdminService.drop(SEARCH_INDEX)
  }

  public void recreate() {
    drop()
    indexAdminService.ensureSearch()
  }

  public Map search(Map searchParams) {
    def response = queryElasticSearch(searchParams)
    response
  }

  public Map totalCounts() {
    def collectionResponse = searchClient.prepareSearch(SEARCH_INDEX).setTypes(COLLECTION_TYPE).setQuery(QueryBuilders.matchAllQuery()).setSize(0).execute().actionGet()
    // TODO granule counts... requires scripting: search all granules where fileIdentifier != parentIdentifier (omits 'synthesized' granules)

    return [
        data: [
            [
                type : "count",
                id   : "collection",
                count: collectionResponse.hits.totalHits
            ]/*,
            [
              type:  "count",
              id:    "granule",
              count: granuleResponse.hits.totalHits
            ]*/
        ]
    ]
  }

  private Map queryElasticSearch(Map params) {
    def query = searchRequestParserService.parseSearchQuery(params)
    def getCollections = searchRequestParserService.shouldReturnCollections(params)
    def getFacets = params.facets as boolean
    def pageParams = params.page as Map

    def searchBuilder = searchRequestBuilder(query, getFacets, getCollections)
    return getCollections ? getCollectionResults(searchBuilder, pageParams) : getGranuleResults(searchBuilder, pageParams)
  }

  private SearchRequestBuilder searchRequestBuilder(QueryBuilder query, boolean getFacets, boolean getCollections) {
    def builder = searchClient.prepareSearch(SEARCH_INDEX).setTypes(GRANULE_TYPE).setQuery(query)

    if (getFacets) {
      def aggregations = searchRequestParserService.createGCMDAggregations(getCollections)
      aggregations.each { a -> builder = builder.addAggregation(a) }
    }

    if (getCollections) {
      builder = builder.addAggregation(searchRequestParserService.createCollectionsAggregation())
      builder = builder.setSize(0)
    }

    log.debug("ES query:${builder}")
    return builder
  }

  private Map getCollectionResults(SearchRequestBuilder searchRequestBuilder, Map pageParams) {
    def response = searchRequestBuilder.execute().actionGet()
    def totalCount = response.aggregations.get('collections').getBuckets().size()
    if(!totalCount) {
      return [
          data: [],
          meta: [
              total: 0,
              took: response.tookInMillis
          ]
      ]
    }

    def offset
    def max
    if(pageParams) {
      offset = pageParams.offset
      max = pageParams.max
    }
    else {
      // Default first 100 results returned
      offset = 0
      max = 100
    }

    def collectionsToRetrieve = response.aggregations.get('collections').getBuckets()
        .stream()
        .skip(offset)
        .limit(max)
        .map( {i -> i.keyAsString} )
        .collect()

    MultiGetResponse multiGetItemResponses = searchClient.prepareMultiGet().add(SEARCH_INDEX, COLLECTION_TYPE, collectionsToRetrieve).get()
    def result = [
        data: multiGetItemResponses.responses.collect {
          [type: it.type, id: it.id, attributes: it.response.getSourceAsMap()]
        },
        meta: [
            total: totalCount,
            took: response.tookInMillis
        ]
    ]

    def facets = prepareFacets(response, true)
    if (facets) {
      result.meta.facets = facets
    }

    return result
  }

  private getGranuleResults(SearchRequestBuilder searchRequestBuilder, Map pageParams) {
    int from = pageParams?.offset ?: 0
    int size = pageParams?.max ?: 100
    searchRequestBuilder = searchRequestBuilder.setFrom(from).setSize(size)

    def searchResponse = searchRequestBuilder.execute().actionGet()
    def result = [
        data: searchResponse.hits.hits.collect({ [type: it.type, id: it.id, attributes: it.source] }),
        meta: [
            took : searchResponse.tookInMillis,
            total: searchResponse.hits.totalHits,
        ]
    ]

    def facets = prepareFacets(searchResponse, false)
    if (facets) {
      result.meta.facets = facets
    }

    return result
  }

  private static final topLevelKeywords = [
      'science': [
          'Agriculture', 'Atmosphere', 'Biological Classification', 'Biosphere', 'Climate Indicators',
          'Cryosphere', 'Human Dimensions', 'Land Surface', 'Oceans', 'Paleoclimate', 'Solid Earth',
          'Spectral/Engineering', 'Sun-Earth Interactions', 'Terrestrial Hydrosphere'
      ],
      'location': [
          'Continent', 'Geographic Region', 'Ocean', 'Solid Earth', 'Space', 'Vertical Location'
      ]
  ]

  private Map prepareFacets(SearchResponse searchResponse, boolean collections) {
    def aggregations = searchResponse.aggregations
    if (!aggregations) {
      return null
    }
    def facetNames = searchRequestParserService.facetNameMappings.keySet()
    def hasFacets = false
    def result = [:]
    facetNames.each { name ->
      def topLevelKeywords = topLevelKeywords[name]
      def buckets = aggregations?.get(name)?.getBuckets()
      if (buckets) {
        hasFacets = true
      }
      result[name] = cleanAggregation(topLevelKeywords, buckets, collections)
    }
    return hasFacets ? result : null
  }

  private Map cleanAggregation(List<String> topLevelKeywords, List<Terms.Bucket> originalAgg, boolean collections) {
    def cleanAgg = [:]
    originalAgg.each { e ->
      def term = e.key as String
      def count = collections ?
          e.getAggregations().get('byCollection').getBuckets().size() :
          e.docCount

      if(!topLevelKeywords) {
        cleanAgg.put(term, [count: count])
      }
      else {
        if (term.contains('>')) {
          def splitTerms = term.split('>', 2)
          if (topLevelKeywords.contains(splitTerms[0].trim())) {
            cleanAgg.put(term, [count: count])
          }
        }
        else {
          if(topLevelKeywords.contains(term)) {
            cleanAgg.put(term, [count: count])
          }
        }
      }
    }
    return cleanAgg
  }
}
