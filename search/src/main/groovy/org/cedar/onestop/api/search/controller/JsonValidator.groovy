package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.http.converter.HttpMessageNotReadableException

@Slf4j
class JsonValidator {

  static SchemaParser requestSchema = new SchemaParser("requestBody")

  static Map validateSearchRequestSchema(Map request) {
    validateRequestAgainstSpec(request, requestSchema)
  }

  static Map validateSchema(Map params, String schemaName) {
    final mapper = new ObjectMapper()
    final factory = JsonSchemaFactory.byDefault()
    final schemaJson = mapper.readTree(this.classLoader.getResource(schemaName).text)
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

  static Map validateRequestAgainstSpec(def params, SchemaParser schema){
    ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory())

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
            log.debug("invalid point geometry ${schema.name}: ${coords}")
            throw new Exception("Invalid geometry, not a valid request")
          }
        } else {
          coords.get(0).elements().each{
            def longitude = Math.abs(it.get(0).intValue()) <= 360
            def latitude = Math.abs(it.get(1).intValue()) <= 90
            if(!longitude || !latitude) {
              log.debug("invalid polygon geometry ${schema.name}: ${coords}")
              throw new Exception("Invalid geometry, not a valid request")
            }
          }
        }
      }

      return [success: true]
    } else {
      log.debug("invalid schema ${schema.name}: ${report}")
      throw new HttpMessageNotReadableException("JSON body is well-formed, but not a valid request")
    }
  }
}
