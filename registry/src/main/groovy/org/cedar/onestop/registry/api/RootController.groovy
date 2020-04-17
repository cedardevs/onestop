package org.cedar.onestop.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.*

@Slf4j
@CompileStatic
@RestController
class RootController {

  @RequestMapping(path = "/", method = [GET, HEAD])
  Map getApiRoot(HttpServletResponse response) {
    return response.sendRedirect("openapi.yaml")
  }

}
