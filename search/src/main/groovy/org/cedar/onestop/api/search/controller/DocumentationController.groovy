package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.search.service.DocumentationService
import org.cedar.onestop.api.search.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.HEAD

@Slf4j
@RestController
class DocumentationController {

  private ElasticsearchService elasticsearchService

  @Autowired
  DocumentationController(ElasticsearchService elasticsearchService) {
    this.elasticsearchService = elasticsearchService
  }

  @RequestMapping(path = "/docs/attributes/collection", method = [GET, HEAD], produces = 'application/json')
  Map getCollectionAttributes(HttpServletResponse response) {
    return getAttributesResponse(response, elasticsearchService.getCollectionMapping())
  }

  @RequestMapping(path = "/docs/attributes/granule", method = [GET, HEAD], produces = 'application/json')
  Map getGranuleAttributes(HttpServletResponse response) {
    return getAttributesResponse(response, elasticsearchService.getGranuleMapping())
  }

  @RequestMapping(path = "/docs/attributes/flattened-granule", method = [GET, HEAD], produces = 'application/json')
  Map getFlattenedGranuleAttributes(HttpServletResponse response){
    return getAttributesResponse(response, elasticsearchService.getFlattenedGranuleMapping())
  }

  private Map getAttributesResponse(HttpServletResponse response, Map esMapResponse) {
    if(esMapResponse.data) {
      def attributes = DocumentationService.generateAttributesInfo(esMapResponse)
      response.status = HttpStatus.OK.value()
      return [
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
