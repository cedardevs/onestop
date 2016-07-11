package ncei.onestop.api.controller

import groovy.util.logging.Slf4j
import ncei.onestop.api.service.ElasticsearchAdminService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

@Slf4j
@RestController
class MetadataController {

    private ElasticsearchAdminService esAdminService

    @Autowired
    public MetadataController(ElasticsearchAdminService esAdminService) {
        this.esAdminService = esAdminService
    }

    @RequestMapping(path = '/load', method = RequestMethod.POST,
            consumes = 'application/xml', produces = 'application/json')
    Map load(@RequestBody String xml, HttpServletResponse response) {
        def result = esAdminService.loadDocument(xml)
        if(result.data) {
            response.status = HttpStatus.CREATED.value()
        } else {
            response.status = HttpStatus.BAD_REQUEST.value()
        }
        return result
    }
}
