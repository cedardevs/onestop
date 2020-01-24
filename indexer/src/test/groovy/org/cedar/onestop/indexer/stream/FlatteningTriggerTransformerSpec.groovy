package org.cedar.onestop.indexer.stream

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.processor.MockProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.Stores
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchVersion
import org.cedar.onestop.indexer.util.ElasticsearchService
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.client.Cancellable
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.index.get.GetResult
import org.elasticsearch.index.reindex.BulkByScrollResponse
import org.elasticsearch.index.seqno.SequenceNumbers
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

class FlatteningTriggerTransformerSpec extends Specification {

  static testEsConfig = new ElasticsearchConfig(
      new ElasticsearchVersion("7.5.1"),
      "BulkIndexingTransformerSpec-",
      1,
      1,
      1,
      1,
      false
  )
  static storeName = "FlatteningTriggerTransformerSpecStore"
  static mockScript = "println('test')"
  static startTime = Instant.parse("2020-01-01T00:00:00Z")
  static testInterval = Duration.ofSeconds(1)
  static startTimePlusInterval = startTime + testInterval

  ElasticsearchService mockEsService
  MockProcessorContext mockProcessorContext
  KeyValueStore<String, Long> testStore
  FlatteningTriggerTransformer testTransformer

  def setup() {
    mockEsService = Mock(ElasticsearchService)
    mockEsService.getConfig() >> testEsConfig
    mockProcessorContext = new MockProcessorContext()
    mockProcessorContext.setTimestamp(startTime.toEpochMilli())
    testStore = Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(storeName), Serdes.String(), Serdes.Long())
        .withLoggingDisabled().build()
    testStore.init(mockProcessorContext, testStore)
    testTransformer = new FlatteningTriggerTransformer(storeName, mockEsService, mockScript, testInterval)
    testTransformer.init(mockProcessorContext)
  }

  def "creates a wall clock punctuator"() {
    def punctuators = mockProcessorContext.scheduledPunctuators()

    expect:
    punctuators.size() == 1
    punctuators[0].getIntervalMs() == testInterval.toMillis()
    punctuators[0].type == PunctuationType.WALL_CLOCK_TIME
  }

  def "flattening triggers are fired based on wall clock time"() {
    def testId = 'a'
    def timestamp = 1000L

    when:
    testTransformer.transform(testId, timestamp)

    then:
    0 * mockEsService.reindexAsync(_, _)

    when:
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "collection"}')
    1 * mockEsService.blockUntilTasksAvailable()
    1 * mockEsService.reindexAsync(_, _)
  }

  def "flattening triggers are windowed and the earliest timestamp is used"() {
    def testId = 'a'
    def timestamp1 = 1000L
    def timestamp2 = 2000L
    def timestamp3 = 3000L

    when:
    testTransformer.transform(testId, timestamp2)
    testTransformer.transform(testId, timestamp1)
    testTransformer.transform(testId, timestamp3)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "collection"}')
    1 * mockEsService.blockUntilTasksAvailable()
    1 * mockEsService.reindexAsync({ it.script.params.stagedDate == timestamp1 }, _) // <-- verify timestamp param
  }

  def "does not flatten if collection doesn't exist"() {
    def testId = 'a'
    def timestamp = 1000L

    when:
    testTransformer.transform(testId, timestamp)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, null)
    0 * mockEsService.blockUntilTasksAvailable()
    0 * mockEsService.reindexAsync(_, _)
  }

  def "flattening requests use the parent collection for defaults"() {
    def testId = 'a'
    def timestamp = 1000L

    when:
    testTransformer.transform(testId, timestamp)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "collection"}')
    1 * mockEsService.blockUntilTasksAvailable()
    1 * mockEsService.reindexAsync({ it.script.params.defaults.title == 'collection' }, _) // <-- verify defaults param
  }

  def "successful requests produce successful results"() {
    def testId = 'a'
    def timestamp = 1000L
    ActionListener testListener

    when:
    testTransformer.transform(testId, timestamp)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "collection"}')
    1 * mockEsService.blockUntilTasksAvailable()
    1 * mockEsService.reindexAsync(_, _) >> { r, l -> testListener = l; return Mock(Cancellable)} // <- capture listener

    when:
    testListener.onResponse(Mock(BulkByScrollResponse))

    then:
    mockProcessorContext.forwarded().size() == 1
    def forwardedKeyValue = mockProcessorContext.forwarded()[0].keyValue()
    forwardedKeyValue.key == testId
    forwardedKeyValue.value instanceof FlatteningTriggerTransformer.FlatteningTriggerResult
    forwardedKeyValue.value.successful == true
  }

  def "failed requests produce unsuccessful results"() {
    def testId = 'a'
    def timestamp = 1000L
    ActionListener testListener

    when:
    testTransformer.transform(testId, timestamp)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "collection"}')
    1 * mockEsService.blockUntilTasksAvailable()
    1 * mockEsService.reindexAsync(_, _) >> { r, l -> testListener = l; return Mock(Cancellable)} // <- capture listener

    when:
    testListener.onFailure(new RuntimeException())

    then:
    mockProcessorContext.forwarded().size() == 1
    def forwardedKeyValue = mockProcessorContext.forwarded()[0].keyValue()
    forwardedKeyValue.key == testId
    forwardedKeyValue.value instanceof FlatteningTriggerTransformer.FlatteningTriggerResult
    forwardedKeyValue.value.successful == false
  }

  def "IOExceptions produce unsuccessful results"() {
    def testId = 'a'
    def timestamp = 1000L

    when:
    testTransformer.transform(testId, timestamp)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "collection"}')
    1 * mockEsService.blockUntilTasksAvailable()
    1 * mockEsService.reindexAsync(_, _) >> { throw new IOException() }

    and:
    noExceptionThrown()
    mockProcessorContext.forwarded().size() == 1
    def forwardedKeyValue = mockProcessorContext.forwarded()[0].keyValue()
    forwardedKeyValue.key == testId
    forwardedKeyValue.value instanceof FlatteningTriggerTransformer.FlatteningTriggerResult
    forwardedKeyValue.value.successful == false
  }


  private static advanceWallClockTime(MockProcessorContext context, Long timestamp) {
    context.scheduledPunctuators().get(0).getPunctuator().punctuate(timestamp)
  }

  private static buildGetResponse(String index, String id, String json) {
    def result
    if (json == null) {
      result = new GetResult(index, '_doc', id, SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0, false, null, null, null)
    }
    else {
      def source = new BytesArray(json.bytes)
      result = new GetResult(index, '_doc', id, 1, 1, 0, true, source, null, null)
    }
    return new GetResponse(result)
  }

}
