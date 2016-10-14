package ncei.onestop.api.controller

import groovy.util.logging.Slf4j
import ncei.onestop.api.service.ETLService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.PUT

@Slf4j
@RestController
class AdminController {

  private ETLService etlService

  @Autowired
  AdminController(ETLService etlService) {
    this.etlService = etlService
  }

  @RequestMapping(path = '/admin/reindex', method = [GET, PUT], produces = 'application/json')
  Map reindex() {
    etlService.reindexAsync()
    return [acknowledged: true]
  }

  @RequestMapping(path = '/admin/refresh', method = [GET, PUT], produces = 'application/json')
  Map refresh() {
    etlService.refreshAsync()
    return [acknowledged: true]
  }
}
