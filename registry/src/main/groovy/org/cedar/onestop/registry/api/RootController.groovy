package org.cedar.onestop.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest

import static org.springframework.web.bind.annotation.RequestMethod.*

@Slf4j
@CompileStatic
@RestController
class RootController {

  private ApiRootGenerator apiLinkGenerator

  @Autowired
  MetadataRestController(ApiRootGenerator apiLinkGenerator) {
    this.apiLinkGenerator = apiLinkGenerator
  }

  @RequestMapping(path = "/", method = [GET, HEAD])
  Map getApiRoot(HttpServletRequest request, HttpServletResponse response) {
    return response.sendRedirect("${apiLinkGenerator.getApiRoot(request)}/openapi.yaml")
  }

}
