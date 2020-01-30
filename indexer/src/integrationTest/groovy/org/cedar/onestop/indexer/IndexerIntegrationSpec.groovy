package org.cedar.onestop.indexer

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
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.client.RequestOptions
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.KafkaTestUtils
import spock.lang.Specification

import java.util.concurrent.TimeUnit

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG

class IndexerIntegrationSpec extends Specification {

  static inputCollectionXml = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-collection.xml').text
  static inputGranuleXml = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-granule.xml').text
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

  def "indexes a granule and a collection"() {
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

    when:
    producer.send(collectionRecord).get()
    producer.send(granuleRecord).get()
    app.start()
    sleep(indexingIntervalMs + 2000)

    then:
    getDocument(app.elasticConfig.COLLECTION_SEARCH_INDEX_ALIAS, collectionId).exists
    getDocument(app.elasticConfig.GRANULE_SEARCH_INDEX_ALIAS, granuleId).exists
  }

  private GetResponse getDocument(String index, String id) {
    return app.elasticClient.get(new GetRequest(index, id), RequestOptions.DEFAULT)
  }

}
