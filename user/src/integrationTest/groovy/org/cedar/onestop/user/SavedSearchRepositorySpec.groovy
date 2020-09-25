package org.cedar.onestop.user

import org.cedar.onestop.user.domain.OnestopUser
import org.cedar.onestop.user.domain.SavedSearch
import org.cedar.onestop.user.repository.OnestopUserRepository
import org.cedar.onestop.user.repository.SavedSearchRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Shared
import spock.lang.Specification

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
class SavedSearchRepositorySpec extends Specification {
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer()

  @Autowired
  SavedSearchRepository saveSearchRepository

  @Autowired
  OnestopUserRepository onestopUserRepo

  @Shared
  OnestopUser savedUser

  private SavedSearch saveSearch

  def setup(){
    OnestopUser onestopUser = new OnestopUser("mock_user")
    savedUser = onestopUserRepo.save(onestopUser)

    saveSearch = new SavedSearch(savedUser, "1", "entryName1","{\"test\":\"test\"}", "value 1")
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
    SavedSearch saveSearch1 = new SavedSearch(savedUser, "2", "entryName2", "{\"test\":\"test\"}","value 1")
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
    List<SavedSearch> getByUserId = saveSearchRepository.findAllByUser(savedUser)

    then:
    getByUserId[0].id != null
    getByUserId[0].getUser().getId() == savedUser.getId()

  }

  def "should have multiple entries for a userId"() {
    given:
    SavedSearch saveSearch1 = new SavedSearch(savedUser, "2", "entryName2", "{\"test\":\"test\"}", "value 2")
    SavedSearch saveSearch2 = new SavedSearch(savedUser, "3", "entryName3", "{\"test\":\"test\"}","value 3")
    iterator()
    saveSearchRepository.save(saveSearch1)
    saveSearchRepository.save(saveSearch2)

    when:
    List<SavedSearch> getByUserId = saveSearchRepository.findAllByUser(savedUser)

    then:
    getByUserId.size() == 2
    getByUserId[0].value == "value 2"
    getByUserId[1].value == "value 3"
    getByUserId[0].getUser() == getByUserId[1].getUser()
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

