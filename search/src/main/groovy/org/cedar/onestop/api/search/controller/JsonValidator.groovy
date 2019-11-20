package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.http.converter.HttpMessageNotReadableException


@Slf4j
class JsonValidator {

  static Map validateSearchRequestSchema(Map request) {
    validateRequestAgainstSpec(request, 'requestBody')
  }

  static Map validateSchema(Map params, String schemaName) {
    final mapper = new ObjectMapper()
    final factory = JsonSchemaFactory.byDefault()
    final schemaJson = mapper.readTree(this.classLoader.getResource(schemaName).text)
    // TODO get the schema from OpenAPI spec: final schema = factory.getJsonSchema(schemaJson, '/components/schemas/requestBody')
    final schema = factory.getJsonSchema(schemaJson)
    final requestJson = mapper.valueToTree(params)
    final report = schema.validate(requestJson)

    if (report.success) {
      return [success: true]
    } else {
      log.debug("invalid schema ${schemaName}: ${report}")
      throw new HttpMessageNotReadableException("JSON body is well-formed, but not a valid request")
    }
  }

  static Map validateRequestAgainstSpec(def params, String schemaName){
    ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory())
    JsonNode apiSpec = yamlMapper.readTree(this.classLoader.getResource('openapi.yaml').text)
    JsonNode schemaJson = apiSpec.get('components').get('schemas').get(schemaName)
    ObjectNode schemaJsonObj = (ObjectNode)schemaJson

    // dereference the requestBody schema, so it can be used correctly for validation
    // [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25].each{ // TODO should be while loop, but this does, hilariously, cover it
      // TODO probably horribly inefficient - cache this ?
    JsonNode ref = schemaJson.findValue('$ref')
    // if(ref != null) {
    //   def paths = ref.textValue().replace('#/', '').split('/')
    //   JsonNode deref = apiSpec
    //   paths.each{
    //     deref = deref.findValue(it)
    //   }
    //   ObjectNode parent = schemaJsonObj.findParent('$ref')
    //   parent.remove('$ref')
    //   parent.setAll(deref)
    //   }
    // }
    while(ref != null) {
      def paths = ref.textValue().replace('#/', '').split('/')
      JsonNode deref = apiSpec
      paths.each{
        deref = deref.findValue(it)
      }
      ObjectNode parent = schemaJsonObj.findParent('$ref')
      parent.remove('$ref')
      parent.setAll(deref)
      ref = schemaJson.findValue('$ref')
    }

    final factory = JsonSchemaFactory.byDefault()
    // final schemaJson = mapper.readTree(this.classLoader.getResource(schemaName).text)
    // TODO get the schema from OpenAPI spec: final schema = factory.getJsonSchema(schemaJson, '/components/schemas/requestBody')
    final schema = factory.getJsonSchema(schemaJsonObj)
    final requestJson = yamlMapper.valueToTree(params)
    final report = schema.validate(requestJson)

    if (report.success) {

      // validate geometry point limitations, since the schema no longer does so for us.
      JsonNode coords = requestJson.findPath('coordinates')
      if(coords != null && coords.isArray()) {
        if(!coords.get(0).isArray()) {
          def longitude = Math.abs(coords.get(0).intValue()) < 360
          def latitude = Math.abs(coords.get(1).intValue()) < 90
          if(!longitude || !latitude) {
            log.debug("invalid point geometry ${schemaName}: ${coords}")
            throw new Exception("Invalid geometry, not a valid request")
          }
        } else {
          coords.get(0).elements().each{
            def longitude = Math.abs(it.get(0).intValue()) < 360
            def latitude = Math.abs(it.get(1).intValue()) < 90
            if(!longitude || !latitude) {
              log.debug("invalid polygon geometry ${schemaName}: ${coords}")
              throw new Exception("Invalid geometry, not a valid request")
            }
          }
        }
      }

      return [success: true]
    } else {
      log.debug("invalid schema ${schemaName}: ${report}")
      throw new HttpMessageNotReadableException("JSON body is well-formed, but not a valid request")
    }
  }
}
