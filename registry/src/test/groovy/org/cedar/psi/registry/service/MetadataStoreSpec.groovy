package org.cedar.psi.registry.service

import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.state.HostInfo
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.apache.kafka.streams.state.StreamsMetadata
import org.cedar.schemas.avro.psi.AggregatedInput
import org.cedar.schemas.avro.psi.ErrorEvent
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MetadataStoreSpec extends Specification {

  HostInfo localHostInfo = new HostInfo("local", 8080)
  HostInfo remoteHostInfo = new HostInfo("remote", 9090)
  StreamsMetadata localStreamsMetadata = new StreamsMetadata(localHostInfo, Collections.emptySet(), Collections.emptySet())
  StreamsMetadata remoteStreamsMetadata = new StreamsMetadata(remoteHostInfo, Collections.emptySet(), Collections.emptySet())

  MetadataService mockMetadataService
  ReadOnlyKeyValueStore mockInputStore
  ReadOnlyKeyValueStore mockParsedStore

  MetadataStore metadataStore

  def testType = RecordType.granule
  def testSource = 'class'

  def setup() {
    mockInputStore = Mock(ReadOnlyKeyValueStore)
    mockParsedStore = Mock(ReadOnlyKeyValueStore)
    mockMetadataService = Mock(MetadataService)

    metadataStore = new MetadataStore(mockMetadataService, localHostInfo)
  }

  def 'returns null for unknown types'() {
    expect:
    metadataStore.retrieveInput(null, 'notarealsource', 'notarealid') == null
  }

  def 'returns null for a nonexistent store'() {
    def testId = 'notarealid'

    when:
    def result = metadataStore.retrieveInput(testType, testSource, testId)

    then:
    1 * mockMetadataService.streamsMetadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockMetadataService.getInputStore(testType, testSource) >> null
    0 * mockInputStore.get(testId)

    and:
    result == null
  }

  def 'returns null for unknown id'() {
    def testId = 'notarealid'

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockMetadataService.streamsMetadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockMetadataService.getParsedStore(_) >> mockParsedStore
    1 * mockParsedStore.get(testId) >> null

    and:
    result == null
  }

  def 'handles when a store is in a bad state'() {
    def testId = '123'

    when:
    metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockMetadataService.streamsMetadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockMetadataService.getParsedStore(_) >> {
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
    1 * mockMetadataService.streamsMetadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockMetadataService.getInputStore(_, _) >> mockInputStore
    1 * mockInputStore.get(testId) >> testAggInput

    and:
    result ==  testAggInput
  }

  def 'retrieves a parsed record by type and id'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockMetadataService.streamsMetadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockMetadataService.getParsedStore(_) >> mockParsedStore
    1 * mockParsedStore.get(testId) >> testParsed

    and:
    result == testParsed
  }

  def 'retrieves a record with errors'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockMetadataService.streamsMetadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockMetadataService.getParsedStore(_) >> mockParsedStore
    1 * mockParsedStore.get(testId) >> testErrorRecord

    and:
    result == testErrorRecord
  }

  private static testAggInput = AggregatedInput.newBuilder()
      .setType(RecordType.collection)
      .setRawJson('{"hello":"world"}')
      .setInitialSource('test')
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
