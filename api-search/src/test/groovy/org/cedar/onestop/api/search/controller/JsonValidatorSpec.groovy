package org.cedar.onestop.api.search.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class JsonValidatorSpec extends Specification {

  static def validatePart(request, schema) {
    def jsonSlurper = new JsonSlurper()
    def params = jsonSlurper.parseText(request)
    try {
      return JsonValidator.validateSchema(params, schema)
    } catch (e) {
      println("failed with: ${request}")
      println(e)
      throw(e)
    }
  }

  static def validateSearchSchema(request) {
    def jsonSlurper = new JsonSlurper()
    def params = jsonSlurper.parseText(request)
    try {
      return JsonValidator.validateSearchRequestSchema(params)
    } catch (e) {
      println("failed with: ${request}")
      println(e)
      throw(e)
    }
  }

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

  def 'valid pagination: #desc'() {
    given:
    def schema = 'schema/components/page.json'
    def singleQuery = """{ "page": ${request} }"""

    when:
    def validation = validatePart(request, schema)

    then:
    validation.success

    and: 'no errors are returned'
    !validation.errors

    when:
    def validSearch = validateSearchSchema(singleQuery)

    then:
    validSearch.success

    where:
    desc | request
    'page 2 of 10-item pages' |
        """{ "max": 10, "offset": 10 }"""
    'page 1 of 25-item pages' |
        """{ "max": 25, "offset": 10 }"""
    'page 1 of 100-item pages' |
        """{ "max": 100, "offset": 0 }"""
    'largest page size allowed' |
        """{"max":1000, "offset":0}"""
  }

  def 'invalid pagination: #desc (reason: #reasoning)'() {
    given:
    def schema = 'schema/components/page.json'
    def singleQuery = """{ "page": ${request} }"""

    when:
    def validation = validatePart(request, schema)

    then: "exception is thrown"
    def e = thrown(Exception)
    e.message.contains('not a valid request')

    when:
    def validSearch = validateSearchSchema(singleQuery)

    then: "exception is thrown"
    def searchException = thrown(Exception)
    searchException.message.contains('not a valid request')

    where:
    desc | reasoning| request
    'page size greater than 1000' | '' |
        """{"max":1001, "offset":0}"""
    'huge page size' | 'cripples services' |
        """{"max":9999999, "offset":0}"""
  }

  def 'valid text query: #desc'() {
    given:
    def schema = 'schema/components/textQuery.json'
    def singleQuery = """{ "queries": [ ${request} ] }"""

    when:
    def validation = validatePart(request, schema)

    then:
    validation.success

    and: 'no errors are returned'
    !validation.errors

    when:
    def validSearch = validateSearchSchema(singleQuery)

    then:
    validSearch.success

    where:
    desc | request
    'temperature' |
        """{"type": "queryText", "value": "temperature"}"""
    'pressure' |
        """{"type": "queryText", "value": "pressure"}"""
  }

  def 'invalid text query: #desc (reason: #reasoning)'() {
    given:
    def schema = 'schema/components/textQuery.json'
    def singleQuery = """{ "queries": [ ${request} ] }"""

    when:
    def validation = validatePart(request, schema)

    then: "exception is thrown"
    def e = thrown(Exception)
    e.message.contains('not a valid request')

    when:
    def validSearch = validateSearchSchema(singleQuery)

    then: "exception is thrown"
    def searchException = thrown(Exception)
    searchException.message.contains('not a valid request')

    where:
    desc | reasoning | request
    'begins with ?' | 'cripples services' |
        """{"type": "queryText", "value": "?temperature"}"""
    'begins with *' | 'cripples services' |
        """{"type": "queryText", "value": "*water"}"""
    'begins with * not circumvented by leading whitespace' | '' |
        """{"type": "queryText", "value": " *ocean"}"""
    'invalid field' | "'cat' is not a field on the query object" |
        """{"type": "queryText", "value": "temperature", "cat": "meow"}"""
  }

  def 'valid filter: #component #desc'() {
    given:
    def singleQuery = """{ "filters": [ ${request} ] }"""

    when:
    def validation = validatePart(request, "schema/components/${component}Filter.json")

    then:
    validation.success

    and: 'no errors are returned'
    !validation.errors

    when:
    def validSearch = validateSearchSchema(singleQuery)

    then:
    validSearch.success

    where:
    component | desc | request
    'datetime' | '(relation: default) range' |
        """{"type": "datetime", "before": "2016-06-15T20:20:58Z", "after": "2015-09-22T10:30:06.000Z"}"""
    'datetime' | '(relation: within) unbounded beginning' |
        """{"type": "datetime", "relation": "within", "before": "2016-06-15T20:20:58Z"}"""
    'year' | '(relation: contains) unbounded end' |
        """{"type": "year", "relation": "contains", "after": -5000000}"""
    'year' | '(relation: default) range' |
        """{"type": "year", "before": 1000, "after": -1234567890}"""
    'geometry' | 'contains point' |
        """{"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [22.123, -45.245]}}"""
    'geometry' | 'intersects polygon' |
        """
        {"type": "geometry", "relation": "intersects", "geometry":
          {"type": "Polygon", "coordinates": [[[-5.99, 45.99], [-5.99, 36.49], [36.49, 30.01], [36.49, 45.99], [-5.99, 45.99]]]}
        }
        """
    'facet' | 'atmosphere' |
        """{"type": "facet", "name": "science", "values": ["Atmosphere"]}"""
    'facet' | 'horizontal resolution > 1 km' |
        """{"type": "facet", "name": "horizontalResolution", "values": ["> 1 Km"]}"""
    'facet' | 'oceans' |
        """{"type": "facet", "name": "science", "values": ["Oceans"]}"""
    'excludeGlobal' | 'exclude global' |
        """{ "type": "excludeGlobal", "value": true}"""
    'collection' | 'collection' |
        """{"type":"collection", "values":["fakeUUID"]}"""
    'geometry' | 'point (200, 50)' |
        """{"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [200, 50]}}"""
    'geometry' | 'point (-45.123, 75.245)' |
        """{"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [-45.123, 75.245]}}"""
    'geometry' | 'point (75.245, -45.123)' |
        """{"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [75.245, -45.123]}}"""

  }

  def 'invalid filter: #component #desc'() {
    given:
    def singleQuery = """{ "filters": [ ${request} ] }"""

    when:
    def validation = validatePart(request, "schema/components/${component}Filter.json")

    then: "exception is thrown"
    def e = thrown(Exception)
    e.message.contains('not a valid request')

    when:
    def validSearch = validateSearchSchema(singleQuery)

    then: "exception is thrown"
    def searchException = thrown(Exception)
    searchException.message.contains('not a valid request')

    where:
    component | desc | request
    'datetime' | 'bad formatting - timestamps required' |
        """{ "type": "datetime", "before": "2012-12-31", "after": "2012-01-01"}"""
    'year' | 'bad formatting: number not timestamps required' |
        """{ "type": "year", "before": "2016-06-15T20:20:58Z"}"""
    'year' | 'bad formatting: 3.4 is an invalid year type' |
        """{ "type": "year", "before": 3.4}"""
    'datetime' | "invalid relation - 'outside' is not one of the options" |
        """{"type": "datetime", "relation": "outside", "before": "2016-06-15T20:20:58Z"}"""
    'year' | 'bad formatting: -200.5 is an invalid year type' |
        """{"type": "year", "before": -200.5}"""
    'year' | 'bad formatting: "-100000" is an invalid year type (string)' |
        """{"type": "year", "after": "-100000"}"""
    'excludeGlobal' | 'bad formatting: value is boolean, not string' |
        """{ "type": "excludeGlobal", "value": "taco tuesday"}"""
    'facet' | 'invalid value: facet name not in enum' |
        """{"type": "facet", "name": "notScience", "values": ["Atmosphere"]}"""
    'geometry' | 'point (-100, -100) exceeds allowed lat/long bounds' |
        """{"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [-100, -100]}}"""
    'geometry' | 'point (50, 200) exceeds allowed lat/long bounds' |
        """{"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [50, 200]}}"""
    'geometry' | 'point (400, 0) exceeds allowed lat/long bounds' |
        """{"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [400, 0]}}"""
  }

  def 'valid search requests: #desc'() {
    when:
    def validSearch = validateSearchSchema(request)

    then:
    validSearch.success

    and: 'no errors are returned'
    !validSearch.errors

    where:
    desc | request
    'empty' | """{}"""
    'text query, facets true' |
        """\
        {
          "queries": [
            {"type": "queryText", "value": "temperature"}
          ],
          "facets": true
        }"""
    'text query, facets false' |
        """\
        {
          "queries": [
            {"type": "queryText", "value": "temperature"}
          ],
          "facets": false
        }"""
    'text query, summary false' |
        """\
        {
          "queries": [
            {"type": "queryText", "value": "temperature"}
          ],
          "summary": false
        }"""
    'text query, summary true' |
        """\
        {
          "queries": [
            {"type": "queryText", "value": "temperature"}
          ],
          "summary": true
        }"""
    'multiple text queries' |
        """\
        {
          "queries": [
            {"type": "queryText", "value": "temperature"},
            {"type": "queryText", "value": "pressure"}
          ]
        }"""
    'multiple filters applied' |
        """\
        {
          "filters": [
            {"type": "facet", "name": "science", "values": ["Atmosphere"]},
            {"type": "datetime", "before": "2016-06-15T20:20:58Z", "after": "2015-09-22T10:30:06.000Z"},
            {"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [22.123, -45.245]}},
            {"type": "geometry", "relation": "intersects", "geometry":
              {"type": "Polygon", "coordinates": [[[-5.99, 45.99], [-5.99, 36.49], [36.49, 30.01], [36.49, 45.99], [-5.99, 45.99]]]}
            }
          ]
        }"""
    'text query, multiple filters, paging' |
        """\
        {
          "queries": [
            {"type": "queryText", "value": "temperature"}
          ],
          "filters": [
            {"type": "facet", "name": "science", "values": ["Atmosphere"]},
            {"type": "year", "before": 1000, "after": -1234567890},
            {"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [22.123, -45.245]}}
          ],
          "page": { "max": 100, "offset": 0 }
        }"""
    'filter, summary false' |
        """\
        {
          "filters": [
            {"type":"collection", "values":["fakeUUID"]}
          ],
          "summary": false
        }"""
    'queries as empty array, filter, facets true, paging' |
        """\
        {
          "queries":[],
          "filters":[{"type":"datetime", "after":"0000-01-01T00:00:00Z", "before":"2018-08-31T00:00:00Z"}],
          "facets":true,
          "page":{"max":1000, "offset":0}
        }"""
  }

  def 'invalid search requests: #desc'() {
    when:
    def validSearch = validateSearchSchema(request)

    then: "exception is thrown"
    def searchException = thrown(Exception)
    searchException.message.contains('not a valid request')

    where:
    desc | request
    'invalid query structure' |
        """{
          "queries": {
            "queryText": {"value": "temperature"}
          }
        }"""
    'summary value must be boolean' |
        """{
          "summary": "gibberish"
        }"""
    'facets value must be boolean, not string' |
        """{
          "facets": "false"
        }"""
    'invalid filter type - geopoint' |
        """{
          "filters": [
            { "type": "geopoint", "coordinates": {"lat": -100, "lon": -100}}
          ]
        }"""
    'invalid filter type - bbox' |
        """{
          "filters": [
            { "type": "bbox", "topLeft": {"lat": 45.99, "lon": -5.99}, "bottomRight": {"lat": 30.01, "lon": 36.49}}
          ]
        }"""
  }

}
