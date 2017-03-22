package ncei.onestop.api.etl.controller

import groovy.util.logging.Slf4j
import ncei.onestop.api.etl.service.ETLService
import ncei.onestop.api.etl.service.IndexAdminService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.PUT

@Slf4j
@RestController
class AdminController {

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.name}')
  String STAGING_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.name}')
  String SEARCH_INDEX

  private ETLService etlService
  private IndexAdminService adminService

  @Autowired
  AdminController(ETLService etlService, IndexAdminService adminService) {
    this.etlService = etlService
    this.adminService = adminService
  }

  @RequestMapping(path = '/admin/index/search/rebuild', method = [GET, PUT], produces = 'application/json')
  Map rebuildSearchIndex() {
    etlService.rebuildSearchIndexAsync()
    return [acknowledged: true]
  }

  @RequestMapping(path = '/admin/index/search/update', method = [GET, PUT], produces = 'application/json')
  Map updateSearchIndex() {
    etlService.updateSearchIndexAsync()
    return [acknowledged: true]
  }

  @RequestMapping(path = '/admin/index/search/recreate', method = [GET, PUT], produces = 'application/json')
  Map recreateSearchIndex(@RequestParam Boolean sure) {
    if (sure != null) {
      adminService.recreate(SEARCH_INDEX)
      return [acknowledged: true]
    }
  }

  @RequestMapping(path = '/admin/index/search/refresh', method = GET, produces = 'application/json')
  Map refreshSearchIndex() {
    adminService.refresh(SEARCH_INDEX)
    return [acknowledged: true]
  }

  @RequestMapping(path = '/admin/index/metadata/recreate', method = [GET, PUT], produces = 'application/json')
  Map recreateMetadataIndex(@RequestParam Boolean sure) {
    if (sure != null) {
      adminService.recreate(STAGING_INDEX)
      return [acknowledged: true]
    }
  }

  @RequestMapping(path = '/admin/index/metadata/refresh', method = GET, produces = 'application/json')
  Map refreshMetadataIndex() {
    adminService.refresh(STAGING_INDEX)
    return [acknowledged: true]
  }
}
