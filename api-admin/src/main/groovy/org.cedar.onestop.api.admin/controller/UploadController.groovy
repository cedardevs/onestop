package org.cedar.onestop.api.admin.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.admin.service.MetadataManagementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.POST

@Slf4j
@Controller
@Profile("manual-upload")
class UploadController {

  private MetadataManagementService metadataService

  @Autowired
  public UploadController(MetadataManagementService metadataService) {
    this.metadataService = metadataService
  }

  @GetMapping('/upload.html')
  String upload(HttpServletResponse response) {
    return 'upload'
  }

  @RequestMapping(path = '/metadata-form', method = POST, produces = 'application/json')
  ModelAndView load(@RequestParam("files") MultipartFile[] metadataRecords, RedirectAttributes redirectAttributes) {
    log.debug("Received ${metadataRecords.length} metadata files to load")

    def results = metadataService.loadXMLdocuments(metadataRecords as List)

    def successes = results.data.findAll {
      !it.meta.error
    }

    def errors = results.data.findAll {
      it.meta.error
    }

    int nSuccess = successes.size()
    int nErrors = errors.size()
    int total = nSuccess + nErrors

    ModelAndView mav = new ModelAndView('redirect:/uploadResponse.html')

    redirectAttributes.addFlashAttribute("nSuccess", nSuccess)
    redirectAttributes.addFlashAttribute("nErrors", nErrors)
    redirectAttributes.addFlashAttribute("successes", successes)
    redirectAttributes.addFlashAttribute("errors", errors)
    redirectAttributes.addFlashAttribute("results", results)

    return mav
  }

  @GetMapping('/uploadResponse.html')
  ModelAndView uploadResponse(@ModelAttribute("results") Map results) {
    return new ModelAndView('uploadResponse')
  }

}
