package org.cedar.onestop.user

import org.cedar.onestop.user.domain.OnestopUser
import org.cedar.onestop.user.repository.OnestopPrivilegeRepository
import org.cedar.onestop.user.repository.OnestopRoleRepository
import org.cedar.onestop.user.repository.OnestopUserRepository
import org.cedar.onestop.user.service.OnestopUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.Pageable
import spock.lang.Specification

import static org.cedar.onestop.user.config.AuthorizationConfiguration.*

@DataJpaTest
class UserServiceSpec extends Specification {

  @Autowired OnestopUserRepository userRepository
  @Autowired OnestopRoleRepository roleRepository
  @Autowired OnestopPrivilegeRepository privilegeRepository

  OnestopUserService userService

  def setup() {
    userService = new OnestopUserService(userRepository, roleRepository, privilegeRepository)
    userService.ensureDefaults()
  }

  def "retrieves user, roles and privileges by user id"() {
    def id = 'test'
    userService.save(userService.findOrCreateUser(id))

    when:
    def user = userService.findById(id)
    def roleNames = userService.findRolesByUserId(id, Pageable.unpaged()).toList()*.name.toSet()
    def privilegeNames = userService.findPrivilegesByUserId(id, Pageable.unpaged()).toList()*.name.toSet()

    then:
    user.get() instanceof OnestopUser
    roleNames == [ROLE_PREFIX + PUBLIC_ROLE].toSet()
    privilegeNames == NEW_USER_PRIVILEGES.toSet()
  }

}
