package ncei.onestop.api.controller

import groovy.util.logging.Slf4j
import ncei.onestop.api.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.*

@Slf4j
@RestController
class SearchController {

    private ElasticsearchService elasticsearchService

    @Autowired
    public SearchController(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService
    }

    // POST in order to support request bodies from clients that won't send bodies with GETs
    @RequestMapping(path = "/search", method = [POST, GET])
    Map search(@RequestBody Map params, HttpServletResponse response) {
        Map validation = JsonValidator.validateSearchRequestSchema(params)
        if (!validation.success) {
            log.debug("invalid request: ${validation.errors.detail?.join(', ')}")
            response.status = HttpStatus.BAD_REQUEST.value()
            return [errors: validation.errors]
        }

        return elasticsearchService.search(params)
    }

    @RequestMapping(path = '/search/reindex', method = [GET, PUT], produces = 'application/json')
    Map reindex() {
        elasticsearchService.reindexAsync()
        return [acknowledged: true]
    }
}

