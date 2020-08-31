package org.cedar.onestop.user

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
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles('integration')
@SpringBootTest(classes = [UserApplication.class], webEnvironment = RANDOM_PORT)
class OnestopUserControllerSpec extends Specification  {

  private static final PostgreSQLContainer postgres = new PostgreSQLContainer()

  @Autowired
  private WebApplicationContext context

  private MockMvc mvc

  def setup() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build()

  }
  // Run before all the tests:
  def setupSpec() {
    postgres.start()
  }

  // Run after all the tests, even after failures:
  def cleanupSpec() {
    postgres.stop()
  }

//  @WithMockUser(username = "mockUser", roles = "PUBLIC")
//  def "public user can hit profile endpoint"() {
//    when:
//    def getResults = mvc.perform(MockMvcRequestBuilders
//        .get("/v1/user")
//        .accept(MediaType.APPLICATION_JSON))
//
//    then:
//    getResults.andExpect(MockMvcResultMatchers.status().isOk())
//        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
//        .andExpect(MockMvcResultMatchers.jsonPath("\$.data.userId").value("mockUser"))
//  }
//
  @WithMockUser(username = "mockUser", roles = "PUBLIC")
  def "user is created"() {
    when:
    def postSearch = mvc.perform(MockMvcRequestBuilders
        .post("/v1/user")
        .contentType("application/json")
        .content(('{ "name": "test"}'))
        .accept(MediaType.APPLICATION_JSON))

    then:
    postSearch.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").value("mockUser"))

  }

}
