package org.cedar.onestop.indexer


import org.apache.kafka.common.serialization.Serdes

//import org.apache.kafka.streams.TestInputTopic
//import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.TopologyTestDriver
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchVersion
import org.cedar.onestop.indexer.stream.BulkIndexingTransformer
import org.cedar.onestop.indexer.stream.FlatteningTriggerTransformer
import org.cedar.onestop.indexer.util.ElasticsearchService
import org.cedar.onestop.kafka.common.conf.AppConfig
import org.cedar.onestop.kafka.common.constants.StreamsApps
import org.cedar.onestop.kafka.common.constants.Topics
import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.avro.psi.Discovery
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.Publishing
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.avro.psi.Relationship
import org.cedar.schemas.avro.psi.RelationshipType
import org.cedar.schemas.avro.util.MockSchemaRegistrySerde
import org.elasticsearch.action.DocWriteRequest
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration
import java.time.Instant

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import static org.apache.kafka.streams.StreamsConfig.*
import static org.cedar.onestop.indexer.util.StreamsTestingPolyfills.*
import static org.elasticsearch.action.DocWriteRequest.OpType.DELETE

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
  static testEsConfig = new ElasticsearchConfig(
      new ElasticsearchVersion("7.5.1"),
      "SearchIndexTopologySpec-",
      1,
      1,
      1,
      1,
      false
  )
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
  FlatteningTriggerTransformer mockFlatteningTransformer

  def setup() {
    def testAppConfig = new AppConfig()
    mockEsService = Mock(ElasticsearchService)
    mockEsService.getConfig() >> testEsConfig
    mockIndexingTransformer = Mock(BulkIndexingTransformer)
    mockEsService.buildBulkIndexingTransformer(_ as Duration, _ as Long) >> mockIndexingTransformer
    mockFlatteningTransformer = Mock(FlatteningTriggerTransformer)
    mockEsService.buildFlatteningTriggerTransformer(_ as String) >> mockFlatteningTransformer
    topology = SearchIndexTopology.buildSearchIndexTopology(mockEsService, testAppConfig)
    driver = new TopologyTestDriver(topology, new Properties(streamsConfig))

    //---------
    // Apply Polyfills (remove w/ kafka 2.4+)
    // --------
    applyPolyfills(driver)

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
    def testKey1 = 'a'
    def testValue1 = buildTestRecord(RecordType.collection)
    def testKey2 = 'b'
    def testValue2 = buildTestRecord(RecordType.granule)

    when:
    collectionInput.pipeInput(testKey1, testValue1)
    granuleInput.pipeInput(testKey2, testValue2)

    then:
    1 * mockIndexingTransformer.transform(testKey1, _)
    1 * mockIndexingTransformer.transform(testKey2, _)
  }

  def "granule tombstones send deletes to granule and flattened indices"() {
    def testKey = 'a'

    when:
    granuleInput.pipeInput(testKey, null)

    then:
    1 * mockIndexingTransformer.transform(testKey, { DocWriteRequest r ->
      r.opType() == DELETE && r.index() == testEsConfig.GRANULE_SEARCH_INDEX_ALIAS
    })
    1 * mockIndexingTransformer.transform(testKey, { DocWriteRequest r ->
      r.opType() == DELETE && r.index() == testEsConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS
    })
  }

  def "collection triggers flattening from beginning on time"() {
    def testKey1 = 'a'
    def testValue1 = buildTestRecord(RecordType.collection)

    when:
    collectionInput.pipeInput(testKey1, testValue1)
    def result = flatteningTriggersOut.readKeyValuesToMap()

    then:
    result.size() == 1
    result[testKey1] == 0L
  }

  def "granule triggers flattening for parent from granule update time"() {
    def testGranId = 'a'
    def testCollId = 'c'
    def baseValue = buildTestRecord(RecordType.granule)
    def parentRelationship = Relationship.newBuilder().setId(testCollId).setType(RelationshipType.COLLECTION).build()
    def testGranValue = ParsedRecord.newBuilder(baseValue).setRelationships([parentRelationship]).build()

    when:
    granuleInput.pipeInput(testGranId, testGranValue)
    def result = flatteningTriggersOut.readKeyValuesToMap()

    then:
    result.size() == 1
    result[testCollId] == publishingStartTime.toEpochMilli()
  }

  def "granule with multiple parents produces multiple flattening triggers"() {
    def testGranId = 'a'
    def testCollId1 = 'c1'
    def testCollId2 = 'c2'
    def baseValue = buildTestRecord(RecordType.granule)
    def parentRelationship1 = Relationship.newBuilder().setId(testCollId1).setType(RelationshipType.COLLECTION).build()
    def parentRelationship2 = Relationship.newBuilder().setId(testCollId2).setType(RelationshipType.COLLECTION).build()
    def testGranValue = ParsedRecord.newBuilder(baseValue).setRelationships([parentRelationship1, parentRelationship2]).build()

    when:
    granuleInput.pipeInput(testGranId, testGranValue)
    def result = flatteningTriggersOut.readKeyValuesToMap()

    then:
    result.size() == 2
    result[testCollId1] == publishingStartTime.toEpochMilli()
    result[testCollId2] == publishingStartTime.toEpochMilli()
  }

  def "flattening triggers are windowed and the earliest timestamp is used"() {
    def testId = 'a'
    def timestamp1 = 1000L
    def timestamp2 = 2000L
    def timestamp3 = 3000L

    when:
    flatteningTriggersIn.pipeInput(testId, timestamp2)
    flatteningTriggersIn.pipeInput(testId, timestamp1)
    flatteningTriggersIn.pipeInput(testId, timestamp3)

    then:
    1 * mockFlatteningTransformer.transform(_, timestamp1)
  }

  def "collection triggers sitemap building with its timestamp and a common key"() {
    def testKey1 = 'a'
    def testValue1 = buildTestRecord(RecordType.collection)

    when:
    collectionInput.pipeInput(testKey1, testValue1)
    driver.advanceWallClockTime(Duration.ofMinutes(1))
    def result = sitemapTriggersOut.readKeyValuesToMap()

    then:
    result.size() == 1
    result[testKey1] == null
    result['ALL'] == publishingStartTime.toEpochMilli()
  }

  def "granules do not trigger sitemap building"() {
    def testKey1 = 'a'
    def testValue1 = buildTestRecord(RecordType.granule)

    when:
    granuleInput.pipeInput(testKey1, testValue1)
    driver.advanceWallClockTime(Duration.ofMinutes(1))
    def result = sitemapTriggersOut.readKeyValuesToMap()

    then:
    result.size() == 0
  }

  private static buildTestRecord(RecordType type) {
    def builder = ParsedRecord.newBuilder()
    builder.setType(type)
    def discovery = Discovery.newBuilder().build()
    builder.setAnalysis(Analyzers.analyze(discovery))
    builder.setDiscovery(discovery)
    builder.setPublishing(Publishing.newBuilder().build())
    return builder.build()
  }

}
