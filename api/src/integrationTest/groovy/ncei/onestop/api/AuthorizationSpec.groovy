package ncei.onestop.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.http.RequestEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.http.HttpMethod.*
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.UNAUTHORIZED
import static org.springframework.http.MediaType.*


@Unroll
@WebIntegrationTest
@ActiveProfiles("integration")
@ContextConfiguration(loader = SpringApplicationContextLoader,
    classes = [Application, IntegrationTestConfig])
@TestPropertySource(properties = ['security.user.name:user', 'security.user.password:pass'])
class AuthorizationSpec extends Specification {

  @Value('${local.server.port}')
  private String port
  @Value('${server.context-path}')
  private String contextPath
  private String baseUri

  @Value('${security.user.name}')
  private String username
  @Value('${security.user.password}')
  private String password

  void setup() {
    baseUri = "http://localhost:${port}${contextPath}"
  }

  def '#path does not require authorization'() {
    setup:
    def uri = "${baseUri}${path}".toURI()
    println uri
    def requestEntity = RequestEntity.method(method, uri)
        .contentType(contentType)
        .body(body)

    when:
    def result = new RestTemplate().exchange(requestEntity, Map)

    then: // no authorization required
    noExceptionThrown()
    result.statusCode.value() >= 200
    result.statusCode.value() < 300

    where:
    path        | method  | contentType       | body
    '/search'   | POST    | APPLICATION_JSON  | '{"queries":[{"type":"queryText","value":"lake"}]}'
  }

  def '#path requires authorization'() {
    setup:
    def uri = "${baseUri}${path}".toURI()
    println uri
    def unauthorizedRequest = RequestEntity.method(method, uri)
        .contentType(contentType)
        .body(body)

    def authorizedRequest = RequestEntity.method(method, uri)
        .header('Authorization', buildAuthHeader(username, password))
        .contentType(contentType)
        .body(body)

    when:
    new RestTemplate().exchange(unauthorizedRequest, Map)

    then:
    def httpException = thrown(HttpStatusCodeException)
    println httpException?.responseBodyAsString
    httpException.statusCode in [UNAUTHORIZED, FORBIDDEN]

    when:
    def result = new RestTemplate().exchange(authorizedRequest, Map)

    then:
    noExceptionThrown()
    result.statusCode.value() >= 200
    result.statusCode.value() < 300

    where:
    path              | method  | contentType       | body
    '/search/reindex' | GET     | APPLICATION_JSON  | null
    '/metadata'       | POST    | APPLICATION_XML   | '<xml sarcasm="very yes">xml is my favorite format of all!</xml>'
//    'metadata/id'    | GET     | APPLICATION_XML   | null
  }

  private static buildAuthHeader(String username, String password) {
    def result = 'Basic ' + new String(Base64.encoder.encode("$username:$password".bytes ) as byte[])
    println "encoded $username:$password => $result"
    return result
  }

}
