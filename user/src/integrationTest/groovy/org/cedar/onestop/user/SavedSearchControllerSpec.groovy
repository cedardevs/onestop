package org.cedar.onestop.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles('integration')
@SpringBootTest(classes = [UserApplication.class], webEnvironment = RANDOM_PORT)
class SavedSearchControllerSpec extends Specification {
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

  def "saved search denied"() {
    when:
    def postSearch = mvc.perform(MockMvcRequestBuilders
        .post("/v1/saved-search")
        .contentType("application/json")
        .content(('{ "userId": "u1", "name": "test", "value": "value" }'))
        .accept(MediaType.APPLICATION_JSON))

    then:
    postSearch.andExpect(MockMvcResultMatchers.status().isUnauthorized())
  }

  @WithMockUser(username = "mockMvcUser", roles = "PUBLIC")
  def "save search items, userId is taken from Authentication principal"() {
    when:
    ResultActions postOneResults = mvc.perform(MockMvcRequestBuilders
        .post("/v1/saved-search")
        .contentType("application/json")
        .content(('{ "userId": "userOne", "name": "testOne", "value": "valueOne" }'))
        .accept(MediaType.APPLICATION_JSON))

    ResultActions postTwoResults = mvc.perform(MockMvcRequestBuilders
        .post("/v1/saved-search")
        .contentType("application/json")
        .content(('{ "userId": "userOne", "name": "testTwo", "value": "valueTwo" }'))
        .accept(MediaType.APPLICATION_JSON))

    then:
    postOneResults.andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data.name").value("testOne"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data.value").value("valueOne"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data.userId").value("mockMvcUser")) //userId comes from authentication.getName(), payload is ignored

    postTwoResults.andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data.name").value("testTwo"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data.value").value("valueTwo"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data.userId").value("mockMvcUser"))

    when:
    def getResults = mvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search")
        .accept(MediaType.APPLICATION_JSON))

    then:
    getResults.andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].name").value("testOne"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].value").value("valueOne"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].userId").value("mockMvcUser")) //userId comes from authentication.getName(), payload is ignored
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[1].name").value("testTwo"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[1].value").value("valueTwo"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[1].userId").value("mockMvcUser")) //userId comes from authentication.getName(), payload is ignored
  }

}
