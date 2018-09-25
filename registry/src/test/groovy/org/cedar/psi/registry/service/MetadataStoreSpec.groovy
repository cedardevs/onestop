package org.cedar.psi.registry.service

import groovy.json.JsonSlurper
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.state.QueryableStoreType
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import spock.lang.Specification
import spock.lang.Unroll

import static org.cedar.psi.common.constants.Topics.*


@Unroll
class MetadataStoreSpec extends Specification {

  MetadataStreamService mockMetadataStreamService
  KafkaStreams mockStreamsApp
  ReadOnlyKeyValueStore mockInputStore
  ReadOnlyKeyValueStore mockParsedStore
  MetadataStore metadataStore

  final testType = 'granule'
  final testSource = 'class'

  def setup() {
    mockStreamsApp = Mock(KafkaStreams)
    mockInputStore = Mock(ReadOnlyKeyValueStore)
    mockParsedStore = Mock(ReadOnlyKeyValueStore)
    mockMetadataStreamService = Mock(MetadataStreamService)

    metadataStore = new MetadataStore(mockMetadataStreamService)
  }

  def 'returns null for unknown types'() {
    expect:
    metadataStore.retrieveEntity('notarealtype', 'notarealsource', 'notarealid') == null
  }

  def 'returns null for a nonexistent store'() {
    def testId = 'notarealid'

    when:
    def result = metadataStore.retrieveEntity(testType, testSource, testId)

    then:
    _ * mockMetadataStreamService.getStreamsApp() >> mockStreamsApp
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
    _ * mockMetadataStreamService.getStreamsApp() >> mockStreamsApp
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
    _ * mockMetadataStreamService.getStreamsApp() >> mockStreamsApp
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> mockInputStore
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> {
      throw new InvalidStateStoreException('test')
    }
    1 * mockInputStore.get(testId)
    0 * mockParsedStore.get(testId)

    and:
    thrown(InvalidStateStoreException)
  }

  def 'retrieves an unparsed object by type and id'() {
    def testId = '123'
    def slurper = new JsonSlurper()

    when:
    def result = metadataStore.retrieveEntity(testType, testSource, testId)

    then:
    _ * mockMetadataStreamService.getStreamsApp() >> mockStreamsApp
    1 * mockStreamsApp.store(inputStore(testType, testSource), _ as QueryableStoreType) >> mockInputStore
    1 * mockStreamsApp.store(parsedStore(testType), _ as QueryableStoreType) >> mockParsedStore
    1 * mockInputStore.get(testId) >> rawValue
    1 * mockParsedStore.get(testId) >> parsedValue

    and:
    result == [
        id: testId,
        type: testType,
        attributes: combined
    ]

    where:
    rawValue            | parsedValue   | combined
    ["hello": "world"]  | null          | ["hello": "world"]
    null                | ["answer": 42]| ["answer": 42]
    ["hello": "world"]  | ["answer": 42]| ["hello": "world", "answer": 42]
  }

}
