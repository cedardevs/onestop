package ncei.onestop.api.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory

class JsonValidator {

    public static Map validateSearchRequestSchema(Map request) {
        validateSchema(request, 'onestop-request-schema.json')
    }

    public static Map validateSchema(Map params, String schemaName) {

        ObjectMapper mapper = new ObjectMapper()
        JsonNode topologySchema = mapper.readTree(this.classLoader.getResource(schemaName).text)
        JsonNode json = mapper.valueToTree(params)

        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault()
        final JsonSchema schema = factory.getJsonSchema(topologySchema)

        ProcessingReport report = schema.validate(json);

        def response = [:]
        def errors = [:]
        if (!report.success) {
            response.put("success", report.success)
            errors.put("status", "Invalid Request")
            errors.put("code", "400")
            errors.put("title", "Json request failed validation")
            errors.put("detail", "${report?.messages[0]}")
            response.put("errors", errors)

        } else {
            response.put("success", report.success)
            response.put("status", "OK")
            response.put("code", "200")
        }
        response
    }

}
