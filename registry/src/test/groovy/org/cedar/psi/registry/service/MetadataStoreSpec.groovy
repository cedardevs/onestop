package org.cedar.psi.registry.service

import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.state.HostInfo
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.apache.kafka.streams.state.StreamsMetadata
import org.cedar.psi.registry.util.AvroTransformers
import org.cedar.schemas.avro.psi.AggregatedInput
import org.cedar.schemas.avro.psi.ErrorEvent
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MetadataStoreSpec extends Specification {

  HostInfo localHostInfo = new HostInfo("local", 8080)
  HostInfo remoteHostInfo = new HostInfo("remote", 9090)
  StreamsMetadata localStreamsMetadata = new StreamsMetadata(localHostInfo, Collections.emptySet(), Collections.emptySet())
  StreamsMetadata remoteStreamsMetadata = new StreamsMetadata(remoteHostInfo, Collections.emptySet(), Collections.emptySet())

  StreamsStateService mockStreamsStateService
  ReadOnlyKeyValueStore mockInputStore
  ReadOnlyKeyValueStore mockParsedStore

  MetadataStore metadataStore

  def testType = RecordType.granule
  def testSource = 'class'
  def testContext = 'testContext'

  def setup() {
    mockInputStore = Mock(ReadOnlyKeyValueStore)
    mockParsedStore = Mock(ReadOnlyKeyValueStore)
    mockStreamsStateService = Mock(StreamsStateService)

    metadataStore = new MetadataStore(mockStreamsStateService, localHostInfo, testContext)
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
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getInputStore(testType, testSource) >> null
    0 * mockInputStore.get(testId)

    and:
    result == null
  }

  def 'returns null for unknown id'() {
    def testId = 'notarealid'

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getParsedStore(_) >> mockParsedStore
    1 * mockParsedStore.get(testId) >> null

    and:
    result == null
  }

  def 'handles when a store is in a bad state'() {
    def testId = '123'

    when:
    metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getParsedStore(_) >> {
      throw new InvalidStateStoreException('test')
    }

    0 * mockParsedStore.get(testId)

    and:
    thrown(InvalidStateStoreException)
  }

  def 'retrieves a local input record by type and id'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveInput(testType, testSource, testId)

    then:
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getInputStore(_, _) >> mockInputStore
    1 * mockInputStore.get(testId) >> testAggInput

    and:
    result ==  testAggInput
  }

  def 'retrieves a local parsed record by type and id'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getParsedStore(_) >> mockParsedStore
    1 * mockParsedStore.get(testId) >> testParsed

    and:
    result == testParsed
  }

  def 'retrieves a local record with errors'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getParsedStore(_) >> mockParsedStore
    1 * mockParsedStore.get(testId) >> testErrorRecord

    and:
    result == testErrorRecord
  }

  def 'retrieves a remote entity'() {
    def testId = '123'
    def testBytes = AvroTransformers.avroToBytes(testParsed)
    def mockResponse = Mock(ResponseEntity)
    mockResponse.getStatusCode() >> HttpStatus.OK
    mockResponse.getBody() >> testBytes
    def mockRestTemplate = Mock(RestTemplate)
    metadataStore.restTemplate = mockRestTemplate

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> remoteStreamsMetadata
    0 * mockStreamsStateService.getParsedStore(_)
    0 * mockParsedStore.get(_)
    1 * mockRestTemplate.getForEntity(_, byte[].class) >> mockResponse

    and:
    // comparing toStrings because deep equal comparisons on avro objects don't work when they have maps in them
    result.toString() == testParsed.toString()
  }

  def 'returns null when a remote entity is not found'() {
    def testId = '123'
    def mockRestTemplate = Mock(RestTemplate)
    metadataStore.restTemplate = mockRestTemplate

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> remoteStreamsMetadata
    0 * mockStreamsStateService.getParsedStore(_)
    0 * mockParsedStore.get(_)
    1 * mockRestTemplate.getForEntity(_, byte[].class) >> {
      throw new HttpClientErrorException.NotFound('not found', null, null, null)
    }

    and:
    result == null
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
