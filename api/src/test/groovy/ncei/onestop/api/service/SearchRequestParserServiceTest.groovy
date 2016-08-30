package ncei.onestop.api.service

import groovy.json.JsonSlurper
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder
import spock.lang.Ignore
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
    def expectedPostFilterString = """\
        {
          "bool" : {
            "must_not" : {
              "exists" : {
                "field" : "parentIdentifier"
              }
            }
          }
        }""".stripIndent()

    then:
    !queryResult.toString().empty
    queryResult.toString() == expectedQueryString
    postFilters.toString() == expectedPostFilterString

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
    def expectedPostFilterString = """\
        {
          "bool" : {
            "must_not" : {
              "exists" : {
                "field" : "parentIdentifier"
              }
            }
          }
        }""".stripIndent()

    then:
    !queryResult.toString().empty
    queryResult.toString() == expectedQueryString
    postFilters.toString() == expectedPostFilterString
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
    def expectedPostFilterString = """\
        {
          "bool" : {
            "must_not" : {
              "exists" : {
                "field" : "parentIdentifier"
              }
            }
          }
        }""".stripIndent()

    then:
    !queryResult.toString().empty
    queryResult.toString() == expectedQueryString
    postFilters.toString() == expectedPostFilterString
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
    def expectedPostFilterString = """\
        {
          "bool" : {
            "must_not" : {
              "exists" : {
                "field" : "parentIdentifier"
              }
            }
          }
        }""".stripIndent()

    then:
    !queryResult.toString().empty
    queryResult.toString() == expectedQueryString
    postFilters.toString() == expectedPostFilterString
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
    def expectedPostFilterString = """\
        {
          "bool" : {
            "must_not" : {
              "exists" : {
                "field" : "parentIdentifier"
              }
            }
          }
        }""".stripIndent()

    then:
    !queryResult.toString().empty
    queryResult.toString() == expectedQueryString
    postFilters.toString() == expectedPostFilterString
  }

  def 'Facet filter request (not on parentIdentifier) creates post-filter for collections only'() {
    given:
    def request = '{"filters":[{"type":"facet","name":"science","values":"Atmosphere > Aerosols"}]}'
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
            },
            "must_not" : {
              "exists" : {
                "field" : "parentIdentifier"
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

  def 'Facet filter request on parentIdentifier creates post-filter on granules'() {
    given:
    def request = '{"filters":[{"type":"facet","name":"science","values":"Atmosphere > Aerosols"},' +
        '{"type":"facet","name":"parentIdentifier","values":"GHRSST_Something_Something"}]}'
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
            "must" : [ {
              "terms" : {
                "gcmdScience" : [ "Atmosphere > Aerosols" ]
              }
            }, {
              "terms" : {
                "parentIdentifier" : [ "GHRSST_Something_Something" ]
              }
            } ]
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
