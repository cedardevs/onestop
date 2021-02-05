package org.cedar.onestop.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.cedar.onestop.kafka.common.constants.Topics
import org.cedar.onestop.registry.service.MetadataStore
import org.cedar.onestop.registry.util.UUIDValidator
import org.cedar.schemas.avro.psi.RecordType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.HEAD

@Slf4j
@CompileStatic
@RestController
class MetadataRestController {

  private MetadataStore metadataStore
  private ApiRootGenerator apiLinkGenerator

  @Autowired
  MetadataRestController(MetadataStore metadataStore,
                         ApiRootGenerator apiLinkGenerator) {
    this.metadataStore = metadataStore
    this.apiLinkGenerator = apiLinkGenerator
  }

  @RequestMapping(path = '/metadata/{type}/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveInput(
      @PathVariable String type,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    retrieveInput(type, Topics.DEFAULT_SOURCE, id, request, response)
  }

  @RequestMapping(path = '/metadata/{type}/{source}/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveInput(
      @PathVariable String type,
      @PathVariable String source,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    if(!UUIDValidator.isValid(id)){
      return UUIDValidator.uuidErrorMsg(id)
    }
    RecordType recordType = type in RecordType.values()*.name() ? RecordType.valueOf(type) : null
    def result = metadataStore.retrieveInput(recordType, source, id)
    def links = buildLinks(request, type, source, id)
    links.self = links.remove('input')

    if (result) {
      if (result.deleted) {
        response.status = HttpStatus.NOT_FOUND.value()
        links.remove('parsed')
        links.resurrection = buildResurrectionLink(request, type, source, id)
        return [
            links : links,
            errors: [
                [
                    status: HttpStatus.NOT_FOUND.value(),
                    title : HttpStatus.NOT_FOUND.toString(),
                    detail: "DELETE processed for ${type} with id [${id}] from source [${source}]" as String
                ]
            ]
        ]
      }
      else {
        return [
            links: links,
            data : [
                id        : id,
                type      : type,
                attributes: result
            ]
        ]
      }
    }
    else {
      response.status = HttpStatus.NOT_FOUND.value()
      links.remove('parsed')
      return [
          links : links,
          errors: [
              [
                  status: HttpStatus.NOT_FOUND.value(),
                  title : HttpStatus.NOT_FOUND.toString(),
                  detail: "No input exists for ${type} with id [${id}] from source [${source}]" as String
              ]
          ]
      ]
    }

  }

  @RequestMapping(path = '/metadata/{type}/{id}/parsed', method = [GET, HEAD], produces = 'application/json')
  Map retrieveParsed(
      @PathVariable String type,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    retrieveParsed(type, Topics.DEFAULT_SOURCE, id, request, response)
  }

  @RequestMapping(path = '/metadata/{type}/{source}/{id}/parsed', method = [GET, HEAD], produces = 'application/json')
  Map retrieveParsed(
      @PathVariable String type,
      @PathVariable String source,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) {
    if(!UUIDValidator.isValid(id)){
      return UUIDValidator.uuidErrorMsg(id)
    }
    RecordType recordType = type in RecordType.values()*.name() ? RecordType.valueOf(type) : null
    def links = buildLinks(request, type, source, id)
    def result = metadataStore.retrieveParsed(recordType, source, id as String)
    links.self = links.remove('parsed')

    if (result) {
      if (result?.errors?.size() > 0) {
        return [
            links : links,
            errors: result.errors
        ]
      }
      else {
        return [
            links: links,
            data : [
                id        : id,
                type      : type,
                attributes: result
            ]
        ]
      }
    }
    else {
      response.status = HttpStatus.NOT_FOUND.value()
      return [
          links : links,
          errors: [
              [
                  status: HttpStatus.NOT_FOUND.value(),
                  title : HttpStatus.NOT_FOUND.toString(),
                  detail: "No parsed values exist for ${type} with id [${id}] from source [${source}]" as String
              ]
          ]
      ]
    }
  }

  @RequestMapping(path = '/metadata/{type}/{id}/raw/xml', method = [GET, HEAD], produces = 'application/xml')
  String retrieveRawXml(
      @PathVariable String type,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    retrieveRawXml(type, Topics.DEFAULT_SOURCE, id, request, response)
  }

  @RequestMapping(path = '/metadata/{type}/{source}/{id}/raw/xml', method = [GET, HEAD], produces = 'application/xml')
  String retrieveRawXml(
      @PathVariable String type,
      @PathVariable String source,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    if (!UUIDValidator.isValid(id)) {
      response.status = HttpStatus.INTERNAL_SERVER_ERROR.value()
      return
    }
    RecordType recordType = type in RecordType.values()*.name() ? RecordType.valueOf(type) : null
    def result = metadataStore.retrieveInput(recordType, source, id)

    if (result?.rawXml) {
      return result.rawXml
    }

    response.status = HttpStatus.NOT_FOUND.value()
    return
  }

  private Map buildLinks(HttpServletRequest request, String type, String source, String id) {
    def root = apiLinkGenerator.getApiRoot(request)
    return [
        input : "${root}/metadata/${type}/${source}/${id}" as String,
        parsed: "${root}/metadata/${type}/${source}/${id}/parsed" as String,
        xml: "${root}/metadata/${type}/${source}/${id}/xml" as String
    ]
  }

  private String buildResurrectionLink(HttpServletRequest request, String type, String source, String id) {
    def root = apiLinkGenerator.getApiRoot(request)
    return "${root}/metadata/${type}/${source}/${id}/resurrection" as String
  }

}
