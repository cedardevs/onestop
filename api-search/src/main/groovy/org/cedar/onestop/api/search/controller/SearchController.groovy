package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.search.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.HEAD
import static org.springframework.web.bind.annotation.RequestMethod.POST

import org.springframework.beans.factory.annotation.Value

@Slf4j
@RestController
class SearchController {

  private UiConfig uiConfig
  private ElasticsearchService elasticsearchService

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.collection.name}')
  private String COLLECTION_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.granule.name}')
  private String GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.flattenedGranule.name}')
  private String FLATTENED_GRANULE_SEARCH_INDEX

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
    return elasticsearchService.search(params, FLATTENED_GRANULE_SEARCH_INDEX)
  }

  // GET Collection by ID
  @RequestMapping(path = "/collection/{id}", method = [GET, HEAD], produces = 'application/json')
  Map getCollection(@PathVariable String id, HttpServletResponse response) {
    def result = elasticsearchService.getCollectionById(id)
    if (result.data) {
      response.status = HttpStatus.OK.value()
    }
    else {
      response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
    }
    return result
  }

  // Search Collections
  @RequestMapping(path = "/collection/search", method = [POST, GET])
  Map searchCollections(@RequestBody Map params, HttpServletResponse response) {
    Map validation = JsonValidator.validateSearchRequestSchema(params)
    if (!validation.success) {
      log.debug("invalid request: ${validation.errors.detail?.join(', ')}")
      response.status = HttpStatus.BAD_REQUEST.value()
      return [errors: validation.errors]
    }
    log.info("incoming search params: ${params}")
    return elasticsearchService.search(params, COLLECTION_SEARCH_INDEX)
  }

  // GET Granule by ID
  @RequestMapping(path = "/granule/{id}", method = [GET, HEAD], produces = 'application/json')
  Map getGranule(@PathVariable String id, HttpServletResponse response) {
    def result = elasticsearchService.getGranuleById(id)
    if (result.data) {
      response.status = HttpStatus.OK.value()
    }
    else {
      response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
    }
    return result
  }

  // Search Granules
  @RequestMapping(path = "/granule/search", method = [POST, GET])
  Map searchGranules(@RequestBody Map params, HttpServletResponse response) {
    Map validation = JsonValidator.validateSearchRequestSchema(params)
    if (!validation.success) {
      log.debug("invalid request: ${validation.errors.detail?.join(', ')}")
      response.status = HttpStatus.BAD_REQUEST.value()
      return [errors: validation.errors]
    }
    log.info("incoming search params: ${params}")
    return elasticsearchService.search(params, GRANULE_SEARCH_INDEX)
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
