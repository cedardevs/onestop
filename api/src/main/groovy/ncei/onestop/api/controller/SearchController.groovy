package ncei.onestop.api.controller

import groovy.util.logging.Slf4j
import ncei.onestop.api.pojo.OneStopSearchRequest
import ncei.onestop.api.pojo.OneStopSearchResponse
import ncei.onestop.api.service.SearchService
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@Slf4j
@RestController
//@RequestMapping(value = "/")
class SearchController {

    private SearchService searchService

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService
    }

    // POST in order to support request body
    @RequestMapping(value = "/search", method = RequestMethod.POST)//, consumes = "application/json")
    public @ResponseBody OneStopSearchResponse search(@RequestBody OneStopSearchRequest searchRequest) {
        println("searchRequest:${searchRequest}")
        def response = searchService.search(searchRequest)
        response
    }
}

