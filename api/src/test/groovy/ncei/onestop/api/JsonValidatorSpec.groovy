package ncei.onestop.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonSlurper
import ncei.onestop.api.controller.JsonValidator
import spock.lang.Specification

class JsonValidatorSpec extends Specification {

    def 'OneStop schema is a valid schema'() {
        when:
        ObjectMapper mapper = new ObjectMapper()
        JsonNode jsonSchema = mapper.readTree(this.getClass().classLoader.getResource('json-schema-draft4.json').text)
        JsonNode requestSchema = mapper.readTree(this.getClass().classLoader.getResource('onestop-request-schema.json').text)

        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault()
        final JsonSchema schema = factory.getJsonSchema(jsonSchema)

        ProcessingReport globalReport = schema.validate(requestSchema)
        System.out.println(globalReport);

        then:
        globalReport.success

    }

    def blanksearch = """
    {
    }
    """
    def baresearch = """
    {
        "queries":
            [
                { "type": "queryText", "value": "temperature"}
            ]
    }
    """
    def baresearch2queries = """
    {
        "queries":
            [
                { "type": "queryText", "value": "temperature"},
                { "type": "datetime", "before": "YYYY-MM-DD", "after": "YYYY-MM-DD"}
            ]
    }
    """
    def baresearch3filters = """
    {
        "filters":
            [
                { "type": "point", "value": "temperature"},
                { "type": "facet", "name": "facet-name", "values": ["value1"]},
                { "type": "point", "coordinates": [[-110.5024410624507,41.382728733019135],[-104.7456054687466,36.25063618524021]], "relation":"intersects"}
            ]
    }
    """
    def baresearch2filtersonebad = """
    {
        "filters":
            [
                { "type": "point", "value": "temperature"},
                { "type": "dateTime", "before": "YYYY-MM-DD", "after": "YYYY-MM-DD"}
            ]
    }
    """

    def 'Test search request is not valid Json'() {
        when: "Valid json if validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearch2filtersonebad)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is not returned"
        println("validation:${validation}")
        !validation.success
        validation.errors
    }

    def 'Test search request is valid Json'() {
        when: "Valid json if validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearch3filters)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is returned"
        println("validation:${validation}")
        validation.success
    }

    def 'Test search request is not valid Json1'() {
        when: "Invalid json if validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearch2queries)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Error is returned"
        println("validation:${validation}")
        !validation.success
        validation.errors
        validation.errors.code == "400"
        validation.errors.detail
        validation.errors.status
        validation.errors.title == "Json request failed validation"

    }

    def 'Test search request is valid Json2'() {
        when: "Valid json if validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearch)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is returned"
        println("validation:${validation}")
        validation.success
    }

    def 'Test search request is valid Json3'() {
        when: "Valid json if validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(blanksearch)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is returned"
        println("validation:${validation}")
        validation.success
    }
}
