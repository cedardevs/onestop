package ncei.onestop.api.controller

import groovy.util.logging.Slf4j
import ncei.onestop.api.service.ETLService
import ncei.onestop.api.service.MetadataIndexService
import ncei.onestop.api.service.SearchIndexService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.PUT

@Slf4j
@RestController
class AdminController {

  private ETLService etlService
  private SearchIndexService searchIndexService
  private MetadataIndexService metadataIndexService

  @Autowired
  AdminController(ETLService etlService, SearchIndexService searchIndexService, MetadataIndexService metadataIndexService) {
    this.etlService = etlService
    this.searchIndexService = searchIndexService
    this.metadataIndexService = metadataIndexService
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
      searchIndexService.recreate()
      return [acknowledged: true]
    }
  }

  @RequestMapping(path = '/admin/index/metadata/recreate', method = [GET, PUT], produces = 'application/json')
  Map recreateMetadataIndex(@RequestParam Boolean sure) {
    if (sure != null) {
      metadataIndexService.recreate()
      return [acknowledged: true]
    }
  }
}
