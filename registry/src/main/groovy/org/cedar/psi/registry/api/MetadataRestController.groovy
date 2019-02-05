package org.cedar.psi.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.cedar.psi.common.constants.Topics
import org.cedar.psi.registry.service.MetadataStore
import org.cedar.schemas.avro.psi.RecordType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
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
  Map retrieveInput(
      @PathVariable String type,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) {
    retrieveInput(type, Topics.DEFAULT_SOURCE, id, request, response)
  }

  @RequestMapping(path = '/metadata/{type}/{source}/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveInput(
      @PathVariable String type,
      @PathVariable String source,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) {
    RecordType recordType = type in RecordType.values()*.name() ? RecordType.valueOf(type) : null
    def result = metadataStore.retrieveInput(recordType, source, id)
    def baseUrl = entityBaseUrl(request)

    if (result) {
      return [
          "links": [
              "self"  : baseUrl,
              "parsed": baseUrl + '/parsed'
          ],
          data   : [
              id        : id,
              type      : type,
              attributes: result
          ]
      ]
    }
    else {
      response.status = HttpStatus.NOT_FOUND.value()
      return [
          "links": [
              "self"  : baseUrl,
          ],
          errors : [
              [
                  status: HttpStatus.NOT_FOUND.value(),
                  title : HttpStatus.NOT_FOUND.toString(),
                  detail: "No input exists for ${type} with id [${id}] from source [${source}]" as String
              ]
          ]
      ]
    }
  }

  @RequestMapping(path = '/metadata/{type}/{id}/parsed', method = [GET, HEAD], produces = 'application/json')
  Map retrieveParsed(
      @PathVariable String type,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) {
    retrieveParsed(type, Topics.DEFAULT_SOURCE, id, request, response)
  }

  @RequestMapping(path = '/metadata/{type}/{source}/{id}/parsed', method = [GET, HEAD], produces = 'application/json')
  Map retrieveParsed(
      @PathVariable String type,
      @PathVariable String source,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) {

    RecordType recordType = type in RecordType.values()*.name() ? RecordType.valueOf(type) : null
    def result = metadataStore.retrieveParsed(recordType, source, id)
    def baseUrl = entityBaseUrl(request)

    if (result) {
      return [
          "links": [
              "self" : baseUrl,
              "input": baseUrl.replaceAll('\\/parsed$', '')
          ],
          data   : [
              id        : id,
              type      : type,
              attributes: result
          ]
      ]
    }
    else {
      response.status = HttpStatus.NOT_FOUND.value()
      return [
          "links": [
              "self" : baseUrl,
              "input": baseUrl.replaceAll('\\/parsed$', '')
          ],
          errors : [
              [
                  status: HttpStatus.NOT_FOUND.value(),
                  title : HttpStatus.NOT_FOUND.toString(),
                  detail: "No parsed values exist for ${type} with id [${id}] from source [${source}]" as String
              ]
          ]
      ]
    }
  }

  private String entityBaseUrl(HttpServletRequest request) {
    try {
      return request.requestURL.toString()
    }
    catch (Exception e) {
      log.error("Failed to construct uri for ${request}", e)
      throw null
    }
  }
}
