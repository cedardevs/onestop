package org.cedar.psi.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.cedar.psi.common.constants.Topics
import org.cedar.psi.registry.service.MetadataStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.HEAD

@Slf4j
@CompileStatic
@RestController
class MetadataRestController {

  @Value('${server.servlet.context-path:}')
  String contextPath

  private MetadataStore metadataStore

  @Autowired
  MetadataRestController(MetadataStore metadataStore) {
    this.metadataStore = metadataStore
  }

  @RequestMapping(path = '/metadata/{type}/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveJson(@PathVariable String type, @PathVariable String id, HttpServletResponse response) {
    retrieveParsedJson(type, Topics.DEFAULT_SOURCE, id, response)
  }

  @RequestMapping(path = '/metadata/{type}/{source}/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveParsedJson(@PathVariable String type, @PathVariable String source, @PathVariable String id, HttpServletResponse response) {
    def result = metadataStore.retrieveParsed(type, source, id)
    String path = "${contextPath}/metadata/${type}/${source}/${id}/input"
    def link = metadataStore.constructUri(path)

    if (!result) {
      response.sendError(404, "No such ${type} with id ${id}")
    }

    return [
        "links": [
            "InputRecord": link
        ],
        data   : result
    ]

  }

  @RequestMapping(path = '/metadata/{type}/{source}/{id}/input', method = [GET, HEAD], produces = 'application/json')
  Map retrieveInputJson(@PathVariable String type, @PathVariable String source, @PathVariable String id, HttpServletResponse response) {
    def result = metadataStore.retrieveInput(type, source, id)
    String path = "${contextPath}/metadata/${type}/${source}/${id}"
    def link  = metadataStore.constructUri(path)

    if (!result) {
      response.sendError(404, "No such ${type} with id ${id}")
    }

    return [
        "links": [
            "ParsedRecord": link
        ],
        data   : result
    ]
  }
}
