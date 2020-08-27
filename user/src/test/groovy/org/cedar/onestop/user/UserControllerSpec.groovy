package org.cedar.onestop.user

import org.cedar.onestop.user.controller.UserController
import org.cedar.onestop.user.repository.OnestopUserRepository
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

@WebMvcTest(controllers = UserController.class)
//@AutoConfigureMockMvc(addFilters = false)
class UserControllerSpec extends Specification {
//  private MockMvc mockMvc
  UserController controller = new UserController()

  @Autowired
  private MockMvc mockMvc

//  @Autowired
//  private ObjectMapper objectMapper;
//
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
