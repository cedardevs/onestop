package org.cedar.onestop.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.cedar.onestop.kafka.common.constants.Topics
import org.cedar.onestop.registry.service.Publisher
import org.cedar.schemas.avro.psi.RecordType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.*

@Slf4j
@CompileStatic
@RestController
@RequestMapping(value = "/metadata")
class PublisherController {

  @Autowired
  Publisher publisher

  @RequestMapping(value = "/{type}/{source}/{id}", method = [POST, PUT], consumes = ["application/xml", "application/json"], produces = 'application/json')
  Map receiveContent(HttpServletRequest request, HttpServletResponse response, @RequestBody String data,
                     @PathVariable String type,
                     @PathVariable String source,
                     @PathVariable String id) throws Exception {
    handleRequest(request, response, data, type, source, id, null)
  }

  @RequestMapping(value = "/{type}", method = [POST, PUT], consumes = ["application/xml", "application/json"], produces = 'application/json')
  Map receiveContent(HttpServletRequest request, HttpServletResponse response, @RequestBody String data,
                     @PathVariable String type) throws Exception {
    handleRequest(request, response, data, type, Topics.DEFAULT_SOURCE, null, null)
  }

  @RequestMapping(value = "/{type}/{id}", method = [POST, PUT], consumes = ["application/xml", "application/json"], produces = 'application/json')
  Map receiveContent(HttpServletRequest request, HttpServletResponse response, @RequestBody String data,
                     @PathVariable String type,
                     @PathVariable String id) throws Exception {
    handleRequest(request, response, data, type, Topics.DEFAULT_SOURCE, id, null)
  }

  @RequestMapping(value = "/{type}/{id}", method = PATCH, consumes = "application/json", produces = 'application/json')
  Map patchContent(HttpServletRequest request, HttpServletResponse response, @RequestBody String data,
                   @PathVariable String type,
                   @PathVariable String id,
                   @RequestParam(value = "op", required = false, defaultValue = "NO_OP") String op) throws Exception {
    handleRequest(request, response, data, type, Topics.DEFAULT_SOURCE, id, op)
  }

  @RequestMapping(value = "/{type}/{source}/{id}", method = PATCH, consumes = "application/json", produces = 'application/json')
  Map patchContent(HttpServletRequest request, HttpServletResponse response, @RequestBody String data,
                   @PathVariable String type,
                   @PathVariable String source,
                   @PathVariable String id,
                   @RequestParam(value = "op", required = false, defaultValue = "NO_OP") String op) throws Exception {
    handleRequest(request, response, data, type, source, id, op)
  }

  @RequestMapping(value = "/{type}/{id}", method = [DELETE], consumes = ['*'],produces = 'application/json')
  Map removeContent(HttpServletRequest request, HttpServletResponse response,
                    @PathVariable String type,
                    @PathVariable String id) throws Exception {
    handleRequest(request, response, null, type, Topics.DEFAULT_SOURCE, id, null)
  }

  @RequestMapping(value = "/{type}/{source}/{id}", method = [DELETE], consumes = ['*'],produces = 'application/json')
  Map removeContent(HttpServletRequest request, HttpServletResponse response,
                    @PathVariable String type,
                    @PathVariable String source,
                    @PathVariable String id) throws Exception {
    handleRequest(request, response, null, type, source, id, null)
  }

  @RequestMapping(value = "/{type}/{id}/resurrection", method = GET, produces = 'application/json')
  Map resurrectContent(@PathVariable String type,
                       @PathVariable String id,
                       HttpServletRequest request, HttpServletResponse response) {
    handleRequest(request, response, null, type, Topics.DEFAULT_SOURCE, id, null)
  }

  @RequestMapping(value = "/{type}/{source}/{id}/resurrection", method = GET, produces = 'application/json')
  Map resurrectContent(@PathVariable String type,
                       @PathVariable String source,
                       @PathVariable String id,
                       HttpServletRequest request, HttpServletResponse response) {
    handleRequest(request, response, null, type, source, id, null)
  }

  private Map handleRequest(HttpServletRequest request, HttpServletResponse response, String data, String type,
                            String source, String id, String op) {
    RecordType recordType = type in RecordType.values()*.name() ? RecordType.valueOf(type) : null
    def result = publisher.publishMetadata(request, recordType, data, source, id, op)
    response.status = result.status as Integer
    return result.content as Map
  }
}
