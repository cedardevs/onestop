package org.cedar.onestop.indexer

import groovy.json.JsonOutput
import io.confluent.kafka.schemaregistry.RestApp
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.cedar.onestop.indexer.util.TestUtils
import org.cedar.onestop.kafka.common.conf.AppConfig
import org.cedar.onestop.kafka.common.constants.Topics
import org.cedar.onestop.kafka.common.util.KafkaHelpers
import org.cedar.schemas.avro.psi.Discovery
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.Relationship
import org.cedar.schemas.avro.psi.RelationshipType
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.client.RequestOptions
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.KafkaTestUtils
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.TimeUnit

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG

class IndexerIntegrationSpec extends Specification {

  static indexingIntervalMs = 1000
  static indexPrefix = 'indexer_integration_spec_'

  static EmbeddedKafkaBroker kafka
  static RestApp schemaRegistry

  static final inputCollectionXml = ClassLoader.systemClassLoader.getResourceAsStream('test/data/xml/COOPS/C1.xml').text
  static inputCollectionRecord = TestUtils.buildRecordFromXML(inputCollectionXml)

  static inputGranuleXml = ClassLoader.systemClassLoader.getResourceAsStream('test/data/xml/COOPS/G1.xml').text
  static inputGranuleRecord = TestUtils.buildRecordFromXML(inputGranuleXml)

  static inputFlattenedGranuleRecord = ParsedRecord.newBuilder(inputGranuleRecord)
      .setDiscovery(Discovery.newBuilder(inputGranuleRecord.discovery).setTitle("flattened").build())
      .build()

  def setupSpec() {
    kafka = new EmbeddedKafkaBroker(1, false)
        .brokerListProperty('kafka.bootstrap.servers')
    kafka.afterPropertiesSet()
    schemaRegistry = new RestApp(8081, kafka.zookeeperConnectionString, '_schemas')
    schemaRegistry.start()
  }

  def cleanupSpec() {
    kafka.destroy()
    schemaRegistry.stop()
  }

  AppConfig config
  AdminClient adminClient
  Map<String, Object> producerProps
  KafkaProducer<String, ParsedRecord> producer
  IndexerApp app

  def setup() {
    // system props will override defaults in AppConfig
    System.setProperty("kafka.$StreamsConfig.APPLICATION_ID_CONFIG", "IndexerIntegrationSpec-${System.currentTimeMillis()}")
    System.setProperty('elasticsearch.index.prefix', indexPrefix)
    System.setProperty('elasticsearch.bulk.interval.ms', indexingIntervalMs.toString())
    System.setProperty('flattening.interval.ms', indexingIntervalMs.toString())
    System.setProperty('sitemap.interval.ms', indexingIntervalMs.toString())

    config = new AppConfig()
    adminClient = AdminClient.create(KafkaHelpers.buildAdminConfig(config))
    producerProps = KafkaTestUtils.producerProps(kafka)
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Serdes.String().serializer().class.name)
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SpecificAvroSerializer.class.name)
    producerProps.put(SCHEMA_REGISTRY_URL_CONFIG, 'http://localhost:8081')
    producer = new KafkaProducer<>(producerProps)

    app = new IndexerApp(config)
    app.init()
  }

  def cleanup() {
    app.stop()
  }

  def "connects to ES"() {
    expect:
    app.elasticClient.info(RequestOptions.DEFAULT).version.number instanceof String
  }

  def "connects to kafka"() {
    expect:
    app.adminClient.describeCluster().clusterId().get(10, TimeUnit.SECONDS) instanceof String
  }

  def "indexes then deletes a granule and a collection"() {
    def collectionId = "C"
    def collection = inputCollectionRecord
    def collectionTopic = TestUtils.collectionTopic
    def collectionRecord = new ProducerRecord<>(collectionTopic, collectionId, collection)
    def granuleId = "G"
    def granule = ParsedRecord.newBuilder(inputGranuleRecord)
        .setRelationships([Relationship.newBuilder().setId(collectionId).setType(RelationshipType.COLLECTION).build()])
        .build()
    def granuleTopic = TestUtils.granuleTopic
    def granuleRecord = new ProducerRecord<>(granuleTopic, granuleId, granule)
    def flattenedGranuleTopic = Topics.flattenedGranuleTopic()
    def flattenedGranuleRecord = new ProducerRecord<>(flattenedGranuleTopic, granuleId, inputFlattenedGranuleRecord)
    def collectionIndex = app.elasticConfig.COLLECTION_SEARCH_INDEX_ALIAS
    def granuleIndex = app.elasticConfig.GRANULE_SEARCH_INDEX_ALIAS
    def flattenedIndex = app.elasticConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS
    def sitemapIndex = app.elasticConfig.SITEMAP_INDEX_ALIAS
    def pollingConditions = new PollingConditions(initialDelay: (indexingIntervalMs / 1000) * 3, factor: 2.0)

    when: // produce records before starting app so the input topics get created
    producer.send(collectionRecord).get()
    producer.send(granuleRecord).get()
    producer.send(flattenedGranuleRecord).get()
    app.start()

    then:
    pollingConditions.within(10, {
      def collectionResult = getDocument(collectionIndex, collectionId)
      def granuleResult = getDocument(granuleIndex, granuleId)
      def flattenedResult = getDocument(flattenedIndex, granuleId)
      def sitemapResult = getDocument(sitemapIndex, '0')
//      printSource(collectionResult)
//      printSource(granuleResult)
//      printSource(flattenedResult)
      assert collectionResult.exists
      assert collectionResult.id == collectionId
      assert granuleResult.exists
      assert granuleResult.id == granuleId
      assert flattenedResult.exists
      assert flattenedResult.id == granuleId
      assert sitemapResult.exists
      assert sitemapResult.source["content"] instanceof List
      assert sitemapResult.source["content"].contains(collectionId)
    })

    when: // send tombstones for inputs
    producer.send(new ProducerRecord<>(granuleTopic, granuleId, null))
    producer.send(new ProducerRecord<>(collectionTopic, collectionId, null))
    producer.send(new ProducerRecord<>(flattenedGranuleTopic, granuleId, null))

    then:
    pollingConditions.within(10, {
      assert !getDocument(collectionIndex, collectionId).exists
      assert !getDocument(granuleIndex, granuleId).exists
      assert !getDocument(flattenedIndex, granuleId).exists
      assert !getDocument(sitemapIndex, '0').exists
    })
  }

  private GetResponse getDocument(String index, String id) {
    return app.elasticClient.get(new GetRequest(index, id).refresh(true), RequestOptions.DEFAULT)
  }

  private static void printSource(GetResponse result) {
    println "get result from index $result.index with id $result.id:"
    println JsonOutput.prettyPrint(JsonOutput.toJson(result.source))
  }

}
