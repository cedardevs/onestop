package ncei.onestop.api.service

import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class SearchRequestParserServiceTest extends Specification {

    private slurper = new JsonSlurper()
    private requestParser = new SearchRequestParserService()

    def "request with #label creates empty elasticsearch request" () {
        given:
        def params = slurper.parseText(json)

        when:
        def result = requestParser.parseSearchRequest(params)
        def expectedString = """\
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
        !result.toString().empty
        result.toString() == expectedString

        where:
        label                       | json
        'nothing'                   | '{}'
        'empty queries and filters' | '{"queries":[],"filters":[]}'
        'only queries'              | '{"queries":[]}'
        'only filters'              | '{"filters":[]}'
    }

    def "Test only queryText specified" () {
        given:
        def request = '{"queries":[{"type":"queryText","value":"winter"}]}'
        def params = slurper.parseText(request)

        when:
        def result = requestParser.parseSearchRequest(params)
        def expectedString = """\
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
        !result.toString().empty
        result.toString().equals expectedString
    }


    def 'Test datetime filter request generates expected elasticsearch query'() {
        given:
        def request = '{"filters":[{"type":"datetime","before":"2011-11-11", "after":"2010-10-10"}]}'
        def params = slurper.parseText(request)

        when:
        def result = requestParser.parseSearchRequest(params)
        def expectedString = """\
        {
          "bool" : {
            "must" : {
              "bool" : { }
            },
            "filter" : {
              "bool" : {
                "should" : [ {
                  "bool" : {
                    "must" : [ {
                      "range" : {
                        "temporalBounding.beginDate" : {
                          "from" : "2010-10-10",
                          "to" : "2011-11-11",
                          "include_lower" : true,
                          "include_upper" : true
                        }
                      }
                    }, {
                      "range" : {
                        "temporalBounding.endDate" : {
                          "from" : "2011-11-11",
                          "to" : null,
                          "include_lower" : true,
                          "include_upper" : true
                        }
                      }
                    } ]
                  }
                }, {
                  "bool" : {
                    "must" : [ {
                      "range" : {
                        "temporalBounding.beginDate" : {
                          "from" : null,
                          "to" : "2010-10-10",
                          "include_lower" : true,
                          "include_upper" : true
                        }
                      }
                    }, {
                      "range" : {
                        "temporalBounding.endDate" : {
                          "from" : "2011-11-11",
                          "to" : null,
                          "include_lower" : true,
                          "include_upper" : true
                        }
                      }
                    } ]
                  }
                }, {
                  "bool" : {
                    "must" : [ {
                      "range" : {
                        "temporalBounding.beginDate" : {
                          "from" : null,
                          "to" : "2010-10-10",
                          "include_lower" : true,
                          "include_upper" : true
                        }
                      }
                    }, {
                      "range" : {
                        "temporalBounding.endDate" : {
                          "from" : "2010-10-10",
                          "to" : "2011-11-11",
                          "include_lower" : true,
                          "include_upper" : true
                        }
                      }
                    } ]
                  }
                } ]
              }
            }
          }
        }""".stripIndent()

        then:
        !result.toString().empty
        result.toString().equals expectedString
    }
}
