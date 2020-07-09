package org.cedar.onestop.controller

import org.cedar.onestop.user.controller.UserController
import org.cedar.onestop.user.repository.OnestopUserRepository
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

class UserControllerSpec extends Specification {
  private MockMvc mockMvc
  UserController controller = new UserController()

  def setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
  }

  def "user controller exists and is protected"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/user/")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isUnauthorized())
  }

}
