package org.cedar.onestop.api.metadata

import org.cedar.onestop.api.metadata.service.ElasticsearchService
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.web.client.RestTemplate

class LoadIntegrationTests extends IntegrationTest {

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
  @Qualifier("elasticsearchRestClient")
  RestClient restClient

  @Autowired
  ElasticsearchService elasticsearchService

  private collectionPath = "data/COOPS/C1.xml"
  private granulePath = "data/COOPS/G1.xml"

  RestTemplate restTemplate
  String metadataURI

  void setup() {
    restTemplate = new RestTemplate()

    restTemplate.errorHandler = new TestResponseErrorHandler()
    metadataURI = "http://localhost:${port}${contextPath}/metadata"
    elasticsearchService.dropSearchIndices()
    elasticsearchService.dropStagingIndices()
    elasticsearchService.ensureIndices()
    elasticsearchService.ensurePipelines()
  }


  def 'returns errors when a bad record is loaded'() {
    when:
    def request = RequestEntity.post(metadataURI.toURI()).contentType(MediaType.APPLICATION_XML).body('THIS IS NOT XML')
    def result = restTemplate.exchange(request, Map)

    then:
    result.statusCode == HttpStatus.BAD_REQUEST
    result.body.errors.every({ it.title instanceof String })
    result.body.errors.every({ it.detail instanceof String })
  }

  def 'load a single metadata record'() {
    when:
    def loadRequest = buildLoadRequest(collectionPath)
    def loadResult = restTemplate.exchange(loadRequest, Map)
    refreshIndices()

    then: "Load returns CREATED"
    loadResult.statusCode == HttpStatus.CREATED

    and: "Document can be retrieved"
    def elasticsearchId = loadResult.body.data.id
    def getRequest = RequestEntity.get("$metadataURI/$elasticsearchId".toURI()).build()
    def getResult = restTemplate.exchange(getRequest, Map)
    getResult.statusCode == HttpStatus.OK
    getResult.body.data[0].id == elasticsearchId

    when: "Same metadata is loaded again"
    def reloadResult = restTemplate.exchange(loadRequest, Map)

    then: "Load returns OK"
    reloadResult.statusCode == HttpStatus.OK
  }


  /**
   * Load three metadata records:
   * 1) fileId: A, doi: null --> creates ES Doc 1
   * 2) fileId: B, doi: null --> creates ES Doc 2
   * 3) fileId: A, doi: X    --> updates doi on ES Doc 1
   * Now, if we load a fourth document with fileId B and doi X, it is impossible to
   * determine if we're supposed to update ES Doc 1 or 2, so an error should be thrown
   * 4) fileId: B, doi: X    --> ERROR
   */
  def 'does not allow a record with ambiguous ids to be loaded'() {
    when:
    def step1Result = restTemplate.exchange(buildLoadRequest('data/BadFiles/conflictStep1.xml'), Map)
    def step2Result = restTemplate.exchange(buildLoadRequest('data/BadFiles/conflictStep2.xml'), Map)
    refreshIndices() // need to refresh so that step 1 is searchable and step 3 can update it
    def step3Result = restTemplate.exchange(buildLoadRequest('data/BadFiles/conflictStep3.xml'), Map)
    def doc1Id = step1Result.body.data.id
    def doc2Id = step2Result.body.data.id

    then: 'ensure everything is set up right'
    doc1Id != doc2Id
    step3Result.body.data.id == doc1Id

    when:
    refreshIndices() // refresh again so step 3 is searchable and the conflict is triggered
    def step4Result = restTemplate.exchange(buildLoadRequest('data/BadFiles/conflictStep4.xml'), Map)

    then:
    step4Result.statusCode == HttpStatus.CONFLICT
    step4Result.body.errors[0].title.toLowerCase().contains('ambiguous')
    step4Result.body.errors[0].detail.contains(doc1Id)
    step4Result.body.errors[0].detail.contains(doc2Id)
  }

