package org.cedar.onestop.indexer.stream

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.processor.MockProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.Stores
import org.cedar.onestop.indexer.util.ElasticsearchService
import org.cedar.onestop.indexer.util.TestUtils
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

class SitemapProcessorSpec extends Specification {

  static storeName = "SitemapTriggerProcessorSpecStore"
  static startTime = Instant.parse("2020-01-01T00:00:00Z")
  static testInterval = Duration.ofSeconds(1)
  static startTimePlusInterval = startTime + testInterval

  ElasticsearchService mockEsService
  MockProcessorContext mockProcessorContext
  KeyValueStore<String, Long> testStore
  SitemapConfig testConfig
  SitemapProcessor testProcessor

  def setup() {
    mockEsService = Mock(ElasticsearchService)
    mockEsService.getConfig() >> TestUtils.esConfig
    mockProcessorContext = new MockProcessorContext()
    mockProcessorContext.setTimestamp(startTime.toEpochMilli())
    testStore = Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(storeName), Serdes.String(), Serdes.Long())
        .withLoggingDisabled().build()
    testStore.init(mockProcessorContext, testStore)
    testConfig = SitemapConfig.newBuilder().withStoreName(storeName).withInterval(testInterval).build()
    testProcessor = new SitemapProcessor(mockEsService, testConfig)
    testProcessor.init(mockProcessorContext)
  }

  def "creates a wall clock punctuator"() {
    def punctuators = mockProcessorContext.scheduledPunctuators()

    expect:
    punctuators.size() == 1
    punctuators[0].getIntervalMs() == testInterval.toMillis()
    punctuators[0].type == PunctuationType.WALL_CLOCK_TIME
  }

  def "sitemap triggers are fired based on wall clock time"() {
    def testId = 'a'
    def timestamp = 1000L

    when:
    testProcessor.process(testId, timestamp)

    then:
    0 * mockEsService.buildSitemap(_)

    when:
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.buildSitemap(timestamp)
  }

  def "sitemap triggers are windowed and the latest timestamp is used"() {
    def testId = 'a'
    def timestamp1 = 1000L
    def timestamp2 = 2000L
    def timestamp3 = 3000L

    when:
    testProcessor.process(testId, timestamp2)
    testProcessor.process(testId, timestamp1)
    testProcessor.process(testId, timestamp3)
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    1 * mockEsService.buildSitemap(timestamp3)
  }

  def "sitemap is not built if no triggers have been received"() {
    when:
    advanceWallClockTime(mockProcessorContext, startTimePlusInterval.toEpochMilli())

    then:
    0 * mockEsService.buildSitemap(_)
  }


  private static advanceWallClockTime(MockProcessorContext context, Long timestamp) {
    context.scheduledPunctuators().get(0).getPunctuator().punctuate(timestamp)
  }

}
