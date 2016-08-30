package ncei.onestop.api.controller

import groovy.util.logging.Slf4j
import ncei.onestop.api.service.MetadataIndexService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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

    @RequestMapping(path = '/metadata/{id}', method = [GET, HEAD], produces = 'application/xml')
    String retrieveXml(@PathVariable String id, HttpServletResponse response) {
        def result = metadataIndexService.getMetadata(id)
        if (result.data) {
            response.status = HttpStatus.OK.value()
            return result.data.attributes.isoXml
        }
        else {
            response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
            return result.title
        }
    }
    @RequestMapping(path = '/metadata/{id}', method = [GET, HEAD], produces = 'application/json')
    Map retrieveJson(@PathVariable String id, HttpServletResponse response) {
        def result = metadataIndexService.getMetadata(id)
        if (result.data) {
            response.status = HttpStatus.OK.value()
        }
        else {
            response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
        }
        return result
    }

    @RequestMapping(path = '/metadata/{id}', method = DELETE, produces = 'application/json')
    Map delete(@PathVariable String id, HttpServletResponse response) {
        def result = metadataIndexService.deleteMetadata(id)
        if (result.meta?.deleted) {
            response.status = HttpStatus.OK.value()
        }
        else {
            response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
        }
        return result
    }

}
