package ncei.onestop.api.service

import groovy.util.logging.Slf4j
import org.elasticsearch.client.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

@Slf4j
@Service
class SearchIndexService {

  @Value('${elasticsearch.index.search.name}')
  private String SEARCH_INDEX

  @Value('${elasticsearch.index.search.type}')
  private String SEARCH_TYPE

  private Client client
  private SearchRequestParserService searchRequestParserService
  private SearchResponseParserService searchResponseParserService
  private IndexAdminService indexAdminService

  @Autowired
  public SearchIndexService(Client client,
                            SearchRequestParserService searchRequestParserService,
                            SearchResponseParserService searchResponseParserService,
                            IndexAdminService indexAdminService) {
    this.client = client
    this.searchRequestParserService = searchRequestParserService
    this.searchResponseParserService = searchResponseParserService
    this.indexAdminService = indexAdminService
  }


  Map search(Map searchParams) {
    def response = queryElasticSearch(searchParams)
    response
  }

  private Map queryElasticSearch(Map params) {
    def parsedRequest = searchRequestParserService.parseSearchRequest(params)
    def query = parsedRequest.query
    def postFilters = parsedRequest.postFilters

    // Assemble the search request:
    def srb = client.prepareSearch(SEARCH_INDEX)
    srb = srb.setTypes(SEARCH_TYPE).setQuery(query)
    if(postFilters) { srb = srb.setPostFilter(postFilters) }
    if(params.facets) {
      def aggregations = searchRequestParserService.createDefaultAggregations()
      aggregations.each { a -> srb = srb.addAggregation(a) }
    }


    if(params.page) {
      srb = srb.setFrom(params.page.offset).setSize(params.page.max)
    } else {
      srb = srb.setFrom(0).setSize(100)
    }

    log.debug("ES query:${srb} params:${params}")

    def searchResponse = srb.execute().actionGet()
    return searchResponseParserService.searchResponseParser(searchResponse)
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
      def realName = indexAdminService.create(SEARCH_INDEX, [SEARCH_TYPE])
      client.admin().indices().prepareAliases().addAlias(realName, SEARCH_INDEX).execute().actionGet()
    }
  }

  public void recreate() {
    drop()
    ensure()
  }
}
