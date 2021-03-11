package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j

import org.cedar.onestop.api.search.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.HEAD

import org.cedar.onestop.api.search.service.DocumentationService
import org.cedar.onestop.api.search.service.ApiRootGenerator

@Slf4j
@RestController
class DocumentationController {

  private ElasticsearchService elasticsearchService
  private ApiRootGenerator apiLinkGenerator

  @Autowired
  DocumentationController(ElasticsearchService elasticsearchService, ApiRootGenerator apiLinkGenerator) {
    this.elasticsearchService = elasticsearchService
    this.apiLinkGenerator = apiLinkGenerator
  }

  @RequestMapping(path = "/", method = [GET, HEAD])
  Map getApiRoot(HttpServletRequest request, HttpServletResponse response) {
    return response.sendRedirect("${apiLinkGenerator.getApiRoot(request)}/openapi.yaml")
  }

  @RequestMapping(path = ["/docs/attributes/collection", "/v1/docs/attributes/collection"], method = [GET, HEAD], produces = 'application/json')
  Map getCollectionAttributes(HttpServletResponse response) {
    return getAttributesResponse(response, elasticsearchService.getCollectionMapping())
  }

  @RequestMapping(path = ["/docs/attributes/granule", "/v1/docs/attributes/granule"], method = [GET, HEAD], produces = 'application/json')
  Map getGranuleAttributes(HttpServletResponse response) {
    return getAttributesResponse(response, elasticsearchService.getGranuleMapping())
  }

  @RequestMapping(path = ["/docs/attributes/flattened-granule", "/v1/docs/attributes/flattened-granule"], method = [GET, HEAD], produces = 'application/json')
  Map getFlattenedGranuleAttributes(HttpServletResponse response){
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
