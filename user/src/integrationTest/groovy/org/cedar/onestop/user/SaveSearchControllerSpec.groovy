package org.cedar.onestop.user

import org.cedar.onestop.user.service.SaveSearch
import org.cedar.onestop.user.service.SaveSearchRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles('integration')
@SpringBootTest(classes = [UserApplication.class], webEnvironment = RANDOM_PORT)
class SaveSearchControllerSpec extends Specification {
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer()

  @LocalServerPort
  private int port

  private String getRootUrl() {
    return "http://localhost:" + port
  }

  @Autowired
  SaveSearch saveSearch

  private TestRestTemplate restTemplate
  String baseUrl

  @Autowired
  SaveSearchRepository saveSearchRepository

  private SaveSearch saveSearch

  def setup() {
    restTemplate = new TestRestTemplate()
    baseUrl = getRootUrl()
  }
  // Run before all the tests:
  def setupSpec() {
    postgres.start()
  }

  // Run after all the tests, even after failures:
  def cleanupSpec() {
    postgres.stop()
  }

  def "Create and get all save searches"() {
    given:
    saveSearch = new SaveSearch("1", "userOne", "entryName1", "value 1")
    saveSearchRepository.save(saveSearch)

    when:
    def postResponse = restTemplate
        .postForEntity(getBaseUrl() + "/api/v1/savesearches", saveSearch, SaveSearch.class)
    def savedSearchId = postResponse.body.id

    then:
    savedSearchId instanceof String
    postResponse.statusCode == HttpStatus.OK

    when:
    sleep(200)
    def retrieveEntity = restTemplate
        .getForEntity(getBaseUrl()+ "/api/v1/savesearches/${savedSearchId}", SaveSearch.class)

    then:
    retrieveEntity.statusCode == HttpStatus.OK

    and:
    def data = retrieveEntity.body
    data.id == savedSearchId
    data.userId == "userOne"
    data.name == "entryName1"
    data.value == "value 1"
  }

  def "Create and Get all save searches by user id"() {
    given:
    SaveSearch saveSearch1 = new SaveSearch("2", "userIdTwo", "entryName2", "value 2")
    SaveSearch saveSearch2 = new SaveSearch("3", "userIdTwo", "entryName3", "value 3")
    saveSearchRepository.save(saveSearch1)

    when:
    def postResponse = restTemplate
        .postForEntity(getBaseUrl() + "/api/v1/savesearches", saveSearch1, SaveSearch.class)

    def postResponseTwo = restTemplate
        .postForEntity(getBaseUrl() + "/api/v1/savesearches", saveSearch2, SaveSearch.class)

    def savedSearchId = postResponse.body.id

    then:
    postResponse.statusCode == HttpStatus.OK

    when:
    sleep(200)
    def retrieveEntity = restTemplate.getForEntity(getBaseUrl()+ "/api/v1/savesearches/${savedSearchId}", SaveSearch.class)

    then:
    retrieveEntity.statusCode == HttpStatus.OK

    and:
    def data = retrieveEntity.body
    data.userId == "userIdTwo"
    data.name == "entryName2"
    data.value == "value 2"
  }
}
