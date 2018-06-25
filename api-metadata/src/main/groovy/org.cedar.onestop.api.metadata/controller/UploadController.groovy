package org.cedar.onestop.api.metadata.controller

import groovy.util.logging.Slf4j
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.stereotype.Controller

import javax.servlet.http.HttpServletResponse
// import org.springframework.ui.Model

@Slf4j
@Controller
class UploadController {

  @GetMapping('/upload.html')
  String upload(HttpServletResponse response) {
    return 'upload'
  }

}
