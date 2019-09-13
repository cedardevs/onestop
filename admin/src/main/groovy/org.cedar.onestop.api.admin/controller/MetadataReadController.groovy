package org.cedar.onestop.api.admin.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.admin.service.MetadataManagementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.HEAD

@Slf4j
@RestController
class MetadataReadController {

  private MetadataManagementService metadataService

  @Autowired
  public MetadataReadController(MetadataManagementService metadataService) {
    this.metadataService = metadataService
  }

  @RequestMapping(path = '/metadata/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveJson(@PathVariable String id, HttpServletResponse response) {
    def result = metadataService.getMetadata(id)
    if (result.data) {
      response.status = HttpStatus.OK.value()
    }
    else {
      response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
    }
    return result
  }

  @RequestMapping(path = '/metadata', method = [GET, HEAD], produces = 'application/json')
  Map retrieveJson(@RequestParam(value="fileIdentifier", required=false) String fileId,
                   @RequestParam(value="doi", required=false) String doi, HttpServletResponse response) {
    if (!fileId && !doi) {
      response.status = HttpStatus.BAD_REQUEST.value()
      return [
          errors: [[
              id    : null,
              status: HttpStatus.BAD_REQUEST.value(),
              title : 'No identifiers provided with request',
              detail: 'Provide a fileId and/or doi request parameter'
          ]]
      ]
    }
    def result = metadataService.findMetadata(fileId, doi)
    if (result.data) {
      response.status = HttpStatus.OK.value()
    }
    else {
      response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
    }
    return result
  }

}
