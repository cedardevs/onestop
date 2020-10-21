package org.cedar.onestop.user

import org.cedar.onestop.user.config.AuthorizationConfiguration
import org.cedar.onestop.user.controller.UserController
import org.cedar.onestop.user.domain.OnestopPrivilege
import org.cedar.onestop.user.domain.OnestopRole
import org.cedar.onestop.user.domain.OnestopUser
import org.cedar.onestop.user.service.OnestopUserService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification

@WebMvcTest(controllers = UserController.class)
class UserControllerSpec extends Specification {

  static final String PUBLIC_USER_ID = 'public'
  static final String ADMIN_USER_ID = 'admin'

  @Autowired
  private MockMvc mockMvc

  @Autowired
  private UserController userController

  @SpringBean
  private OnestopUserService mockUserService = Mock()

  def "user controller exists and is protected from unauthenticated users"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders.get("/v1/user/"))

    then:
    results.andExpect(MockMvcResultMatchers.status().isUnauthorized())
  }

  @WithMockUser(username = PUBLIC_USER_ID, roles = [AuthorizationConfiguration.READ_OWN_PROFILE])
  def "public user cannot hit user endpoint"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders.get("/v1/user/"))

    then:
    results.andExpect(MockMvcResultMatchers.status().isForbidden())
  }

  @WithMockUser(username = ADMIN_USER_ID, roles = [AuthorizationConfiguration.READ_USER])
  def "admin user can hit user endpoint"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders.get("/v1/user"))

    then:
    1 * mockUserService.findAll(_ as Pageable) >> PageImpl.empty()
    results.andExpect(MockMvcResultMatchers.status().isOk())
  }

  @WithMockUser(roles = ["RANDO"])
  def "random user can't hit user endpoint"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders.get("/v1/user"))

    then:
    results.andExpect(MockMvcResultMatchers.status().isForbidden())
  }

  @WithMockUser(username = ADMIN_USER_ID, roles = [AuthorizationConfiguration.CREATE_USER])
  def "user is created"() {
    when:
    def postSearch = mockMvc.perform(MockMvcRequestBuilders
        .post("/v1/user")
        .contentType("application/json")
        .content('{ "name": "test"}'))

    then:
    mockUserService.save(_ as OnestopUser) >> new OnestopUser("new_user")

    postSearch.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").value("new_user"))
  }

  @WithMockUser(username = PUBLIC_USER_ID, roles = [AuthorizationConfiguration.READ_OWN_PROFILE])
  def "public user can fetch their own user info"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/self"))

    then:
    1 * mockUserService.findById(PUBLIC_USER_ID) >> Optional.of(new OnestopUser(PUBLIC_USER_ID))
    results.andExpect(MockMvcResultMatchers.status().isOk())
  }

  @WithMockUser(username = PUBLIC_USER_ID, roles = [AuthorizationConfiguration.READ_OWN_PROFILE])
  def "public user can initialize their own info"() {
    def testUser = new OnestopUser(PUBLIC_USER_ID, new OnestopRole('public', [new OnestopPrivilege('read')]))

    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .post("/v1/self")
        .contentType(MediaType.APPLICATION_JSON)
        .content('{}'))

    then:
    results.andExpect(MockMvcResultMatchers.status().isCreated())

    1 * mockUserService.exists(PUBLIC_USER_ID) >> false
    1 * mockUserService.findOrCreateUser(PUBLIC_USER_ID) >> testUser
    1 * mockUserService.save({ it.id == PUBLIC_USER_ID} ) >> testUser
  }

  @WithMockUser(username = PUBLIC_USER_ID, roles = [AuthorizationConfiguration.READ_OWN_PROFILE])
  def "public user can update their own info"() {
    def existingUser = new OnestopUser(PUBLIC_USER_ID)

    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .put("/v1/self")
        .contentType(MediaType.APPLICATION_JSON)
        .content(/{"id": "${PUBLIC_USER_ID}"}/))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())

    1 * mockUserService.exists(PUBLIC_USER_ID) >> true
    1 * mockUserService.findOrCreateUser(PUBLIC_USER_ID) >> existingUser
    1 * mockUserService.save({ it.id == PUBLIC_USER_ID} ) >> existingUser
  }

  @WithMockUser(username = PUBLIC_USER_ID, roles = [AuthorizationConfiguration.READ_OWN_PROFILE])
  def "public user cannot edit readonly fields"() {
    def defaultRole = new OnestopRole('default', 'ROLE_' + AuthorizationConfiguration.ADMIN_ROLE)
    def existingUser = new OnestopUser(PUBLIC_USER_ID, defaultRole)

    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .put("/v1/self")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""{
          "id": "${PUBLIC_USER_ID}",
          "createdOn": "1234-01-01T00:00:00Z",
          "lastUpdatedOn": "2345-01-01T00:00:00Z",
          "roles": [ {"id": "MALICIOUS_ROLE_ATTEMPT"} ]}"""))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())

    1 * mockUserService.exists(PUBLIC_USER_ID) >> true
    1 * mockUserService.findOrCreateUser(PUBLIC_USER_ID) >> existingUser
    1 * mockUserService.save({ OnestopUser it ->
      println "mock save called with: ${it}"
      return it.createdOn == null && it.lastUpdatedOn == null && it.roles == existingUser.roles
    }) >> existingUser
  }

  @WithMockUser(username = PUBLIC_USER_ID, roles = [AuthorizationConfiguration.READ_OWN_PROFILE])
  def "public user can only update their own info"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .put("/v1/self")
        .contentType(MediaType.APPLICATION_JSON)
        .content('{"id": "SOMEONE_ELSES_ID"}'))

    then:
    results.andExpect(MockMvcResultMatchers.status().isForbidden())
  }
}
