package org.cedar.psi.registry.stream

import spock.lang.Specification


class StreamFunctionsSpec extends Specification {

  def 'merge function merges json strings'() {
    def currentAggregate = [
        contentType: 'application/json',
        requestBody: '{"trackingId":"ABC","message":"this is a test","answer": 42}'
    ]
    def newValue = [
        contentType: 'application/json',
        requestBody: '{"trackingId":"ABC", "message":"this is only a test","greeting": "hello, world!"}'
    ]
    def mergedAggregate = [
        contentType: 'application/json',
        requestBody: '{"trackingId":"ABC","message":"this is only a test","answer":42,"greeting":"hello, world!"}'
    ]

    when:
    def mergedMaps = StreamFunctions.mergeMaps.apply(currentAggregate, newValue)

    then:
    mergedMaps == mergedAggregate
  }


  def 'merge function replaces xml strings'() {
    def currentAggregate = [
        contentType: 'application/xml',
        requestBody: '<text>xml wooooOne....</text>'
    ]
    def newValue = [
        contentType: 'application/xml',
        requestBody: '<text>xml wooooTwo....</text>'
    ]
    def mergedAggregate = [
        contentType: 'application/xml',
        requestBody: '<text>xml wooooTwo....</text>'
    ]

    when:
    def mergedMaps = StreamFunctions.mergeMaps.apply(currentAggregate, newValue)

    then:
    mergedMaps == mergedAggregate
  }

}
