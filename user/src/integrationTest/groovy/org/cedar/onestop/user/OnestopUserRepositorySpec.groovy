package org.cedar.onestop.user

import org.cedar.onestop.user.repository.OnestopPrivilegeRepository
import org.cedar.onestop.user.repository.OnestopRoleRepository
import org.cedar.onestop.user.repository.OnestopUserRepository
import org.cedar.onestop.user.domain.OnestopRole
import org.cedar.onestop.user.domain.OnestopPrivilege
import org.cedar.onestop.user.domain.OnestopUser
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
@ActiveProfiles("integrationTest")
class OnestopUserRepositorySpec extends Specification {
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer()

  Logger logger = LoggerFactory.getLogger(OnestopUserRepositorySpec.class)

  @Autowired
  OnestopUserRepository onestopUserRepo

  @Autowired
  private OnestopRoleRepository roleRepository

  @Autowired
  private OnestopPrivilegeRepository privilegeRepository

  def setup(){
    onestopUserRepo.deleteAll()
    roleRepository.deleteAll()
    privilegeRepository.deleteAll()
  }

  // Run before all the tests:
  def setupSpec() {
    postgres.start()
  }

  // Run after all the tests, even after failures:
  def cleanupSpec() {
    postgres.stop()
  }

  def "create a simple user"() {
    given:
    OnestopUser onestopUser = new OnestopUser("1")
    onestopUserRepo.save(onestopUser)

    when:
    long count = onestopUserRepo.count()

    then:
    count == 1
  }

  def "create user with role"() {
    given:
    OnestopRole role = new OnestopRole("PUBLIC")
    roleRepository.save(role)
    HashSet<OnestopRole> roles = [role]
    OnestopUser onestopUser = new OnestopUser("1", roles)
    OnestopUser id = onestopUserRepo.save(onestopUser)

    when:
    OnestopUser getById = onestopUserRepo.getOne(id.getId())


    then:
    getById == id
    getById.getRoles().toString() == roles.toString()
  }

  def "create user with privs and roles"() {
    given: 'two privs, read and write'
    OnestopPrivilege readPrivilege = new OnestopPrivilege("READ")
    privilegeRepository.save(readPrivilege)
    OnestopPrivilege writePrivilege = new OnestopPrivilege("WRITE")
    privilegeRepository.save(writePrivilege)

    and: 'two roles'
    OnestopRole adminRole = new OnestopRole("ADMIN")
    List<OnestopPrivilege> adminPrivileges = Arrays.asList(readPrivilege, writePrivilege)
    adminRole.setPrivileges(adminPrivileges)

    OnestopRole publicRole = new OnestopRole("PUBLIC")
    List<OnestopPrivilege> publicPrivilege = Arrays.asList(readPrivilege)
    publicRole.setPrivileges(publicPrivilege)

    and: 'two users'
    OnestopUser adminUser = new OnestopUser("abc")
    Collection<OnestopRole> adminRoles = Arrays.asList(adminRole)
    adminUser.setRoles(adminRoles)

    OnestopUser publicUser = new OnestopUser("xyz")
    Collection<OnestopRole> publicRoles = Arrays.asList(publicRole)
    publicUser.setRoles(publicRoles)

    when: 'we save the users'
    OnestopUser savedAdminUser = onestopUserRepo.save(adminUser)
    OnestopUser savedPublicUser = onestopUserRepo.save(publicUser)
    long userCount = onestopUserRepo.count()
    long privCount = privilegeRepository.count()
    long roleCount = roleRepository.count()

    then: 'we can see both users were saved'
    userCount == 2
    privCount == 2
    roleCount == 2

    adminUser.getRoles()[0].getName() == savedAdminUser.getRoles()[0].getName()
    adminUser.getRoles()[0].getPrivileges()[0].getName() == savedAdminUser.getRoles()[0].getPrivileges()[0].getName()

    publicUser.getRoles()[0].getName() == savedPublicUser.getRoles()[0].getName()
    publicUser.getRoles()[0].getPrivileges()[0].getName() == savedPublicUser.getRoles()[0].getPrivileges()[0].getName()

    and: 'they both share the read priv'
    savedAdminUser.getRoles()[0].getPrivileges().contains(readPrivilege)
    savedPublicUser.getRoles()[0].getPrivileges().contains(readPrivilege)
    !savedPublicUser.getRoles()[0].getPrivileges().contains(writePrivilege)
  }
}

