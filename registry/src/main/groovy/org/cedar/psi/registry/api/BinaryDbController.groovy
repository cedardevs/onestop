package org.cedar.psi.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.avro.specific.SpecificRecord
import org.cedar.psi.registry.service.MetadataService
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

  private static final EncoderFactory encoderFactory = EncoderFactory.get()
  private MetadataService metadataService

  @Autowired
  BinaryDbController(MetadataService metadataService) {
    this.metadataService = metadataService
  }

  @RequestMapping(path = '/db/{table}/{key}', method = [RequestMethod.GET], produces = 'application/octet-stream')
  void retrieve(@PathVariable String table, @PathVariable String key, HttpServletResponse response) {
    log.debug("Retrieving binary data from store [${table}] with key [${key}]")
    def store = metadataService.getAvroStore(table)
    def result = store?.get(key)
    if (result != null) {
      def schema = result.getSchema()
      def encoder = encoderFactory.binaryEncoder(response.outputStream, null)
      def writer = new SpecificDatumWriter<SpecificRecord>(schema)
      writer.write(result, encoder)
      encoder.flush()
    }
    else {
      response.sendError(404)
    }
    return
  }

}
