package org.cedar.onestop.registry.api

import org.cedar.onestop.kafka.common.constants.Topics
import org.cedar.onestop.registry.service.MetadataStore
import org.cedar.schemas.avro.psi.AggregatedInput
import org.cedar.schemas.avro.psi.ErrorEvent
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

@ExtendWith(SpringExtension.class)
class MetadataRestControllerSpec extends Specification {
  static final testId = '8834cfd3-3b71-40b8-b037-315607a30c42'
  static final testType = RecordType.collection
  static final testSource = Topics.DEFAULT_SOURCE
  static final testAggInput = AggregatedInput.newBuilder()
      .setType(RecordType.collection)
      .setRawJson('{"hello":"world"}')
      .setRawXml('<?xml version="1.0" encoding="UTF-8"?><fileIdentifier>foo:bar:baz</fileIdentifier>')
      .setInitialSource('test')
      .build()
  static final testParsed = ParsedRecord.newBuilder().setType(RecordType.collection).build()

  MetadataStore mockMetadataStore = Mock(MetadataStore)
  ApiRootGenerator mockApiRootGenerator = Mock(ApiRootGenerator)
  MetadataRestController controller = new MetadataRestController(mockMetadataStore, mockApiRootGenerator)
  HttpServletResponse mockResponse = new MockHttpServletResponse()

  def 'validate an incoming UUID string'() throws Exception {
    def path = "/metadata/${testType}/${"abc"}"
    def request = buildMockRequest(path)
    when:
    def result = controller.retrieveInput(testType.toString(), "abc", request, mockResponse)

    then:
    result.status == 500
    result.content == ["errors":["title":"Invalid UUID String (ensure lowercase): abc"]]
  }

  def 'validate an incoming UUID string with uppercase A-Z'() throws Exception {
    def path = "/metadata/${testType}/${"8834CFD3-3B71-40B8-B037-315607A30C42"}"
    def request = buildMockRequest(path)
    when:
    def result = controller.retrieveInput(testType.toString(), "8834CFD3-3B71-40B8-B037-315607A30C42", request, mockResponse)

    then:
    result.status == 500
    result.content == ["errors":["title":"Invalid UUID String (ensure lowercase): 8834CFD3-3B71-40B8-B037-315607A30C42"]]
  }

  def 'validate an incoming UUID string for raw XML'() throws Exception {
    def id = "abc"
    def path = "/metadata/${testType}/${id}/raw/xml"
    def request = buildMockRequest(path)

    when:
    controller.retrieveRawXml(testType.toString(), id, request, mockResponse)

    then:
    ResponseStatusException e = thrown()
    e.message == "500 INTERNAL_SERVER_ERROR \"Invalid UUID String (ensure lowercase): " + id + '"'
    e.status == HttpStatus.INTERNAL_SERVER_ERROR
  }

  def 'validate an incoming UUID string with uppercase A-Z for raw xml'() throws Exception {
    def id = "8834CFD3-3B71-40B8-B037-315607A30C42"
    def path = "/metadata/${testType}/${id}/raw/xml"
    def request = buildMockRequest(path)

    when:
    controller.retrieveRawXml(testType.toString(), id, request, mockResponse)

    then:
    Exception e = thrown()
    e.message == "500 INTERNAL_SERVER_ERROR \"Invalid UUID String (ensure lowercase): " + id + '"'
  }

  def 'returns input with default source'() {
    def path = "/metadata/${testType}/${testId}"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveInput(testType.toString(), testId, request, mockResponse)

    then:
    1 * mockApiRootGenerator.getApiRoot(_) >> 'http://localhost:8080'
    1 * mockMetadataStore.retrieveInput(testType, testSource, testId) >> testAggInput

    and:
    result.links.self == "http://localhost:8080/metadata/$testType/$testSource/$testId"
    result.links.parsed == "http://localhost:8080/metadata/$testType/$testSource/$testId/parsed"
    result.data.id == testId
    result.data.type == testType.toString()
    result.data.attributes == testAggInput
    result.errors == null
  }

