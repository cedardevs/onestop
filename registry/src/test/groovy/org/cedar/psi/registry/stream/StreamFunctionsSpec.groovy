package org.cedar.psi.registry.stream

import org.cedar.schemas.avro.psi.Input
import org.cedar.schemas.avro.psi.Method
import org.cedar.schemas.avro.psi.RecordType
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

  def 'input merger merges json content'() {
    def currentAggregate = new Input([
        type: RecordType.granule,
        method: Method.POST,
        content: '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        contentType: 'application/json',
        source: 'test'
    ])
    def newValue = new Input([
        type: RecordType.granule,
        method: Method.PUT,
        content: '{"trackingId":"ABC", "message":"this is only a test","greeting": "hello, world!"}',
        contentType: 'application/json',
        source: 'test'
    ])
    def mergedAggregate = new Input([
        type: RecordType.granule,
        method: Method.PUT,
        content: '{"trackingId":"ABC","message":"this is only a test","answer":42,"greeting":"hello, world!"}',
        contentType: 'application/json',
        source: 'test'
    ])

    when:
    def mergedInputs = StreamFunctions.mergeInputContent.apply(currentAggregate, newValue)

    then:
    mergedInputs == mergedAggregate
  }


  def 'input merger replaces xml strings'() {
    def currentAggregate = new Input([
        type: RecordType.granule,
        method: Method.POST,
        contentType: 'application/xml',
        content: '<text>xml wooooOne....</text>',
        source: 'test'
    ])
    def newInput = new Input([
        type: RecordType.granule,
        method: Method.POST,
        contentType: 'application/xml',
        content: '<text>xml wooooTwo....</text>',
        source: 'test'
    ])

    when:
    def mergedMaps = StreamFunctions.mergeInputContent.apply(currentAggregate, newInput)

    then:
    mergedMaps == newInput
  }

  def 'reduce inputs with PATCH method'() {
    def currentAggregate = new Input([
        type: RecordType.granule,
        method: Method.POST,
        content: '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        contentType: 'application/json',
        source: 'test'
    ])
    def newValue = new Input([
        type: RecordType.granule,
        method: Method.PATCH,
        content: '{"trackingId":"ABC", "message":"this is only a test","greeting": "hello, world!"}',
        contentType: 'application/json',
        source: 'test'
    ])

    def expectedValue = new Input([
        type: RecordType.granule,
        method: Method.PATCH,
        content: '{"trackingId":"ABC","message":"this is only a test","answer":42,"greeting":"hello, world!"}',
        contentType: 'application/json',
        source: 'test'
    ])

    when:
    def value = StreamFunctions.reduceInputs.apply(currentAggregate, newValue)

    then:
    value == expectedValue
  }

  def 'publish function with PUT method'() {
    def currentAggregate = new Input([
        type: RecordType.granule,
        method: Method.POST,
        content: '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        contentType: 'application/json',
        source: 'test'
    ])
    def newValue = new Input([
        type: RecordType.granule,
        method: Method.PUT,
        content: '{"trackingId":"ABC", "message":"this is only a test","greeting": "hello, world!"}',
        contentType: 'application/json',
        source: 'test'
    ])

    when:
    def value = StreamFunctions.reduceInputs.apply(currentAggregate, newValue)

    then:
    value == newValue
  }

  def 'publish function with DELETE method'() {
    def currentAggregate = new Input([
        type: RecordType.granule,
        method: Method.POST,
        content: '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        contentType: 'application/json',
        source: 'test'
    ])
    def newValue = new Input([
        method: Method.DELETE,
    ])

    def expected = new Input([
        type: RecordType.granule,
        method: Method.DELETE,
        content: '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        contentType: 'application/json',
        source: 'test'
    ])
    when:
    def value = StreamFunctions.reduceInputs.apply(currentAggregate, newValue)

    then:
    value == expected
  }

}
