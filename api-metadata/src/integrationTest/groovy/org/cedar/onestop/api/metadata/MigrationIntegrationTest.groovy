package org.cedar.onestop.api.metadata

import groovy.util.logging.Slf4j
import groovy.json.JsonOutput
import io.confluent.kafka.schemaregistry.RestApp
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.cedar.onestop.api.metadata.service.ETLService
import org.cedar.onestop.api.metadata.service.ElasticsearchService
import org.cedar.onestop.api.metadata.service.InventoryManagerToOneStopUtil
import org.cedar.onestop.api.metadata.service.MetadataManagementService
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.util.AvroUtils
import org.elasticsearch.client.RestClient
import org.junit.Ignore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static org.cedar.onestop.elastic.common.DocumentUtil.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@DirtiesContext
@EmbeddedKafka
@ActiveProfiles(["integration", "kafka-ingest"])
@SpringBootTest(
    classes = [
        Application,
        DefaultApplicationConfig,
        KafkaConsumerConfig,
    ],
    webEnvironment = RANDOM_PORT
)
@Slf4j
@TestPropertySource(properties = ['kafka.bootstrap.servers=${spring.embedded.kafka.brokers}'])
class MigrationIntegrationTest extends Specification {

  @Autowired
  private ElasticsearchService elasticsearchService

  @Autowired
  private ETLService etlService


  RestTemplate restTemplate
  String baseUrl

  ElasticsearchConfig esConfig

  @LocalServerPort
  String port

  @Value('${server.servlet.context-path}')
  String contextPath

  @Value('${kafka.bootstrap.servers}')
  String bootstrapServers

  @Value('${kafka.topic.collections}')
  String collectionTopic

  @Value('${kafka.topic.granules}')
  String granuleTopic

  @Autowired
  IntegrationTestUtil integrationTestUtil

  def setup() {
    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()
    baseUrl = "http://localhost:${port}${contextPath}"
    esConfig = elasticsearchService.esConfig

    elasticsearchService.ensureIndices()
    elasticsearchService.ensurePipelines()
  }

