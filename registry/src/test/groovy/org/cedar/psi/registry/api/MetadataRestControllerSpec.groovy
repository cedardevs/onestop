package org.cedar.psi.registry.api

import org.cedar.psi.common.constants.Topics
import org.cedar.psi.registry.service.MetadataStore
import org.cedar.schemas.avro.psi.*
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

class MetadataRestControllerSpec extends Specification {

  static final testId = 'abc'
  static final testType = RecordType.collection
  static final testSource = Topics.DEFAULT_SOURCE
  static final testInput = Input.newBuilder()
      .setType(RecordType.collection)
      .setContent('{"hello":"world"}')
      .setMethod(Method.POST)
      .setContentType('application/json')
      .setSource('test')
      .build()
  static final testParsed = ParsedRecord.newBuilder().setType(RecordType.collection).build()

  MetadataStore mockMetadataStore = Mock(MetadataStore)
  ApiRootGenerator mockApiRootGenerator = Mock(ApiRootGenerator)
  MetadataRestController controller = new MetadataRestController(mockMetadataStore, mockApiRootGenerator)
  HttpServletResponse mockResponse = new MockHttpServletResponse()


  def 'returns input with default source'() {
    def path = "/metadata/${testType}/${testId}"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveInput(testType.toString(), testId, request, mockResponse)

    then:
    1 * mockApiRootGenerator.getApiRoot(_) >> 'http://localhost:8080'
    1 * mockMetadataStore.retrieveInput(testType, testSource, testId) >> testInput

    and:
    result.links.self == "http://localhost:8080/metadata/$testType/$testSource/$testId"
    result.links.parsed == "http://localhost:8080/metadata/$testType/$testSource/$testId/parsed"
    result.data.id == testId
    result.data.type == testType.toString()
    result.data.attributes == testInput
    result.errors == null
  }

  def 'returns input with explicit source'() {
    def path = "/metadata/${testType}/${testSource}/${testId}"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveInput(testType.toString(), testSource, testId, request, mockResponse)

    then:
    1 * mockApiRootGenerator.getApiRoot(_) >> 'http://localhost:8080'
    1 * mockMetadataStore.retrieveInput(testType, testSource, testId) >> testInput

    and:
    result.links.self == "http://localhost:8080/metadata/$testType/$testSource/$testId"
    result.links.parsed == "http://localhost:8080/metadata/$testType/$testSource/$testId/parsed"
    result.data.id == testId
    result.data.type == testType.toString()
    result.data.attributes == testInput
    result.errors == null
  }

  def 'returns parsed with default source'() {
    def path = "/metadata/${testType}/${testId}/parsed"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveParsed(testType.toString(), testId, request, mockResponse)

    then:
    1 * mockApiRootGenerator.getApiRoot(_) >> 'http://localhost:8080'
    1 * mockMetadataStore.retrieveParsed(testType, testSource, testId) >> testParsed

    and:
    result.links.self == "http://localhost:8080/metadata/$testType/$testSource/$testId/parsed"
    result.links.input == "http://localhost:8080/metadata/$testType/$testSource/$testId"
    result.data.id == testId
    result.data.type == testType.toString()
    result.data.attributes == testParsed
    result.errors == null
  }

  def 'returns parsed with explicit source'() {
    def path = "/metadata/${testType}/${testSource}/${testId}"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveParsed(testType.toString(), testSource, testId, request, mockResponse)

    then:
    1 * mockApiRootGenerator.getApiRoot(_) >> 'http://localhost:8080'
    1 * mockMetadataStore.retrieveParsed(testType, testSource, testId) >> testParsed

    and:
    result.links.self == "http://localhost:8080/metadata/$testType/$testSource/$testId/parsed"
    result.links.input == "http://localhost:8080/metadata/$testType/$testSource/$testId"
    result.data.id == testId
    result.data.type == testType.toString()
    result.data.attributes == testParsed
    result.errors == null
  }

