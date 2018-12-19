package org.cedar.onestop.api.metadata

import org.cedar.onestop.api.metadata.authorization.configs.SpringSecurityDisabled
import org.cedar.onestop.api.metadata.service.ElasticsearchService
import org.cedar.onestop.api.metadata.service.MetadataManagementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles(["integration", "kafka-ingest", "securitydisabled"])
@SpringBootTest(classes = [Application, IntegrationTestConfig, SpringSecurityDisabled, KafkaConsumerConfig], webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = ["features.secure.upload=true"])
class KafkaIngestIntegrationSpec extends Specification {

  @LocalServerPort
  String port

  @Value('${server.servlet.context-path}')
  String contextPath

  @Autowired
  ElasticsearchService elasticsearchService

  @Autowired
  MetadataManagementService metadataManagementService

  String collectionPath = "data/COOPS/C1.xml"

  RestTemplate restTemplate
  String baseUrl

  def setup() {
    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()
    baseUrl = "http://localhost:${port}${contextPath}"
    elasticsearchService.ensureIndices()
    elasticsearchService.ensurePipelines()
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
