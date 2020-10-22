package org.cedar.onestop.user

import org.cedar.onestop.user.config.AuthorizationConfiguration
import org.cedar.onestop.user.config.SecurityConfig
import org.cedar.onestop.user.domain.OnestopPrivilege
import org.cedar.onestop.user.domain.OnestopRole
import org.cedar.onestop.user.domain.OnestopUser
import org.cedar.onestop.user.domain.SavedSearch
import org.cedar.onestop.user.repository.OnestopPrivilegeRepository
import org.cedar.onestop.user.repository.OnestopRoleRepository
import org.cedar.onestop.user.repository.OnestopUserRepository
import org.cedar.onestop.user.repository.SavedSearchRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
class OnestopUserRepositorySpec extends Specification {

  Logger logger = LoggerFactory.getLogger(OnestopUserRepositorySpec.class)

  @Autowired
  OnestopUserRepository onestopUserRepo

  @Autowired
  OnestopRoleRepository roleRepository

  @Autowired
  SavedSearchRepository savedSearchRepository

  @Autowired
  private OnestopPrivilegeRepository privilegeRepository

  def setup(){
    onestopUserRepo.deleteAll()
    roleRepository.deleteAll()
    privilegeRepository.deleteAll()
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
    OnestopRole role = new OnestopRole(AuthorizationConfiguration.PUBLIC_ROLE)
    roleRepository.save(role)
    List<OnestopRole> roles = [role]
    OnestopUser onestopUser = new OnestopUser("1", roles)
    OnestopUser id = onestopUserRepo.save(onestopUser)

    when:
    OnestopUser getById = onestopUserRepo.getOne(id.getId())


    then:
    getById == id
    getById.getRoles().toString() == roles.toString()
  }

  def "create a complete user with roles, privileges, and searches"() {
    given: 'two privs, read and write'
    OnestopPrivilege readPrivilege = new OnestopPrivilege("READ")
    privilegeRepository.save(readPrivilege)
    OnestopPrivilege writePrivilege = new OnestopPrivilege("WRITE")
    privilegeRepository.save(writePrivilege)

    and: 'two roles - admin can read and write, public can read only'
    OnestopRole adminRole = new OnestopRole(AuthorizationConfiguration.ADMIN_ROLE)
    List<OnestopPrivilege> adminPrivileges = Arrays.asList(readPrivilege, writePrivilege)
    adminRole.setPrivileges(adminPrivileges)

    OnestopRole publicRole = new OnestopRole(AuthorizationConfiguration.PUBLIC_ROLE)
    List<OnestopPrivilege> publicPrivilege = Arrays.asList(readPrivilege)
    publicRole.setPrivileges(publicPrivilege)

    and: 'two users - an admin and a public user'
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

    and: 'they both share the read privilege'
    savedAdminUser.getRoles()[0].getPrivileges().contains(readPrivilege)
    savedPublicUser.getRoles()[0].getPrivileges().contains(readPrivilege)

    and: 'the public user does not have the write privilege'
    !savedPublicUser.getRoles()[0].getPrivileges().contains(writePrivilege)

    and:
    SavedSearch mockSearch = new SavedSearch(user: savedPublicUser, value: "/collection/search")
    savedSearchRepository.save(mockSearch)

    List<SavedSearch> mockSearches = [mockSearch]
    savedPublicUser.setSearches(mockSearches)
    OnestopUser updatedPublicUser = onestopUserRepo.findById(savedPublicUser.getId()).get()

    then:
    updatedPublicUser.getSearches()[0] == mockSearch

    and:
    SavedSearch savedMockSearch = savedSearchRepository.getOne(mockSearch.getId())

    then:
    savedMockSearch == mockSearch
    savedMockSearch.getUser() == updatedPublicUser
  }
}

