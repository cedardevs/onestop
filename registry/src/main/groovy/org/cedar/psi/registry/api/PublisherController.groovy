package org.cedar.psi.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.cedar.psi.registry.service.Publisher

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.POST
import static org.springframework.web.bind.annotation.RequestMethod.PUT

@Slf4j
@CompileStatic
@RestController
@RequestMapping(value = "/metadata", consumes = ["application/xml", "application/json"], method = [POST, PUT])
class PublisherController {

  @Autowired
  Publisher publisher

  @RequestMapping(value = "/{type}")
  Map receiveContent(HttpServletRequest request, HttpServletResponse response, @RequestBody String data, @PathVariable String type) throws Exception {
    receiveContent(request, response, data, type, null, null)
  }

  @RequestMapping(value = "/{type}/{id}")
  Map receiveContent(HttpServletRequest request, HttpServletResponse response, @RequestBody String data, @PathVariable String type, @PathVariable UUID id) throws Exception {
    receiveContent(request, response, data, type, null, id)
  }

  @RequestMapping(value = "/{type}/{source}/{id}")
  Map receiveContent(HttpServletRequest request, HttpServletResponse response, @RequestBody String data, @PathVariable String type, @PathVariable String source, @PathVariable UUID id) throws Exception {
    def result = publisher.publishMetadata(request, type, data, id as String, source)
    response.status = result.status as Integer
    return result.content as Map
  }

}
