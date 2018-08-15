package org.cedar.onestop.api.search.service

import groovy.json.JsonSlurper
import spock.lang.Specification

class TrendingSearchServiceSpec extends Specification {

  def "Turns logstash search into our output JSON"() {
    when: "We get an output from Elastic Search as a Map"
    Map esResponse = esOutput()
    Map expected = expectedOutput()

    then: "We filter the result to include only top search results with number of occurrences"
    Map result = TrendingSearchService.numOccurencesOfTerms(esResponse)

    expect:
    expected == result
  }

  private static Map esOutput() {
      String output = """{
      "took": 8,
      "timed_out": false,
      "_shards": {
        "total": 5,
        "successful": 5,
        "failed": 0
      },
      "hits": {
        "total": 6799,
        "max_score": 0,
        "hits": []
      },
      "aggregations": {
        "group_by_term": {
          "doc_count_error_upper_bound": 0,
          "sum_other_doc_count": 0,
          "buckets": [
            {
              "key": "GHRSST",
              "doc_count": 4
            },
            {
              "key": "viirs",
              "doc_count": 3
            },
            {
              "key": "satellite",
              "doc_count": 2
            },
            {
              "key": "weather",
              "doc_count": 1
            }
          ]
        }
      }
    }"""
    Map mappedOutput = new JsonSlurper().parseText(output) as Map
    return mappedOutput
  }

  private static Map expectedOutput() {
    Map output = [
      "GHRSST": 4,
      "viirs": 3,
      "satellite": 2,
      "weather": 1
    ]
  }
}
