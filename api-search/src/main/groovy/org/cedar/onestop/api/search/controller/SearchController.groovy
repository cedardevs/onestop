package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.search.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.POST

@Slf4j
@RestController
class SearchController {

  private UiConfig uiConfig
  private ElasticsearchService elasticsearchService

  @Autowired
  public SearchController(ElasticsearchService elasticsearchService, UiConfig uiConfig) {
    this.elasticsearchService = elasticsearchService
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
    log.info("incoming search params: ${params}")
    return elasticsearchService.search(params)
  }

  @RequestMapping(path = '/search/totalCounts', method = GET)
  Map totalCounts() {
    return elasticsearchService.totalCounts()
  }

  @RequestMapping(path = '/search/uiConfig', method = GET)
  UiConfig uiConfig() {
    return uiConfig
  }

}
