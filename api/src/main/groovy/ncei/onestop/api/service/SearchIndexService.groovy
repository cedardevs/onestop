package ncei.onestop.api.service

import groovy.util.logging.Slf4j
import org.elasticsearch.action.get.MultiGetResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.search.aggregations.Aggregations
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

@Slf4j
@Service
class SearchIndexService {

  @Value('${elasticsearch.index.search.name}')
  private String SEARCH_INDEX

  @Value('${elasticsearch.index.search.collectionType}')
  private String COLLECTION_TYPE

  @Value('${elasticsearch.index.search.granuleType}')
  private String GRANULE_TYPE

  private Client client
  private SearchRequestParserService searchRequestParserService
  private IndexAdminService indexAdminService

  @Autowired
  public SearchIndexService(Client client,
                            SearchRequestParserService searchRequestParserService,
                            IndexAdminService indexAdminService) {
    this.client = client
    this.searchRequestParserService = searchRequestParserService
    this.indexAdminService = indexAdminService
  }

  public void refresh() {
    indexAdminService.refresh(SEARCH_INDEX)
  }

  public void drop() {
    indexAdminService.drop(SEARCH_INDEX)
  }

  @PostConstruct
  public void ensure() {
    def searchExists = client.admin().indices().prepareAliasesExist(SEARCH_INDEX).execute().actionGet().exists
    if (!searchExists) {
      def realName = indexAdminService.create(SEARCH_INDEX, [GRANULE_TYPE, COLLECTION_TYPE])
      client.admin().indices().prepareAliases().addAlias(realName, SEARCH_INDEX).execute().actionGet()
    }
  }

  public void recreate() {
    drop()
    ensure()
  }

  public Map search(Map searchParams) {
    def response = queryElasticSearch(searchParams)
    response
  }

  private Map queryElasticSearch(Map params) {

    // Parse the request
    def parsedRequest = searchRequestParserService.parseSearchRequest(params)
    def query = parsedRequest.query
    def queryWithAllFilters = parsedRequest.queryWithAllFilters
    def postFilter = parsedRequest.postFilter

    if (parsedRequest.collections) {
      // Returning collection results:
      if (params.facets) {
        if (postFilter) {
          /* facets && postFilter */
          def searchResponse1 = queryAgainstGranules(query, true, false, false)
          def facets = prepareAggregationsForUI(searchResponse1.aggregations, parsedRequest.collections)

          def searchResponse2 = queryAgainstGranules(queryWithAllFilters, false, false, true)
          def result = getAllCollectionDocuments(searchResponse2, params.page)
          result.meta.took = searchResponse1.tookInMillis + searchResponse2.tookInMillis
          result.meta.facets = facets
          return result

        } else {
          /* facets && !postFilter */
          def searchResponse = queryAgainstGranules(query, true, false, true)
          def result = getAllCollectionDocuments(searchResponse, params.page)
          result.meta.took = searchResponse.tookInMillis
          result.meta.facets = prepareAggregationsForUI(searchResponse.aggregations, parsedRequest.collections)
          return result
        }

      } else {
        def searchResponse
        if (postFilter) {
          /* !facets && postFilter */
          searchResponse = queryAgainstGranules(queryWithAllFilters, false, false, true)
        } else {
          /* !facets && !postFilter */
          searchResponse = queryAgainstGranules(query, false, false, true)
        }
        def result = getAllCollectionDocuments(searchResponse, params.page)
        result.meta.took = searchResponse.tookInMillis
        return result
      }


    } else {
      // Returning granule results:
      def searchResponse = queryAgainstGranules(query, postFilter, params.page, params.facets ?: false, true, false)
      def result = [
          data: searchResponse.hits.hits.collect({ [type: it.type, id: it.id, attributes: it.source] }),
          meta: [
              took : searchResponse.tookInMillis,
              total: searchResponse.hits.totalHits,
          ]
      ]

      if (searchResponse.aggregations) {
        result.meta.facets = prepareAggregationsForUI(searchResponse.aggregations, false)
      }
      return result
    }

  }

