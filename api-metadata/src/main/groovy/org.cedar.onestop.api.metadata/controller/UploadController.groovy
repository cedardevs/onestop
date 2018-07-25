package org.cedar.onestop.api.metadata.controller

import groovy.util.logging.Slf4j
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.stereotype.Controller
import org.springframework.beans.factory.annotation.Value

import javax.servlet.http.HttpServletResponse

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@Slf4j
@ConditionalOnProperty("features.secure.upload")
@Controller
class UploadController {

  @GetMapping('/upload.html')
  String upload(HttpServletResponse response) {
    return 'upload'
  }

}
