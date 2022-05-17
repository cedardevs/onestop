package org.cedar.onestop.indexer

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic

import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.state.ValueAndTimestamp
import org.cedar.onestop.data.util.FileUtils

import org.cedar.onestop.indexer.stream.BulkIndexingTransformer
import org.cedar.onestop.indexer.stream.FlatteningConfig
import org.cedar.onestop.indexer.stream.FlatteningTransformer
import org.cedar.onestop.indexer.stream.BulkIndexingConfig
import org.cedar.onestop.indexer.util.ElasticsearchService
import org.cedar.onestop.indexer.util.IndexingOutput
import org.cedar.onestop.indexer.util.TestUtils
import org.cedar.onestop.kafka.common.conf.AppConfig
import org.cedar.onestop.kafka.common.constants.StreamsApps
import org.cedar.onestop.kafka.common.constants.Topics
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.avro.psi.Relationship
import org.cedar.schemas.avro.psi.RelationshipType
import org.cedar.schemas.avro.util.MockSchemaRegistrySerde
import org.elasticsearch.action.DocWriteRequest.OpType
import org.elasticsearch.action.bulk.BulkItemResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.index.shard.ShardId
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration
import java.time.Instant

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import static org.apache.kafka.streams.StreamsConfig.*
import static org.elasticsearch.action.DocWriteRequest.OpType.INDEX

@Unroll
class SearchIndexTopologySpec extends Specification {

