package org.cedar.psi.registry.api

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.common.serialization.Serdes
import org.cedar.psi.common.constants.Topics

import org.cedar.psi.registry.service.MetadataService
import org.cedar.psi.registry.service.MetadataStore
import org.cedar.schemas.avro.psi.RecordType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.cedar.psi.common.constants.Topics.inputStore
import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.HEAD

@Slf4j
@CompileStatic
@RestController
class MetadataRestController {

  private MetadataStore metadataStore
  private ApiRootGenerator apiLinkGenerator
  private MetadataService metadataService

  @Autowired
  MetadataRestController(MetadataStore metadataStore,
                         ApiRootGenerator apiLinkGenerator,
                         MetadataService metadataService) {
    this.metadataStore = metadataStore
    this.apiLinkGenerator = apiLinkGenerator
    this.metadataService = metadataService
  }

  @RequestMapping(path = '/metadata/{type}/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveInput(
      @PathVariable String type,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) {
    retrieveInput(type, Topics.DEFAULT_SOURCE, id, request, response)
  }

  @RequestMapping(path = '/metadata/{type}/{source}/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveInput(
      @PathVariable String type,
      @PathVariable String source,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) {
    RecordType recordType = type in RecordType.values()*.name() ? RecordType.valueOf(type) : null
    String storeName = inputStore(recordType, source).toString()
    def metadata = metadataService.streamsMetadataForStoreAndKey(storeName, id, Serdes.String().serializer())
    String path = "/registry/metadata/${type}/${source}/${id}"

    if(!metadataStore.thisHost(metadata)){
      log.info("remote instance : " + metadata)
      return metadataStore.getRemoteStoreState(metadata, path, id)
    }
    else {
      def result = metadataStore.retrieveLocalInput(recordType, source, id)
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
        } else {
          return [
              links: links,
              data : [
                  id        : id,
                  type      : type,
                  attributes: result
              ]
          ]
        }
      } else {
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
  }

  @RequestMapping(path = '/metadata/{type}/{id}/parsed', method = [GET, HEAD], produces = 'application/json')
  Map retrieveParsed(
      @PathVariable String type,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) {
    retrieveParsed(type, Topics.DEFAULT_SOURCE, id, request, response)
  }

  @RequestMapping(path = '/metadata/{type}/{source}/{id}/parsed', method = [GET, HEAD], produces = 'application/json')
  Map retrieveParsed(
      @PathVariable String type,
      @PathVariable String source,
      @PathVariable String id,
      HttpServletRequest request,
      HttpServletResponse response) {

    RecordType recordType = type in RecordType.values()*.name() ? RecordType.valueOf(type) : null
    String storeName = inputStore(recordType, source).toString()
    def metadata = metadataService.streamsMetadataForStoreAndKey(storeName, id, Serdes.String().serializer())
    def links = buildLinks(request, type, source, id)
    String path =  "/registry/metadata/${type}/${source}/${id}/parsed"

    if (!metadataStore.thisHost(metadata)) {
      log.info("remote instance : " + metadata)

      return metadataStore.getRemoteStoreState(metadata, path, id)
    } else {
      def result = metadataStore.retrieveLocalParsed(recordType, source, id)
      links.self = links.remove('parsed')

      if (result) {
        if (result?.errors?.size() > 0) {
          return [
              links : links,
              errors: result.errors
          ]
        } else {
          return [
              links: links,
              data : [
                  id        : id,
                  type      : type,
                  attributes: result
              ]
          ]
        }
      } else {
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
  }

  private Map buildLinks(HttpServletRequest request, String type, String source, String id) {
    def root = apiLinkGenerator.getApiRoot(request)
    return [
        input : "${root}/metadata/${type}/${source}/${id}" as String,
        parsed: "${root}/metadata/${type}/${source}/${id}/parsed" as String
    ]
  }

  private String buildResurrectionLink(HttpServletRequest request, String type, String source, String id) {
    def root = apiLinkGenerator.getApiRoot(request)
    return "${root}/metadata/${type}/${source}/${id}/resurrection" as String
  }

}
