package org.cedar.onestop.indexer.stream

import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.indexer.util.IndexingHelpers
import org.cedar.onestop.kafka.common.util.TimestampedValue
import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.avro.psi.Analysis
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.Publishing
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.parse.ISOParser
import org.elasticsearch.Version
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
  def testEsConfig = new ElasticsearchConfig(
      "bulkRequestMapperSpec", 10, 10, 10,
      10, true, Version.V_6_7_2
  )
  def testMapper = new ElasticsearchRequestMapper(testEsConfig, testIndexName)

  def "tombstones create delete requests"() {
    def testKey = "ABC"

    when:
    def result = testMapper.apply(testKey, null)

    then:
    result instanceof DeleteRequest
    result.index() == testIndexName
    result.id() == testKey
  }

  def "record that is #testCase creates delete request"() {
    def testKey = "ABC"
    def testValue = ParsedRecord.newBuilder().setPublishing(publishingObject).build()

    when:
    def result = testMapper.apply(testKey, new TimestampedValue<>(System.currentTimeMillis(), testValue))

    then:
    result.id() == testKey
    result.index() == testIndexName
    result instanceof DeleteRequest

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
    def result = testMapper.apply(testKey, new TimestampedValue<>(System.currentTimeMillis(), testValue))

    then:
    result.id() == testKey
    result.index() == testIndexName
    result instanceof IndexRequest

    where:
    testCase                | publishingObject
    "private until past"    | Publishing.newBuilder().setIsPrivate(true).setUntil(System.currentTimeMillis() - 60000).build()
    "public with no time"   | Publishing.newBuilder().setIsPrivate(false).build()
    "public until future"   | Publishing.newBuilder().setIsPrivate(false).setUntil(System.currentTimeMillis() + 60000).build()
  }

}
