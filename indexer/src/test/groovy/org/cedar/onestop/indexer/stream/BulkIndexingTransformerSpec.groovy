package org.cedar.onestop.indexer.stream

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.processor.MockProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.Stores
import org.apache.kafka.streams.state.TimestampedKeyValueStore
import org.apache.kafka.streams.state.ValueAndTimestamp
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchVersion
import org.cedar.onestop.indexer.util.*
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.util.MockSchemaRegistrySerde
import org.elasticsearch.action.DocWriteRequest
import org.elasticsearch.action.DocWriteRequest.OpType
import org.elasticsearch.action.bulk.BulkItemResponse
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.index.shard.ShardId
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

class BulkIndexingTransformerSpec extends Specification {

  static inputCollectionXml = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-collection.xml').text
  static inputCollectionRecord = TestUtils.buildRecordFromXML(inputCollectionXml)
  static testEsConfig = new ElasticsearchConfig(
      new ElasticsearchVersion("7.5.1"),
      "BulkIndexingTransformerSpec-",
      1,
      1,
      1,
      1,
      false
  )
  static publishingStartTime = Instant.parse("2020-01-01T00:00:00Z")
  static storeName = "BulkIndexingTransformerSpecStore"
  static testTopic = 'testTopic'
  static testIndex = 'testIndex'

  Duration testBulkInterval = Duration.ofSeconds(1)
  Long testMaxBytes = 1_000_000
  ElasticsearchService mockEsService
  MockProcessorContext mockProcessorContext
  TimestampedKeyValueStore<String, ParsedRecord> testStore
  BulkIndexingConfig testIndexingConfig
  BulkIndexingTransformer testIndexingTransformer

  def setup() {
    mockEsService = Mock(ElasticsearchService)
    mockEsService.getConfig() >> testEsConfig
    mockProcessorContext = new MockProcessorContext()
    mockProcessorContext.setTimestamp(publishingStartTime.toEpochMilli())
    mockProcessorContext.setTopic(testTopic)
    testStore = Stores.timestampedKeyValueStoreBuilder(
        Stores.inMemoryKeyValueStore(storeName), Serdes.String(), new MockSchemaRegistrySerde())
        .withLoggingDisabled().build()
    testStore.init(mockProcessorContext, testStore)
    testIndexingConfig = BulkIndexingConfig.newBuilder()
        .withStoreName(storeName)
        .withMaxPublishBytes(testMaxBytes)
        .withMaxPublishInterval(testBulkInterval)
        .addIndexMapping(testTopic, OpType.INDEX, testIndex)
        .build()
    testIndexingTransformer = new BulkIndexingTransformer(mockEsService, testIndexingConfig)
    testIndexingTransformer.init(mockProcessorContext)
  }


  def "wall clock time triggers bulk request"() {
    def testKeyA = 'a'
    def testValueA = ValueAndTimestamp.make(inputCollectionRecord, publishingStartTime.toEpochMilli())
    def testKeyB = 'b'
    def testValueB = ValueAndTimestamp.make(inputCollectionRecord, publishingStartTime.toEpochMilli())
    def punctuator = mockProcessorContext.scheduledPunctuators().get(0)

    when:
    testIndexingTransformer.transform(testKeyA, testValueA)
    testIndexingTransformer.transform(testKeyB, testValueB)

    then:
    punctuator != null
    punctuator.getIntervalMs() == testBulkInterval.toMillis()
    punctuator.type == PunctuationType.WALL_CLOCK_TIME
    0 * mockEsService.bulk(_)

    when:
    punctuator.getPunctuator().punctuate(publishingStartTime.plus(testBulkInterval).toEpochMilli())

    then:
    1 * mockEsService.bulk(_) >> buildBulkResponse(OpType.INDEX, [testKeyA, testKeyB])
    mockProcessorContext.forwarded().size() == 2
  }

  def "byte size triggers bulk request"() {
    def testValue = ValueAndTimestamp.make(inputCollectionRecord, publishingStartTime.toEpochMilli())
    def sizeCheckItemRequests = IndexingHelpers.mapRecordToRequests(new IndexingInput(testTopic, 'dummy', testValue, testIndexingConfig, testEsConfig))
    def sizeCheckBulkRequest = new BulkRequest()
    sizeCheckItemRequests.each { sizeCheckBulkRequest.add(it) }
    def testValueSize = sizeCheckBulkRequest.estimatedSizeInBytes()
    def numRequests = (testMaxBytes / testValueSize).toInteger() + 1 // enough to trigger size-based flushing

    when:
    numRequests.times { n ->
      testIndexingTransformer.transform(n.toString(), testValue)
    }

    then:
    1 * mockEsService.bulk(_) >> buildBulkResponse(OpType.INDEX, (1..numRequests).collect{it.toString()})
    mockProcessorContext.forwarded().size() == numRequests
  }

  def "forwarded outputs contain the input parsed record and the indexing result"() {
    def testKey = 'a'
    def testValue = ValueAndTimestamp.make(inputCollectionRecord, publishingStartTime.toEpochMilli())
    def punctuator = mockProcessorContext.scheduledPunctuators().get(0)

    when:
    testIndexingTransformer.transform(testKey, testValue)
    punctuator.getPunctuator().punctuate(publishingStartTime.plus(testBulkInterval).toEpochMilli())

    then:
    1 * mockEsService.bulk(_) >> buildBulkResponse(OpType.INDEX, [testKey])

    and:
    def forwarded = mockProcessorContext.forwarded()
    forwarded.size() == 1
    def outputKey = forwarded.get(0).keyValue().key
    outputKey instanceof String
    outputKey == testKey
    def outputValue = forwarded.get(0).keyValue().value
    outputValue instanceof IndexingOutput
    outputValue.isSuccessful()
    outputValue.id == testKey
    outputValue.index == testIndex
    outputValue.operation == OpType.INDEX
    outputValue.timestamp == publishingStartTime.toEpochMilli()
    outputValue.record instanceof ParsedRecord
    outputValue.record.discovery.fileIdentifier == inputCollectionRecord.discovery.fileIdentifier
  }

  private static buildBulkResponse(OpType opType, List<String> ids) {
    def itemResponses = ids.collect {
      def shard = new ShardId(testIndex, "uuid", 0)
      def itemResponse = new IndexResponse(shard, '_doc', it, 0, 0, 0, true)
      return new BulkItemResponse(0, opType, itemResponse)
    }
    return new BulkResponse(itemResponses as BulkItemResponse[], 10)
  }
}
