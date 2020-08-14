package org.cedar.onestop.user

import org.cedar.onestop.user.service.SavedSearch
import org.cedar.onestop.user.repository.SavedSearchRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationTest")
class SavedSearchRepositorySpec extends Specification {
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer()

  @Autowired
  SavedSearchRepository saveSearchRepository

  private SavedSearch saveSearch

  def setup(){
    saveSearch = new SavedSearch("1", "UserId1", "entryName1","{\"test\":\"test\"}", "value 1")
  }

  // Run before all the tests:
  def setupSpec() {
    postgres.start()
  }

  // Run after all the tests, even after failures:
  def cleanupSpec() {
    postgres.stop()
  }

  def "should Store Each SaveSearch"() {
    given:
    SavedSearch saveSearch1 = new SavedSearch("2", "UserId2", "entryName2", "{\"test\":\"test\"}","value 1")
    saveSearchRepository.save(saveSearch)
    saveSearchRepository.save(saveSearch1)

    when:
    long count = saveSearchRepository.count()

    then:
    count == 2
  }

  def "Should store with a unique identifier"() {
    given:
    def id = saveSearchRepository.save(saveSearch)

    when:
    def getById = saveSearchRepository.getOne(id.getId())

    then:
    getById == id
  }

  def "Should get by user Identifier"() {
    given:
    def id = saveSearchRepository.save(saveSearch)

    when:
    List<SavedSearch> getByUserId = saveSearchRepository.findAllByUserId(id.getUserId())

    then:
    getByUserId[0].id != null
    getByUserId[0].userId == "UserId1"

  }

  def "should have multiple entries for a userId"() {
    given:
    SavedSearch saveSearch1 = new SavedSearch("2", "UserId2", "entryName2", "{\"test\":\"test\"}", "value 2")
    SavedSearch saveSearch2 = new SavedSearch("3", "UserId2", "entryName3", "{\"test\":\"test\"}","value 3")
    iterator()
    saveSearchRepository.save(saveSearch1)
    saveSearchRepository.save(saveSearch2)

    when:
    List<SavedSearch> getByUserId = saveSearchRepository.findAllByUserId("UserId2")

    then:
    getByUserId.size() == 2
    getByUserId[0].value == "value 2"
    getByUserId[1].value == "value 3"
    getByUserId[0].userId == getByUserId[1].userId
  }

  //  static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
//  @Override
//  public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
//
//      TestPropertyValues
//          .of("spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
//              "spring.datasource.username=" + postgreSQLContainer.getUsername(),
//              "spring.datasource.password=" + postgreSQLContainer.getPassword())
//          .applyTo(configurableApplicationContext.getEnvironment())
//
//    }
//
//  }
//    @ClassRule
//  public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres")
//      .withDatabaseName("test")
//      .withUsername("test")
//      .withPassword("test")
//  static class Initializer
//      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
//    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
//      TestPropertyValues.of(
//          "spring.datasource.url=" + postgres.getJdbcUrl(),
//          "spring.datasource.username=" + postgres.getUsername(),
//          "spring.datasource.password=" + postgres.getPassword()
//      ).applyTo(configurableApplicationContext.getEnvironment())
//    }
//  }

}

