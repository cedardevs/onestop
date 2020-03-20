package org.cedar.onestop.user;


import org.cedar.onestop.user.service.SaveSearch;
import org.cedar.onestop.user.service.SaveSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import spock.lang.Specification;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationTest")
class SaveSearchRepositorySpec extends Specification {
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer()

  @Autowired
  SaveSearchRepository saveSearchRepository

  private SaveSearch saveSearch

  def setup(){
    saveSearch = new SaveSearch("1", "UserId1", "entryName1", "value 1")
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
    SaveSearch saveSearch1 = new SaveSearch("2", "UserId2", "entryName2", "value 1")
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
    List<SaveSearch> getByUserId = saveSearchRepository.findAllByUserId(id.getUserId())

    then:
    getByUserId[0].id != null
    getByUserId[0].userId == "UserId1"

  }

  def "should have multiple entries for a userId"() {
    given:
    SaveSearch saveSearch1 = new SaveSearch("2", "UserId2", "entryName2", "value 2")
    SaveSearch saveSearch2 = new SaveSearch("3", "UserId2", "entryName3", "value 3")
    iterator()
    saveSearchRepository.save(saveSearch1)
    saveSearchRepository.save(saveSearch2)

    when:
    List<SaveSearch> getByUserId = saveSearchRepository.findAllByUserId("UserId2")

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

