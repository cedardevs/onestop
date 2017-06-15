package ncei.onestop.api.controller

import groovy.util.logging.Slf4j
import ncei.onestop.api.service.SearchIndexService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.*

@Slf4j
@RestController
class SearchController {

  private SearchIndexService searchIndexService
  private UiConfig uiConfig

  @Autowired
  public SearchController(SearchIndexService searchIndexService, UiConfig uiConfig) {
    this.searchIndexService = searchIndexService
    this.uiConfig = uiConfig
  }

  // POST in order to support request bodies from clients that won't send bodies with GETs
  @RequestMapping(path = "/search", method = [POST, GET])
  Map search(@RequestBody Map params, HttpServletResponse response) {
    Map validation = JsonValidator.validateSearchRequestSchema(params)
    if (!validation.success) {
      log.debug("invalid request: ${validation.errors.detail?.join(', ')}")
      response.status = HttpStatus.BAD_REQUEST.value()
      return [errors: validation.errors]
    }

    return searchIndexService.search(params)
  }

  @RequestMapping(path = '/search/totalCounts', method = GET)
  Map totalCounts() {
    return searchIndexService.totalCounts()
  }


  @RequestMapping(path = '/search/uiConfig', method = GET)
  UiConfig uiConfig() {
    return uiConfig
  }

}
