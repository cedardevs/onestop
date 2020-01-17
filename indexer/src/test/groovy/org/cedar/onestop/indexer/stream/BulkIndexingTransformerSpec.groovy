package org.cedar.onestop.indexer.stream

import groovy.json.JsonOutput
import org.apache.kafka.streams.processor.MockProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchVersion
import org.cedar.onestop.indexer.util.ElasticsearchService
import org.elasticsearch.action.DocWriteRequest
import org.elasticsearch.action.bulk.BulkItemResponse
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.index.shard.ShardId
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

class BulkIndexingTransformerSpec extends Specification {

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

  Duration testBulkInterval = Duration.ofSeconds(1)
  Long testMaxBytes = 1000
  ElasticsearchService mockEsService
  MockProcessorContext mockProcessorContext
  BulkIndexingTransformer testIndexingTransformer

  def setup() {
    mockEsService = Mock(ElasticsearchService)
    mockEsService.getConfig() >> testEsConfig
    mockProcessorContext = new MockProcessorContext()
    mockProcessorContext.setTimestamp(publishingStartTime.toEpochMilli())
    testIndexingTransformer = new BulkIndexingTransformer(mockEsService, testBulkInterval, testMaxBytes)
    testIndexingTransformer.init(mockProcessorContext)
  }


  def "wall clock time triggers bulk request"() {
    def testIndex = 'testdocs'
    def testKeyA = 'a'
    def testValueA = new IndexRequest(testIndex).id(testKeyA).source([name: testKeyA])
    def testKeyB = 'b'
    def testValueB = new IndexRequest(testIndex).id(testKeyB).source([name: testKeyB])
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
    1 * mockEsService.bulk(_) >> buildBulkResponse([testValueA, testValueB])

    and:
    def forwarded = mockProcessorContext.forwarded()
    forwarded.size() == 2
    forwarded.get(0).keyValue().key == testKeyA
    forwarded.get(0).keyValue().value instanceof BulkItemResponse
    forwarded.get(1).keyValue().key == testKeyB
    forwarded.get(1).keyValue().value instanceof BulkItemResponse
  }

  def "byte size triggers bulk request"() {
    def testIndex = 'testdocs'
    def testSource = ["name": "thisisatest"]
    def numRequests = (testMaxBytes / JsonOutput.toJson(testSource).bytes.length / 2).toInteger() // enough to trigger size-based flushing
    def requests = (1..numRequests).collect { n ->
      new IndexRequest(testIndex).id(n.toString()).source(testSource)
    }

    when:
    requests.each {
      testIndexingTransformer.transform(it.id(), it)
    }

    then:
    1 * mockEsService.bulk(_) >> buildBulkResponse(requests)

    and:
    mockProcessorContext.forwarded().size() == numRequests
  }

  private static buildBulkResponse(List<DocWriteRequest> requests) {
    def itemResponses = requests.collect {
      def shard = new ShardId(it.index(), "uuid", 0)
      def itemResponse = new IndexResponse(shard, '_doc', it.id(), 0, 0, 0, true)
      return new BulkItemResponse(0, it.opType(), itemResponse)
    }
    return new BulkResponse(itemResponses as BulkItemResponse[], 10)
  }
}
