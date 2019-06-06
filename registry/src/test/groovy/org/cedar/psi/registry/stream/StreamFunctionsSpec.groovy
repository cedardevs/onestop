package org.cedar.psi.registry.stream

import org.cedar.schemas.avro.psi.*
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

  def 'can merge maps'() {
    def first = ["trackingId":"ABC","message":"this is a test","answer": 42]
    def second = ["trackingId":"ABC", "message":"this is only a test","greeting": "hello, world!"]
    def merged = ["trackingId":"ABC","message":"this is only a test","answer":42,"greeting":"hello, world!"]

    expect:
    StreamFunctions.mergeMaps(first, second) == merged
  }

  def 'aggregated input initializer returns a default AggregatedInput'() {
    expect:
    StreamFunctions.aggregatedInputInitializer.apply().equals(AggregatedInput.newBuilder().build())
  }

  def 'aggregates an initial input'() {
    def key = 'ABC'
    def input = new Input([
        type: RecordType.granule,
        method: Method.POST,
        content: '{"trackingId":"ABC","fileInformation":{"size":42},"fileLocations":{"test:one":{"uri":"test:one"},"test:two":{"uri":"test:two"}}}',
        contentType: 'application/json',
        source: 'test',
        operation: null
    ])
    def aggregate = AggregatedInput.newBuilder().build()

    when:
    def result = StreamFunctions.inputAggregator.apply(key, input, aggregate)

    then:
    result instanceof AggregatedInput
    result.rawJson == input.content
    result.rawXml == null
    result.initialSource == input.source
    result.type == input.type
    result.fileInformation instanceof FileInformation
    result.fileInformation.size == 42
    result.fileLocations instanceof Map
    result.fileLocations.size() == 2
    result.fileLocations['test:one'].uri == 'test:one'
    result.fileLocations['test:two'].uri == 'test:two'
    result.publishing == null
    result.relationships instanceof List
    result.relationships.size() == 0
    result.deleted == false
    result.events instanceof List
    result.events.size() == 1
    result.events[0].source == input.source
    result.events[0].method == input.method
    result.events[0].operation == input.operation
    result.errors instanceof List
    result.errors.size() == 0
  }

  def 'aggregate inputs with PATCH method'() {
    def currentAggregate = new AggregatedInput([
        type: RecordType.granule,
        rawJson: '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        initialSource: 'test',
        events: [new InputEvent(null, Method.POST, 'test', null)]
    ])
    def newValue = Input.newBuilder()
        .setType(RecordType.granule)
        .setMethod(Method.PATCH)
        .setContent('{"trackingId":"ABC", "message":"this is only a test","greeting": "hello, world!"}')
        .setContentType('application/json')
        .setSource('test')
        .build()

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', newValue, currentAggregate)

    then:
    result.type == currentAggregate.type
    result.initialSource == currentAggregate.initialSource
    result.deleted == false
    result.events.size() == 2
    // note: json should be merged together when PATCH is used
    result.rawJson == '{"trackingId":"ABC","message":"this is only a test","answer":42,"greeting":"hello, world!"}'
  }

  def 'aggregate input with PUT method'() {
    def currentAggregate = new AggregatedInput([
        type: RecordType.granule,
        rawJson: '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        initialSource: 'test',
        events: [new InputEvent(null, Method.POST, 'test', null)]
    ])
    def newValue = new Input([
        type: RecordType.granule,
        method: Method.PUT,
        content: '{"trackingId":"ABC","message":"this is only a test","greeting":"hello, world!"}',
        contentType: 'application/json',
        source: 'test'
    ])

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', newValue, currentAggregate)

    then:
    result.type == currentAggregate.type
    result.initialSource == currentAggregate.initialSource
    result.deleted == false
    result.events.size() == 2
    // note: newer json replaces existing value when PUT is used
    result.rawJson == newValue.content
  }

  def 'aggregate input with DELETE method'() {
    def currentAggregate = new AggregatedInput([
        type: RecordType.granule,
        rawJson: '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        initialSource: 'test',
        deleted: false,
        events: [new InputEvent(null, Method.POST, 'test', null)]
    ])
    def newValue = new Input([
        method: Method.DELETE,
    ])

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', newValue, currentAggregate)

    then:
    result.type == currentAggregate.type
    result.initialSource == currentAggregate.initialSource
    result.deleted == true // <--
    result.events.size() == 2
    result.rawJson == currentAggregate.rawJson
  }

  def 'aggregate input with GET method'() {
    def currentAggregate = new AggregatedInput([
        type: RecordType.granule,
        rawJson: '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        initialSource: 'test',
        deleted: true,
        events: [new InputEvent(null, Method.POST, 'test', null)]
    ])
    def newValue = new Input([
        method: Method.GET,
    ])

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', newValue, currentAggregate)

    then:
    result.type == currentAggregate.type
    result.initialSource == currentAggregate.initialSource
    result.deleted == false // <--
    result.events.size() == 2
    result.rawJson == currentAggregate.rawJson
  }

}
