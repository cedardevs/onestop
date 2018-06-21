package org.cedar.onestop.api.search.controller

import groovy.json.JsonOutput

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.search.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.HEAD
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

  // Get Collection Info
  @RequestMapping(path = "/collection", method = [GET, HEAD], produces = 'application/json')
  Map getCollectionInfo(HttpServletResponse response) {
    return elasticsearchService.totalCollections()
  }

  // GET Collection by ID
  @RequestMapping(path = "/collection/{id}", method = [GET, HEAD], produces = 'application/json')
  Map getCollection(@PathVariable String id, HttpServletResponse response, HttpServletRequest request) {
    def result = elasticsearchService.getCollectionById(id)
    if (result.data) {
      response.status = HttpStatus.OK.value()
      log.info("${request.getMethod()} collection ID param: {\"id\":\"${id}\"}")
    }
    else {
      response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
    }

    return result
  }

  // Search Collections
  @RequestMapping(path = "/search/collection", method = [POST, GET])
  Map searchCollections(@RequestBody Map params, HttpServletResponse response, HttpServletRequest request) {
    Map validation = JsonValidator.validateSearchRequestSchema(params)
    if (!validation.success) {
      log.debug("invalid request: ${validation.errors.detail?.join(', ')}")
      response.status = HttpStatus.BAD_REQUEST.value()
      return [errors: validation.errors]
    }
    log.info("${request.getMethod()} collection search params: ${JsonOutput.toJson(params)}")
    return elasticsearchService.searchCollections(params)
  }

  // Get Granule Info
  @RequestMapping(path = "/granule", method = [GET, HEAD], produces = 'application/json')
  Map getGranuleInfo(HttpServletResponse response) {
      return elasticsearchService.totalGranules()
  }

  // GET Granule by ID
  @RequestMapping(path = "/granule/{id}", method = [GET, HEAD], produces = 'application/json')
  Map getGranule(@PathVariable String id, HttpServletResponse response, HttpServletRequest request) {
    def result = elasticsearchService.getGranuleById(id)
    if (result.data) {
      response.status = HttpStatus.OK.value()
      log.info("${request.getMethod()} granule ID param: {\"id\":\"${id}\"}")
    }
    else {
      response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
    }

    return result
  }

  // Search Granules
  @RequestMapping(path = "/search/granule", method = [POST, GET])
  Map searchGranules(@RequestBody Map params, HttpServletResponse response, HttpServletRequest request) {
    Map validation = JsonValidator.validateSearchRequestSchema(params)
    if (!validation.success) {
      log.debug("invalid request: ${validation.errors.detail?.join(', ')}")
      response.status = HttpStatus.BAD_REQUEST.value()
      return [errors: validation.errors]
    }
    log.info("${request.getMethod()} granule search params: ${JsonOutput.toJson(params)}")
    return elasticsearchService.searchGranules(params)
  }

  // Get Flattened Granule Info
  @RequestMapping(path = "/flattened-granule", method = [GET, HEAD], produces = 'application/json')
  Map getFlattenedGranuleInfo(HttpServletResponse response) {
    return elasticsearchService.totalFlattenedGranules()
  }

  // GET Flattened Granule by ID
  @RequestMapping(path = "/flattened-granule/{id}", method = [GET, HEAD], produces = 'application/json')
  Map getFlattenedGranule(@PathVariable String id, HttpServletResponse response, HttpServletRequest request) {
    def result = elasticsearchService.getFlattenedGranuleById(id)
    if (result.data) {
      response.status = HttpStatus.OK.value()
      log.info("${request.getMethod()} flattened-granule ID param: {\"id\":\"${id}\"}")
    }
    else {
      response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
    }
    return result
  }

  // Search Flattened Granules
  @RequestMapping(path = "/search/flattened-granule", method = [POST, GET])
  Map searchFlattenedGranules(@RequestBody Map params, HttpServletResponse response, HttpServletRequest request) {
    Map validation = JsonValidator.validateSearchRequestSchema(params)
    if (!validation.success) {
      log.debug("invalid request: ${validation.errors.detail?.join(', ')}")
      response.status = HttpStatus.BAD_REQUEST.value()
      return [errors: validation.errors]
    }
    log.info("${request.getMethod()} flattened-granule search params: ${JsonOutput.toJson(params)}")
    return elasticsearchService.searchFlattenedGranules(params)
  }

  @RequestMapping(path = '/uiConfig', method = GET)
  UiConfig uiConfig() {
    return uiConfig
  }

}
