package org.cedar.onestop.api.admin

import groovy.util.logging.Slf4j
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.cedar.onestop.api.admin.service.ETLService
import org.cedar.onestop.api.admin.service.ElasticsearchService
import org.cedar.onestop.api.admin.service.Indexer
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.schemas.avro.psi.ParsedRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
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
@ActiveProfiles(["integration", "migration-ingest"])
@SpringBootTest(
    classes = [
        Application,
        DefaultApplicationConfig,
        KafkaConsumerConfig,
        MigrationIntegrationConfig
    ],
    webEnvironment = RANDOM_PORT
)
@Slf4j
@TestPropertySource(properties = ['kafka.bootstrap.servers=${spring.embedded.kafka.brokers}'])
class MigrationIntegrationTest extends Specification {

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

  // 8082 is used for adjacent Spring integration tests (with separate contexts) which utilize schema registry
  // when the last test hasn't yet let go of port 8081, the tests can easily fail with a port in use error
  @Value('${schema-registry.url:localhost:8082}')
  String schemaUrl

  @Autowired
  private ElasticsearchService elasticsearchService

  @Autowired
  private ETLService etlService

  RestTemplate restTemplate
  String baseUrl

  ElasticsearchConfig esConfig

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

  def 'simulate migration re-key'() {
    setup: 'prepare 2 collection messages and a granule'
    elasticsearchService.ensureIndices()
    def producer = new KafkaProducer<>([
        (ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)                : bootstrapServers,
        (ProducerConfig.CLIENT_ID_CONFIG)                        : 'api_publisher',
        (AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG): schemaUrl,
        (ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)             : StringSerializer.class.getName(),
        (ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)           : SpecificAvroSerializer.class.getName(),
    ])

    String collectionXml = ClassLoader.systemClassLoader.getResourceAsStream('test/data/xml/COOPS/C1.xml').text
    ParsedRecord parsedCollection = Indexer.xmlToParsedRecord(collectionXml).parsedRecord
    String collectionKey1 = 'api_ingest_ABC'
    String collectionKey2 = 'kafka_ingest_XYZ'
    ProducerRecord collectionRecord = new ProducerRecord(collectionTopic, collectionKey1, parsedCollection)
    ProducerRecord collectionRecordUpdate = new ProducerRecord(collectionTopic, collectionKey2, parsedCollection)

    String granuleXml = ClassLoader.systemClassLoader.getResourceAsStream('test/data/xml/COOPS/G1.xml').text
    ParsedRecord parsedGranule = Indexer.xmlToParsedRecord(granuleXml).parsedRecord
    String granuleKey1 = 'api_ingest_123'
    String granuleKey2  = 'kafka_ingest_789'
    ProducerRecord granuleRecord = new ProducerRecord(granuleTopic, granuleKey1, parsedGranule)
    ProducerRecord granuleRecordUpdate = new ProducerRecord(granuleTopic, granuleKey2, parsedGranule)

    when: 'we send a collection and granule to simulate pre-existing docs in the index'
    producer.send(collectionRecord)
    producer.send(granuleRecord)
    sleep(1000)

    and: 'we request them by id from the api'
    def requestStagedCollection = RequestEntity.get("${baseUrl}/metadata/${collectionKey1}".toURI()).build()
    def requestStagedGranule = RequestEntity.get("${baseUrl}/metadata/${granuleKey1}".toURI()).build()
    def stagedCollectionResult = restTemplate.exchange(requestStagedCollection, Map)
    def stagedGranuleResult = restTemplate.exchange(requestStagedGranule, Map)

    then: 'the records exists'
    stagedCollectionResult.statusCode == HttpStatus.OK
    stagedGranuleResult.statusCode == HttpStatus.OK
    stagedGranuleResult.getBody().data[0].id == granuleKey1
    stagedGranuleResult.getBody().data[0].attributes.parentIdentifier == stagedCollectionResult.getBody().data[0].attributes.fileIdentifier

    and: 'then we trigger etl to populate the search index'
    etlService.updateSearchIndices()

    then: 'the search granules and flattened granules have been indexed in search'
    def firstIndexing = integrationTestUtil.documentsByType(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
    List<Map> indexedCollections = firstIndexing[esConfig.TYPE_COLLECTION] as List<Map>
    def collection = indexedCollections.first()
    List<Map> indexedGranules = firstIndexing[esConfig.TYPE_GRANULE] as List<Map>
    def granule = indexedGranules.first()
    List<Map> indexedFlatGranules = firstIndexing[esConfig.TYPE_FLATTENED_GRANULE] as List<Map>
    def flattenedGranule = indexedFlatGranules.first()

    //confirm each doc went to the right place
    getFileIdentifier(collection) == 'gov.noaa.nodc:NDBC-COOPS'
    getFileIdentifier(granule) == 'CO-OPS.NOS_8638614_201602_D1_v00'

    and: 'all the pre-conditions for a migration update exists'
    //Important precondition
    getInternalParentIdentifier(granule) == collection._id
    getInternalParentIdentifier(flattenedGranule) == collection._id

    then: 'WE MIGRATE TO PSI - trigger re-key scenario by sending the same collection with a different ID' //this should not happen when PSI starts resolving records duplicate IDs
    producer.send(collectionRecordUpdate)
    sleep(1000)

    and: 'we reguest the old ID and the new ID'
    def oldCollectionRequest = RequestEntity.get("${baseUrl}/metadata/${collectionKey1}".toURI()).build()
    def oldCollectionResult = restTemplate.exchange(oldCollectionRequest, Map)

    def rekeyedCollectionRequest = RequestEntity.get("${baseUrl}/metadata/${collectionKey2}".toURI()).build()
    def rekeyedCollectionResult = restTemplate.exchange(rekeyedCollectionRequest, Map)

    then: 'the first record is deleted and the second was created, i.e. the collection was re-keyed'
    rekeyedCollectionResult.statusCode == HttpStatus.OK
    oldCollectionResult.statusCode == HttpStatus.NOT_FOUND

    and: 'if we refresh the search index'
    etlService.updateSearchIndices()

    then: 'the search granules and flattened granules have an updated parent identifier'
    def reindexed = integrationTestUtil.documentsByType(esConfig.COLLECTION_SEARCH_INDEX_ALIAS, esConfig.GRANULE_SEARCH_INDEX_ALIAS, esConfig.FLAT_GRANULE_SEARCH_INDEX_ALIAS)
    def reindexedCollections = reindexed[esConfig.TYPE_COLLECTION] as List<Map>
    def reindexedCollection = reindexedCollections.first()
    def reindexedGranules = reindexed[esConfig.TYPE_GRANULE] as List<Map>
    def reindexedGranule = reindexedGranules.first()

    //confirm each doc went to the right place
    getFileIdentifier(reindexedCollection) == 'gov.noaa.nodc:NDBC-COOPS'
    getFileIdentifier(reindexedGranule) == 'CO-OPS.NOS_8638614_201602_D1_v00'
    reindexedCollection._id == collectionKey2

  //THIS IS THE BIG ONE!
    getInternalParentIdentifier(reindexedGranule) == collectionKey2

    and:
    def rekeyedFlattenedGranule = reindexed[esConfig.TYPE_FLATTENED_GRANULE].first()
    getInternalParentIdentifier(rekeyedFlattenedGranule) == collectionKey2

    then: 'we can  also update the granule'
    producer.send(granuleRecordUpdate)
    sleep(1000)
    def oldGranuleRequest = RequestEntity.get("${baseUrl}/metadata/${granuleKey1}".toURI()).build()
    def oldGranuleResult = restTemplate.exchange(oldGranuleRequest, Map)
    def rekeyedGranuleRequest = RequestEntity.get("${baseUrl}/metadata/${granuleKey2}".toURI()).build()
    def rekeyedGranuleResult = restTemplate.exchange(rekeyedGranuleRequest, Map)
    oldGranuleResult.statusCode == HttpStatus.NOT_FOUND
    rekeyedGranuleResult.statusCode == HttpStatus.OK
  }
}