  def 'returns input with explicit source'() {
    def path = "/metadata/${testType}/${testSource}/${testId}"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveInput(testType.toString(), testSource, testId, request, mockResponse)

    then:
    1 * mockApiRootGenerator.getApiRoot(_) >> 'http://localhost:8080'
    1 * mockMetadataStore.retrieveInput(testType, testSource, testId) >> testAggInput

    and:
    result.links.self == "http://localhost:8080/metadata/$testType/$testSource/$testId"
    result.links.parsed == "http://localhost:8080/metadata/$testType/$testSource/$testId/parsed"
    result.data.id == testId
    result.data.type == testType.toString()
    result.data.attributes == testAggInput
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

  def 'produce implicit raw json with default source'() {
    def path = "/metadata/${testType}/${testId}/raw"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveRaw(testType.toString(), testId, request, mockResponse)

    then:
    1 * mockMetadataStore.retrieveInput(testType, testSource, testId) >> testAggInput

    and:
    result.getViewName() == "redirect:/metadata/collection/unknown/8834cfd3-3b71-40b8-b037-315607a30c42/raw/json"
    mockResponse.status == 200
  }

  def 'produce implicit raw json with explicit source'() {
    def path = "/metadata/${testType}/${testSource}/${testId}/raw"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveRaw(testType.toString(), testSource, testId, request, mockResponse)

    then:
    1 * mockMetadataStore.retrieveInput(testType, testSource, testId) >> testAggInput

    and:
    result.getViewName() == "redirect:/metadata/collection/unknown/8834cfd3-3b71-40b8-b037-315607a30c42/raw/json"
    mockResponse.status == 200
  }

  def 'returns raw XML with default source'() {
    def path = "/metadata/${testType}/${testId}/raw/xml"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveRawXml(testType.toString(), testId, request, mockResponse)

    then:
    1 * mockMetadataStore.retrieveInput(testType, testSource, testId) >> testAggInput

    and:
    result == testAggInput.rawXml
  }

  def 'returns raw XML with explicit source'() {
    def path = "/metadata/${testType}/${testSource}/${testId}/raw/xml"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveRawXml(testType.toString(), testSource, testId, request, mockResponse)

    then:
    1 * mockMetadataStore.retrieveInput(testType, testSource, testId) >> testAggInput

    and:
    result == testAggInput.rawXml
  }

  def 'returns raw JSON with default source'() {
    def path = "/metadata/${testType}/${testId}/raw/json"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveRawJson(testType.toString(), testId, request, mockResponse)

    then:
    1 * mockMetadataStore.retrieveInput(testType, testSource, testId) >> testAggInput

    and:
    result == testAggInput.rawJson
  }

  def 'returns raw JSON with explicit source'() {
    def path = "/metadata/${testType}/${testSource}/${testId}/raw/json"
    def request = buildMockRequest(path)

    when:
    def result = controller.retrieveRawJson(testType.toString(), testSource, testId, request, mockResponse)

    then:
    1 * mockMetadataStore.retrieveInput(testType, testSource, testId) >> testAggInput

    and:
    result == testAggInput.rawJson
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

  def 'handles nonexistent raw XML'() {
    def path = "/metadata/${testType}/${testId}"
    def request = buildMockRequest(path)

    when:
    controller.retrieveRawXml(testType.toString(), testId, request, mockResponse)

    then:
    1 * mockMetadataStore.retrieveInput(testType, testSource, testId) >> null

    and:
    ResponseStatusException e = thrown()
    e.message == "404 NOT_FOUND \"No input exists for collection with id [${testId}] from source [${testSource}]\""
    e.status == HttpStatus.NOT_FOUND
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
    def deletedInput = AggregatedInput.newBuilder()
        .setType(RecordType.collection)
        .setRawJson('{"hola":"mundo"}')
        .setDeleted(true)
        .setInitialSource('test')
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
