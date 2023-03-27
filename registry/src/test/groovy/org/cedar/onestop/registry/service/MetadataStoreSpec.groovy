package org.cedar.onestop.registry.service

import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.state.HostInfo
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.apache.kafka.streams.KeyQueryMetadata
import org.cedar.schemas.avro.psi.*
import org.cedar.schemas.avro.util.MockSchemaRegistrySerde
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MetadataStoreSpec extends Specification {

  HostInfo localHostInfo = new HostInfo("thetesthost", 8080)
  KeyQueryMetadata localStreamsMetadata = new KeyQueryMetadata(localHostInfo, Collections.emptySet(), 0)

  StreamsStateService mockStreamsStateService = Mock(StreamsStateService)
  ReadOnlyKeyValueStore mockAvroStore = Mock(ReadOnlyKeyValueStore)
  MockSchemaRegistrySerde mockSerde = new MockSchemaRegistrySerde()

  def testType = RecordType.granule
  def testSource = 'class'
  def testPort = 9090

  MetadataStore metadataStore = new MetadataStore(mockStreamsStateService, localHostInfo, testPort, 'http://dummyurl')

  def setup() {
    // override the internal serde with one that uses a MockSchemaRegistryClient
    metadataStore.serde = mockSerde
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
    1 * mockStreamsStateService.getAvroStore(_) >> null
    0 * mockAvroStore.get(testId)

    and:
    result == null
  }

  def 'returns null for unknown id'() {
    def testId = 'notarealid'

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getAvroStore(_) >> mockAvroStore
    1 * mockAvroStore.get(testId) >> null

    and:
    result == null
  }

  def 'handles when a store is in a bad state'() {
    def testId = '123'

    when:
    metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getAvroStore(_) >> {
      throw new InvalidStateStoreException('test')
    }

    0 * mockAvroStore.get(testId)

    and:
    thrown(InvalidStateStoreException)
  }

  def 'retrieves a local input record by type and id'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveInput(testType, testSource, testId)

    then:
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getAvroStore(_) >> mockAvroStore
    1 * mockAvroStore.get(testId) >> testAggInput

    and:
    result ==  testAggInput
  }

  def 'retrieves a local parsed record by type and id'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getAvroStore(_) >> mockAvroStore
    1 * mockAvroStore.get(testId) >> testParsed

    and:
    result == testParsed
  }

  def 'retrieves a local record with errors'() {
    def testId = '123'

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getAvroStore(_) >> mockAvroStore
    1 * mockAvroStore.get(testId) >> testErrorRecord

    and:
    result == testErrorRecord
  }

  def 'hosts binary db endpoint on embedded server'() {
    setup:
    def table = 'test-table'
    def key = '123'
    metadataStore.start()

    when:
    def bytes = WebClient.create().get()
        .uri("http://localhost:$testPort/db/$table/$key")
        .retrieve().bodyToMono(byte[].class).block()

    then:
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getAvroStore(_) >> mockAvroStore
    1 * mockAvroStore.get(key) >> testRecord

    and:
    def expected = mockSerde.serializer().serialize('null-topic', testRecord)
    bytes.length == expected.length
    bytes == expected

    cleanup:
    metadataStore.stop()

    where:
    testRecord << [
        testAggInput,
        testErrorRecord,
        testParsed,
    ]
  }

  def 'retrieves a remote entity'() {
    metadataStore.start()
    def testId = '123'

    // NOTE: the "remote" metadata actually points to the location where the embedded server is running
    def remoteHostInfo = new HostInfo("localhost", testPort)
    def remoteStreamsMetadata = new KeyQueryMetadata(remoteHostInfo, Collections.emptySet(), 0)

    when:
    def result = metadataStore.retrieveInput(testType, testSource, testId)

    then:
    // first kafka metadata lookup indicates remote, subsequent lookups indicate local
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> remoteStreamsMetadata
    _ * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getAvroStore(_) >> mockAvroStore
    1 * mockAvroStore.get(_) >> testAggInput

    and:
    // comparing toStrings because deep equal comparisons on avro objects don't work when they have maps in them
    result.toString() == testAggInput.toString()

    cleanup:
    metadataStore.stop()
  }

  def 'returns null when a remote entity is not found'() {
    setup:
    metadataStore.start()
    def testId = '123'

    // NOTE: the "remote" metadata actually points to the location where the embedded server is running
    def remoteHostInfo = new HostInfo("localhost", testPort)
    def remoteStreamsMetadata = new KeyQueryMetadata(remoteHostInfo, Collections.emptySet(), 0)

    when:
    def result = metadataStore.retrieveParsed(testType, testSource, testId)

    then:
    // first kafka metadata lookup indicates remote, subsequent lookups indicate local
    1 * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> remoteStreamsMetadata
    _ * mockStreamsStateService.metadataForStoreAndKey(_, _, _) >> localStreamsMetadata
    1 * mockStreamsStateService.getAvroStore(_) >> mockAvroStore
    1 * mockAvroStore.get(_) >> null

    and:
    result == null

    cleanup:
    metadataStore.stop()
  }

  private static testAggInput = AggregatedInput.newBuilder()
      .setType(RecordType.collection)
      .setRawJson('{"hello":"world"}')
      .setInitialSource('test')
      .build()

  private static testParsed = ParsedRecord.newBuilder()
      .setType(RecordType.collection)
      .setRelationships([Relationship.newBuilder().setId('abc').setType(RelationshipType.COLLECTION).build()])
      .build()

  private static testError = ErrorEvent.newBuilder()
      .setTitle('this is a test')
      .setDetail('this is only a test')
      .build()

  private static testErrorRecord = ParsedRecord.newBuilder()
      .setType(RecordType.collection)
      .setErrors([testError])
      .build()

}
