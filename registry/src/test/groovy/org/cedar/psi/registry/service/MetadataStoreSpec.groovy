package org.cedar.psi.registry.service

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.state.QueryableStoreType
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.cedar.psi.common.avro.ErrorEvent
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.Method
import org.cedar.psi.common.avro.ParsedRecord
import org.cedar.psi.common.avro.RecordType
import org.cedar.psi.common.util.AvroUtils
import spock.lang.Specification
import spock.lang.Unroll

import static org.cedar.psi.common.constants.Topics.*


@Unroll
class MetadataStoreSpec extends Specification {

  KafkaStreams mockStreamsApp
  ReadOnlyKeyValueStore mockInputStore
  ReadOnlyKeyValueStore mockParsedStore
  MetadataStore metadataStore

  final testType = RecordType.granule
  final testSource = 'class'

  def setup() {
    mockStreamsApp = Mock(KafkaStreams)
    mockInputStore = Mock(ReadOnlyKeyValueStore)
    mockParsedStore = Mock(ReadOnlyKeyValueStore)

    metadataStore = new MetadataStore(mockStreamsApp)
  }

  def 'returns null for unknown types'() {
    expect:
    metadataStore.retrieveEntity(null, 'notarealsource', 'notarealid') == null
  }

  def 'returns null for a nonexistent store'() {
    def testId = 'notarealid'

    when:
    def result = metadataStore.retrieveEntity(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> null
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> null
    0 * mockInputStore.get(testId)
    0 * mockParsedStore.get(testId)

    and:
    result == null
  }

  def 'returns null for unknown id'() {
    def testId = 'notarealid'

    when:
    def result = metadataStore.retrieveEntity(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> mockInputStore
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> mockParsedStore
    1 * mockInputStore.get(testId) >> null
    1 * mockParsedStore.get(testId) >> null

    and:
    result == null
  }

  def 'handles when a store is in a bad state'() {
    def testId = '123'

    when:
    metadataStore.retrieveEntity(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> mockInputStore
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> {
      throw new InvalidStateStoreException('test')
    }
    1 * mockInputStore.get(testId)
    0 * mockParsedStore.get(testId)

    and:
    thrown(InvalidStateStoreException)
  }

  def 'retrieves a record by type and id'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveEntity(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> mockInputStore
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> mockParsedStore
    1 * mockInputStore.get(testId) >> rawValue
    1 * mockParsedStore.get(testId) >> parsedValue

    and:
    result == [
        data: [
            id        : testId,
            type      : testType,
            attributes: combined
        ]
    ]

    where:
    rawValue  | parsedValue | combined
    testInput | null        | ["input": testInput]
    null      | testParsed  | AvroUtils.avroToMap(testParsed)
    testInput | testParsed  | ["input": testInput] + AvroUtils.avroToMap(testParsed)
  }

  def 'retrieves a record with errors'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveEntity(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> mockInputStore
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> mockParsedStore
    1 * mockInputStore.get(testId) >> testInput
    1 * mockParsedStore.get(testId) >> testErrorRecord

    and:
    result ==  [
        data: [
            id        : testId,
            type      : testType,
            attributes: ["input": testInput] + AvroUtils.avroToMap(testErrorRecord)
        ]
    ]
  }

  private static testInput = Input.newBuilder()
      .setContent('{"hello":"world"}')
      .setMethod(Method.POST)
      .setContentType('application/json')
      .setHost('localhost')
      .setProtocol('http')
      .setRequestUrl('/test')
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
