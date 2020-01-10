package org.cedar.onestop.indexer

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer
//import org.apache.kafka.streams.TestInputTopic
//import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.cedar.onestop.elastic.common.ElasticsearchConfig
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
import org.elasticsearch.action.bulk.BulkItemResponse
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.index.shard.ShardId
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration
import java.time.Instant

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import static org.apache.kafka.streams.StreamsConfig.*

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
      "SearchIndexTopologySpec",
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
  TestOutputTopic flatteningTriggers
  TestOutputTopic sitemapTriggers

  ElasticsearchService mockEsService

  def setup() {
    def testAppConfig = new AppConfig()
    mockEsService = Mock(ElasticsearchService)
    mockEsService.getConfig() >> testEsConfig
    topology = SearchIndexTopology.buildSearchIndexTopology(mockEsService, testAppConfig)
    driver = new TopologyTestDriver(topology, new Properties(streamsConfig))

    //---------
    // Apply Polyfills (remove w/ kafka 2.4+)
    // --------
    driver.metaClass.createInputTopic = { String s, Serializer k, Serializer v, Instant i, Duration d ->
      new TestInputTopic(driver, s, k, v, i, d)
    }
    driver.metaClass.createOutputTopic = { String s, Deserializer k, Deserializer v ->
      new TestOutputTopic(driver, s, k, v)
    }
    driver.metaClass.advanceWallClockTime << { Duration d ->
      driver.advanceWallClockTime(d.toMillis())
    }
    //---------
    // End Polyfills
    // --------

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
    flatteningTriggers = driver.createOutputTopic(
        testAppConfig.get("flattening.topic.name").toString(),
        Serdes.String().deserializer(), Serdes.Long().deserializer()
    )
    sitemapTriggers = driver.createOutputTopic(
        testAppConfig.get("sitemap.topic.name").toString(),
        Serdes.String().deserializer(), Serdes.Long().deserializer()
    )
  }

  def cleanup(){
    driver.close()
  }


  def "wall clock time triggers bulk request"() {
    def testKey = 'a'
    def testValue = buildTestRecord(RecordType.collection)

    when:
    collectionInput.pipeInput(testKey, testValue)

    then:
    0 * mockEsService.bulk(_)

    when:
    driver.advanceWallClockTime(Duration.ofMinutes(1))

    then:
    1 * mockEsService.bulk(_) >> buildBulkResponse(RecordType.collection, testKey)
  }

  def "granules and collections get bulked together"() {
    def testKey1 = 'a'
    def testValue1 = buildTestRecord(RecordType.collection)
    def testKey2 = 'b'
    def testValue2 = buildTestRecord(RecordType.granule)

    when:
    collectionInput.pipeInput(testKey1, testValue1)
    granuleInput.pipeInput(testKey2, testValue2)
    driver.advanceWallClockTime(Duration.ofMinutes(1))

    then:
    1 * mockEsService.bulk(_) >> buildBulkResponse(null, testKey1, testKey2)
  }

  def "collection triggers flattening from beginning on time"() {
    def testKey1 = 'a'
    def testValue1 = buildTestRecord(RecordType.collection)

    when:
    collectionInput.pipeInput(testKey1, testValue1)
    driver.advanceWallClockTime(Duration.ofMinutes(1))
    def result = flatteningTriggers.readKeyValuesToMap()

    then:
    1 * mockEsService.bulk(_) >> buildBulkResponse(RecordType.collection, testKey1)

    and:
    result.size() == 1
    result[testKey1] == 0L
  }

  def "granule triggers flattening from granule update time"() {
    def testGranId = 'a'
    def testCollId = 'c'
    def baseValue = buildTestRecord(RecordType.granule)
    def parentRelationship = Relationship.newBuilder().setId(testCollId).setType(RelationshipType.COLLECTION).build()
    def testGranValue = ParsedRecord.newBuilder(baseValue).setRelationships([parentRelationship]).build()

    when:
    granuleInput.pipeInput(testGranId, testGranValue)
    driver.advanceWallClockTime(Duration.ofDays(1))
    def result = flatteningTriggers.readKeyValuesToMap()

    then:
    1 * mockEsService.bulk(_) >> buildBulkResponse(RecordType.granule, testGranId)

    and:
    result.size() == 1
    result[testCollId] == publishingStartTime.toEpochMilli()
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

  private static buildBulkResponse(RecordType type, String... ids) {
    def index = type == RecordType.collection ? testEsConfig.COLLECTION_SEARCH_INDEX_ALIAS :
        type == RecordType.granule ? testEsConfig.GRANULE_SEARCH_INDEX_ALIAS :
            "default"
    def shard = new ShardId(index, "uuid", 0)
    def itemResponses = ids.collect {
      def itemResponse = new IndexResponse(shard, '_doc', it, 0, 0, 0, true)
      return new BulkItemResponse(0, DocWriteRequest.OpType.INDEX, itemResponse)
    }
    return new BulkResponse(itemResponses as BulkItemResponse[], 10)
  }


  //---------
  // Polyfills (remove w/ kafka 2.4+)
  // --------
  class TestInputTopic<K, V> {
    TopologyTestDriver driver
    String topic
    Serializer keySerializer
    Serializer valueSerializer
    Instant publishTime
    Duration publishInterval

    ConsumerRecordFactory inputFactory

    TestInputTopic(TopologyTestDriver driver, String topic, Serializer<K> keySerializer, Serializer<V> valueSerializer, Instant startTime, Duration publishInterval) {
      this.driver = driver
      this.topic = topic
      this.keySerializer = keySerializer
      this.valueSerializer = valueSerializer
      this.publishTime = startTime
      this.publishInterval = publishInterval

      this.inputFactory = new ConsumerRecordFactory(keySerializer, valueSerializer)
    }

    void pipeInput(K key, V value) {
      driver.pipeInput(inputFactory.create(topic, key, value, publishTime.toEpochMilli()))
      publishTime == publishTime.plus(publishInterval)
    }
  }

  class TestOutputTopic<K, V> {
    TopologyTestDriver driver
    String topic
    Deserializer keyDeserializer
    Deserializer valueDeserializer

    TestOutputTopic(TopologyTestDriver driver, String topic, Deserializer<K> keyDeserializer, Deserializer<V> valueDeserializer) {
      this.driver = driver
      this.topic = topic
      this.keyDeserializer = keyDeserializer
      this.valueDeserializer = valueDeserializer
    }

    Map<K, V> readKeyValuesToMap() {
      def result = new LinkedHashMap<K, V>()
      while(true) {
        def record = driver.readOutput(topic, keyDeserializer, valueDeserializer)
        if (record) {
          result.put(record.key(), record.value())
        }
        else {
          return result
        }
      }
    }
  }

}
