package org.cedar.psi.registry.api

import groovy.util.logging.Slf4j
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

  @Autowired
  MetadataRestController() {
  }


  @RequestMapping(path = '/metadata/{type}/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveJson(@PathVariable String type, @PathVariable String id, HttpServletResponse response) {
    return ['message': 'hello world']
  }

}
