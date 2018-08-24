package org.cedar.psi.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.cedar.psi.registry.service.Publisher

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

@Slf4j
@CompileStatic
@RestController
@RequestMapping(value = "/metadata")
class PublisherController {

  @Autowired
  Publisher publisher

  @RequestMapping(value = "/granule/{id}", consumes = ["application/xml", "application/json"], method = [RequestMethod.POST, RequestMethod.PUT])
  void receiveGranule(HttpServletRequest request, @RequestBody String data, @PathVariable(required = false) UUID id) throws Exception {
    publisher.publishMetadata(request, 'granule', '', id as String,  data)
  }

  @RequestMapping(value = "/granule/{source}/{id}", consumes = ["application/xml", "application/json"], method = [RequestMethod.POST, RequestMethod.PUT])
  void receiveGranuleFromSource(HttpServletRequest request, @RequestBody String data, @PathVariable(required=false) String source, @PathVariable(required = false) UUID id) throws Exception {
    publisher.publishMetadata(request, 'granule', source, id as String,  data)
  }

  @RequestMapping(value = "/collection/{id}", consumes = ["application/xml", "application/json"], method = [RequestMethod.POST, RequestMethod.PUT])
  void receiveCollection(HttpServletRequest request, @RequestBody String data, @PathVariable(required = false) UUID id) throws Exception {
    publisher.publishMetadata(request, 'collection', '', id as String, data)
  }

  @RequestMapping(value = "/collection/{source}/{id}", consumes = ["application/xml", "application/json"], method = [RequestMethod.POST, RequestMethod.PUT])
  void receiveCollectionFromSource(HttpServletRequest request, @RequestBody String data, @PathVariable(required=false) String source, @PathVariable(required = false) UUID id) throws Exception {
    publisher.publishMetadata(request, 'collection', source, id as String, data)
  }

}
