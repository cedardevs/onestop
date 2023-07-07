package org.cedar.onestop.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.cedar.onestop.kafka.common.constants.Topics
import org.cedar.onestop.registry.service.MetadataStore
import org.cedar.onestop.registry.util.UUIDValidator
import org.cedar.schemas.avro.psi.AggregatedInput
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.server.ResponseStatusException

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
    links = removeBrokenLinks(links, result)

    if (result) {
      if (result.deleted) {
        response.status = HttpStatus.NOT_FOUND.value()
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
    links = removeBrokenLinks(links, result)

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

  @RequestMapping(path = '/metadata/{type}/{id}/raw', method = [GET, HEAD])
  ModelAndView  retrieveRaw(
      @PathVariable String type,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) throws ResponseStatusException {
    return retrieveRaw(type, Topics.DEFAULT_SOURCE, id, request, response)
  }

  @RequestMapping(path = '/metadata/{type}/{source}/{id}/raw', method = [GET, HEAD])
  ModelAndView  retrieveRaw(
      @PathVariable String type,
      @PathVariable String source,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) throws ResponseStatusException {
    if (!UUIDValidator.isValid(id)) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid UUID String (ensure lowercase): " + id)
    }
    RecordType recordType = type in RecordType.values()*.name() ? RecordType.valueOf(type) : null
    def result = metadataStore.retrieveInput(recordType, source, id)

    if (!result || (!result.rawJson && !result.rawXml)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No input exists for ${type} with id [${id}] from source [${source}]" as String)
    }
    if (!response) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    if (result.rawJson) {
      return new ModelAndView("redirect:/metadata/${type}/${source}/${id}/raw/json")
    } else if (result.rawXml) {
      return new ModelAndView("redirect:/metadata/${type}/${source}/${id}/raw/xml")
    }
  }

  @RequestMapping(path = '/metadata/{type}/{id}/raw/json', method = [GET, HEAD], produces = 'application/json')
  String retrieveRawJson(
      @PathVariable String type,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) throws ResponseStatusException {
    return retrieveRawJson(type, Topics.DEFAULT_SOURCE, id, request, response)
  }

  @RequestMapping(path = '/metadata/{type}/{source}/{id}/raw/json', method = [GET, HEAD], produces = 'application/json')
  String retrieveRawJson(
      @PathVariable String type,
      @PathVariable String source,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) throws ResponseStatusException {
    if (!UUIDValidator.isValid(id)) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid UUID String (ensure lowercase): " + id)
    }
    RecordType recordType = type in RecordType.values()*.name() ? RecordType.valueOf(type) : null
    def result = metadataStore.retrieveInput(recordType, source, id)

    if (result?.rawJson) {
      return result.rawJson
    }

    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No input exists for ${type} with id [${id}] from source [${source}]" as String)
  }

  @RequestMapping(path = '/metadata/{type}/{id}/raw/xml', method = [GET, HEAD], produces = 'application/xml')
  String retrieveRawXml(
      @PathVariable String type,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) throws ResponseStatusException {
    retrieveRawXml(type, Topics.DEFAULT_SOURCE, id, request, response)
  }

  @RequestMapping(path = '/metadata/{type}/{source}/{id}/raw/xml', method = [GET, HEAD], produces = 'application/xml')
  String retrieveRawXml(
      @PathVariable String type,
      @PathVariable String source,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) throws ResponseStatusException {
    if (!UUIDValidator.isValid(id)) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid UUID String (ensure lowercase): " + id)
    }
    RecordType recordType = type in RecordType.values()*.name() ? RecordType.valueOf(type) : null
    def result = metadataStore.retrieveInput(recordType, source, id)

    if (result?.rawXml) {
      return result.rawXml
    }

    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No input exists for ${type} with id [${id}] from source [${source}]" as String)
  }

  private Map buildLinks(HttpServletRequest request, String type, String source, String id) {
    def root = apiLinkGenerator.getApiRoot(request)
    return [
        input : "${root}/metadata/${type}/${source}/${id}" as String,
        parsed: "${root}/metadata/${type}/${source}/${id}/parsed" as String,
        raw: "${root}/metadata/${type}/${source}/${id}/raw" as String,
        rawJson: "${root}/metadata/${type}/${source}/${id}/raw/json" as String,
        rawXml: "${root}/metadata/${type}/${source}/${id}/raw/xml" as String
    ]
  }

  private Map removeBrokenLinks(Map links, AggregatedInput stored) {
    Map goodLinks = [:] + links
    if (!stored || stored.deleted) {
      goodLinks.remove('parsed')
    }
    if (stored && !stored.rawJson) {
      goodLinks.remove('rawJson')
    }
    if (stored && !stored.rawXml) {
      goodLinks.remove('rawXml')
    }
    return goodLinks
  }

  private Map removeBrokenLinks(Map links, ParsedRecord stored) {
    Map goodLinks = [:] + links
    if (!stored) {
      goodLinks.remove('parsed')
    }
    goodLinks.remove('rawJson')
    goodLinks.remove('rawXml')
    return goodLinks
  }

  private String buildResurrectionLink(HttpServletRequest request, String type, String source, String id) {
    def root = apiLinkGenerator.getApiRoot(request)
    return "${root}/metadata/${type}/${source}/${id}/resurrection" as String
  }

}
