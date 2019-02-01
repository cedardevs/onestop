package org.cedar.psi.registry.service

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.state.QueryableStoreType
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.cedar.schemas.avro.psi.*
import org.cedar.schemas.avro.util.AvroUtils
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import spock.lang.Unroll

import static org.cedar.psi.common.constants.Topics.inputStore
import static org.cedar.psi.common.constants.Topics.parsedStore


@Unroll
class MetadataStoreSpec extends Specification {

  KafkaStreams mockStreamsApp
  ReadOnlyKeyValueStore mockInputStore
  ReadOnlyKeyValueStore mockParsedStore
  MetadataStore metadataStore
  private MockHttpServletRequest request
  final testType = RecordType.granule
  final testSource = 'class'

  def setup() {
    mockStreamsApp = Mock(KafkaStreams)
    mockInputStore = Mock(ReadOnlyKeyValueStore)
    mockParsedStore = Mock(ReadOnlyKeyValueStore)

    metadataStore = new MetadataStore(mockStreamsApp)
    this.request = new MockHttpServletRequest()
    this.request.setScheme("http")
    this.request.setServerName("localhost")
    this.request.setServerPort(8080)
    this.request.setRequestURI("/mvc-showcase")
    this.request.setContextPath("/mvc-showcase")
  }

  def 'returns null for unknown types'() {
    expect:
    metadataStore.retrieveInput(null, 'notarealsource', 'notarealid') == [error: "No such input record of null with id notarealid"]
  }

  def 'returns null for a nonexistent store'() {
    def testId = 'notarealid'

    when:
    def result = metadataStore.retrieveInput(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> null
    0 * mockInputStore.get(testId)

    and:
    result == [error: "No such input record of ${testType} with id ${testId}"]
  }

  def 'returns null for unknown id'() {
    def testId = 'notarealid'

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> mockParsedStore
    1 * mockParsedStore.get(testId) >> null

    and:
    result == [error: "No such parsed record of ${testType} with id ${testId}"]
  }

  def 'handles when a store is in a bad state'() {
    def testId = '123'

    when:
    metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> {
      throw new InvalidStateStoreException('test')
    }

    0 * mockParsedStore.get(testId)

    and:
    thrown(InvalidStateStoreException)
  }

  def 'retrieves an input record by type and id'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveInput(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> mockInputStore
    1 * mockInputStore.get(testId) >> rawValue

    and:
    result == [
            id        : testId,
            type      : testType,
            attributes: inputValue
    ]

    where:
    rawValue  | inputValue
    testInput | ["input": testInput]
  }

  def 'retrieves a parsed record by type and id'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> mockInputStore
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> mockParsedStore
    1 * mockInputStore.get(testId) >> testInput
    1 * mockParsedStore.get(testId) >> parsedValue

    and:
    result == [
            id        : testId,
            type      : testType,
            source    : testInput.source,
            attributes: testValue
    ]

    where:
    parsedValue | testValue
    testParsed  | AvroUtils.avroToMap(testParsed)
  }

  def 'record parsed record by type and id'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> mockInputStore
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> mockParsedStore
    1 * mockInputStore.get(testId) >> testInput
    1 * mockParsedStore.get(testId) >> parsedValue

    and:
    result == [
        id        : testId,
        type      : testType,
        source    : testInput.source,
        attributes: testValue
    ]

    where:
    parsedValue | testValue
    testParsed  | AvroUtils.avroToMap(testParsed)
  }

  def 'retrieves a record with errors'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> mockInputStore
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> mockParsedStore
    1 * mockInputStore.get(testId) >> testInput
    1 * mockParsedStore.get(testId) >> testErrorRecord

    and:
    result ==  [
            id        : testId,
            type      : testType,
            source    : testInput.source,
            attributes: AvroUtils.avroToMap(testErrorRecord)
    ]
  }


  private static testInput = Input.newBuilder()
      .setType(RecordType.collection)
      .setContent('{"hello":"world"}')
      .setMethod(Method.POST)
      .setContentType('application/json')
      .setSource('test')
      .build()

  private static testParsed = ParsedRecord.newBuilder().setType(RecordType.collection).build()

  private static testError = ErrorEvent.newBuilder()
      .setTitle('this is a test')
      .setDetail('this is only a test')
      .build()

  private static testErrorRecord = ParsedRecord.newBuilder()
      .setType(RecordType.collection)
      .setPublishing()
      .setDiscovery()
      .setAnalysis()
      .setErrors([testError])
      .build()

}
