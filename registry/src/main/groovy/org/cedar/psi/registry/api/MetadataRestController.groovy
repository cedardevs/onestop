package org.cedar.psi.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.cedar.psi.common.constants.Topics
import org.cedar.psi.registry.service.MetadataStore
import org.cedar.schemas.avro.psi.RecordType
import org.springframework.beans.factory.annotation.Autowired
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

  private MetadataStore metadataStore

  @Autowired
  MetadataRestController(MetadataStore metadataStore) {
    this.metadataStore = metadataStore
  }


  @RequestMapping(path = '/metadata/{type}/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveJson(@PathVariable String type, @PathVariable String id, HttpServletResponse response) {
    retrieveJson(type, Topics.DEFAULT_SOURCE, id, response)
  }

  @RequestMapping(path = '/metadata/{type}/{source}/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveJson(@PathVariable String type, @PathVariable String source, @PathVariable String id, HttpServletResponse response) {
    RecordType recordType = type in RecordType.values()*.name() ? RecordType.valueOf(type) : null
    def result = metadataStore.retrieveEntity(recordType, source, id)
    if (!result) {
      response.sendError(404, "No such ${type} with id ${id}")
    }
    return result
  }


//  @RequestMapping(path='/metadata/rebuild', method = [PUT], produces = 'application/json')
//  Map rebuild() {
//    this.metadataStream.close()
//    this.metadataStream.cleanUp()
//    this.metadataStream.start()
//    return ['acknowledged': true]
//  }

}
