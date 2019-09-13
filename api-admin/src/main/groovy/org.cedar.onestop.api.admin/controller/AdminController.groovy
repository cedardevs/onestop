package org.cedar.onestop.api.admin.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.admin.service.ETLService
import org.cedar.onestop.api.admin.service.SitemapETLService
import org.cedar.onestop.api.admin.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.PUT

@Slf4j
@RestController
class AdminController {

  @Autowired
  private ETLService etlService

  @Autowired(required = false)
  private SitemapETLService sitemapEtlService

  @Autowired
  private ElasticsearchService elasticsearchService

  AdminController() {}

  @RequestMapping(path = '/admin/test', method = GET, produces = 'text/plain')
  String test(HttpServletRequest request) {
    String uri = request.getRequestURI()
    Authentication auth = SecurityContextHolder.getContext().getAuthentication()
    String user = auth.getName()
    String roles = auth.getAuthorities()
    return "uri: ${uri}, user: ${user}, roles: ${roles}"
  }

  @RequestMapping(path = '/admin/index/search/rebuild', method = [GET, PUT], produces = 'application/json')
  Map rebuildSearchIndex() {
    etlService.rebuildSearchIndicesAsync()
    if(sitemapEtlService) {
      sitemapEtlService.rebuildSearchIndicesAsync()
    }
    return [acknowledged: true]
  }

  @RequestMapping(path = '/admin/index/search/update', method = [GET, PUT], produces = 'application/json')
  Map updateSearchIndex() {
    etlService.updateSearchIndicesAsync()
    if(sitemapEtlService) {
      sitemapEtlService.updateSearchIndicesAsync()
    }
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
