package org.cedar.onestop.user

import org.cedar.onestop.user.config.AuthorizationConfiguration
import org.cedar.onestop.user.domain.OnestopUser
import org.cedar.onestop.user.service.OnestopUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles('integration')
@SpringBootTest(classes = [UserApplication.class], webEnvironment = RANDOM_PORT)
class RoleControllerIntegrationSpec extends Specification {

  static final testUserId = 'ABC'

  @Autowired
  WebApplicationContext context

  @Autowired
  OnestopUserService userService

  MockMvc mvc
  OnestopUser testUser

  def setup() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build()

    testUser = userService.findOrCreateUser(testUserId)
  }

  @WithMockUser(username = "mockAdmin", roles = AuthorizationConfiguration.READ_ROLES_BY_USER_ID)
  def "admin can list roles"() {
    when:
    def getRequest = mvc.perform(MockMvcRequestBuilders
        .get("/v1/role")
        .accept(MediaType.APPLICATION_JSON))

    then:
    getRequest
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").isString())
  }

  @WithMockUser(username = "mockAdmin", roles = AuthorizationConfiguration.READ_ROLES_BY_USER_ID)
  def "admin can list roles by user"() {
    when:
    def getRequest = mvc.perform(MockMvcRequestBuilders
        .get("/v1/role").param("userId", testUser.id)
        .accept(MediaType.APPLICATION_JSON))

    then:
    getRequest
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").isString())
  }

}
