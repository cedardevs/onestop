package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchemaFactory
import org.springframework.http.converter.HttpMessageNotReadableException


@Slf4j
class JsonValidator {

  static Map validateSearchRequestSchema(Map request) {
    validateSchema(request, 'onestop-request-schema.json')
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

}
