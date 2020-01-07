package org.cedar.onestop.indexer


import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.TopologyTestDriver
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
import org.cedar.schemas.avro.util.MockSchemaRegistrySerde
import org.elasticsearch.Version
import org.elasticsearch.action.DocWriteRequest
import org.elasticsearch.action.bulk.BulkItemResponse
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.RestHighLevelClient
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

  def streamsConfig = [
      (APPLICATION_ID_CONFIG)           : 'index_topology_spec',
      (BOOTSTRAP_SERVERS_CONFIG)        : 'http://localhost:9092',
      (SCHEMA_REGISTRY_URL_CONFIG)      : 'http://localhost:8081',
      (DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
      (DEFAULT_VALUE_SERDE_CLASS_CONFIG): MockSchemaRegistrySerde.class.name,
      (AUTO_OFFSET_RESET_CONFIG)        : 'earliest'
  ]

  Topology topology
  TopologyTestDriver driver
  TestInputTopic collectionInput
  TestInputTopic granuleInput

  ElasticsearchService mockEsService

  def setup() {
    def testAppConfig = new AppConfig()
    def testEsConfig = new ElasticsearchConfig("SearchIndexTopologySpec", 1, 1, 1, 1, false)
    mockEsService = Mock(ElasticsearchService)
    mockEsService.getConfig() >> testEsConfig
    topology = SearchIndexTopology.buildSearchIndexTopology(mockEsService, testAppConfig)
    driver = new TopologyTestDriver(topology, new Properties(streamsConfig))
    println topology.describe()

    def recordBaseTime = Instant.parse("2020-01-01T00:00:00Z")
    def advance1Min = Duration.ofMinutes(1)
    collectionInput = driver.createInputTopic(
        Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.collection),
        Serdes.String().serializer(), new MockSchemaRegistrySerde().serializer(),
        recordBaseTime, advance1Min
    )
    granuleInput = driver.createInputTopic(
        Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.granule),
        Serdes.String().serializer(), new MockSchemaRegistrySerde().serializer(),
        recordBaseTime, advance1Min
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
    1 * mockEsService.bulk(_) >> buildBulkResponse(testKey)
  }

  def "granules and collections get bulked together"() {
    def testKey1 = 'a'
    def testValue1 = buildTestRecord(RecordType.collection)
    def testKey2 = 'b'
    def testValue2 = buildTestRecord(RecordType.granule)

    when:
    collectionInput.pipeInput(testKey1, testValue1)
    collectionInput.pipeInput(testKey2, testValue2)
    driver.advanceWallClockTime(Duration.ofMinutes(1))

    then:
    1 * mockEsService.bulk(_) >> buildBulkResponse(testKey1, testKey2)
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

  private static buildBulkResponse(String... ids) {
    def shard = new ShardId("index", "uuid", 0)
    def itemResponses = ids.collect {
      def itemResponse = new IndexResponse(shard, '_doc', it, 0, 0, 0, true)
      return new BulkItemResponse(0, DocWriteRequest.OpType.INDEX, itemResponse)
    }
    return new BulkResponse(itemResponses as BulkItemResponse[], 10)
  }

}
