package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.search.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.HEAD

@Slf4j
@RestController
@Profile("sitemap")
class SitemapController {

  private UiConfig uiConfig
  private ElasticsearchService elasticsearchService

  @Value('${sitemap.api-path}')
  private String API_BASE_URL

  @Value('${sitemap.client-path}')
  private String CLIENT_BASE_URL

  @Autowired
  public SitemapController(ElasticsearchService elasticsearchService, UiConfig uiConfig) {
    this.elasticsearchService = elasticsearchService
    this.uiConfig = uiConfig
  }


  @RequestMapping(path = '/sitemap.xml', method = GET)
  String getSitemap( HttpServletRequest request, HttpServletResponse response) {
    return SitemapGenerator.makeSitemap(API_BASE_URL, elasticsearchService.searchSitemap().data)
  }

  @RequestMapping(path = "/sitemap/{id}.txt", method = [GET, HEAD])
  String getSubSitemap( @PathVariable String id, HttpServletRequest request, HttpServletResponse response
   ) {
    def result = elasticsearchService.getSitemapById(id)

    if (result.data) {
      response.status = HttpStatus.OK.value()
      return  SitemapGenerator.makeSiteSubmap(CLIENT_BASE_URL, result.data)

    }
    else {
      response.status = result.status ?: HttpStatus.BAD_REQUEST.value()
    }
  }

}
