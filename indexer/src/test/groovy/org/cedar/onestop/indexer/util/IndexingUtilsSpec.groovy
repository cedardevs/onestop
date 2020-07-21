package org.cedar.onestop.indexer.util

import org.apache.kafka.streams.state.ValueAndTimestamp
import org.cedar.onestop.kafka.common.constants.Topics
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.Publishing
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import spock.lang.Specification
import spock.lang.Unroll


@Unroll
class IndexingUtilsSpec extends Specification {

  ////////////////////////////
  // Transform ES Requests  //
  ////////////////////////////
  def "tombstones create delete requests for collections"() {
    def testKey = "ABC"

    when:
    def results = IndexingUtils.mapRecordToRequests(new IndexingInput(testKey, null, TestUtils.collectionTopic, TestUtils.esConfig))

    then:
    results.size() == 2
    results.each { r -> r instanceof DeleteRequest }
    results.each { r -> r.id() == testKey }
    results*.index().containsAll([
        TestUtils.esConfig.COLLECTION_SEARCH_INDEX_ALIAS,
        TestUtils.esConfig.COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS
    ])
  }

  def "tombstones create delete requests for granules"() {
    def testKey = "ABC"

    when:
    def results = IndexingUtils.mapRecordToRequests(new IndexingInput(testKey, null, TestUtils.granuleTopic, TestUtils.esConfig))

    then:
    results.size() == 2
    results.each { r -> r instanceof DeleteRequest }
    results.each { r -> r.id() == testKey }
    results*.index().containsAll([
        TestUtils.esConfig.GRANULE_SEARCH_INDEX_ALIAS,
        TestUtils.esConfig.GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS,
    ])
  }

  def "creates index requests for collections"() {
    def testKey = 'ABC'
    def testRecord = ParsedRecord.newBuilder(TestUtils.inputCollectionRecord)
        .setPublishing(Publishing.newBuilder().build()).build()
    def testValue = ValueAndTimestamp.make(testRecord, System.currentTimeMillis())

    when:
    def results = IndexingUtils.mapRecordToRequests(new IndexingInput(testKey, testValue, TestUtils.collectionTopic, TestUtils.esConfig))

    then:
    results.size() == 2
    results.every { it instanceof IndexRequest }
    results.every { it.id() == testKey }
    results*.index().containsAll([
        TestUtils.esConfig.COLLECTION_SEARCH_INDEX_ALIAS,
        TestUtils.esConfig.COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS
    ])
  }

  def "creates index requests for granules"() {
    def testKey = 'ABC'
    def testRecord = ParsedRecord.newBuilder(TestUtils.inputGranuleRecord)
        .setPublishing(Publishing.newBuilder().build()).build()
    def testValue = ValueAndTimestamp.make(testRecord, System.currentTimeMillis())

    when:
    def results = IndexingUtils.mapRecordToRequests(new IndexingInput(testKey, testValue, TestUtils.granuleTopic, TestUtils.esConfig))

    then:
    results.size() == 2
    results.every { it instanceof IndexRequest }
    results.every { it.id() == testKey }
    results*.index().containsAll([
        TestUtils.esConfig.GRANULE_SEARCH_INDEX_ALIAS,
        TestUtils.esConfig.GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS,
    ])
  }

  def "creates index requests for flattened granules"() {
    def testKey = 'ABC'
    def testRecord = ParsedRecord.newBuilder(TestUtils.inputGranuleRecord)
        .setPublishing(Publishing.newBuilder().build()).build()
    def testValue = ValueAndTimestamp.make(testRecord, System.currentTimeMillis())

    when:
    def results = IndexingUtils.mapRecordToRequests(new IndexingInput(testKey, testValue, Topics.flattenedGranuleTopic(), TestUtils.esConfig))

    then:
    results.size() == 1
    results.every { it instanceof IndexRequest }
    results.every { it.id() == testKey }
    results*.index().containsAll([
        TestUtils.esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS
        // no A&E index for flattened granules
    ])
  }

  def "record that is #testCase creates delete request"() {
    def testKey = "ABC"
    def testRecord = ParsedRecord.newBuilder(TestUtils.inputCollectionRecord)
        .setPublishing(publishingObject).build()
    def testValue = ValueAndTimestamp.make(testRecord, System.currentTimeMillis())

    when:
    def results = IndexingUtils.mapRecordToRequests(new IndexingInput(testKey, testValue, TestUtils.collectionTopic, TestUtils.esConfig))

    then:
    results.size() == 2
    results.every { it instanceof DeleteRequest }
    results.every { it.id() == testKey }
    results*.index().containsAll([
        TestUtils.esConfig.COLLECTION_SEARCH_INDEX_ALIAS,
        TestUtils.esConfig.COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS
    ])

    where:
    testCase               | publishingObject
    "private with no time" | Publishing.newBuilder().setIsPrivate(true).build()
    "private until future" | Publishing.newBuilder().setIsPrivate(true).setUntil(System.currentTimeMillis() + 60000).build()
    "public until past"    | Publishing.newBuilder().setIsPrivate(false).setUntil(System.currentTimeMillis() - 60000).build()
  }

  def "record that is #testCase creates index request"() {
    def testKey = "ABC"
    def testRecord = ParsedRecord.newBuilder(TestUtils.inputCollectionRecord)
        .setPublishing(publishingObject).build()
    def testValue = ValueAndTimestamp.make(testRecord, System.currentTimeMillis())

    when:
    def results = IndexingUtils.mapRecordToRequests(new IndexingInput(testKey, testValue, TestUtils.collectionTopic, TestUtils.esConfig))

    then:
    results.size() == 2
    results.every { it instanceof IndexRequest }
    results.every { it.id() == testKey }
    results*.index().containsAll([
        TestUtils.esConfig.COLLECTION_SEARCH_INDEX_ALIAS,
        TestUtils.esConfig.COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS
    ])

    where:
    testCase              | publishingObject
    "private until past"  | Publishing.newBuilder().setIsPrivate(true).setUntil(System.currentTimeMillis() - 60000).build()
    "public with no time" | Publishing.newBuilder().setIsPrivate(false).build()
    "public until future" | Publishing.newBuilder().setIsPrivate(false).setUntil(System.currentTimeMillis() + 60000).build()
  }
}
