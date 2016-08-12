package ncei.onestop.api.service

import groovy.json.JsonSlurper
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class SearchRequestParserServiceTest extends Specification {

  private slurper = new JsonSlurper()
  private requestParser = new SearchRequestParserService()

  def "Request with #label creates empty elasticsearch request"() {
    given:
    def params = slurper.parseText(json)

    when:
    def parsedRequest = requestParser.parseSearchRequest(params)
    def queryResult = parsedRequest.query
    def postFilters = parsedRequest.postFilters
    def expectedQueryString = """\
        {
          "bool" : {
            "must" : {
              "bool" : { }
            },
            "filter" : {
              "bool" : { }
            }
          }
        }""".stripIndent()

    then:
    !queryResult.toString().empty
    queryResult.toString() == expectedQueryString
    postFilters == null

    where:
    label                       | json
    'nothing'                   | '{}'
    'empty queries and filters' | '{"queries":[],"filters":[]}'
    'only queries'              | '{"queries":[]}'
    'only filters'              | '{"filters":[]}'
  }

  def "Test only queryText specified"() {
    given:
    def request = '{"queries":[{"type":"queryText","value":"winter"}]}'
    def params = slurper.parseText(request)

    when:
    def parsedRequest = requestParser.parseSearchRequest(params)
    def queryResult = parsedRequest.query
    def postFilters = parsedRequest.postFilters
    def expectedQueryString = """\
        {
          "bool" : {
            "must" : {
              "bool" : {
                "must" : {
                  "query_string" : {
                    "query" : "winter"
                  }
                }
              }
            },
            "filter" : {
              "bool" : { }
            }
          }
        }""".stripIndent()

    then:
    !queryResult.toString().empty
    queryResult.toString() == expectedQueryString
    postFilters == null
  }

  def 'Datetime filter request generates expected elasticsearch query'() {
    given:
    def request = '{"filters":[{"type":"datetime","before":"2011-11-11", "after":"2010-10-10"}]}'
    def params = slurper.parseText(request)

    when:
    def parsedRequest = requestParser.parseSearchRequest(params)
    def queryResult = parsedRequest.query
    def postFilters = parsedRequest.postFilters
    def expectedQueryString = """\
        {
          "bool" : {
            "must" : {
              "bool" : { }
            },
            "filter" : {
              "bool" : {
                "must" : [ {
                  "range" : {
                    "temporalBounding.beginDate" : {
                      "from" : null,
                      "to" : "2011-11-11",
                      "include_lower" : true,
                      "include_upper" : true
                    }
                  }
                }, {
                  "range" : {
                    "temporalBounding.endDate" : {
                      "from" : "2010-10-10",
                      "to" : null,
                      "include_lower" : true,
                      "include_upper" : true
                    }
                  }
                } ]
              }
            }
          }
        }""".stripIndent()

    then:
    !queryResult.toString().empty
    queryResult.toString() == expectedQueryString
    postFilters == null
  }

  def 'Geopoint filter request generates expected elasticsearch query'() {
    given:
    def request = '{"filters":[{"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [67.89, 12.345]}}]}'
    def params = slurper.parseText(request)

    when:
    def parsedRequest = requestParser.parseSearchRequest(params)
    def queryResult = parsedRequest.query
    def postFilters = parsedRequest.postFilters
    def expectedQueryString = """\
        {
          "bool" : {
            "must" : {
              "bool" : { }
            },
            "filter" : {
              "bool" : {
                "must" : {
                  "geo_shape" : {
                    "spatialBounding" : {
                      "shape" : {
                        "type" : "point",
                        "coordinates" : [ 67.89, 12.345 ]
                      },
                      "relation" : "contains"
                    },
                    "_name" : null
                  }
                }
              }
            }
          }
        }""".stripIndent()

    then:
    !queryResult.toString().empty
    queryResult.toString() == expectedQueryString
    postFilters == null
  }

  def 'Bbox filter request generates expected elasticsearch query'() {
    given:
    def request = '{"filters":[{"type": "geometry", "relation": "disjoint", "geometry":' +
        '  {"type": "Polygon", "coordinates": [[[-5.99, 45.99], [-5.99, 36.49], [36.49, 30.01], [36.49, 45.99], [-5.99, 45.99]]]}' +
        '}]}'
    def params = slurper.parseText(request)

    when:
    def parsedRequest = requestParser.parseSearchRequest(params)
    def queryResult = parsedRequest.query
    def postFilters = parsedRequest.postFilters
    def expectedQueryString = """\
        {
          "bool" : {
            "must" : {
              "bool" : { }
            },
            "filter" : {
              "bool" : {
                "must" : {
                  "geo_shape" : {
                    "spatialBounding" : {
                      "shape" : {
                        "type" : "polygon",
                        "coordinates" : [ [ [ -5.99, 45.99 ], [ -5.99, 36.49 ], [ 36.49, 30.01 ], [ 36.49, 45.99 ], [ -5.99, 45.99 ] ] ]
                      },
                      "relation" : "disjoint"
                    },
                    "_name" : null
                  }
                }
              }
            }
          }
        }""".stripIndent()

    then:
    !queryResult.toString().empty
    queryResult.toString() == expectedQueryString
    postFilters == null
  }

  def 'Facet filter request creates post-filter'() {
    given:
    def request = '{"filters":[{"type":"facet","name":"gcmdScience","values":"Atmosphere > Aerosols"}]}'
    def params = slurper.parseText(request)

    when:
    def parsedRequest = requestParser.parseSearchRequest(params)
    def queryResult = parsedRequest.query
    def postFilters = parsedRequest.postFilters
    def expectedQueryString = """\
        {
          "bool" : {
            "must" : {
              "bool" : { }
            },
            "filter" : {
              "bool" : { }
            }
          }
        }""".stripIndent()

    def expectedPostFiltersString = """\
        {
          "bool" : {
            "must" : {
              "terms" : {
                "gcmdScience" : [ "Atmosphere > Aerosols" ]
              }
            }
          }
        }""".stripIndent()

    then:
    !queryResult.toString().empty
    !postFilters.toString().empty
    queryResult.toString() == expectedQueryString
    postFilters.toString() == expectedPostFiltersString
  }

  def 'Default aggregations are built'() {
    when:
    def aggs = requestParser.createDefaultAggregations()

    then:
    // This is about all that can be verified w/o changing the List to a Map unnecessarily...
    aggs.size() == 7 // 7 GCMD types
    aggs.each { a ->
      a.class == TermsBuilder
    }
  }
}
