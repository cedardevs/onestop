package ncei.onestop.api.controller

import ncei.onestop.api.pojo.OneStopSearchRequest
import ncei.onestop.api.pojo.OneStopSearchResponse
import ncei.onestop.api.service.ElasticSearchService
import ncei.onestop.api.service.SearchRequestParserService
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.ResponseBody

//@Controller or @RestController?
@RequestMapping(value = "") // FIXME
class SearchController {

    private SearchRequestParserService searchService
    private ElasticSearchService esService

    @Autowired
    public SearchController(SearchRequestParserService searchService, ElasticSearchService esService) {
        this.searchService = searchService
        this.esService = esService
    }



    // TODO method GET or POST?
    @RequestMapping(value = "/search", method = RequestMethod.GET, consumes = "application/json")
    public @ResponseBody OneStopSearchResponse search(@RequestBody OneStopSearchRequest searchRequest) {

        def request = searchService.parseSearchRequest(searchRequest)
        def response = esService.queryElasticSearch(request)

        response
    }
}