  def 'record with no ids fails'() {
    String fileWoIds = 'data/BadFiles/noIdExample.xml'

    when: 'we load 2 valid docs to create the potential for a false conflict'
    def step1Result = restTemplate.exchange(buildLoadRequest('data/BadFiles/conflictStep1.xml'), Map)
    def step2Result = restTemplate.exchange(buildLoadRequest('data/BadFiles/conflictStep2.xml'), Map)
    def doc1Id = step1Result.body.data.id
    def doc2Id = step2Result.body.data.id

    and: 'one with no ids and refresh'
    def step3Result = restTemplate.exchange(buildLoadRequest(fileWoIds), Map)
    refreshIndices()

    then: 'doc1 and 2 are unique records, doc3 fails no id'
    doc1Id != doc2Id

    step3Result.statusCode == HttpStatus.BAD_REQUEST
    step3Result.body.errors[0].title.contains('Unable to parse')
  }

  def 'does not allow a record with malformed temporal bounding to be loaded'() {
    when:
    def badDateResult = restTemplate.exchange(buildLoadRequest('data/BadFiles/test-iso-invalid-dates-metadata.xml'), Map)

    then:
    badDateResult.statusCode == HttpStatus.BAD_REQUEST
    badDateResult.body.errors[0].title.contains('malformed data')
    badDateResult.body.errors[0].detail.contains('DateTimeParseException')
  }

  def 'retrieve a metadata record by elasticsearch id'() {
    setup:
    def loadResult = restTemplate.exchange(buildLoadRequest(collectionPath), Map)
    def elasticsearchId = loadResult.body.data.id
    refreshIndices()

    expect:
    def getRequest = RequestEntity.get("$metadataURI/$elasticsearchId".toURI()).build()
    def getResult = restTemplate.exchange(getRequest, Map)
    getResult.statusCode == HttpStatus.OK
  }

  def 'retrieve a non-existent record'() {
    expect:
    def getRequest = RequestEntity.get("$metadataURI/notarealid".toURI()).build()
    def getResult = restTemplate.exchange(getRequest, Map)
    getResult.statusCode == HttpStatus.NOT_FOUND
    getResult.body.title instanceof String
    getResult.body.detail instanceof String
  }

  def 'find a metadata record by fileIdentifier'() {
    setup:
    def loadResult = restTemplate.exchange(buildLoadRequest(collectionPath), Map)
    def fileIdentifier = loadResult.body.data.attributes.fileIdentifier
    refreshIndices()

    expect:
    def getRequest = RequestEntity.get("$metadataURI?fileIdentifier=$fileIdentifier".toURI()).build()
    def getResult = restTemplate.exchange(getRequest, Map)
    getResult.statusCode == HttpStatus.OK
  }

  def 'find a metadata record by doi'() {
    setup:
    def loadResult = restTemplate.exchange(buildLoadRequest(collectionPath), Map)
    def doi = loadResult.body.data.attributes.doi
    refreshIndices()

    expect:
    def getRequest = RequestEntity.get("$metadataURI?doi=$doi".toURI()).build()
    def getResult = restTemplate.exchange(getRequest, Map)
    getResult.statusCode == HttpStatus.OK
  }

  def 'find a metadata record by fileIdentifier and doi'() {
    setup:
    def loadResult = restTemplate.exchange(buildLoadRequest(collectionPath), Map)
    def fileIdentifier = loadResult.body.data.attributes.fileIdentifier
    def doi = loadResult.body.data.attributes.doi
    refreshIndices()

    expect:
    def getRequest = RequestEntity.get("$metadataURI?fileIdentifier=$fileIdentifier&doi=$doi".toURI()).build()
    def getResult = restTemplate.exchange(getRequest, Map)
    getResult.statusCode == HttpStatus.OK
  }

  def 'find a non-existent record'() {
    expect:
    def getRequest = RequestEntity.get("$metadataURI?fileIdentifier=notarealfileid".toURI()).build()
    def getResult = restTemplate.exchange(getRequest, Map)
    getResult.statusCode == HttpStatus.NOT_FOUND
    getResult.body.title instanceof String
    getResult.body.detail instanceof String
  }

  private buildLoadRequest(String content) {
    RequestEntity.post(metadataURI.toURI())
            .contentType(MediaType.APPLICATION_XML)
            .header(HttpHeaders.USER_AGENT, "Spring's RestTemplate") // value can be whatever
            .body(new ClassPathResource(content))
  }

  private refreshIndices() {
    restClient.performRequest('POST', '_refresh')
  }

}
