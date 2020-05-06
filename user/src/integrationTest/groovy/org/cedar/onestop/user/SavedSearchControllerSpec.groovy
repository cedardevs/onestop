package org.cedar.onestop.user

import org.cedar.onestop.user.service.SavedSearch
import org.cedar.onestop.user.repository.SavedSearchRepository
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
class SavedSearchControllerSpec extends Specification {
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer()

  @LocalServerPort
  private int port

  private String getRootUrl() {
    return "http://localhost:" + port
  }

  @Autowired
  SavedSearch saveSearch

  private TestRestTemplate restTemplate
  String baseUrl

  @Autowired
  SavedSearchRepository saveSearchRepository

  private SavedSearch saveSearch

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

  def "Create and get all saved searches"() {
    given:
    saveSearch = new SavedSearch("1", "userOne", "entryName1", "value 1")
    saveSearchRepository.save(saveSearch)

    when:
    def postResponse = restTemplate
        .postForEntity(getBaseUrl() + "/v1/saved-search", saveSearch, SavedSearch.class)
    def savedSearchId = postResponse.body.id

    then:
    savedSearchId instanceof String
    postResponse.statusCode == HttpStatus.OK

    when:
    sleep(200)
    def retrieveEntity = restTemplate
        .getForEntity(getBaseUrl()+ "/v1/saved-search/${savedSearchId}", SavedSearch.class)

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
    SavedSearch saveSearch1 = new SavedSearch("2", "userIdTwo", "entryName2", "value 2")
    SavedSearch saveSearch2 = new SavedSearch("3", "userIdTwo", "entryName3", "value 3")
    saveSearchRepository.save(saveSearch1)

    when:
    def postResponse = restTemplate
        .postForEntity(getBaseUrl() + "/v1/saved-search", saveSearch1, SavedSearch.class)

    def postResponseTwo = restTemplate
        .postForEntity(getBaseUrl() + "/v1/saved-search", saveSearch2, SavedSearch.class)

    def savedSearchId = postResponse.body.id

    then:
    postResponse.statusCode == HttpStatus.OK

    when:
    sleep(200)
    def retrieveEntity = restTemplate.getForEntity(getBaseUrl()+ "/v1/saved-search/${savedSearchId}", SavedSearch.class)

    then:
    retrieveEntity.statusCode == HttpStatus.OK

    and:
    def data = retrieveEntity.body
    data.userId == "userIdTwo"
    data.name == "entryName2"
    data.value == "value 2"
  }
}
