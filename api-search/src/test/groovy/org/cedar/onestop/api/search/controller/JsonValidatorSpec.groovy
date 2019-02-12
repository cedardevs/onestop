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

  def 'valid requests return success true and no errors (#desc)'() {
    when: 'valid json is validated'
    def jsonSlurper = new JsonSlurper()
    def params = jsonSlurper.parseText(request)
    def validation
    try {
      validation = JsonValidator.validateSearchRequestSchema(params)
    } catch (e) {
      println("failed with: ${request}")
      println(e)
      throw(e)
    }

    then: 'success is true'
    println("validation:${validation}")
    validation.success

    and: 'no errors are returned'
    !validation.errors

    where:
    request | desc
        """\
{
}""" | 'blank'
        """\
{
  "queries": [
    {"type": "queryText", "value": "temperature"}
  ]
}""" | 'query for temperature'
        """\
{
  "queries": [
    {"type": "queryText", "value": "temperature"}
  ],
  "facets": true
}"""| 'query for temperature with facets'
        """\
{
  "queries": [
    {"type": "queryText", "value": "temperature"}
  ],
  "facets": false
}""" | 'query for temperature without facets'

        """\
{
  "queries": [
    {"type": "queryText", "value": "temperature"}
  ],
  "summary": false
}""" | 'query for temperature without summary'


        """\
{
  "queries": [
    {"type": "queryText", "value": "temperature"}
  ],
  "summary": true
}""" | 'query for temperature as summary'
        """\
{
  "queries": [
    {"type": "queryText", "value": "temperature"},
    {"type": "queryText", "value": "pressure"}
  ]
}""" | 'query for temperature and pressure'
        """\
{
  "filters": [
    {"type": "datetime", "before": "2016-06-15T20:20:58Z", "after": "2015-09-22T10:30:06.000Z"}
  ]
}""" | 'filter with datetime range'
        """\
{
  "filters": [
    {"type": "datetime", "relation": "within", "before": "2016-06-15T20:20:58Z"}
  ]
}""" | 'filter for datetime within range with unbounded end'
        """\
{
  "filters": [
    {"type": "year", "relation": "contains", "after": -5000000}
  ]
}""" | 'filter for year containing year after -5000000'
        """\
{
  "filters": [
    {"type": "facet", "name": "science", "values": ["Atmosphere"]}
  ]
}""" | 'filter Atomosphere facet'
        """\
{
  "filters": [
    {"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [22.123, -45.245]}}
  ]
}""" | 'filter containing point geometry'
        """\
{
  "filters": [
    {"type": "geometry", "relation": "intersects", "geometry":
      {"type": "Polygon", "coordinates": [[[-5.99, 45.99], [-5.99, 36.49], [36.49, 30.01], [36.49, 45.99], [-5.99, 45.99]]]}
    }
  ]
}""" | 'filter intersecting polygon geometry'
        """\
{
  "filters": [
    {"type": "facet", "name": "horizontalResolution", "values": ["> 1 Km"]},
    {"type": "datetime", "before": "2016-06-15T20:20:58Z", "after": "2015-09-22T10:30:06.000Z"},
    {"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [22.123, -45.245]}},
    {"type": "geometry", "relation": "intersects", "geometry":
      {"type": "Polygon", "coordinates": [[[-5.99, 45.99], [-5.99, 36.49], [36.49, 30.01], [36.49, 45.99], [-5.99, 45.99]]]}
    }
  ]
}""" | 'multiple filter types'
        """\
{
  "page": { "max": 10, "offset": 10 }
}""" | 'paging'
        """\
{
  "page": { "max": 25, "offset": 10 }
}""" | 'paging with max 25'
        """\
{
  "queries": [
    {"type": "queryText", "value": "temperature"}
  ],
  "filters": [
    {"type": "facet", "name": "science", "values": ["Oceans"]},
    {"type": "year", "before": 1000, "after": -1234567890},
    {"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [22.123, -45.245]}}
  ],
  "page": { "max": 100, "offset": 0 }
}""" | 'request with query, filters, and paging'
        """\
{
  "filters": [
    { "type": "excludeGlobal", "value": true}
  ]
}""" | 'exclude global filter'
        """\
{
  "filters": [
    {"type":"collection", "values":["fakeUUID"]}
  ],
  "summary": false
}""" | 'filter by collection uuid'
"""\
{"queries":[], "filters":[{"type":"datetime", "after":"0000-01-01T00:00:00Z", "before":"2018-08-31T00:00:00Z"}], "facets":true, "page":{"max":1000, "offset":0}}
}""" | 'empty query with filters, facets, paging'
  }

  def 'invalid requests throw an exception to be caught by the default exception handler (#desc)'() {
    when: "invalid json is validated"
    def jsonSlurper = new JsonSlurper()
    def params = jsonSlurper.parseText(request)
    JsonValidator.validateSearchRequestSchema(params)

    then: "exception is thrown"
    def e = thrown(Exception)
    e.message.contains('not a valid request')

    where:
    request | desc
        """\
{
  "queries": {
    "queryText": {"value": "temperature"}
  }
}
""" | 'badly nested querytext'
        """\
{
  "queries": [
    {"type": "queryText", "value": "temperature", "cat": "meow"}
  ]
}""" | 'invalid field \'cat\''
        """\
{
  "queries": [
    {"type": "queryText", "value": "?temperature"}
  ]
}""" | 'query beginning with \'?\''
        """\
{
  "queries": [
    {"type": "queryText", "value": "*water"}
  ]
}""" | 'query beginning with \'*\''
        """\
{
  "queries": [
    {"type": "queryText", "value": " *anything"}
  ]
}""" | 'query beginning with \'*\', anything'
        """\
{
  "queries": [
    {"type": "queryText", "value": "temperature"}
  ],
  "summary": "gibberish"
}""" | 'non-boolean value for summary'
        """\
{
  "facets": "false"
}""" | 'string instead of boolean'
        """\
{
  "filters": [
    { "type": "datetime", "before": "2012-12-31", "after": "2012-01-01"}
  ]
}""" | 'datetime without timestamps'
        """\
{
  "filters": [
    {"type": "datetime", "relation": "outside", "before": "2016-06-15T20:20:58Z"}
  ]
}""" | 'datetime relation that does not exist'
        """\
{
  "filters": [
    {"type": "year", "before": -200.5}
  ]
}""" | 'year type is long not double'
        """\
{
  "filters": [
    {"type": "year", "after": "-100000"}
  ]
}""" | 'year type is long not string'
        """\
{
  "filters": [
    { "type": "geopoint", "coordinates": {"lat": -100, "lon": -100}}
  ]
}""" | 'badly formatted geopoint'
        """\
{
  "filters": [
    { "type": "geopoint", "coordinates": {"lat": 50, "lon": 200}}
  ]
}""" | 'another badly formatted geopoint'
        """\
{
  "filters": [
    { "type": "geopoint", "lat": -45.123, "lon": 75.245}
  ]
}""" | 'a third badly formatted geopoint'
        """\
{
  "filters": [
    { "type": "bbox", "topLeft": {"lat": 45.99, "lon": -5.99}, "bottomRight": {"lat": 30.01, "lon": 36.49}}
  ]
}""" | 'badly formatted bbox'
        """\
{
  "filters": [
    { "type": "excludeGlobal", "value": "taco tuesday"}
  ]
}""" | 'non-boolean value for excludeGlobal'
        """\
{
  "filters": [
    {"type": "facet", "name": "notScience", "values": ["Atmosphere"]}
  ]
}""" | 'facet notScience that does not exist'
"""\
{"queries":[], "filters":[{"type":"datetime", "after":"0000-01-01T00:00:00Z", "before":"2018-08-31T00:00:00Z"}], "facets":true, "page":{"max":9999999, "offset":0}}
}""" | 'max page number far far far too big'
"""\
{"queries":[], "filters":[{"type":"datetime", "after":"0000-01-01T00:00:00Z", "before":"2018-08-31T00:00:00Z"}], "facets":true, "page":{"max":1001, "offset":0}}
}""" | 'max page number ever so slightly too big'
  }

}