  private SearchResponse queryAgainstGranules(QueryBuilder query,
                                              QueryBuilder postFilter = null,
                                              Map paginationParams = null,
                                              boolean getGCMDFacets,
                                              boolean getGranuleResults,
                                              boolean getCollectionsAgg) {

    def srb = client.prepareSearch(SEARCH_INDEX).setTypes(GRANULE_TYPE).setQuery(query)

    if(postFilter) { srb = srb.setPostFilter(postFilter) }

    if(getGCMDFacets) {
      def aggregations = searchRequestParserService.createGCMDAggregations(true)
      aggregations.each { a -> srb = srb.addAggregation(a) }
    }

    if(!getGranuleResults) {
      // Pagination needs to be handled on collections -- only getting aggregations here
      if(getCollectionsAgg) { srb = srb.addAggregation(searchRequestParserService.createCollectionsAggregation()) }
      srb = srb.setSize(0)
    } else {
      if (paginationParams) {
        srb = srb.setFrom(paginationParams.offset).setSize(paginationParams.max)
      } else {
        srb = srb.setFrom(0).setSize(100)
      }
    }

    log.debug("ES query:${srb}")
    return srb.execute().actionGet()
  }

  private Map getAllCollectionDocuments(SearchResponse response, Map paginationParams) {

    def collections = response.aggregations.get('collections').getBuckets().collect({ it.key as String })

    if(!collections) {
      return [
          data: [],
          meta: [
              total: 0
          ]
      ]
    }

    def rangeStart
    def rangeEnd
    if(paginationParams) {
      rangeStart = paginationParams.offset
      rangeEnd = rangeStart + paginationParams.max - 1
    } else {
      // Default first 100 results returned
      rangeStart = 0
      if (collections.size >= 100) {
        rangeEnd = 99
      } else {
        rangeEnd = collections.size - 1
      }
    }
    def collectionsToRetrieve = collections[rangeStart..rangeEnd]

    MultiGetResponse multiGetItemResponses = client.prepareMultiGet().add(SEARCH_INDEX, COLLECTION_TYPE, collectionsToRetrieve).get()
    def result = [
        data: multiGetItemResponses.responses.collect {
          [type: it.type, id: it.id, attributes: it.response.getSourceAsMap()]
        },
        meta: [
            total: collections.size()
        ]
    ]
    return result
  }

  private Map prepareAggregationsForUI(Aggregations aggs, boolean collections) {

    def topLevelLocations = ['Continent', 'Geographic Region', 'Ocean', 'Solid Earth', 'Space', 'Vertical Location']
    def topLevelScience =
        ['Agriculture', 'Atmosphere', 'Biological Classification', 'Biosphere', 'Climate Indicators',
         'Cryosphere', 'Human Dimensions', 'Land Surface', 'Oceans', 'Paleoclimate', 'Solid Earth',
         'Spectral/Engineering', 'Sun-Earth Interactions', 'Terrestrial Hydrosphere']

    def scienceAgg = cleanAggregation(topLevelScience, aggs.get('science').getBuckets(), collections)
    def locationsAgg = cleanAggregation(topLevelLocations, aggs.get('locations').getBuckets(), collections)

    def instrumentsAgg = cleanAggregation(null, aggs.get('instruments').getBuckets(), collections)
    def platformsAgg = cleanAggregation(null, aggs.get('platforms').getBuckets(), collections)
    def projectsAgg = cleanAggregation(null, aggs.get('projects').getBuckets(), collections)
    def dataCentersAgg = cleanAggregation(null, aggs.get('dataCenters').getBuckets(), collections)
    def dataResolutionAgg = cleanAggregation(null, aggs.get('dataResolution').getBuckets(), collections)

    return [
        science: scienceAgg,
        locations: locationsAgg,
        instruments: instrumentsAgg,
        platforms: platformsAgg,
        projects: projectsAgg,
        dataCenters: dataCentersAgg,
        dataResolution: dataResolutionAgg
    ]
  }

  private List cleanAggregation(List<String> topLevelKeywords, List<Terms.Bucket> originalAgg, boolean collections) {

    def cleanAgg = []
    originalAgg.each { e ->
      def term = e.key as String
      def count
      if(collections) {
        count = e.getAggregations().get('byCollection').getBuckets().size()
      } else {
        count = e.docCount
      }

      if(!topLevelKeywords) {
        cleanAgg.add([term: term, count: count])

      } else {
        if(term.contains('>')) {
          def splitTerms = term.split('>', 2)
          if(topLevelKeywords.contains(splitTerms[0].trim())) {
            cleanAgg.add([term: term, count: count])
          }

        } else {
          if(topLevelKeywords.contains(term)) {
            cleanAgg.add([term: term, count: count])
          }
        }
      }
    }
    return cleanAgg
  }
}
