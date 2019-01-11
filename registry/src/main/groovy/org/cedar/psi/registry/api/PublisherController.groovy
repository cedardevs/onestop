package org.cedar.psi.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.cedar.psi.common.constants.Topics
import org.cedar.psi.registry.service.Publisher
import org.cedar.schemas.avro.psi.RecordType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.*

@Slf4j
@CompileStatic
@RestController
@RequestMapping(value = "/metadata", consumes = ["application/xml", "application/json"])
class PublisherController {

  @Autowired
  Publisher publisher

  @RequestMapping(value = "/{type}", method = [POST, PUT])
  Map receiveContent(HttpServletRequest request, HttpServletResponse response, @RequestBody String data, @PathVariable String type) throws Exception {
    receiveContent(request, response, data, type, Topics.DEFAULT_SOURCE, null)
  }

  @RequestMapping(value = "/{type}/{id}", method = [POST, PUT, PATCH])
  Map receiveContent(HttpServletRequest request, HttpServletResponse response, @RequestBody String data, @PathVariable String type, @PathVariable UUID id) throws Exception {
    receiveContent(request, response, data, type, Topics.DEFAULT_SOURCE, id)
  }

  @RequestMapping(value = "/{type}/{source}/{id}", method = [DELETE])
  Map removeContent(HttpServletRequest request, HttpServletResponse response, @PathVariable String type, @PathVariable String source, @PathVariable UUID id) throws Exception {
    receiveContent(request, response, null, type, source, id)
  }

  @RequestMapping(value = "/{type}/{source}/{id}", method = [POST, PUT, PATCH])
  Map receiveContent(HttpServletRequest request, HttpServletResponse response, @RequestBody String data, @PathVariable String type, @PathVariable String source, @PathVariable UUID id) throws Exception {
    RecordType recordType = type in RecordType.values()*.name() ? RecordType.valueOf(type) : null
    def result = publisher.publishMetadata(request, recordType, data, source, id as String)
    response.status = result.status as Integer
    return result.content as Map
  }

}