  def 'simulate migration  re-key'() {
    setup: 'create kafka producer and messages'
    elasticsearchService.ensureIndices()
    def producer = new KafkaProducer<>([
        (ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)                : bootstrapServers,
        (ProducerConfig.CLIENT_ID_CONFIG)                        : 'api_publisher',
        (AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG): 'http://localhost:8081',
        (ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)             : StringSerializer.class.getName(),
        (ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)           : SpecificAvroSerializer.class.getName(),
    ])

    String collectionXml = ClassLoader.systemClassLoader.getResourceAsStream('test/data/xml/COOPS/C1.xml').text
    ParsedRecord parsedCollection = InventoryManagerToOneStopUtil.xmlToParsedRecord(collectionXml).parsedRecord
    String collectionKey1 = 'api_ingest_ABC'
    String collectionKey2 = 'kafka_ingest_XYZ'
    ProducerRecord collectionRecord = new ProducerRecord(collectionTopic, collectionKey1, parsedCollection)
    ProducerRecord collectionRecordUpdate = new ProducerRecord(collectionTopic, collectionKey2, parsedCollection)

    String granuleXml = ClassLoader.systemClassLoader.getResourceAsStream('test/data/xml/COOPS/G1.xml').text
    ParsedRecord parsedGranule = InventoryManagerToOneStopUtil.xmlToParsedRecord(granuleXml).parsedRecord
    String granuleKey1 = 'kafka_ingest_123'
    ProducerRecord granuleRecord = new ProducerRecord(granuleTopic, granuleKey1, parsedGranule)

    when: 'we send a collection and granule'
    producer.send(collectionRecord)
    producer.send(granuleRecord)
    sleep(2000)

    and: 'request them by id from the api'
    def requestStagedCollection = RequestEntity.get("${baseUrl}/metadata/${collectionKey1}".toURI()).build()
    def requestStagedGranule = RequestEntity.get("${baseUrl}/metadata/${granuleKey1}".toURI()).build()
    def stagedCollectionResult = restTemplate.exchange(requestStagedCollection, Map)
    def stagedGranuleResult = restTemplate.exchange(requestStagedGranule, Map)

    then: 'the records exists'
    stagedCollectionResult.statusCode == HttpStatus.OK
    stagedGranuleResult.statusCode == HttpStatus.OK

    and: 'if we refresh the search index'
    etlService.updateSearchIndices()

    then: 'the search granules and flattened granules have an updated parent identifier'
    def firstIndexing = integrationTestUtil.documentsByType(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
    List<Map> indexedCollections = firstIndexing[esConfig.TYPE_COLLECTION] as List<Map>
    def collection = indexedCollections.first()
    getFileIdentifier(collection) == 'gov.noaa.nodc:NDBC-COOPS'

    List<Map> indexedGranules = firstIndexing[esConfig.TYPE_GRANULE] as List<Map>
    def granule = indexedGranules.first()
    getFileIdentifier(granule) == 'CO-OPS.NOS_8638614_201602_D1_v00'

    getInternalParentIdentifier(granule) == collection._id

    then:
    List<Map> indexedFlatGranules = firstIndexing[esConfig.TYPE_FLATTENED_GRANULE] as List<Map>
    def flattenedGranule = indexedFlatGranules.first()
    getInternalParentIdentifier(flattenedGranule) == collection._id

    and: 'if we send the same record with a different id to simulate a re-key scenario' //this should not happen when PSI starts resolving records duplicate IDs
    producer.send(collectionRecordUpdate)
    sleep(5000)
    def oldCollectionRequest = RequestEntity.get("${baseUrl}/metadata/${collectionKey1}".toURI()).build()
    def oldCollectionResult = restTemplate.exchange(oldCollectionRequest, Map)

    def rekeyedCollectionRequest = RequestEntity.get("${baseUrl}/metadata/${collectionKey2}".toURI()).build()
    def rekeyedCollectionResult = restTemplate.exchange(rekeyedCollectionRequest, Map)

    then: 'the first record is deleted and the second was created, i.e. re-keyed'
    rekeyedCollectionResult.statusCode == HttpStatus.OK
    oldCollectionResult.statusCode == HttpStatus.NOT_FOUND

    and: 'the granule is updated'
    def rekeyedGranuleRequest = RequestEntity.get("${baseUrl}/metadata/${granuleKey1}".toURI()).build()
    def rekeyedGranuleResult = restTemplate.exchange(rekeyedGranuleRequest, Map)

    then: 'the first record is deleted and the second was created, i.e. re-keyed'
    rekeyedGranuleResult.statusCode == HttpStatus.OK
    rekeyedGranuleResult.getBody().data[0].id == granuleKey1
    rekeyedGranuleResult.getBody().data[0].attributes.parentIdentifier == rekeyedCollectionResult.getBody().data[0].attributes.fileIdentifier

    and: 'if we refresh the search index'
    etlService.updateSearchIndices()

    then: 'the search granules and flattened granules have an updated parent identifier'
    def reindexed = integrationTestUtil.documentsByType(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
    def reindexedCollections = reindexed[esConfig.TYPE_COLLECTION] as List<Map>
    def reindexedCollection = indexedCollections.first()
    getFileIdentifier(reindexedCollection) == 'gov.noaa.nodc:NDBC-COOPS'
    def reindexedGranules = reindexed[esConfig.TYPE_GRANULE] as List<Map>
    def reindexedGranule = reindexedGranules.first()
    getFileIdentifier(reindexedGranule) == 'CO-OPS.NOS_8638614_201602_D1_v00'

    getInternalParentIdentifier(reindexedGranule) == collection._id
    and:
    def rekeyedFlattenedGranule = reindexed[esConfig.TYPE_FLATTENED_GRANULE].first()
    getInternalParentIdentifier(rekeyedFlattenedGranule) == collection._id
  }
}
