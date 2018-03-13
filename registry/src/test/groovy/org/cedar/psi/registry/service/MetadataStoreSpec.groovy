package org.cedar.psi.registry.service

import groovy.json.JsonSlurper
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.state.QueryableStoreType
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import spock.lang.Specification
import spock.lang.Unroll

import static org.cedar.psi.registry.service.MetadataStreamService.RAW_GRANULE_STORE
import static org.cedar.psi.registry.service.MetadataStreamService.PARSED_GRANULE_STORE


@Unroll
class MetadataStoreSpec extends Specification {

  MetadataStreamService mockMetadataStreamService
  KafkaStreams mockStreamsApp
  ReadOnlyKeyValueStore mockRawStore
  ReadOnlyKeyValueStore mockParsedStore
  MetadataStore metadataStore

  def setup() {
    mockStreamsApp = Mock(KafkaStreams)
    mockRawStore = Mock(ReadOnlyKeyValueStore)
    mockParsedStore = Mock(ReadOnlyKeyValueStore)
    mockMetadataStreamService = Mock(MetadataStreamService)

    metadataStore = new MetadataStore(mockMetadataStreamService)
  }

  def 'returns null for unknown types'() {
    expect:
    metadataStore.retrieveEntity('notarealtype', 'notarealid') == null
  }

  def 'returns null for a nonexistent store'() {
    def testId = 'notarealid'

    when:
    def result = metadataStore.retrieveEntity('granule', testId)

    then:
    _ * mockMetadataStreamService.getStreamsApp() >> mockStreamsApp
    1 * mockStreamsApp.store(RAW_GRANULE_STORE, _ as QueryableStoreType) >> null
    1 * mockStreamsApp.store(PARSED_GRANULE_STORE, _ as QueryableStoreType) >> null
    0 * mockRawStore.get(testId)
    0 * mockParsedStore.get(testId)

    and:
    result == null
  }

  def 'returns null for unknown id'() {
    def testId = 'notarealid'

    when:
    def result = metadataStore.retrieveEntity('granule', testId)

    then:
    _ * mockMetadataStreamService.getStreamsApp() >> mockStreamsApp
    1 * mockStreamsApp.store(RAW_GRANULE_STORE, _ as QueryableStoreType) >> mockRawStore
    1 * mockStreamsApp.store(PARSED_GRANULE_STORE, _ as QueryableStoreType) >> mockParsedStore
    1 * mockRawStore.get(testId) >> null
    1 * mockParsedStore.get(testId) >> null

    and:
    result == null
  }

  def 'handles when a store is in a bad state'() {
    def testId = '123'

    when:
    metadataStore.retrieveEntity('granule', testId)

    then:
    _ * mockMetadataStreamService.getStreamsApp() >> mockStreamsApp
    1 * mockStreamsApp.store(RAW_GRANULE_STORE, _ as QueryableStoreType) >> mockRawStore
    1 * mockStreamsApp.store(PARSED_GRANULE_STORE, _ as QueryableStoreType) >> {
      throw new InvalidStateStoreException('test')
    }
    1 * mockRawStore.get(testId)
    0 * mockParsedStore.get(testId)

    and:
    thrown(InvalidStateStoreException)
  }

  def 'retrieves an unparsed object by type and id'() {
    def testId = '123'
    def slurper = new JsonSlurper()

    when:
    def result = metadataStore.retrieveEntity('granule', testId)

    then:
    _ * mockMetadataStreamService.getStreamsApp() >> mockStreamsApp
    1 * mockStreamsApp.store(RAW_GRANULE_STORE, _ as QueryableStoreType) >> mockRawStore
    1 * mockStreamsApp.store(PARSED_GRANULE_STORE, _ as QueryableStoreType) >> mockParsedStore
    1 * mockRawStore.get(testId) >> rawValue
    1 * mockParsedStore.get(testId) >> parsedValue

    and:
    def expectedRaw = rawValue ? slurper.parseText(rawValue) : null
    def expectedParsed = parsedValue ? slurper.parseText(parsedValue) : null
    result == [
        id: testId,
        type: 'granule',
        attributes: [
            raw: expectedRaw,
            parsed: expectedParsed
        ]
    ]

    where:
    rawValue              | parsedValue
    '{"hello": "world"}'  | null
    null                  | '{"answer": 42}'
    '{"hello": "world"}'  | '{"answer": 42}'
  }

}
