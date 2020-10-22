package org.cedar.onestop.user

import org.cedar.onestop.user.config.AuthorizationConfiguration
import org.cedar.onestop.user.controller.UserController
import org.cedar.onestop.user.repository.OnestopPrivilegeRepository
import org.cedar.onestop.user.repository.OnestopRoleRepository
import org.cedar.onestop.user.repository.OnestopUserRepository
import org.cedar.onestop.user.service.OnestopUserService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
@WebMvcTest(controllers = UserController.class)
class ExceptionHandlerSpec extends Specification {

  @Autowired
  private MockMvc mockMvc

  @Autowired
  private UserController userController

  @SpringBean
  private OnestopUserRepository mockUserRepository = Mock()
  @SpringBean
  private OnestopPrivilegeRepository mockPrivilegeRepository = Mock()
  @SpringBean
  private OnestopRoleRepository mockRoleRepository = Mock()
  @SpringBean
  private OnestopUserService onestopUserService =
      new OnestopUserService(mockUserRepository, mockRoleRepository, mockPrivilegeRepository)

  def "unauthenticated requests result in json api response body"() {
    expect:
    mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/user")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.errors").isArray())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.errors[0].title").isString())
  }

  @WithMockUser(roles = [AuthorizationConfiguration.READ_OWN_PROFILE])
  def "authenticated user w/o correct permissions results in json api response body"() {
    expect:
    mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/user")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isForbidden())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.errors").isArray())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.errors[0].title").isString())
  }

  @WithMockUser(roles = [AuthorizationConfiguration.UPDATE_USER])
  def "request for unknown resource results in json api response body"() {
    def id = 'ABC'
    _ * mockUserRepository.existsById(id) >> false

    expect:
    mockMvc.perform(MockMvcRequestBuilders
        .put("/v1/user/{id}", id)
        .content('{}')
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isNotFound())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.errors").isArray())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.errors[0].title").isString())
  }

  @WithMockUser(roles = [AuthorizationConfiguration.READ_OWN_PROFILE])
  def "request that causes internal error results in json api response body"() {
    _ * mockUserRepository.findAll(_) >> { throw new RuntimeException() }

    expect:
    mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/user")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isForbidden())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.errors").isArray())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.errors[0].title").isString())
  }

  @WithMockUser(roles = [AuthorizationConfiguration.READ_USER])
  def "general spring mvc error results in json api response body"() {
    expect:
    mockMvc.perform(MockMvcRequestBuilders
        .delete("/v1/user") // <-- delete not allowed for /user
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isMethodNotAllowed())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.errors").isArray())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.errors[0].title").isString())
  }

}
