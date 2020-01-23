package org.cedar.onestop.indexer.stream

import org.apache.kafka.streams.state.ValueAndTimestamp
import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.Publishing
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.parse.ISOParser
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ElasticsearchRequestMapperSpec extends Specification {

  static inputCollectionXml = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-collection.xml').text
  static inputDiscovery = ISOParser.parseXMLMetadataToDiscovery(inputCollectionXml)
  static inputAnalysis = Analyzers.analyze(inputDiscovery)

  def testIndexName = "TEST"
  def testMapper = new ElasticsearchRequestMapper([testIndexName], [testIndexName])

  def "tombstones create delete requests"() {
    def testKey = "ABC"

    when:
    def results = testMapper.apply(testKey, null)

    then:
    results.size() == 1
    results[0] instanceof DeleteRequest
    results[0].index() == testIndexName
    results[0].id() == testKey
  }

  def "tombstones create multiple delete requests when configured"() {
    def testKey = 'ABC'
    def multipleIndexMapper = new ElasticsearchRequestMapper(['a', 'b'], ['a', 'b'])

    when:
    def results = multipleIndexMapper.apply(testKey, null)

    then:
    results.size() == 2
    results.every { it instanceof DeleteRequest }
    results.every { it.id() == testKey }
    results*.index() == ['a', 'b']
  }

  def "creates multiple index requests when configured"() {
    def testKey = 'ABC'
    def testValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setAnalysis(inputAnalysis)
        .setDiscovery(inputDiscovery)
        .setPublishing(Publishing.newBuilder().build()).build()
    def multipleIndexMapper = new ElasticsearchRequestMapper(['a', 'b'], ['a', 'b'])

    when:
    def results = multipleIndexMapper.apply(testKey, ValueAndTimestamp.make(testValue, System.currentTimeMillis()))

    then:
    results.size() == 2
    results.every { it instanceof IndexRequest }
    results.every { it.id() == testKey }
    results*.index() == ['a', 'b']
  }

  def "record that is #testCase creates delete request"() {
    def testKey = "ABC"
    def testValue = ParsedRecord.newBuilder().setPublishing(publishingObject).build()

    when:
    def results = testMapper.apply(testKey, ValueAndTimestamp.make(testValue, System.currentTimeMillis()))

    then:
    results.size() == 1
    results[0].id() == testKey
    results[0].index() == testIndexName
    results[0] instanceof DeleteRequest

    where:
    testCase                | publishingObject
    "private with no time"  | Publishing.newBuilder().setIsPrivate(true).build()
    "private until future"  | Publishing.newBuilder().setIsPrivate(true).setUntil(System.currentTimeMillis() + 60000).build()
    "public until past"     | Publishing.newBuilder().setIsPrivate(false).setUntil(System.currentTimeMillis() - 60000).build()
  }

  def "record that is #testCase creates index request"() {
    def testKey = "ABC"
    def testValue = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setAnalysis(inputAnalysis)
        .setDiscovery(inputDiscovery)
        .setPublishing(publishingObject).build()

    when:
    def result = testMapper.apply(testKey, ValueAndTimestamp.make(testValue, System.currentTimeMillis()))

    then:
    result[0].id() == testKey
    result[0].index() == testIndexName
    result[0] instanceof IndexRequest

    where:
    testCase                | publishingObject
    "private until past"    | Publishing.newBuilder().setIsPrivate(true).setUntil(System.currentTimeMillis() - 60000).build()
    "public with no time"   | Publishing.newBuilder().setIsPrivate(false).build()
    "public until future"   | Publishing.newBuilder().setIsPrivate(false).setUntil(System.currentTimeMillis() + 60000).build()
  }

}
