package org.cedar.onestop.api.metadata.controller

import groovy.util.logging.Slf4j
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.stereotype.Controller
import org.springframework.beans.factory.annotation.Value

import javax.servlet.http.HttpServletResponse

@Slf4j
@Controller
class UploadController {


  @GetMapping('/upload.html')
  String upload(HttpServletResponse response) {
    return 'upload'
  }

}
