package org.cedar.onestop.user

import org.cedar.onestop.user.config.AuthorizationConfiguration
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
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
@SpringBootTest(classes = [UserApplication.class], webEnvironment = RANDOM_PORT)
class OnestopUserServiceIntegrationTest extends Specification {
  Logger logger = LoggerFactory.getLogger(OnestopUserServiceIntegrationTest.class)

  @Autowired
  OnestopUserService onestopUserService

  def setup() {
    onestopUserService.userRepository.deleteAll()
  }

  def 'default roles and privs have been created'(){
    when:
    def defaultRole = onestopUserService.roleRepository
        .findByName(AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.PUBLIC_ROLE)
        .get()
    def adminRole = onestopUserService.roleRepository
        .findByName(AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.ADMIN_ROLE)
        .get()

    then:
    defaultRole instanceof OnestopRole
    defaultRole.privileges*.name as Set == AuthorizationConfiguration.NEW_USER_PRIVILEGES as Set
    adminRole instanceof OnestopRole
    adminRole.privileges*.name as Set == AuthorizationConfiguration.ADMIN_PRIVILEGES as Set

    when:
    onestopUserService.ensureDefaults()

    then: // default role creation is idempotent
    defaultRole.id == onestopUserService.roleRepository
        .findByName(AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.PUBLIC_ROLE)
        .get().id
    adminRole.id == onestopUserService.roleRepository
        .findByName(AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.ADMIN_ROLE)
        .get().id
  }

  def "create new user with default roles and privileges"() {
    given:
    String uuid = UUID.randomUUID() //this comes from login.gov or other IdPs

    when:
    OnestopUser defaultUser = onestopUserService.findOrCreateUser(uuid)
    Collection<OnestopRole> roles = defaultUser.getRoles()
    OnestopRole defaultRole = roles[0]
    Collection<OnestopPrivilege> privileges = defaultRole.getPrivileges()

    OnestopPrivilege defaultPrivilege = privileges[0]

    then:
    defaultUser.getId() == uuid
    roles.size() == 1 //the public role
    defaultRole.getName() == "ROLE_" + AuthorizationConfiguration.PUBLIC_ROLE
    defaultPrivilege.getName() == AuthorizationConfiguration.NEW_USER_PRIVILEGES[0]
  }

  def "create default admin user with admin roles and privileges"() {
    given:
    String uuid = UUID.randomUUID() //this comes from login.gov or other IdPs

    when:
    OnestopUser adminUser = onestopUserService.findOrCreateUser(uuid, true)
    Collection<OnestopRole> roles = adminUser.getRoles()
    OnestopRole defaultRole = roles[0]
    Collection<OnestopPrivilege> privileges = defaultRole.getPrivileges()

    OnestopPrivilege defaultPrivilege = privileges[0]

    then:
    adminUser.getId() == uuid
    roles.size() == 1 //the public role
    defaultRole.getName() == "ROLE_" + AuthorizationConfiguration.ADMIN_ROLE
    defaultPrivilege.getName() == AuthorizationConfiguration.ADMIN_PRIVILEGES[0]
    adminUser.getPrivileges().collect{priv -> priv.toString()} == AuthorizationConfiguration.ADMIN_PRIVILEGES
  }
}
