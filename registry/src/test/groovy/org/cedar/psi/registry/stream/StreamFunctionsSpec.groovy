package org.cedar.psi.registry.stream

import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.Method
import spock.lang.Specification


class StreamFunctionsSpec extends Specification {

  def 'identity reducer returns the next value'() {
    def curr = 'A'
    def next = 'B'

    expect:
    StreamFunctions.identityReducer.apply(curr, next) == next
  }

  def 'set reducer merges sets'() {
    def curr = [1, 2, 3] as Set
    def next = [3, 4, 5] as Set

    expect:
    StreamFunctions.setReducer.apply(curr, next) == [1, 2, 3, 4, 5] as Set
  }

  def 'set reducer supports tombstones'() {
    def curr = [1, 2, 3] as Set
    def next = null

    expect:
    StreamFunctions.setReducer.apply(curr, next) == null
  }

  def 'merge function merges json strings'() {
    def currentAggregate = [
        contentType: 'application/json',
        content: '{"trackingId":"ABC","message":"this is a test","answer": 42}'
    ]
    def newValue = [
        contentType: 'application/json',
        content: '{"trackingId":"ABC", "message":"this is only a test","greeting": "hello, world!"}'
    ]
    def mergedAggregate = [
        contentType: 'application/json',
        content: '{"trackingId":"ABC","message":"this is only a test","answer":42,"greeting":"hello, world!"}'
    ]

    when:
    def mergedMaps = StreamFunctions.mergeContentMaps.apply(currentAggregate, newValue)

    then:
    mergedMaps == mergedAggregate
  }

  def 'merge function merges inputs'() {
    def currentAggregate = new Input([
        method: Method.POST,
        host: 'localhost',
        requestUrl: '/test',
        protocol: 'http',
        content: '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        contentType: 'application/json',
        source: 'test'
    ])
    def newValue = new Input([
        method: Method.PUT,
        host: 'localhost_number_2',
        requestUrl: '/test/again',
        protocol: 'https',
        content: '{"trackingId":"ABC", "message":"this is only a test","greeting": "hello, world!"}',
        contentType: 'application/json',
        source: 'test'
    ])
    def mergedAggregate = new Input([
        method: Method.PUT,
        host: 'localhost_number_2',
        requestUrl: '/test/again',
        protocol: 'https',
        content: '{"trackingId":"ABC","message":"this is only a test","answer":42,"greeting":"hello, world!"}',
        contentType: 'application/json',
        source: 'test'
    ])

    when:
    println mergedAggregate.properties
    def mergedInputs = StreamFunctions.mergeInputs.apply(currentAggregate, newValue)

    then:
    mergedInputs == mergedAggregate
  }


  def 'merge function replaces xml strings'() {
    def currentAggregate = [
        contentType: 'application/xml',
        content: '<text>xml wooooOne....</text>'
    ]
    def newValue = [
        contentType: 'application/xml',
        content: '<text>xml wooooTwo....</text>'
    ]
    def mergedAggregate = [
        contentType: 'application/xml',
        content: '<text>xml wooooTwo....</text>'
    ]

    when:
    def mergedMaps = StreamFunctions.mergeContentMaps.apply(currentAggregate, newValue)

    then:
    mergedMaps == mergedAggregate
  }

}
