package org.cedar.psi.registry.service

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.state.QueryableStoreType
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import spock.lang.Specification


class MetadataStoreSpec extends Specification {

  static MOCK_GRANULE_STORE = 'test-granules'
  static MOCK_COLLECTION_STORE = 'test-collections'

  KafkaStreams mockStreamsApp
  ReadOnlyKeyValueStore mockSteamStore
  MetadataStore metadataStore

  def setup() {
    mockStreamsApp = Mock(KafkaStreams)
    mockSteamStore = Mock(ReadOnlyKeyValueStore)

    metadataStore = new MetadataStore(mockStreamsApp)
    metadataStore.RAW_GRANULE_STORE = MOCK_GRANULE_STORE
    metadataStore.RAW_COLLECTION_STORE = MOCK_COLLECTION_STORE
  }

  def 'returns null for unknown types'() {
    expect:
    metadataStore.retrieveFromStore('notarealtype', 'notarealid') == null
  }

  def 'returns null for a nonexistent store'() {
    def testId = 'notarealid'

    when:
    def result = metadataStore.retrieveFromStore('granule', testId)

    then:
    1 * mockStreamsApp.store(MOCK_GRANULE_STORE, _ as QueryableStoreType) >> null
    0 * mockSteamStore.get(testId)

    and:
    result == null
  }

  def 'returns null for unknown id'() {
    def testId = 'notarealid'

    when:
    def result = metadataStore.retrieveFromStore('granule', testId)

    then:
    1 * mockStreamsApp.store(MOCK_GRANULE_STORE, _ as QueryableStoreType) >> mockSteamStore
    1 * mockSteamStore.get(testId) >> null

    and:
    result == null
  }

  def 'handles when a store is in a bad state'() {
    def testId = '123'

    when:
    metadataStore.retrieveFromStore('granule', testId)

    then:
    1 * mockStreamsApp.store(MOCK_GRANULE_STORE, _ as QueryableStoreType) >> {
      throw new InvalidStateStoreException('test')
    }
    0 * mockSteamStore.get(testId)

    and:
    thrown(InvalidStateStoreException)
  }

  def 'retrieves an object by type and id'() {
    def testId = '123'
    def testValue = '{"hello": "world"}'

    when:
    def result = metadataStore.retrieveFromStore('granule', testId)

    then:
    1 * mockStreamsApp.store(MOCK_GRANULE_STORE, _ as QueryableStoreType) >> mockSteamStore
    1 * mockSteamStore.get(testId) >> testValue

    and:
    result == [id: testId, type: 'granule', attributes: [hello: 'world']]
  }

}
