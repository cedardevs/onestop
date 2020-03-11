package org.cedar.onestop.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

import java.net.http.HttpHeaders

@DirtiesContext
@ActiveProfiles(["integration"])
@SpringBootTest(
    classes = [ UserApplication],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Unroll
class SaveSearchControllerIntegrationSpec extends Specification {
  @Autowired
  private TestRestTemplate restTemplate

  @LocalServerPort
  private int port

  private String getRootUrl() {
    return "http://localhost:" + port
  }

  def setup() {
    restTemplate = new TestRestTemplate()
//    baseUrl = "http://localhost:${port}/${contextPath}"
  }

  def "test get all save searchs "() {
    def headers = new HttpHeaders()
    def entity = new HttpEntity<String>(null, headers)

    when:
    ResponseEntity<String> response = restTemplate.exchange(getRootUrl() + "/savesearchs", HttpMethod.GET, entity, String.class)

    then:
    response.getBody() == null
  }
}
