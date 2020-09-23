package org.cedar.onestop.user

import org.cedar.onestop.user.config.SecurityConfig
import org.cedar.onestop.user.controller.UserController
import org.cedar.onestop.user.domain.OnestopUser
import org.cedar.onestop.user.repository.OnestopPrivilegeRepository
import org.cedar.onestop.user.repository.OnestopRoleRepository
import org.cedar.onestop.user.repository.OnestopUserRepository
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification

@WebMvcTest(controllers = UserController.class)
class UserControllerSpec extends Specification {

  @Autowired
  private MockMvc mockMvc

  @Autowired
  private UserController userController

  @SpringBean
  private OnestopUserRepository onestopUserRepository = Mock()
  @SpringBean
  private OnestopRoleRepository onestopRoleRepository = Mock()
  @SpringBean
  private OnestopPrivilegeRepository onestopPrivilegeRepository = Mock()

  def "user controller exists and is protected from unauthenticated users"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/user/")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isUnauthorized())
  }

  @WithMockUser(roles = [SecurityConfig.PUBLIC_PRIVILEGE])
  def "public user can hit user endpoint"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/user/")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
  }

  @WithMockUser(roles = ["ADMIN"])
  def "admin user can hit user endpoint"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/user")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
  }

  @WithMockUser(roles = ["RANDO"])
  def "random user can't hit user endpoint"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/user")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isForbidden())
  }

  @WithMockUser(username = "new_user", roles = [SecurityConfig.PUBLIC_PRIVILEGE])
  def "user is created"() {
    when:
    def postSearch = mockMvc.perform(MockMvcRequestBuilders
        .post("/v1/user")
        .contentType("application/json")
        .content(('{ "name": "test"}'))
        .accept(MediaType.APPLICATION_JSON))

    then:
    onestopUserRepository.save(_ as OnestopUser) >> new OnestopUser("new_user") //>> new OnestopUser("new_user")

    postSearch.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").value("new_user"))

  }
}
