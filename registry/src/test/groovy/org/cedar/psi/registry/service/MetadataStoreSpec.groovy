package org.cedar.psi.registry.service

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.state.QueryableStoreType
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.cedar.schemas.avro.psi.*
import spock.lang.Specification
import spock.lang.Unroll

import static org.cedar.psi.common.constants.Topics.inputStore
import static org.cedar.psi.common.constants.Topics.parsedStore

@Unroll
class MetadataStoreSpec extends Specification {

  KafkaStreams mockStreamsApp
  ReadOnlyKeyValueStore mockInputStore
  ReadOnlyKeyValueStore mockParsedStore
  MetadataStore mockMetadataStore

  def testType = RecordType.granule
  def testSource = 'class'

  def setup() {
    mockStreamsApp = Mock(KafkaStreams)
    mockInputStore = Mock(ReadOnlyKeyValueStore)
    mockParsedStore = Mock(ReadOnlyKeyValueStore)
    mockMetadataStore = new MetadataStore(mockStreamsApp)
  }

  def 'returns null for unknown types'() {
    expect:
    mockMetadataStore.retrieveInput(null, 'notarealsource', 'notarealid') == null
  }

  def 'returns null for a nonexistent store'() {
    def testId = 'notarealid'

    when:
    def result = mockMetadataStore.retrieveInput(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> null
    0 * mockInputStore.get(testId)

    and:
    result == null
  }

  def 'returns null for unknown id'() {
    def testId = 'notarealid'

    when:
    def result = mockMetadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> mockParsedStore
    1 * mockParsedStore.get(testId) >> null

    and:
    result == null
  }

  def 'handles when a store is in a bad state'() {
    def testId = '123'

    when:
    mockMetadataStore.retrieveParsed(testType, testSource, testId)

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
    def result = mockMetadataStore.retrieveInput(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> mockInputStore
    1 * mockInputStore.get(testId) >> testInput

    and:
    result ==  testInput
  }

  def 'retrieves a parsed record by type and id'() {
    def testId = '123'

    when:
    def result = mockMetadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> mockParsedStore
    1 * mockParsedStore.get(testId) >> testParsed

    and:
    result == testParsed
  }

  def 'retrieves a record with errors'() {
    def testId = '123'

    when:
    def result = mockMetadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> mockParsedStore
    1 * mockParsedStore.get(testId) >> testErrorRecord

    and:
    result == testErrorRecord
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
