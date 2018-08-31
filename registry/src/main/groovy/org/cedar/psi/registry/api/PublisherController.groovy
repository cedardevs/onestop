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

import static org.springframework.web.bind.annotation.RequestMethod.POST
import static org.springframework.web.bind.annotation.RequestMethod.PUT

@Slf4j
@CompileStatic
@RestController
@RequestMapping(value = "/metadata", consumes = ["application/xml", "application/json"], method = [POST, PUT])
class PublisherController {

  @Autowired
  Publisher publisher

  @RequestMapping(value = "/granule")
  Map receiveGranule(HttpServletRequest request, @RequestBody String data) throws Exception {
    publisher.publishMetadata(request, 'granule', data, null, null)
  }

  @RequestMapping(value = "/granule/{id}")
  Map receiveGranule(HttpServletRequest request, @RequestBody String data, @PathVariable UUID id) throws Exception {
    publisher.publishMetadata(request, 'granule', data, id as String, null)
  }

  @RequestMapping(value = "/granule/{source}/{id}")
  Map receiveGranuleFromSource(HttpServletRequest request, @RequestBody String data, @PathVariable String source, @PathVariable UUID id) throws Exception {
    publisher.publishMetadata(request, 'granule', data, id as String, source)
  }

  @RequestMapping(value = "/collection")
  Map receiveCollection(HttpServletRequest request, @RequestBody String data) throws Exception {
    publisher.publishMetadata(request, 'collection', data, null, null)
  }

  @RequestMapping(value = "/collection/{id}")
  Map receiveCollection(HttpServletRequest request, @RequestBody String data, @PathVariable UUID id) throws Exception {
    publisher.publishMetadata(request, 'collection', data, id as String, null)
  }

  @RequestMapping(value = "/collection/{source}/{id}")
  Map receiveCollectionFromSource(HttpServletRequest request, @RequestBody String data, @PathVariable String source, @PathVariable UUID id) throws Exception {
    publisher.publishMetadata(request, 'collection', data, id as String, source)
  }

}
