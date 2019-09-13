package org.cedar.onestop.api.admin.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.admin.service.MetadataManagementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.DELETE
import static org.springframework.web.bind.annotation.RequestMethod.POST

@Slf4j
@RestController
@Profile("!kafka-ingest")
class MetadataWriteController {

  private MetadataManagementService metadataService

  @Autowired
  public MetadataWriteController(MetadataManagementService metadataService) {
    this.metadataService = metadataService
  }

  @RequestMapping(path = '/metadata', method = POST, produces = 'application/json')
  Map load(@RequestParam("files") MultipartFile[] metadataRecords, HttpServletResponse response) {
    log.debug("Received ${metadataRecords.length} metadata files to load")

    def result = metadataService.loadMetadata(metadataRecords)
    response.status = HttpStatus.MULTI_STATUS.value()
    return result
  }

  @RequestMapping(path = '/metadata', method = POST, consumes = 'application/xml', produces = 'application/json')
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
