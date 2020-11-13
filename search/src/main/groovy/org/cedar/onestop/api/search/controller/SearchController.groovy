package org.cedar.onestop.api.search.controller

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.cedar.onestop.api.search.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.*

@Slf4j
@RestController
class SearchController {

  @Autowired
  private Environment environment

  private UiConfig uiConfig
  private ElasticsearchService elasticsearchService

  @Autowired
  SearchController(ElasticsearchService elasticsearchService, UiConfig uiConfig) {
    this.elasticsearchService = elasticsearchService
    this.uiConfig = uiConfig
  }

  // Get Collection Info
  @CrossOrigin(origins = "*")
  @RequestMapping(path = ["/collection", "/v1/collection"], method = [GET, HEAD], produces = 'application/json')
  Map getCollectionInfo(HttpServletRequest request, HttpServletResponse response) {
    log.info("Request URI: ${request.getRequestURI()}")
    return elasticsearchService.getTotalCollections()
  }

  // GET Collection by ID
  @CrossOrigin(origins = "*")
  @RequestMapping(path = ["/collection/{id}", "/v1/collection/{id}"], method = [GET, HEAD], produces = 'application/json')
  Map getCollection(@PathVariable String id, HttpServletResponse response, HttpServletRequest request) {
    def result = elasticsearchService.getCollectionById(id)
    log.info("Request URI: ${request.getRequestURI()}")
    if (result.data) {
      response.status = HttpStatus.OK.value()
      log.info("${request.getMethod()} collection ID param: {\"id\":\"${id}\"}")
    }
    else {
      response.status = result.errors[0].status ?: HttpStatus.BAD_REQUEST.value()
    }

    return result
  }

  // Search Collections
  @CrossOrigin(origins = "*")
  @RequestMapping(path = ["/search/collection", "/v1/search/collection"], method = [POST, GET])
  Map searchCollections(@RequestBody Map params, HttpServletResponse response, HttpServletRequest request) {
    Map validation = JsonValidator.validateSearchRequestSchema(params)
    log.info("Request URI: ${request.getRequestURI()}")
    if (!validation.success) {
      log.debug("invalid request: ${validation.errors.detail?.join(', ')}")
      response.status = HttpStatus.BAD_REQUEST.value()
      return [errors: validation.errors]
    }
    log.info("${request.getMethod()} collection search params: ${JsonOutput.toJson(params)}")
    return elasticsearchService.searchCollections(params)
  }

  // Get Granule Info
  @CrossOrigin(origins = "*")
  @RequestMapping(path = ["/granule", "/v1/granule"], method = [GET, HEAD], produces = 'application/json')
  Map getGranuleInfo(HttpServletRequest request, HttpServletResponse response) {
    log.info("Request URI: ${request.getRequestURI()}")
    return elasticsearchService.getTotalGranules()
  }

  // GET Granule by ID
  @CrossOrigin(origins = "*")
  @RequestMapping(path = ["/granule/{id}", "/v1/granule/{id}"], method = [GET, HEAD], produces = 'application/json')
  Map getGranule(@PathVariable String id, HttpServletResponse response, HttpServletRequest request) {
    def result = elasticsearchService.getGranuleById(id)
    log.info("Request URI: ${request.getRequestURI()}")
    if (result.data) {
      response.status = HttpStatus.OK.value()
      log.info("${request.getMethod()} granule ID param: {\"id\":\"${id}\"}")
    }
    else {
      response.status = result.errors[0].status ?: HttpStatus.BAD_REQUEST.value()
    }

    return result
  }

  // Search Granules
  @CrossOrigin(origins = "*")
  @RequestMapping(path = ["/search/granule", "/v1/search/granule"], method = [POST, GET])
  Map searchGranules(@RequestBody Map params, HttpServletResponse response, HttpServletRequest request) {
    Map validation = JsonValidator.validateSearchRequestSchema(params)
    log.info("Request URI: ${request.getRequestURI()}")
    if (!validation.success) {
      log.debug("invalid request: ${validation.errors.detail?.join(', ')}")
      response.status = HttpStatus.BAD_REQUEST.value()
      return [errors: validation.errors]
    }
    log.info("${request.getMethod()} granule search params: ${JsonOutput.toJson(params)}")
    return elasticsearchService.searchGranules(params)
  }

  // Get Flattened Granule Info
  @CrossOrigin(origins = "*")
  @RequestMapping(path = ["/flattened-granule", "/v1/flattened-granule"], method = [GET, HEAD], produces = 'application/json')
  Map getFlattenedGranuleInfo(HttpServletRequest request, HttpServletResponse response) {
    log.info("Request URI: ${request.getRequestURI()}")
    return elasticsearchService.getTotalFlattenedGranules()
  }

  // GET Flattened Granule by ID
  @CrossOrigin(origins = "*")
  @RequestMapping(path = ["/flattened-granule/{id}", "/v1/flattened-granule/{id}"], method = [GET, HEAD], produces = 'application/json')
  Map getFlattenedGranule(@PathVariable String id, HttpServletResponse response, HttpServletRequest request) {
    def result = elasticsearchService.getFlattenedGranuleById(id)
    log.info("Request URI: ${request.getRequestURI()}")
    if (result.data) {
      response.status = HttpStatus.OK.value()
      log.info("${request.getMethod()} flattened-granule ID param: {\"id\":\"${id}\"}")
    }
    else {
      response.status = result.errors[0].status ?: HttpStatus.BAD_REQUEST.value()
    }
    return result
  }

  // Search Flattened Granules
  @CrossOrigin(origins = "*")
  @RequestMapping(path = ["/search/flattened-granule", "/v1/search/flattened-granule"], method = [POST, GET])
  Map searchFlattenedGranules(@RequestBody Map params, HttpServletResponse response, HttpServletRequest request) {
    Map validation = JsonValidator.validateSearchRequestSchema(params)
    log.info("Request URI: ${request.getRequestURI()}")
    if (!validation.success) {
      log.debug("invalid request: ${validation.errors.detail?.join(', ')}")
      response.status = HttpStatus.BAD_REQUEST.value()
      return [errors: validation.errors]
    }
    log.info("${request.getMethod()} flattened-granule search params: ${JsonOutput.toJson(params)}")
    return elasticsearchService.searchFlattenedGranules(params)
  }

  @RequestMapping(path = '/uiConfig', method = GET)
  Map uiConfig() {
    // casting as Map seems not to work, so we use Jackson directly
    // to get our UiConfig properties as a Map
    ObjectMapper converter = new ObjectMapper()
    Map uiConfigMap = converter.convertValue(uiConfig, Map.class)

    return uiConfigMap
  }

}
