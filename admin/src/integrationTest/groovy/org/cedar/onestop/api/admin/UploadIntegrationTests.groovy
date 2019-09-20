package org.cedar.onestop.api.admin

import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.LaxRedirectStrategy
import org.cedar.onestop.api.admin.service.ElasticsearchService
import org.cedar.onestop.elastic.common.ElasticsearchTestConfig
import org.cedar.onestop.elastic.common.RequestUtil
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.ModelAndView
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@DirtiesContext
@ActiveProfiles(["integration", "manual-upload"])
@SpringBootTest(
    classes = [
        Application,
        DefaultApplicationConfig,

        // provides:
        // - `RestClient` 'restClient' bean via test containers
        ElasticsearchTestConfig,
    ],
    webEnvironment = RANDOM_PORT,
    properties = ["elasticsearch.index.prefix=admin_upload_"]
)
@Unroll
class UploadIntegrationTests extends Specification {

  /**
   * These tests cover:
   *  - Load single record (confirm with GET)
   *  - Load multiple records (confirm with GET)
   *  - GET by ES id
   *  - GET by fileId; doi; fileId & doi
   *  - DELETE by ES id (recursive & not recursive)
   *  - DELETE by fileId; doi; fileId & doi (recursive & not recursive)
   *  - Verify error responses
   */

  @LocalServerPort
  private String port

  @Value('${server.servlet.context-path}')
  private String contextPath

  @Autowired
  RestClient restClient

  @Autowired
  ElasticsearchService elasticsearchService

  private collectionPath = "test/data/xml/COOPS/C1.xml"
  private granulePath = "test/data/xml/COOPS/G1.xml"

  RestTemplate restTemplate
  String metadataFormURI

  def allowRedirects() {
    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory()
    HttpClient httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build()
    factory.setHttpClient(httpClient)
    restTemplate.setRequestFactory(factory)
  }

  void setup() {

    restTemplate = new RestTemplate()
    restTemplate.errorHandler = new TestResponseErrorHandler()

    // TODO: if we ever figure out how to actually use HTTPS in a test,
    // remember to change this to `https` instead of `http`
    metadataFormURI = "http://localhost:${port}${contextPath}/metadata-form"
    elasticsearchService.dropSearchIndices()
    elasticsearchService.dropStagingIndices()
    elasticsearchService.ensureIndices()
    elasticsearchService.ensurePipelines()
  }

  def 'upload multi part form data results in redirect'() {
    when:
    def multiRequest = buildMultiLoadRequest(collectionPath, granulePath)
    def multiResult = restTemplate.exchange(multiRequest, ModelAndView)
    String redirectPath = multiResult.headers.getLocation().getPath()

    RequestUtil.refreshAllIndices(restClient)

    then: "Request returns 302 redirect to the expected response endpoint"

    multiResult.statusCode == HttpStatus.FOUND
    redirectPath == "/onestop-admin/uploadResponse.html"
  }

  private buildMultiLoadRequest(String[] paths) {
    def multipartMap = new LinkedMultiValueMap<String, Object>()
    paths.each { multipartMap.add("files", new ClassPathResource(it)) }
    RequestEntity.post(metadataFormURI.toURI()).contentType(MediaType.MULTIPART_FORM_DATA).body(multipartMap)
  }

}
