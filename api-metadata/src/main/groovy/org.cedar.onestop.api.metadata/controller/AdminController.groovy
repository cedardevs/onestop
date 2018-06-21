package org.cedar.onestop.api.metadata.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.metadata.service.ETLService
import org.cedar.onestop.api.metadata.service.ElasticsearchService
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
  private ElasticsearchService elasticsearchService

  @Autowired
  AdminController(ETLService etlService, ElasticsearchService elasticsearchService) {
    this.etlService = etlService
    this.elasticsearchService = elasticsearchService
  }

  @RequestMapping(path = '/admin/test', method = GET, produces = 'text/plain')
  String test() {
      return "YOU DID IT"
  }

  @RequestMapping(path = '/admin/index/search/rebuild', method = [GET, PUT], produces = 'application/json')
  Map rebuildSearchIndex() {
    etlService.rebuildSearchIndicesAsync()
    return [acknowledged: true]
  }

  @RequestMapping(path = '/admin/index/search/update', method = [GET, PUT], produces = 'application/json')
  Map updateSearchIndex() {
    etlService.updateSearchIndicesAsync()
    return [acknowledged: true]
  }

  @RequestMapping(path = '/admin/index/search/recreate', method = [GET, PUT], produces = 'application/json')
  Map recreateSearchIndex(@RequestParam Boolean sure) {
    if (sure) {
      elasticsearchService.dropSearchIndices()
      elasticsearchService.ensureSearchIndices()
      return [acknowledged: true]
    }
  }

  @RequestMapping(path = '/admin/index/metadata/recreate', method = [GET, PUT], produces = 'application/json')
  Map recreateMetadataIndex(@RequestParam Boolean sure) {
    if (sure) {
      elasticsearchService.dropStagingIndices()
      elasticsearchService.ensureStagingIndices()
      return [acknowledged: true]
    }
  }
}
