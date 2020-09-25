package org.cedar.onestop.user

import org.cedar.onestop.user.config.SecurityConfig
import org.cedar.onestop.user.domain.OnestopUser
import org.cedar.onestop.user.repository.OnestopPrivilegeRepository
import org.cedar.onestop.user.repository.OnestopRoleRepository
import org.cedar.onestop.user.repository.OnestopUserRepository
import org.cedar.onestop.user.repository.SavedSearchRepository
import org.cedar.onestop.user.service.OnestopUserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
class OnestopUserServiceIntegrationTest extends Specification {
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer()

  Logger logger = LoggerFactory.getLogger(OnestopUserServiceIntegrationTest.class)

//  @Autowired
  OnestopUserService onestopUserService

  @Autowired
  OnestopUserRepository onestopUserRepo
  @Autowired
  OnestopRoleRepository roleRepository
  @Autowired
  OnestopPrivilegeRepository privilegeRepository
  @Autowired
  SavedSearchRepository savedSearchRepository


  def setup(){
    onestopUserService = new OnestopUserService()
    onestopUserRepo.deleteAll()
    roleRepository.deleteAll()
    privilegeRepository.deleteAll()
  }

  def setupSpec() {
    postgres.start()
  }

  // Run after all the tests, even after failures:
  def cleanupSpec() {
    postgres.stop()
  }

  def "create user with role"() {
    given:
    String uuid = 'abc-123'
    OnestopUser defaultUser = onestopUserService.findOrCreateUser(uuid)

    when:
    OnestopUser savedUser = onestopUserRepo.getOne(defaultUser.getId())

    then:
    savedUser.getId() == uuid
    savedUser.getId() == defaultUser.getId()
    savedUser.getRoles().toString() == defaultUser.getRoles().toString()
    savedUser.getRoles()[1].privToStringList()[1].toString() == "ROLE_" + SecurityConfig.PUBLIC_PRIVILEGE
  }

  def 'test orElse'(){
    String uuid = 'abc-123'
    onestopUserRepo.findById(uuid).orElse(onestopUserService.createDefaultUser(uuid));
  }
}
