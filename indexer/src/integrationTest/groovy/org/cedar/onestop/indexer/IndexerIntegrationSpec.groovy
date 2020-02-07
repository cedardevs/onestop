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
import org.cedar.onestop.kafka.common.constants.StreamsApps
import org.cedar.onestop.kafka.common.constants.Topics
import org.cedar.onestop.kafka.common.util.KafkaHelpers
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.avro.psi.Relationship
import org.cedar.schemas.avro.psi.RelationshipType
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.client.RequestOptions
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.KafkaTestUtils
import spock.lang.Specification

import java.util.concurrent.TimeUnit

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG

class IndexerIntegrationSpec extends Specification {

  // test iso records from elastic-common
  static inputCollectionXml = ClassLoader.systemClassLoader.getResourceAsStream('test/data/xml/COOPS/C1.xml').text
  static inputGranuleXml = ClassLoader.systemClassLoader.getResourceAsStream('test/data/xml/COOPS/G1.xml').text
  static indexingIntervalMs = 1000
  static indexPrefix = 'indexer_integration_spec_'

  static EmbeddedKafkaBroker kafka
  static RestApp schemaRegistry

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
    def collection = TestUtils.buildRecordFromXML(inputCollectionXml)
    def collectionTopic = Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.collection)
    def collectionRecord = new ProducerRecord<>(collectionTopic, collectionId, collection)
    def granuleId = "G"
    def granule = ParsedRecord.newBuilder(TestUtils.buildRecordFromXML(inputGranuleXml))
        .setRelationships([Relationship.newBuilder().setId(collectionId).setType(RelationshipType.COLLECTION).build()])
        .build()
    def granuleTopic = Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.granule)
    def granuleRecord = new ProducerRecord<>(granuleTopic, granuleId, granule)
    def collectionIndex = app.elasticConfig.COLLECTION_SEARCH_INDEX_ALIAS
    def granuleIndex = app.elasticConfig.GRANULE_SEARCH_INDEX_ALIAS
    def flattenedIndex = app.elasticConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS
    def sitemapIndex = app.elasticConfig.SITEMAP_INDEX_ALIAS

    when: // produce records before starting app so the input topics get created
    producer.send(collectionRecord).get()
    producer.send(granuleRecord).get()
    app.start()
    sleep(indexingIntervalMs + 5000)
    refresh(collectionIndex, granuleIndex, flattenedIndex, sitemapIndex)

    then:
    def collectionResult = getDocument(collectionIndex, collectionId)
    def granuleResult = getDocument(granuleIndex, granuleId)
    def flattenedResult = getDocument(flattenedIndex, granuleId)
    def sitemapResult = getDocument(sitemapIndex, '0')
    printSource(collectionResult)
    printSource(granuleResult)
    printSource(flattenedResult)

    collectionResult.exists
    collectionResult.id == collectionId
    granuleResult.exists
    granuleResult.id == granuleId
    flattenedResult.exists
    flattenedResult.id == granuleId
    sitemapResult.exists
    sitemapResult.source["content"] instanceof List
    sitemapResult.source["content"].contains(collectionId)

    when: // send tombstones for inputs
    producer.send(new ProducerRecord<>(granuleTopic, granuleId, null))
    producer.send(new ProducerRecord<>(collectionTopic, collectionId, null))
    sleep(indexingIntervalMs + 5000)
    refresh(collectionIndex, granuleIndex, flattenedIndex, sitemapIndex)

    then:
    !getDocument(collectionIndex, collectionId).exists
    !getDocument(granuleIndex, granuleId).exists
    !getDocument(flattenedIndex, granuleId).exists
    !getDocument(sitemapIndex, '0').exists
  }

  private void refresh(String... indices) {
    app.elasticClient.indices().refresh(new RefreshRequest(indices), RequestOptions.DEFAULT)
  }

  private GetResponse getDocument(String index, String id) {
    return app.elasticClient.get(new GetRequest(index, id), RequestOptions.DEFAULT)
  }

  private static void printSource(GetResponse result) {
    println "get result from index $result.index with id $result.id:"
    println JsonOutput.prettyPrint(JsonOutput.toJson(result.source))
  }

}
