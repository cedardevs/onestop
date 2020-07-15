package org.cedar.onestop.indexer.stream

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.processor.MockProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.Stores
import org.cedar.onestop.indexer.util.ElasticsearchService
import org.cedar.onestop.indexer.util.TestUtils
import org.elasticsearch.action.bulk.BulkItemResponse
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.client.Cancellable
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.json.JsonXContent
import org.elasticsearch.index.get.GetResult
import org.elasticsearch.index.reindex.BulkByScrollResponse
import org.elasticsearch.index.reindex.BulkByScrollTask
import org.elasticsearch.index.seqno.SequenceNumbers
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

class FlatteningTransformerSpec extends Specification {

  static storeName = "FlatteningTriggerTransformerSpecStore"
  static testScript = "scripts/flattenGranules.painless"
  static startTime = Instant.parse("2020-01-01T00:00:00Z")
  static testInterval = Duration.ofSeconds(1)
  static startTimePlusInterval = startTime + testInterval

  ElasticsearchService mockEsService
  MockProcessorContext mockProcessorContext
  KeyValueStore<String, Long> testStore
  FlatteningConfig testConfig
  FlatteningTransformer testTransformer
  BulkByScrollResponse mockBulkByScrollResponse

  def setup() {
    mockEsService = Mock(ElasticsearchService)
    mockBulkByScrollResponse = Mock(BulkByScrollResponse)
    mockEsService.getConfig() >> TestUtils.esConfig
    mockEsService.currentRunningTasks() >> 0
    mockProcessorContext = new MockProcessorContext()
    mockProcessorContext.setTimestamp(startTime.toEpochMilli())
    testStore = Stores.keyValueStoreBuilder(
        Stores.inMemoryKeyValueStore(storeName), Serdes.String(), Serdes.Long()
    ).withLoggingDisabled().build()
    testStore.init(mockProcessorContext, testStore)
    testConfig = FlatteningConfig.newBuilder()
        .withStoreName(storeName)
        .withScriptPath(testScript)
        .withInterval(testInterval)
        .build()
    testTransformer = new FlatteningTransformer(mockEsService, testConfig)
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
    0 * mockEsService.reindexAsync(_)

    when:
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "collection"}')
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
    1 * mockEsService.reindexAsync({ it.script.params.stagedDate == timestamp1 }, _)// <-- verify timestamp param

  }

  def "does not flatten if collection doesn't exist"() {
    def testId = 'a'
    def timestamp = 1000L

    when:
    testTransformer.transform(testId, timestamp)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, null)
    0 * mockEsService.reindexAsync(_, _)
  }

  def "flattening requests use the parent collection for defaults"() {
    def testId = 'a'
    def timestamp = 1000L
    def actionListener

    when:
    testTransformer.transform(testId, timestamp)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "collection"}')
    1 * mockEsService.reindexAsync(
        { it.script.params.defaults.title == 'collection' }, // <-- verify defaults param
        { actionListener = it } // <-- capture listener
    ) >> Mock(Cancellable)
  }

  def "successful requests produce successful results"() {
    def testId = 'a'
    def timestamp = 1000L
    def actionListener

    when:
    testTransformer.transform(testId, timestamp)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "collection"}')
    1 * mockEsService.reindexAsync(_, { actionListener = it }) >> Mock(Cancellable) // <- capture listener

    when:
    actionListener.onResponse(buildSuccessBulkByScrollResponse())

    then:
    mockProcessorContext.forwarded().size() == 1
    def forwardedKeyValue = mockProcessorContext.forwarded()[0].keyValue()
    forwardedKeyValue.key == testId
    forwardedKeyValue.value instanceof FlatteningTransformer.FlatteningTriggerResult
    forwardedKeyValue.value.successful == true
  }

  def "failed requests produce unsuccessful results"() {
    def testId = 'a'
    def timestamp = 1000L
    def actionListener

    when:
    testTransformer.transform(testId, timestamp)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "collection"}')
    1 * mockEsService.reindexAsync(_, { actionListener = it }) // <- capture listener

    when:
    actionListener.onResponse(buildFailBulkByScrollResponse([BulkItemResponse.Failure.PARSER]))

    then:
    mockProcessorContext.forwarded().size() == 1
    def forwardedKeyValue = mockProcessorContext.forwarded()[0].keyValue()
    forwardedKeyValue.key == testId
    forwardedKeyValue.value instanceof FlatteningTransformer.FlatteningTriggerResult
    forwardedKeyValue.value.successful == false
  }

  def "IOExceptions produce unsuccessful results"() {
    def testId = 'a'
    def timestamp = 1000L
    def actionListener

    when:
    testTransformer.transform(testId, timestamp)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "collection"}')
    1 * mockEsService.reindexAsync(_, { actionListener = it }) >> Mock(Cancellable) // <-- capture listener

    when:
    actionListener.onFailure(new IOException())

    then:
    mockProcessorContext.forwarded().size() == 0
  }

  def "second collection update cancels in-flight flattening task"() {
    def testId = 'a'
    def timestamp = 1000L
    def firstRequest = Mock(Cancellable)

    when:
    testTransformer.transform(testId, timestamp)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "collection"}')
    1 * mockEsService.reindexAsync(_, _) >> firstRequest

    when:
    testTransformer.transform(testId, timestamp + 1000)
    advanceWallClockTime(mockProcessorContext, (startTimePlusInterval + testInterval).toEpochMilli())

    then:
    1 * firstRequest.cancel() // <-- cancels first request before submitting second
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "updated collection"}')
    1 * mockEsService.reindexAsync(_, _) >> Mock(Cancellable)
  }

  def "waits for available tasks before submitting"() {
    def testId = 'a'
    def timestamp = 1000L

    when:
    testTransformer.transform(testId, timestamp)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.currentRunningTasks() >> 1000 // <-- too many!
    0 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "collection"}')
    0 * mockEsService.reindexAsync(_, _) >> Mock(Cancellable)

    when:
    advanceWallClockTime(mockProcessorContext, (startTimePlusInterval + testInterval).toEpochMilli())

    then:
    1 * mockEsService.currentRunningTasks() >> 0 // <-- better now
    1 * mockEsService.get(_, testId) >> buildGetResponse('test', testId, '{"title": "updated collection"}')
    1 * mockEsService.reindexAsync(_, _) >> Mock(Cancellable)
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

  def buildFailBulkByScrollResponse(def failure) {
    TimeValue took = new TimeValue(1000)
    XContentBuilder builder = JsonXContent.contentBuilder()
    BulkByScrollTask.Status status = new BulkByScrollTask.Status(Arrays.asList(null, null), null)
    status.toXContent(builder, ToXContent.EMPTY_PARAMS)
    BulkByScrollResponse response = new BulkByScrollResponse(took, status, [failure], [], false)
    return response
  }

  def buildSuccessBulkByScrollResponse() {
    TimeValue took = new TimeValue(1000)
    XContentBuilder builder = JsonXContent.contentBuilder()
    BulkByScrollTask.Status status = new BulkByScrollTask.Status(Arrays.asList(null, null), null)
    status.toXContent(builder, ToXContent.EMPTY_PARAMS)
    BulkByScrollResponse response = new BulkByScrollResponse(took, status, [], [], false)
    return response
  }

}
