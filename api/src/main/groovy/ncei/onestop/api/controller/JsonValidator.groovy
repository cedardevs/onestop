package ncei.onestop.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchemaFactory

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

        final result = [success: report.success]
        if (!report.success) {
            result.errors = report.collect {
                [
                  status: '400',
                  title: 'JSON request failed validation',
                  detail: it.message
                ]
            }
        }
        return result
    }

}
