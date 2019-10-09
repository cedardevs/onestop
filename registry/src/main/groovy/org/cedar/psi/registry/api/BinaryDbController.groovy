package org.cedar.psi.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.cedar.psi.registry.service.StreamsStateService
import org.cedar.psi.registry.util.AvroTransformers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

@Slf4j
@CompileStatic
@RestController
class BinaryDbController {

  private StreamsStateService streamsStateService

  @Autowired
  BinaryDbController(StreamsStateService streamsStateService) {
    this.streamsStateService = streamsStateService
  }

  @RequestMapping(path = '/db/{table}/{key}', method = [RequestMethod.GET], produces = 'application/octet-stream')
  void retrieve(@PathVariable String table, @PathVariable String key, HttpServletResponse response) {
    log.debug("Retrieving binary data from store [${table}] with key [${key}]")
    def store = streamsStateService.getAvroStore(table)
    def result = store?.get(key)
    if (result != null) {
      AvroTransformers.avroToByteStream(result, response.outputStream)
    }
    else {
      response.sendError(404)
    }
  }

}
