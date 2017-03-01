package ncei.onestop.api.service

import groovy.json.JsonSlurper
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class SearchRequestParserServiceTest extends Specification {

  private slurper = new JsonSlurper()
  private requestParser = new SearchRequestParserService(null)

  def "Request with #label creates empty elasticsearch request"() {
    given:
    def params = slurper.parseText(json)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
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
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQueryString = """\
        {
          "bool" : {
            "must" : {
              "bool" : {
                "must" : {
                  "query_string" : {
                    "query" : "winter",
                    "lenient" : true
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
  }

  def "Test only queryText with field boosts configured"() {
    given:
    def config = new SearchConfig(fields: [title: 4])
    config.initialize()
    def parser = new SearchRequestParserService(config)

    def request = '{"queries":[{"type":"queryText","value":"winter"}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = parser.parseSearchQuery(params)
    def expectedQueryString = """\
        {
          "bool" : {
            "must" : {
              "bool" : {
                "must" : {
                  "query_string" : {
                    "query" : "winter",
                    "fields" : [ "title^4.0" ],
                    "lenient" : true
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
  }

  def "Test only queryText with dsmm boosting configured"() {
    given:
    def config = new SearchConfig(dsmm: new SearchConfig.DSMMConfig(factor: 1, modifier: 'log1p'))
    config.initialize()
    def parser = new SearchRequestParserService(config)

    def request = '{"queries":[{"type":"queryText","value":"winter"}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = parser.parseSearchQuery(params)
    def expectedQueryString = """\
        {
          "bool" : {
            "must" : {
              "function_score" : {
                "query" : {
                  "bool" : {
                    "must" : {
                      "query_string" : {
                        "query" : "winter",
                        "lenient" : true
                      }
                    }
                  }
                },
                "functions" : [ {
                  "field_value_factor" : {
                    "field" : "dsmmAverage",
                    "factor" : 1.0,
                    "missing" : 0.0,
                    "modifier" : "log1p"
                  }
                } ],
                "boost_mode" : "sum"
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
  }

  def 'Datetime filter request generates expected elasticsearch query'() {
    given:
    def request = '{"filters":[{"type":"datetime","before":"2011-11-11", "after":"2010-10-10"}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
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
  }

  def 'Geopoint filter request generates expected elasticsearch query'() {
    given:
    def request = '{"filters":[{"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [67.89, 12.345]}}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
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
  }

  def 'Bbox filter request generates expected elasticsearch query'() {
    given:
    def request = '{"filters":[{"type": "geometry", "relation": "disjoint", "geometry":' +
        '  {"type": "Polygon", "coordinates": [[[-5.99, 45.99], [-5.99, 36.49], [36.49, 30.01], [36.49, 45.99], [-5.99, 45.99]]]}' +
        '}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
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
  }

  def 'Facet filter request includes filter in query'() {
    given:
    def request = '{"filters":[{"type":"facet","name":"science","values":"Atmosphere > Aerosols"}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQueryString = """\
        {
          "bool" : {
            "must" : {
              "bool" : { }
            },
            "filter" : {
              "bool" : {
                "must" : {
                  "terms" : {
                    "gcmdScience" : [ "Atmosphere > Aerosols" ]
                  }
                }
              }
            }
          }
        }""".stripIndent()

    then:
    !queryResult.toString().empty
    queryResult.toString() == expectedQueryString
  }

  def 'Default aggregations are built for granules'() {
    when:
    def aggs = requestParser.createGCMDAggregations(false)

    then:
    // This is about all that can be verified w/o changing the List to a Map unnecessarily...
    aggs.size() == 6 // 6 GCMD types
    aggs.each { a ->
      a.class == TermsBuilder
    }
  }

  def 'Default aggregations are built for collections'() {
    when:
    def aggs = requestParser.createGCMDAggregations(true)

    then:
    // This is about all that can be verified w/o changing the List to a Map unnecessarily...
    aggs.size() == 6 // 6 GCMD types
    aggs.each { a ->
      a.class == TermsBuilder
    }
  }
}
