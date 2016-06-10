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
        when: "The OneStop schemas are validated"
        ObjectMapper mapper = new ObjectMapper()
        JsonNode jsonSchema = mapper.readTree(this.getClass().classLoader.getResource('json-schema-draft4.json').text)
        JsonNode requestSchema = mapper.readTree(this.getClass().classLoader.getResource('onestop-request-schema.json').text)

        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault()
        final JsonSchema schema = factory.getJsonSchema(jsonSchema)

        ProcessingReport globalReport = schema.validate(requestSchema)
        System.out.println(globalReport);

        then: "The validation is successful"
        globalReport.success
    }

    def blanksearch = """
    {
    }
    """

    def baresearch = """
    {
        "queries":
            {
                "queryText": {"value": "temperature"}
            }
    }
    """

    def searchqueriesfiltersformatting = """
    {
        "queries":
            {
                "queryText": {"value": "temperature"}
            },
        "filters":
            {
                "facet": {"name": "apiso_TopicCategory_s", "values": ["oceanography", "oceans"]},
                "point": {"bbox": [-110.5024410624507,36.25063618524021,-104.7456054687466,41.382728733019135], "relation":"intersects"},
                "datetime": {"before": "2016-06-15T20:20:58Z", "after": "2015-09-22T10:30:06.000Z"}
            },
        "formatting":
            {
                "sortorder": {"by": "relevance", "dir": "descending"},
                "pagination": {"from": 0, "size": 10}
            }
    }
    """

    def baresearchextraproperty = """
    {
        "queries":
            {
                "queryText": {"value": "temperature", "poo": "xxx"}
            }
    }

    """
    def baresearch2queries = """
    {
        "queries":
            {
                "queryText": {"value": "temperature"},
                "queryText": {"value": "temperature"}
            }
    }
    """

//    [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.SSSZ] requires
    def baresearchdatefilter = """
    {
        "filters":
            {
                "datetime": {"before": "2016-06-15T20:20:58Z", "after": "2015-09-22T10:30:06.000Z"}
            }
    }
    """

    // Format bbox [southwestlong, southwestlat, northwestlong, northwestlat]
    def baresearchpointfilter = """
    {
        "filters":
            {
                "point": {"bbox": [-110.5024410624507,36.25063618524021,-104.7456054687466,41.382728733019135], "relation":"within"}
            }
    }
    """

    def baresearchfacetfilter = """
    {
        "filters":
            {
                "facet": {"name": "name", "values": ["value1"]},
                "facet": {"name": "name2", "values": ["value1", "value2"]}
            }
    }
    """

    def baresearch3filters = """
    {
        "filters":
            {
                "datetime": {"before": "2016-06-15T20:20:58Z", "after": "2015-09-22T10:30:06.000Z"},
                "facet": {"name": "name", "values": ["value1"]},
                "point": {"bbox": [-110.5024410624507,36.25063618524021,-104.7456054687466,41.382728733019135], "relation":"intersects"}
            }
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

    def baresearchformatting = """
    {
        "formatting": {
                    "pagination": {"from": 0, "size": 10}
                    }
    }
    """

    def baresearchformatmultiplesametypeok = """
    {
        "formatting": {
                    "sortorder": {"by": "relevance", "dir": "ascending"},
                    "sortorder": {"by": "relevance", "dir": "descending"},
                    "pagination": {"from": 0, "size": 10}
                    }
    }
    """

    def baresearchformattinginvalidformat = """
    {
        "formatting": {
                    "sortorder": {"by": "relevance", "dir": "ascending"},
                    "dateTime": {"before": "YYYY-MM-DD", "after": "YYYY-MM-DD"},
                    "pagination": {"from": 0, "size": 10}
                    }
    }
    """

    def 'Test search request is not valid Json'() {
        when: "Inalid json is validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearch2filtersonebad)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Error returned"
        println("validation:${validation}")
        !validation.success
        validation.errors
    }

    def 'Test search request is not valid Json invalid format'() {
        when: "Inalid json is validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearchformattinginvalidformat)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Error returned"
        println("validation:${validation}")
        !validation.success
        validation.errors
    }

    def 'Test search request is valid Json for formatting'() {
        when: "Valid json is validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearchformatting)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is returned"
        println("validation:${validation}")
        validation.success
    }

    def 'Test search request is valid Json for formatting containing 2 of same type'() {
        when: "Valid json is validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearchformatmultiplesametypeok)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is returned"
        println("validation:${validation}")
        validation.success
    }

    def 'Test search request is valid Json'() {
        when: "Valid json is validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearch3filters)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is returned"
        println("validation:${validation}")
        validation.success
    }

    def 'Test search request is valid Json search contains queries filters formatting'() {
        when: "Valid json is validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(searchqueriesfiltersformatting)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is returned"
        println("validation:${validation}")
        validation.success
    }

    def 'Test search request is valid Json datetime filter'() {
        when: "Valid json is validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearchdatefilter)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is returned"
        println("validation:${validation}")
        validation.success
    }

    def 'Test search request is valid Json point filter'() {
        when: "Valid json is validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearchpointfilter)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is returned"
        println("validation:${validation}")
        validation.success
    }

    def 'Test search request is valid Json facet filter'() {
        when: "Valid json is validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearchfacetfilter)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is returned"
        println("validation:${validation}")
        validation.success
    }

    def 'Test search request is not valid Json 2 queries ok'() {
        when: "Invalid json is validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearch2queries)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is returned"
        println("validation:${validation}")
        validation.success
    }

    def 'Test search request is not valid Json extra property not allowed'() {
        when: "Invalid json is validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearchextraproperty)
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

    def 'Test search request is valid Json single query'() {
        when: "Valid json is validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(baresearch)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is returned"
        println("validation:${validation}")
        validation.success
    }

    def 'Test search request is valid Json if blank'() {
        when: "Valid json is validated"
        def jsonSlurper = new JsonSlurper()
        def params = jsonSlurper.parseText(blanksearch)
        def validation = JsonValidator.validateSearcRequestSchema(params)
        then: "Success is returned"
        println("validation:${validation}")
        validation.success
    }
}
