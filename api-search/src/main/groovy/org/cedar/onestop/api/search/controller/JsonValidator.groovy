package org.cedar.onestop.api.search.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchemaFactory
import org.springframework.http.converter.HttpMessageNotReadableException

class JsonValidator {

  public static Map validateSearchRequestSchema(Map request) {
    validateSchema(request, 'onestop-request-schema.json')
  }

  public static Map validateSchema(Map params, String schemaName) {
    final mapper = new ObjectMapper()
    final factory = JsonSchemaFactory.byDefault()
    final schemaJson = mapper.readTree(this.classLoader.getResource(schemaName).text)
    final schema = factory.getJsonSchema(schemaJson)
    final requestJson = mapper.valueToTree(params)
    final report = schema.validate(requestJson)

    if (report.success) {
      return [success: true]
    } else {
      throw new HttpMessageNotReadableException("JSON body is well-formed, but not a valid request")
    }
  }

}