  static streamsConfig = [
      (APPLICATION_ID_CONFIG)           : 'index_topology_spec',
      (BOOTSTRAP_SERVERS_CONFIG)        : 'http://localhost:9092',
      (SCHEMA_REGISTRY_URL_CONFIG)      : 'http://localhost:8081',
      (DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
      (DEFAULT_VALUE_SERDE_CLASS_CONFIG): MockSchemaRegistrySerde.class.name,
      (AUTO_OFFSET_RESET_CONFIG)        : 'earliest'
  ]
  static collectionIndex = TestUtils.esConfig.COLLECTION_SEARCH_INDEX_ALIAS
  static granuleIndex = TestUtils.esConfig.GRANULE_SEARCH_INDEX_ALIAS
  static invalidCollectionPath = 'test-iso-collection.xml'
  static validCollectionPath = 'test/data/xml/COOPS/C1.xml'
  static validGranulePath = 'test/data/xml/COOPS/G1.xml'
  static publishingStartTime = Instant.parse("2020-01-01T00:00:00Z")

  Topology topology
  TopologyTestDriver driver
  TestInputTopic collectionInput
  TestInputTopic granuleInput
  TestOutputTopic flatteningTriggersOut
  TestInputTopic flatteningTriggersIn
  TestOutputTopic sitemapTriggersOut
  TestInputTopic sitemapTriggersIn

  ElasticsearchService mockEsService
  BulkIndexingTransformer mockIndexingTransformer
  FlatteningTransformer mockFlatteningTransformer

  def setup() {
    def testAppConfig = new AppConfig()
    mockEsService = Mock(ElasticsearchService)
    mockEsService.getConfig() >> TestUtils.esConfig
    mockIndexingTransformer = Mock(BulkIndexingTransformer)
    mockEsService.buildBulkIndexingTransformer(_ as BulkIndexingConfig) >> mockIndexingTransformer
    mockFlatteningTransformer = Mock(FlatteningTransformer)
    mockEsService.buildFlatteningTransformer(_ as FlatteningConfig) >> mockFlatteningTransformer
    topology = SearchIndexTopology.buildSearchIndexTopology(mockEsService, testAppConfig)
    driver = new TopologyTestDriver(topology, new Properties(streamsConfig))

    def advance1Min = Duration.ofMinutes(1)
    collectionInput = driver.createInputTopic(
        Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.collection),
        Serdes.String().serializer(), new MockSchemaRegistrySerde().serializer(),
        publishingStartTime, advance1Min
    )
    granuleInput = driver.createInputTopic(
        Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.granule),
        Serdes.String().serializer(), new MockSchemaRegistrySerde().serializer(),
        publishingStartTime, advance1Min
    )
    flatteningTriggersOut = driver.createOutputTopic(
        testAppConfig.get("flattening.topic.name").toString(),
        Serdes.String().deserializer(), Serdes.Long().deserializer()
    )
    flatteningTriggersIn = driver.createInputTopic(
        testAppConfig.get("flattening.topic.name").toString(),
        Serdes.String().serializer(), Serdes.Long().serializer(),
        publishingStartTime, advance1Min
    )
    sitemapTriggersOut = driver.createOutputTopic(
        testAppConfig.get("sitemap.topic.name").toString(),
        Serdes.String().deserializer(), Serdes.Long().deserializer()
    )
    sitemapTriggersIn = driver.createInputTopic(
        testAppConfig.get("sitemap.topic.name").toString(),
        Serdes.String().serializer(), Serdes.Long().serializer(),
        publishingStartTime, advance1Min
    )
  }

  def cleanup(){
    driver.close()
  }


  def "granules and collections get bulked together"() {
    def granuleKey = 'a'
    def granuleValue = buildTestRecord(validGranulePath)
    def collectionKey = 'b'
    def collectionValue = buildTestRecord(validCollectionPath)

    when:
    granuleInput.pipeInput(granuleKey, granuleValue)
    collectionInput.pipeInput(collectionKey, collectionValue)

    then:
    1 * mockIndexingTransformer.transform(granuleKey, {
      it.value().discovery.fileIdentifier == granuleValue.discovery.fileIdentifier
    })
    1 * mockIndexingTransformer.transform(collectionKey, {
      it.value().discovery.fileIdentifier == collectionValue.discovery.fileIdentifier
    })
  }

  def "tombstones are forwarded to to the indexer"() {
    def testKey = 'a'

    when:
    granuleInput.pipeInput(testKey, null)

    then:
    1 * mockIndexingTransformer.transform(testKey, null)
  }

  def "collection triggers flattening from beginning of time"() {
    def testKey1 = 'a'
    def testValue1 = buildTestRecord(validCollectionPath)

    when:
    collectionInput.pipeInput(testKey1, testValue1)

    then:
    1 * mockIndexingTransformer.transform(testKey1, _) >> buildIndexerResult(testKey1, testValue1, collectionIndex, INDEX)

    and:
    def result = flatteningTriggersOut.readKeyValuesToMap()
    result.size() == 1
    result[testKey1] == 0L
  }

  def "flattening triggers are sent to the flattening transformer"() {
    def id = 'a'
    def timestamp = 1000L

    when:
    flatteningTriggersIn.pipeInput(id, timestamp)

    then:
    1 * mockFlatteningTransformer.transform(id, timestamp)
  }

  def "failed flattening results are produced back to the triggers topic"() {
    def id = 'a'
    def timestamp = 1000L

    when:
    flatteningTriggersIn.pipeInput(id, timestamp)

    then:
    1 * mockFlatteningTransformer.transform(id, timestamp) >>
        new KeyValue<>(id, new FlatteningTransformer.FlatteningTriggerResult(false, timestamp))
    1 * mockFlatteningTransformer.transform(id, timestamp) // <-- gets called a second time because of message cycle

    and:
    def result = flatteningTriggersOut.readKeyValuesToMap()
    result[id] == timestamp
  }

  def "collection triggers sitemap building with its timestamp and a common key"() {
    def testKey1 = 'a'
    def testValue1 = buildTestRecord(validCollectionPath)

    when:
    collectionInput.pipeInput(testKey1, testValue1)
    driver.advanceWallClockTime(Duration.ofMinutes(1))

    then:
    1 * mockIndexingTransformer.transform(testKey1, _) >> buildIndexerResult(testKey1, testValue1, collectionIndex, INDEX)

    and:
    def result = sitemapTriggersOut.readKeyValuesToMap()
    result.size() == 1
    result[testKey1] == null
    result['ALL'] == publishingStartTime.toEpochMilli()
  }

  def "granules do not trigger sitemap building"() {
    def testKey1 = 'a'
    def testValue1 = buildTestRecord(validGranulePath)

    when:
    granuleInput.pipeInput(testKey1, testValue1)
    driver.advanceWallClockTime(Duration.ofMinutes(1))
    def result = sitemapTriggersOut.readKeyValuesToMap()

    then:
    result.size() == 0
  }

  private static buildTestRecord(String resourcePath) {
    TestUtils.buildRecordFromXML(FileUtils.textFromClasspathFile(resourcePath))
  }

  private static KeyValue<String, IndexingOutput> buildIndexerResult(String key, ParsedRecord record, String index, OpType opType) {
    def shard = new ShardId(index, "uuid", 0)
    def itemResponse = new IndexResponse(shard, '_doc', key, 0, 0, 0, true)
    def bulkItemResponse = new BulkItemResponse(0, opType, itemResponse, null)
    def valueAndTimestamp = ValueAndTimestamp.make(record, publishingStartTime.toEpochMilli())
    def output = new IndexingOutput(valueAndTimestamp, bulkItemResponse)
    return new KeyValue(key, output)
  }

}
