package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j

import org.cedar.onestop.api.search.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.HEAD

import org.cedar.onestop.api.search.service.DocumentationService

@Slf4j
@RestController
class DocumentationController {

  private ElasticsearchService elasticsearchService

  @Autowired
  DocumentationController(ElasticsearchService elasticsearchService) {
    this.elasticsearchService = elasticsearchService
  }

  @RequestMapping(path = ["/docs/attributes/collection", "/v1/docs/attributes/collection"], method = [GET, HEAD], produces = 'application/json')
  Map getCollectionAttributes(HttpServletResponse response) {
    log.info("Request URI: ${request.getRequestURI()}")
    return getAttributesResponse(response, elasticsearchService.getCollectionMapping())
  }

  @RequestMapping(path = ["/docs/attributes/granule", "/v1/docs/attributes/granule"], method = [GET, HEAD], produces = 'application/json')
  Map getGranuleAttributes(HttpServletResponse response) {
    log.info("Request URI: ${request.getRequestURI()}")
    return getAttributesResponse(response, elasticsearchService.getGranuleMapping())
  }

  @RequestMapping(path = ["/docs/attributes/flattened-granule", "/v1/docs/attributes/flattened-granule"], method = [GET, HEAD], produces = 'application/json')
  Map getFlattenedGranuleAttributes(HttpServletResponse response){
    log.info("Request URI: ${request.getRequestURI()}")
    return getAttributesResponse(response, elasticsearchService.getFlattenedGranuleMapping())
  }

  private Map getAttributesResponse(HttpServletResponse response, Map esMapResponse) {
    if(esMapResponse.data) {

      def attributes = DocumentationService.generateAttributesInfo(esMapResponse.data[0].attributes.mappings as Map)

      response.status = HttpStatus.OK.value()
      return [
          meta: [
              info: [
                  queryable: 'Whether an attribute can be searched against. Non-queryable fields provide supplemental information about a record only, such as link URLs.',
                  exactMatchRequired: 'Only applicable to queryable fields. If true, text must match exactly or a regular expression pattern must exactly match the full text. If false, partial matches will return results.',
                  applicableFilter: 'Filter correlating to this attribute. Filters are faster and more precise in narrowing results but do not affect search rankings.'
              ]
          ],
          data: [[
                     id: esMapResponse.data[0].id,
                     type: 'docs-attributes',
                     attributes: attributes
                 ]]
      ]
    }
    else {
      response.status = HttpStatus.NOT_FOUND.value()
      return esMapResponse
    }
  }
}
