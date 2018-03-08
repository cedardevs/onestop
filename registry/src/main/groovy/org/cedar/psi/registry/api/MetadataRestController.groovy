package org.cedar.psi.registry.api

import groovy.util.logging.Slf4j
import org.cedar.psi.registry.service.MetadataStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.HEAD

@Slf4j
@RestController
class MetadataRestController {

  private MetadataStore metadataStore

  @Autowired
  MetadataRestController(MetadataStore metadataStore) {
    this.metadataStore = metadataStore
  }


  @RequestMapping(path = '/metadata/{type}/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveJson(@PathVariable String type, @PathVariable String id, HttpServletResponse response) {
    return metadataStore.retrieveFromStore(type, id) ?: response.sendError(404, "No such ${type} with id ${id}")
  }


//  @RequestMapping(path='/metadata/rebuild', method = [PUT], produces = 'application/json')
//  Map rebuild() {
//    this.metadataStream.close()
//    this.metadataStream.cleanUp()
//    this.metadataStream.start()
//    return ['acknowledged': true]
//  }

}
