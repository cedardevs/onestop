package org.cedar.onestop.registry.api

import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ApiRootGeneratorSpec extends Specification {

  ApiRootGenerator apiRootGenerator = new ApiRootGenerator()
  MockHttpServletRequest mockRequest = new MockHttpServletRequest()

  def setup() {
    mockRequest.setProtocol('http')
    mockRequest.addHeader('Host', 'localhost')
  }

  def 'parses host and protocol from the Forwarded header'() {
    when:
    def result = apiRootGenerator.parseForwardedHeader(header)

    then:
    println result

    result.host == host
    result.proto == proto

    where:
    host        | proto   | header
    'testhost'  | 'http'  | 'host=testhost;proto=http'
    'testhost'  | 'http'  | 'host=testhost; proto=http'
    'testhost'  | 'http'  | 'host=testhost;proto="http"'
    'testhost'  | 'http'  | 'host = testhost ; proto = "http"'
    'testhost'  | 'http'  | 'host=testhost;proto=http;for=client1, proxy1, proxy2'
    null        | 'http'  | 'proto=http'
    'testhost'  | null    | 'host=testhost'
    'testhost'  | null    | 'host=testhost; proto='
  }

  def 'returns api root with no headers'() {
    expect:
    apiRootGenerator.getApiRoot(mockRequest) == 'http://localhost'
  }

  def 'returns api root with Forwarded header'() {
    mockRequest.addHeader('Forwarded', 'host=testhost;proto=https')

    expect:
    apiRootGenerator.getApiRoot(mockRequest) == 'https://testhost'
  }

  def 'returns api root with X-Forwarded-* headers'() {
    mockRequest.addHeader('X-Forwarded-Host', 'testhost')
    mockRequest.addHeader('X-Forwarded-Proto', 'https')

    expect:
    apiRootGenerator.getApiRoot(mockRequest) == 'https://testhost'
  }

  def 'applies context path to api root'() {
    mockRequest.setContextPath('/testcontext')

    expect:
    apiRootGenerator.getApiRoot(mockRequest) == 'http://localhost/testcontext'
  }

}
