package org.cedar.onestop.api.metadata.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.metadata.service.MetadataManagementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.*

@Slf4j
@RestController
class MetadataController {

  private MetadataManagementService metadataService

  @Autowired
  public MetadataController(MetadataManagementService metadataService) {
    this.metadataService = metadataService
  }

  @RequestMapping(path = '/metadata', method = POST,
      consumes = 'application/xml', produces = 'application/json')
  Map load(@RequestBody String xml, HttpServletResponse response) {
    def result = metadataService.loadMetadata(xml)
    if (result.data) {
      response.status = result.data.meta.created ? HttpStatus.CREATED.value() : HttpStatus.OK.value()
    }
    else if (result.errors) {
      response.status = result.errors[0].status
    }
    else {
      response.status = HttpStatus.BAD_REQUEST.value()
    }
    return result

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

  @RequestMapping(path = '/metadata/{id}', method = DELETE, produces = 'application/json')
  Map delete(@PathVariable String id, @RequestParam(value="recursive", required=false, defaultValue="true") Boolean recursive, HttpServletResponse response) {
    def result = metadataService.deleteMetadata(id, recursive)
    response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
    return result.response
  }

  @RequestMapping(path = '/metadata', method = DELETE, produces = 'application/json')
  Map delete(@RequestParam(value="fileIdentifier", required=false) String fileId,
             @RequestParam(value="doi", required=false) String doi,
             @RequestParam(value="recursive", required=false, defaultValue="true") Boolean recursive, HttpServletResponse response) {
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
    def result = metadataService.deleteMetadata(fileId, doi, recursive)
    response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
    return result.response
  }

}
