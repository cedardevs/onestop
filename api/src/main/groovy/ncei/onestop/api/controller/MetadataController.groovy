package ncei.onestop.api.controller

import groovy.util.logging.Slf4j
import ncei.onestop.api.service.MetadataIndexService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.*

@Slf4j
@RestController
class MetadataController {

  private MetadataIndexService metadataIndexService

  @Autowired
  public MetadataController(MetadataIndexService metadataIndexService) {
    this.metadataIndexService = metadataIndexService
  }

  @RequestMapping(path = '/metadata', method = POST, produces = 'application/json')
  Map load(@RequestParam("files") MultipartFile[] metadataRecords, HttpServletResponse response) {
    log.debug("Received ${metadataRecords.length} metadata files to load")

    def result = metadataIndexService.loadMetadata(metadataRecords)
    response.status = HttpStatus.MULTI_STATUS.value()
    return result
  }

  @RequestMapping(path = '/metadata', method = POST,
      consumes = 'application/xml', produces = 'application/json')
  Map load(@RequestBody String xml, HttpServletResponse response) {
    def result = metadataIndexService.loadMetadata(xml)
    if(result.data) {
      response.status = HttpStatus.CREATED.value()
    } else {
      response.status = HttpStatus.BAD_REQUEST.value()
    }
    return result

  }

  @RequestMapping(path = '/metadata/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveJson(@PathVariable String id, HttpServletResponse response) {
    def result = metadataIndexService.getMetadata(id)
    if (result.data) {
      response.status = HttpStatus.OK.value()
    } else {
      response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
    }
    return result
  }

  @RequestMapping(path = '/metadata/doi:10{prefix}/{suffix}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveJson(@PathVariable String prefix, @PathVariable String suffix, HttpServletResponse response) {
    def id = "doi:10${prefix}-${suffix}"
    return retrieveJson(id, response)
  }

  @RequestMapping(path = '/metadata/{id}', method = DELETE, produces = 'application/json')
  Map delete(@PathVariable String id, @RequestParam(value = 'type', required = false) String type, HttpServletResponse response) {
    def result = metadataIndexService.deleteMetadata(id, type)
    if (result.errors) {
      response.status = result.errors.status
    }
    else if(result.attributes.failures) {
      response.status = HttpStatus.INTERNAL_SERVER_ERROR.value() // FIXME Actual failures are problem with elasticsearch?
    }
    else {
      response.status = HttpStatus.OK.value()
    }
    return result
  }

  @RequestMapping(path = '/metadata/doi:10{prefix}/{suffix}', method = DELETE, produces = 'application/json')
  Map delete(@PathVariable String prefix, @PathVariable String suffix,
             @RequestParam(value = 'type', required = false) String type, HttpServletResponse response) {
    def id = "doi:10${prefix}-${suffix}"
    return delete(id, type, response)
  }

}
