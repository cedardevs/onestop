package org.cedar.onestop.api.metadata

import io.confluent.kafka.schemaregistry.RestApp
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.cedar.onestop.api.metadata.service.ElasticsearchService
import org.cedar.onestop.api.metadata.service.MetadataManagementService
import org.cedar.onestop.elastic.common.ElasticsearchTestConfig
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.util.AvroUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@DirtiesContext
@EmbeddedKafka
@ActiveProfiles(["integration", "kafka-ingest"])
@SpringBootTest(
        classes = [
            Application,
            KafkaConsumerConfig,

            // provides:
            // - `RestClient` 'restClient' bean via test containers
            ElasticsearchTestConfig,
        ],
        webEnvironment = RANDOM_PORT
)
@TestPropertySource(properties = ['kafka.bootstrap.servers=${spring.embedded.kafka.brokers}'])
class KafkaIngestIntegrationSpec extends Specification {

  @Configuration
  @Profile('kafka-ingest')
  static class KafkaIntegrationConfig {
    @Value('${spring.embedded.zookeeper.connect:}')
    String zkConnect

    @Bean(initMethod = 'start')
    RestApp schemaRegistryRestApp() {
      new RestApp(8081, zkConnect, '_schemas')
    }
  }

  @LocalServerPort
  String port

  @Value('${server.servlet.context-path}')
  String contextPath

  @Value('${kafka.bootstrap.servers}')
  String bootstrapServers

  @Value('${kafka.topic.collections}')
  String collectionTopic

  @Autowired
  ElasticsearchService elasticsearchService

  @Autowired
  MetadataManagementService metadataManagementService

  String collectionPath = "test/data/COOPS/C1.xml"

  RestTemplate restTemplate
  String baseUrl

  def setup() {
    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()
    baseUrl = "http://localhost:${port}${contextPath}"
    elasticsearchService.ensureIndices()
    elasticsearchService.ensurePipelines()
  }


  def 'ingests a kafka message'() {
    def producer = new KafkaProducer<>([
        (ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)                : bootstrapServers,
        (ProducerConfig.CLIENT_ID_CONFIG)                        : 'api_publisher',
        (AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG): 'http://localhost:8081',
        (ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)             : StringSerializer.class.getName(),
        (ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)           : SpecificAvroSerializer.class.getName(),
    ])

    def inputStream = ClassLoader.systemClassLoader.getResourceAsStream('example-record-avro.json')
    def inputRecord = AvroUtils.<ParsedRecord> jsonToAvro(inputStream, ParsedRecord.classSchema)
    def inputKey = 'kafka_ingest_ABC'

    when:
    def record = new ProducerRecord(collectionTopic, inputKey, inputRecord)
    producer.send(record)

    sleep(1000)

    def request = RequestEntity.get("${baseUrl}/metadata/${inputKey}".toURI()).build()
    def result = restTemplate.exchange(request, Map)

    then:
    result.statusCode == HttpStatus.OK
  }

  def 'upload api is not available'() {
    when:
    def request = RequestEntity.post("${baseUrl}/metadata".toURI()).contentType(MediaType.APPLICATION_XML).body('<xml>test</xml>')
    def result = restTemplate.exchange(request, Map)

    then:
    // this is 405 instead of 404 because the same endpoint is used with GET requests
    // to retrieve documents, which is still accessible while using kafka ingest
    result.statusCode == HttpStatus.METHOD_NOT_ALLOWED
  }

  def 'upload form html is not available'() {
    when:
    def request = RequestEntity.get("${baseUrl}/upload.html".toURI()).build()
    def result = restTemplate.exchange(request, String)

    then:
    result.statusCode == HttpStatus.NOT_FOUND
  }

  def 'upload form post is not available'() {
    setup:
    def multipartMap = new LinkedMultiValueMap<String, Object>()
    multipartMap.add("files", new ClassPathResource(collectionPath))
    def request = RequestEntity.post("${baseUrl}/metadata-form".toURI()).contentType(MediaType.MULTIPART_FORM_DATA).body(multipartMap)

    when:
    def result = restTemplate.exchange(request, Map)

    then:
    result.statusCode == HttpStatus.NOT_FOUND
  }

  def 'upload response html is not available'() {
    when:
    def request = RequestEntity.get("${baseUrl}/uploadResponse.html".toURI()).build()
    def result = restTemplate.exchange(request, String)

    then:
    result.statusCode == HttpStatus.NOT_FOUND
  }

  def 'metadata retrieval api IS available'() {
    setup:
    // bypass api by loading a collection through the metadata service bean directly
    def xml = ClassLoader.systemClassLoader.getResourceAsStream(collectionPath).text
    def loadResult = metadataManagementService.loadMetadata(xml)
    def loadedId = loadResult.data.id

    when:
    def request = RequestEntity.get("${baseUrl}/metadata/${loadedId}".toURI()).build()
    def result = restTemplate.exchange(request, Map)

    then:
    result.statusCode == HttpStatus.OK
  }

}
