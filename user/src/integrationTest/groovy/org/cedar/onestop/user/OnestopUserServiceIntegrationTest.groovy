package org.cedar.onestop.user

import org.cedar.onestop.user.config.SecurityConfig
import org.cedar.onestop.user.domain.OnestopPrivilege
import org.cedar.onestop.user.domain.OnestopRole
import org.cedar.onestop.user.domain.OnestopUser
import org.cedar.onestop.user.service.OnestopUserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
@SpringBootTest(classes = [UserApplication.class], webEnvironment = RANDOM_PORT)
class OnestopUserServiceIntegrationTest extends Specification {
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer()

  Logger logger = LoggerFactory.getLogger(OnestopUserServiceIntegrationTest.class)

  @Autowired
  OnestopUserService onestopUserService

  def setupSpec() {
    postgres.start()
  }

  // Run after all the tests, even after failures:
  def cleanupSpec() {
    postgres.stop()
  }

  def setup() {
    onestopUserService.userRepository.deleteAll()
    onestopUserService.roleRepository.deleteAll()
    onestopUserService.privilegeRepository.deleteAll()
  }

  def 'create new user privs'(){
    when:
    Collection<OnestopPrivilege> privs = onestopUserService.createPrivilegesIfNotFound()
    then:
    privs.toString() == SecurityConfig.NEW_USER_PRIVILEGES.toString()
    when:
    OnestopRole role = onestopUserService.createRole(SecurityConfig.PUBLIC_ROLE, privs)
    then:
    role.getName() == SecurityConfig.PUBLIC_ROLE
  }

  def "create new user with default roles and privileges"() {
    given:
    String uuid = UUID.randomUUID(); //this comes from login.gov or other IdPs

    when:
    OnestopUser defaultUser = onestopUserService.findOrCreateUser(uuid)
    Collection<OnestopRole> roles = defaultUser.getRoles()
    OnestopRole defaultRole = roles[0]
    Collection<OnestopPrivilege> privileges = defaultRole.getPrivileges()

    OnestopPrivilege defaultPrivilege = privileges[0]

    then:
    defaultUser.getId() == uuid
    roles.size() == 1 //the public role
    defaultRole.getName() == "ROLE_" + SecurityConfig.PUBLIC_ROLE
    defaultPrivilege.getName() == SecurityConfig.NEW_USER_PRIVILEGES[0]
  }
}
