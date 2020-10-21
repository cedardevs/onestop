package org.cedar.onestop.registry.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.kafka.streams.state.ValueAndTimestamp
import org.cedar.schemas.avro.geojson.Point
import org.cedar.schemas.avro.psi.*
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class StreamFunctionsSpec extends Specification {

  final static String testGranuleJson = ClassLoader.systemClassLoader.getResourceAsStream("test_granule.json").text

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

  def 'aggregated input initializer returns a default AggregatedInput'() {
    expect:
    StreamFunctions.aggregatedInputInitializer.apply().equals(null)
  }

  def 'aggregates an initial input'() {
    def key = 'ABC'
    def input = Input.newBuilder()
        .setType(RecordType.granule)
        .setMethod(Method.PUT)
        .setContent(testGranuleJson)
        .setContentType('application/json')
        .setSource('common-ingest')
        .setOperation(null)
        .build()
    def uglifiedContent = JsonOutput.toJson(new JsonSlurper().parseText(testGranuleJson))
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())
    def aggregate = StreamFunctions.aggregatedInputInitializer.apply()

    when:
    def result = StreamFunctions.inputAggregator.apply(key, timestampedInput, aggregate)

    then:
    result instanceof AggregatedInput
    result.rawJson == uglifiedContent
    result.rawXml == null
    result.initialSource == input.source
    result.type == input.type
    result.fileInformation instanceof FileInformation
    result.fileInformation.size == 42
    result.fileLocations instanceof Map
    result.fileLocations.size() == 1
    result.fileLocations['http://www.google.com'].uri == 'http://www.google.com'
    result.publishing == null
    result.relationships instanceof List
    result.relationships.size() == 1
    result.deleted == false
    result.events instanceof List
    result.events.size() == 1
    result.events[0].source == input.source
    result.events[0].method == input.method
    result.events[0].operation == input.operation
    result.events[0].timestamp == timestampedInput.timestamp()
    result.events[0].failedState == false
    result.errors instanceof List
    result.errors.size() == 0
  }

  def 'handles a DELETE when no aggregate exists yet'() {
    def key = 'ABC'
    def input = Input.newBuilder()
        .setType(RecordType.collection)
        .setMethod(Method.DELETE)
        .build()
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())
    def aggregate = StreamFunctions.aggregatedInputInitializer.apply()

    when:
    def result = StreamFunctions.inputAggregator.apply(key, timestampedInput, aggregate)

    then:
    result == null
  }

  def 'handles a GET when no aggregate exists yet'() {
    def key = 'ABC'
    def input = Input.newBuilder()
        .setType(RecordType.collection)
        .setMethod(Method.GET)
        .build()
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())
    def aggregate = StreamFunctions.aggregatedInputInitializer.apply()

    when:
    def result = StreamFunctions.inputAggregator.apply(key, timestampedInput, aggregate)

    then:
    result == null
  }

  def 'records errors for bad fields and still parses good ones'() {
    def key = 'ABC'
    def input = new Input([
        type       : RecordType.granule,
        method     : Method.POST,
        content    : '{"fileInformation":{"size":"THIS IS NOT A NUMBER!!"},"fileLocations":{"test:one":{"uri":"test:one"}}}',
        contentType: 'application/json',
        source     : 'test',
        operation  : null
    ])
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())
    def aggregate = AggregatedInput.newBuilder().build()

    when:
    def result = StreamFunctions.inputAggregator.apply(key, timestampedInput, aggregate)

    then:
    result.fileInformation == null
    result.errors instanceof List
    result.errors.size() == 1
    result.errors[0].title.contains("fileInformation")

    and:
    result.fileLocations instanceof Map
    result.fileLocations.size() == 1
    result.fileLocations['test:one'].uri == 'test:one'
  }

  def 'aggregate inputs with PATCH method and no operation declared'() {
    def currentAggregate = new AggregatedInput([
        type         : RecordType.granule,
        rawJson      : '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        initialSource: 'test',
        events       : [new InputEvent(null, Method.POST, 'test', null, false)]
    ])
    def input = Input.newBuilder()
        .setType(RecordType.granule)
        .setMethod(Method.PATCH)
        .setContent('{"trackingId":"ABC", "message":"this is only a test","greeting": "hello, world!"}')
        .setContentType('application/json')
        .setSource('test')
        .build()
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', timestampedInput, currentAggregate)

    then:
    result.type == currentAggregate.type
    result.initialSource == currentAggregate.initialSource
    result.deleted == false
    result.events.size() == 2
    result.rawJson == '{"trackingId":"ABC","message":"this is only a test","answer":42,"greeting":"hello, world!"}'
  }

  def 'aggregate inputs with PATCH method and operation ADD'() {
    def currentAggregate = new AggregatedInput([
        type         : RecordType.granule,
        rawJson      : '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        initialSource: 'test',
        events       : [new InputEvent(null, Method.POST, 'test', null, false)]
    ])
    def input = Input.newBuilder()
        .setType(RecordType.granule)
        .setMethod(Method.PATCH)
        .setContent('{"trackingId":"ABC", "message":"this is only a test","greeting": "hello, world!"}')
        .setContentType('application/json')
        .setSource('test')
        .setOperation(OperationType.ADD)
        .build()
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', timestampedInput, currentAggregate)

    then:
    result.type == currentAggregate.type
    result.initialSource == currentAggregate.initialSource
    result.deleted == false
    result.events.size() == 2
    result.rawJson == '{"trackingId":"ABC","message":"this is only a test","answer":42,"greeting":"hello, world!"}'
  }

  def 'aggregate inputs with PATCH method and operation REMOVE'() {
    def currentAggregate = new AggregatedInput([
        type         : RecordType.granule,
        rawJson      : '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        initialSource: 'test',
        events       : [new InputEvent(null, Method.POST, 'test', null, false)]
    ])
    def input = Input.newBuilder()
        .setType(RecordType.granule)
        .setMethod(Method.PATCH)
        .setContent('{"answer": 42}')
        .setContentType('application/json')
        .setSource('test')
        .setOperation(OperationType.REMOVE)
        .build()
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', timestampedInput, currentAggregate)

    then:
    result.type == currentAggregate.type
    result.initialSource == currentAggregate.initialSource
    result.deleted == false
    result.events.size() == 2
    result.rawJson == '{"trackingId":"ABC","message":"this is a test"}'
  }

  def 'aggregate input with PUT method'() {
    def currentAggregate = new AggregatedInput([
        type         : RecordType.granule,
        rawJson      : '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        initialSource: 'test',
        events       : [new InputEvent(null, Method.POST, 'test', null, false)]
    ])
    def input = new Input([
        type       : RecordType.granule,
        method     : Method.PUT,
        content    : '{"trackingId":"ABC","message":"this is only a test","greeting":"hello, world!"}',
        contentType: 'application/json',
        source     : 'test'
    ])
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', timestampedInput, currentAggregate)

    then:
    result.type == currentAggregate.type
    result.initialSource == currentAggregate.initialSource
    result.deleted == false
    result.events.size() == 2
    // note: newer json replaces existing value when PUT is used
    result.rawJson == input.content
  }

  def 'aggregate input with DELETE method that deletes a record'() {
    def currentAggregate = new AggregatedInput([
        type         : RecordType.granule,
        rawJson      : '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        initialSource: 'test',
        deleted      : false,
        events       : [new InputEvent(null, Method.POST, 'test', null, false)]
    ])
    def input = new Input([
        method: Method.DELETE,
    ])
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', timestampedInput, currentAggregate)

    then:
    result.type == currentAggregate.type
    result.initialSource == currentAggregate.initialSource
    result.deleted == true // <--
    result.events.size() == 2
    result.rawJson == currentAggregate.rawJson
  }

  def 'aggregate input with GET method that resurrects a record'() {
    def currentAggregate = new AggregatedInput([
        type         : RecordType.granule,
        rawJson      : '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        initialSource: 'test',
        deleted      : true,
        events       : [new InputEvent(null, Method.POST, 'test', null, false)]
    ])
    def input = new Input([
        method: Method.GET,
    ])
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', timestampedInput, currentAggregate)

    then:
    result.type == currentAggregate.type
    result.initialSource == currentAggregate.initialSource
    result.deleted == false // <--
    result.events.size() == 2
    result.rawJson == currentAggregate.rawJson
  }

  def 'aggregate input with DELETE method on already deleted record'() {
    def currentAggregate = new AggregatedInput([
        type         : RecordType.granule,
        rawJson      : '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        initialSource: 'test',
        deleted      : true,
        events       : [new InputEvent(null, Method.POST, 'test', null, false)]
    ])
    def input = new Input([
        method: Method.DELETE,
    ])
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', timestampedInput, currentAggregate)

    then:
    result.type == currentAggregate.type
    result.initialSource == currentAggregate.initialSource
    result.deleted == true
    result.events.size() == 1 // <--
    result.rawJson == currentAggregate.rawJson
  }

  def 'aggregate input with GET method on record that is not deleted'() {
    def currentAggregate = new AggregatedInput([
        type         : RecordType.granule,
        rawJson      : '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        initialSource: 'test',
        deleted      : false,
        events       : [new InputEvent(null, Method.POST, 'test', null, false)]
    ])
    def input = new Input([
        method: Method.GET,
    ])
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', timestampedInput, currentAggregate)

    then:
    result.type == currentAggregate.type
    result.initialSource == currentAggregate.initialSource
    result.deleted == false
    result.events.size() == 1 // <--
    result.rawJson == currentAggregate.rawJson
  }

  def 'most recent input event indicates success when successful'() {
    def key = 'ABC'
    def input = new Input([
        type       : RecordType.granule,
        method     : Method.POST,
        content    : '{"fileInformation":{"size":1500},"fileLocations":{"test:one":{"uri":"test:one"}}}',
        contentType: 'application/json',
        source     : 'test',
        operation  : null
    ])
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())
    def aggregate = AggregatedInput.newBuilder().build()

    when:
    def result = StreamFunctions.inputAggregator.apply(key, timestampedInput, aggregate)

    then:
    result.errors instanceof List
    result.errors.size() == 0

    and:
    result.events instanceof List
    result.events.size() == 1
    result.events[0].failedState == false
  }

  def 'most recent input event indicates failure when failed'() {
    def key = 'ABC'
    def input = new Input([
        type       : RecordType.granule,
        method     : Method.POST,
        content    : '{"fileInformation":{"size":"THIS IS NOT A NUMBER!!"},"fileLocations":{"test:one":{"uri":"test:one"}}}',
        contentType: 'application/json',
        source     : 'test',
        operation  : null
    ])
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())
    def aggregate = AggregatedInput.newBuilder().build()

    when:
    def result = StreamFunctions.inputAggregator.apply(key, timestampedInput, aggregate)

    then:
    result.errors instanceof List
    result.errors.size() == 1

    and:
    result.events instanceof List
    result.events.size() == 1
    result.events[0].failedState == true
  }

  def 'null initial input received is ignored'() {
    def key = 'ABC'
    def input = null
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())
    def aggregate = StreamFunctions.aggregatedInputInitializer.apply()

    when:
    def result = StreamFunctions.inputAggregator.apply(key, timestampedInput, aggregate)

    then:
    result == null
  }

  def 'null update input received results in tombstone'() {
    def currentAggregate = new AggregatedInput([
        type         : RecordType.granule,
        rawJson      : '{"trackingId":"ABC","message":"this is a test","answer": 42}',
        initialSource: 'test',
        events       : [new InputEvent(null, Method.POST, 'test', null, false)]
    ])
    def input = null
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', timestampedInput, currentAggregate)

    then:
    result == null
  }

  def 'errors reset on new input'() {
    def currentAggregate = new AggregatedInput([
        type         : RecordType.granule,
        rawJson      : '{"fileInformation":{"size":"THIS IS NOT A NUMBER!!"},"fileLocations":{"test:one":{"uri":"test:one"}}}',
        initialSource: 'test',
        events       : [new InputEvent(null, Method.POST, 'test', null, true)],
        errors       : [ErrorEvent.newBuilder().setTitle("Failed to parse field [fileInformation.size]").setDetail("Expected a Number but found a String").build()]
    ])
    def input = Input.newBuilder()
        .setType(RecordType.granule)
        .setMethod(Method.PATCH)
        .setContent('{"fileInformation":{"size":"THIS IS NOT A NUMBER!!"}}')
        .setContentType('application/json')
        .setSource('test')
        .setOperation(OperationType.REMOVE)
        .build()
    def timestampedInput = ValueAndTimestamp.make(input, System.currentTimeMillis())

    when:
    def result = StreamFunctions.inputAggregator.apply('ABC', timestampedInput, currentAggregate)

    then:
    result.type == currentAggregate.type
    result.initialSource == currentAggregate.initialSource
    result.deleted == false
    result.events.size() == 2
    result.events[0].failedState == true
    result.events[1].failedState == false
    result.errors instanceof List
    result.errors.isEmpty()
    result.rawJson == '{"fileInformation":{},"fileLocations":{"test:one":{"uri":"test:one"}}}'
  }

  def 'flattens records by inheriting discovery attributes'() {
    def parent = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setDiscoveryBuilder(Discovery.newBuilder()
            .setTitle("parent")
            .setAlternateTitle("inherit me")
            .setDsmmAverage(1.0f)
            .setDsmmAccessibility(1)
            .setCiteAsStatements(["parent citation"])
            .setLegalConstraints(["parent constraints"])
            .setSpatialBounding(Point.newBuilder().setCoordinates([0.0, 0.0]).build())
            .setIsGlobal(true))
        .setRelationships([Relationship.newBuilder().setType(RelationshipType.COLLECTION).setId('abc').build()])
        .setPublishing(Publishing.newBuilder().setIsPrivate(true).build())
        .build()
    def childId = 'child'
    def child = ParsedRecord.newBuilder()
        .setType(RecordType.granule)
        .setDiscoveryBuilder(Discovery.newBuilder()
            .setTitle("child")
            .setDsmmAverage(2.0f)
            .setCiteAsStatements(["child citation"])
            .setSpatialBounding(Point.newBuilder().setCoordinates([42.0, 42.0]).build())
            .setIsGlobal(false))
        .build()

    when:
    def wrappedChild = new ParsedRecordWithId(id: childId, record: child)
    def flattened = StreamFunctions.flattenRecords.apply(wrappedChild, parent)

    then: "inherits unset fields from the parent"
    flattened.record.discovery.alternateTitle == parent.discovery.alternateTitle // string
    flattened.record.discovery.dsmmAccessibility == parent.discovery.dsmmAccessibility // int
    flattened.record.discovery.legalConstraints.size() == parent.discovery.legalConstraints.size() // list
    flattened.record.discovery.legalConstraints.every { parent.discovery.legalConstraints.contains(it) } // list

    and: "preserves fields set by the child"
    flattened.record.discovery.title == child.discovery.title // string
    flattened.record.discovery.dsmmAverage == child.discovery.dsmmAverage  // float
    flattened.record.discovery.isGlobal == child.discovery.isGlobal // bool
    flattened.record.discovery.spatialBounding == child.discovery.spatialBounding // geometry
    flattened.record.discovery.citeAsStatements.size() == child.discovery.citeAsStatements.size() // list
    flattened.record.discovery.citeAsStatements.every { child.discovery.citeAsStatements.contains(it) } // list

    and: "preserves fields outside of discovery, even if set on parent"
    flattened.id == childId
    flattened.record.type == RecordType.granule
    flattened.record.relationships.isEmpty()
    !flattened.record.publishing.isPrivate
  }
}
