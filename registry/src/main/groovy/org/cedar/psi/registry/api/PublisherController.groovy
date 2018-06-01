package org.cedar.psi.registry.api

import groovy.transform.CompileStatic
import org.cedar.psi.registry.service.Publisher

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CompileStatic
@RestController
@RequestMapping(value = "/metadata")
class PublisherController {

  @Autowired
  Publisher publisher

  @RequestMapping(value = "/granule", consumes = "application/json")
  void receiveGranule(@RequestBody String data) throws Exception {
    publisher.publishGranule(data)
  }

  @RequestMapping(value = "/granule/{id}", consumes = "application/xml")
  void receiveGranuleIso(@RequestBody String data, @PathVariable(required = false) String id) throws Exception {
    publisher.publishGranuleIso(data, id)
  }

  @RequestMapping(value = "/collection/{id}", consumes = "application/xml")
  void receiveCollection(@RequestBody String data, @PathVariable(required = false) String id) throws Exception {
    publisher.publishCollection(data, id)
  }

}
