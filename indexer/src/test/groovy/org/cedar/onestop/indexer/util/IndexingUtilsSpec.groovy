package org.cedar.onestop.indexer.util

import org.apache.kafka.streams.state.ValueAndTimestamp
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchVersion
import org.cedar.onestop.indexer.stream.BulkIndexingConfig
import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.Publishing
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.parse.ISOParser
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration

import static org.elasticsearch.action.DocWriteRequest.OpType.DELETE
import static org.elasticsearch.action.DocWriteRequest.OpType.INDEX

@Unroll
class IndexingUtilsSpec extends Specification {


  static inputCollectionXml = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-collection.xml').text
  static inputCollectionDiscovery = ISOParser.parseXMLMetadataToDiscovery(inputCollectionXml)
  static inputCollectionAnalysis = Analyzers.analyze(inputCollectionDiscovery)
  static inputGranuleXml = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-granule.xml').text

  static testTopic = 'testtopic'
  static testIndex = 'testindex'
  static indexingConfig = BulkIndexingConfig.newBuilder()
      .withStoreName('teststore')
      .withMaxPublishInterval(Duration.ofSeconds(10))
      .withMaxPublishBytes(10000)
      .withMaxPublishActions(1000)
      .build()
  static esConfig = new ElasticsearchConfig(
      new ElasticsearchVersion("7.5.1"),
      "SearchIndexTopologySpec-",
      1,
      1,
      1,
      1,
      false
  )


  ////////////////////////////
  // Transform ES Requests  //
  ////////////////////////////
  def "tombstones create delete requests"() {
    def testKey = "ABC"

    when:
    def results = IndexingHelpers.mapRecordToRequests(new IndexingInput(testTopic, testKey, null, indexingConfig, esConfig))

    then:
    results.size() == 1
    results[0] instanceof DeleteRequest
    results[0].index() == testIndex
    results[0].id() == testKey
  }

  def "tombstones create multiple delete requests when configured"() {
    def testKey = 'ABC'
    def multipleDeleteConfig = BulkIndexingConfig.newBuilder()
        .withStoreName('teststore')
        .withMaxPublishInterval(Duration.ofSeconds(10))
        .withMaxPublishBytes(10000)
        .withMaxPublishActions(1000)
        .addIndexMapping(testTopic, DELETE, 'a')
        .addIndexMapping(testTopic, DELETE, 'b')
        .build()

    when:
    def results = IndexingHelpers.mapRecordToRequests(new IndexingInput(testTopic, testKey, null, multipleDeleteConfig, esConfig))

    then:
    results.size() == 2
    results.every { it instanceof DeleteRequest }
    results.every { it.id() == testKey }
    results*.index() == ['a', 'b']
  }

  def "creates multiple index requests when configured"() {
    def testKey = 'ABC'
    def testRecord = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setAnalysis(inputCollectionAnalysis)
        .setDiscovery(inputCollectionDiscovery)
        .setPublishing(Publishing.newBuilder().build()).build()
    def testValue = ValueAndTimestamp.make(testRecord, System.currentTimeMillis())
    def multipleIndexConfig = BulkIndexingConfig.newBuilder()
        .withStoreName('teststore')
        .withMaxPublishInterval(Duration.ofSeconds(10))
        .withMaxPublishBytes(10000)
        .withMaxPublishActions(1000)
        .addIndexMapping(testTopic, INDEX, 'a')
        .addIndexMapping(testTopic, INDEX, 'b')
        .build()

    when:
    def results = IndexingHelpers.mapRecordToRequests(new IndexingInput(testTopic, testKey, testValue, multipleIndexConfig, esConfig))

    then:
    results.size() == 2
    results.every { it instanceof IndexRequest }
    results.every { it.id() == testKey }
    results*.index() == ['a', 'b']
  }

  def "record that is #testCase creates delete request"() {
    def testKey = "ABC"
    def testRecord = ParsedRecord.newBuilder().setPublishing(publishingObject).build()
    def testValue = ValueAndTimestamp.make(testRecord, System.currentTimeMillis())

    when:
    def results = IndexingHelpers.mapRecordToRequests(new IndexingInput(testTopic, testKey, testValue, indexingConfig, esConfig))

    then:
    results.size() == 1
    results[0].id() == testKey
    results[0].index() == testIndex
    results[0] instanceof DeleteRequest

    where:
    testCase               | publishingObject
    "private with no time" | Publishing.newBuilder().setIsPrivate(true).build()
    "private until future" | Publishing.newBuilder().setIsPrivate(true).setUntil(System.currentTimeMillis() + 60000).build()
    "public until past"    | Publishing.newBuilder().setIsPrivate(false).setUntil(System.currentTimeMillis() - 60000).build()
  }

  def "record that is #testCase creates index request"() {
    def testKey = "ABC"
    def testRecord = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setAnalysis(inputCollectionAnalysis)
        .setDiscovery(inputCollectionDiscovery)
        .setPublishing(publishingObject).build()
    def testValue = ValueAndTimestamp.make(testRecord, System.currentTimeMillis())

    when:
    def result = IndexingHelpers.mapRecordToRequests(new IndexingInput(testTopic, testKey, testValue, indexingConfig, esConfig))

    then:
    result[0].id() == testKey
    result[0].index() == testIndex
    result[0] instanceof IndexRequest

    where:
    testCase              | publishingObject
    "private until past"  | Publishing.newBuilder().setIsPrivate(true).setUntil(System.currentTimeMillis() - 60000).build()
    "public with no time" | Publishing.newBuilder().setIsPrivate(false).build()
    "public until future" | Publishing.newBuilder().setIsPrivate(false).setUntil(System.currentTimeMillis() + 60000).build()
  }
}
