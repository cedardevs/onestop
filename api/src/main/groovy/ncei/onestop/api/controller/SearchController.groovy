package ncei.onestop.api.controller

import ncei.onestop.api.pojo.OneStopSearchRequest
import ncei.onestop.api.pojo.OneStopSearchResponse
import ncei.onestop.api.service.SearchRequestParserService
import ncei.onestop.api.service.SearchService
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.ResponseBody

//@Controller or @RestController?
@RequestMapping(value = "") // FIXME
class SearchController {

    private SearchService searchService

    @Autowired
    public SearchController(SearchRequestParserService searchParserService, SearchService searchService) {
        this.searchParserService = searchParserService
        this.searchService = searchService
    }



    // TODO method GET or POST?
    @RequestMapping(value = "/search", method = RequestMethod.GET, consumes = "application/json")
    public @ResponseBody OneStopSearchResponse search(@RequestBody OneStopSearchRequest searchRequest) {

        def response = searchService.search(searchRequest)
        response
    }
}