  def 'handles nonexistent input'() {
    def path = "/metadata/${testType}/${testId}"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveInput(testType.toString(), testId, request, mockResponse)

    then:
    1 * mockApiRootGenerator.getApiRoot(_) >> 'http://localhost:8080'
    1 * mockMetadataStore.retrieveInput(testType, testSource, testId) >> null

    and:
    result.links.self == "http://localhost:8080/metadata/$testType/$testSource/$testId"
    result.links.parsed == null // <-- Does NOT have parsed link
    result.data == null
    result.errors instanceof List
    result.errors.size() == 1
    result.errors[0].status == 404
    result.errors[0].title instanceof String
    result.errors[0].detail instanceof String
    mockResponse.status == 404
  }

  def 'handles nonexistent parsed'() {
    def path = "/metadata/${testType}/${testId}"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveParsed(testType.toString(), testId, request, mockResponse)

    then:
    1 * mockApiRootGenerator.getApiRoot(_) >> 'http://localhost:8080'
    1 * mockMetadataStore.retrieveParsed(testType, testSource, testId) >> null

    and:
    result.links.self == "http://localhost:8080/metadata/$testType/$testSource/$testId/parsed"
    result.links.input == "http://localhost:8080/metadata/$testType/$testSource/$testId" // <-- DOES have input link
    result.data == null
    result.errors instanceof List
    result.errors.size() == 1
    result.errors[0].status == 404
    result.errors[0].title instanceof String
    result.errors[0].detail instanceof String
    mockResponse.status == 404
  }

  def 'handles parsed with errors'() {
    def error1 = ErrorEvent.newBuilder().setStatus(400).setTitle('client fail').build()
    def error2 = ErrorEvent.newBuilder().setStatus(500).setTitle('server fail').build()
    def parsed = ParsedRecord.newBuilder().setErrors([error1, error2]).build()

    def path = "/metadata/${testType}/${testId}"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveParsed(testType.toString(), testId, request, mockResponse)

    then:
    1 * mockApiRootGenerator.getApiRoot(_) >> 'http://localhost:8080'
    1 * mockMetadataStore.retrieveParsed(testType, testSource, testId) >> parsed

    and:
    result.links.self == "http://localhost:8080/metadata/$testType/$testSource/$testId/parsed"
    result.links.input == "http://localhost:8080/metadata/$testType/$testSource/$testId"
    result.data == null
    result.errors instanceof List
    result.errors.size() == 2
  }

  def 'handles deleted input'() {
    def deletedInput = Input.newBuilder()
        .setType(RecordType.collection)
        .setContent('{"hola":"mundo"}')
        .setMethod(Method.DELETE)
        .setContentType('application/json')
        .setSource('test')
        .build()
    def path = "/metadata/${testType}/${testSource}/${testId}"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveInput(testType.toString(), testSource, testId, request, mockResponse)

    then:
    _ * mockApiRootGenerator.getApiRoot(_) >> 'http://localhost:8080'
    1 * mockMetadataStore.retrieveInput(testType, testSource, testId) >> deletedInput

    and:
    result.links.self == "http://localhost:8080/metadata/$testType/$testSource/$testId"
    result.links.parsed == null // <-- Does NOT have parsed link
    result.links.resurrection == "http://localhost:8080/metadata/$testType/$testSource/$testId/resurrection" // <-- Has special link to resurrect record
    result.data == null
    result.errors instanceof List
    result.errors.size() == 1
    result.errors[0].status == 404
    result.errors[0].title instanceof String
    result.errors[0].detail instanceof String
    mockResponse.status == 404
  }


  private MockHttpServletRequest buildMockRequest(String path) {
    def request = new MockHttpServletRequest()
    request.setScheme("http")
    request.setServerName("localhost")
    request.setServerPort(8080)
    request.setRequestURI(path)
    request
  }

}
