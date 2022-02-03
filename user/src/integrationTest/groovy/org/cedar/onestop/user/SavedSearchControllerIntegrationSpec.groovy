package org.cedar.onestop.user

import org.cedar.onestop.user.config.AuthorizationConfiguration
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
import spock.lang.Specification
//import groovy.json.JsonSlurper

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles(['integration'])
@SpringBootTest(classes = [UserApplication.class], webEnvironment = RANDOM_PORT)
class SavedSearchControllerIntegrationSpec extends Specification {

  @Autowired
  private WebApplicationContext context

  private MockMvc mvc

  def setup() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build()

    mvc.perform(MockMvcRequestBuilders
        .post("/v1/user")
        .contentType("application/json")
        .content(('{ "id":"mockMvcUser", "name": "test"}'))
        .accept(MediaType.APPLICATION_JSON))
  }

  def "unauthenticated user is NOT authorized and gets translated by controller advice"() {
    when: 'We make a request to a endpoint beyond our scope'
    def getResults = mvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search")
        .accept(MediaType.APPLICATION_JSON))

    then: 'we get the translated controller advice response'
    getResults.andExpect(MockMvcResultMatchers.status().isUnauthorized())
  }

  @WithMockUser(username = "mockMvcUser", roles = AuthorizationConfiguration.LIST_ALL_SAVED_SEARCHES)
  def "admin user authorized to admin getAll endpoint"() {
    when: 'We make a request to a endpoint NOT beyond our scope'
    def getResults = mvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search")
        .accept(MediaType.APPLICATION_JSON))

    then: 'we get 200'
    getResults.andExpect(MockMvcResultMatchers.status().isOk())
  }

  @WithMockUser(username = "mockMvcUser", roles = AuthorizationConfiguration.READ_OWN_PROFILE)
  def "public user cannot get saved searches belonging to other users"() {
    setup: 'must have a user to associate the search with'
    mvc.perform(MockMvcRequestBuilders
        .post("/v1/self")
        .contentType("application/json")
        .content(('{"id":"mockMvcUser"}'))
        .accept(MediaType.APPLICATION_JSON))

    when: 'We make a request to a endpoint beyond our scope'
    def getResults = mvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search/user/{id}", "unknownUser")
        .accept(MediaType.APPLICATION_JSON))

    then: 'we get the translated controller advice response'
    getResults.andExpect(MockMvcResultMatchers.status().isNotFound())
  }

  @WithMockUser(username = "mockMvcUser", roles = [AuthorizationConfiguration.UPDATE_SAVED_SEARCH, AuthorizationConfiguration.CREATE_SAVED_SEARCH, AuthorizationConfiguration.READ_SAVED_SEARCH])
  def "PUT save search items, user is taken from Principal"() {
    setup: 'must have a user to associate the search with'
    mvc.perform(MockMvcRequestBuilders
        .post("/v1/self")
        .contentType("application/json")
        .content(('{ "id":"mockMvcUser"}'))
        .accept(MediaType.APPLICATION_JSON))

    when: 'POST/CREATE saved searches'
    ResultActions postOneResults = mvc.perform(MockMvcRequestBuilders
        .post("/v1/self/saved-search")
        .contentType("application/json")
        .content('{ "name": "testOne", "value": "valueOne" }')
        .accept(MediaType.APPLICATION_JSON))

    then:
    postOneResults.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.name").value("testOne"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.value").value("valueOne"))

    when: 'PUT saved search'
    def getResults = mvc.perform(MockMvcRequestBuilders
        .put("/v1/self/saved-search/11")
        .contentType("application/json")
        .content('{ "name": "testTwo", "value": "valueOne" }')
        .accept(MediaType.APPLICATION_JSON))

    then:
    getResults.andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
  }

/*So added these tests but having trouble passing in correct params for methods that do findByIdAndUserId calls.
  @WithMockUser(username = "mockMvcUser", roles = [AuthorizationConfiguration.READ_OWN_PROFILE, AuthorizationConfiguration.CREATE_SAVED_SEARCH, AuthorizationConfiguration.READ_SAVED_SEARCH])
  def "POST and GET save search items, user is taken from Principal"() {
    setup: 'must have a user to associate the search with'
    mvc.perform(MockMvcRequestBuilders
        .post("/v1/self")
        .contentType("application/json")
        .content(('{ "id":"mockMvcUser"}'))
        .accept(MediaType.APPLICATION_JSON))

    when: 'POST/CREATE saved searches'
    ResultActions postOneResults = mvc.perform(MockMvcRequestBuilders
        .post("/v1/self/saved-search")
        .contentType("application/json")
        .content('{ "name": "testOne", "value": "valueOne" }')
        .accept(MediaType.APPLICATION_JSON))

    ResultActions postTwoResults = mvc.perform(MockMvcRequestBuilders
        .post("/v1/self/saved-search")
        .contentType("application/json")
        .content('{ "name": "testTwo", "value": "valueTwo" }')
        .accept(MediaType.APPLICATION_JSON))

    then:
    postOneResults.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.name").value("testOne"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.value").value("valueOne"))

    postTwoResults.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.name").value("testTwo"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.value").value("valueTwo"))

    when: 'GET saved search'
    def getResults = mvc.perform(MockMvcRequestBuilders
        .get("/v1/self/saved-search")
        .accept(MediaType.APPLICATION_JSON))

    then:
    getResults.andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data.length()").value(2))
        .andExpect((MockMvcResultMatchers.jsonPath("\$.data[*].attributes.name", Matchers.containsInAnyOrder("testOne", "testTwo"))))
        .andExpect((MockMvcResultMatchers.jsonPath("\$.data[*].attributes.value", Matchers.containsInAnyOrder("valueOne", "valueTwo"))))

    when: 'GET saved search'
    def jsonObj = new JsonSlurper().parseText(postOneResults.andReturn().getResponse().getContentAsString())
    System.out.println("jsonObj ${jsonObj}")
    String searchId = jsonObj.data.id
    getResults = mvc.perform(MockMvcRequestBuilders
        .get("/v1/self/saved-search/${searchId}")
        .accept(MediaType.APPLICATION_JSON))

    then:
    getResults.andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data.length()").value(2))
        .andExpect((MockMvcResultMatchers.jsonPath("\$.data[*].attributes.name", Matchers.containsInAnyOrder("testOne", "testTwo"))))
        .andExpect((MockMvcResultMatchers.jsonPath("\$.data[*].attributes.value", Matchers.containsInAnyOrder("valueOne", "valueTwo"))))
  }

  @WithMockUser(username = "mockMvcUser", roles = [AuthorizationConfiguration.DELETE_SAVED_SEARCH, AuthorizationConfiguration.CREATE_SAVED_SEARCH])
  def "DELETE save search item, user is taken from Principal"() {
    setup: 'must have a user to associate the search with'
    mvc.perform(MockMvcRequestBuilders
        .post("/v1/self")
        .contentType("application/json")
        .content(('{ "id":"mockMvcUser"}'))
        .accept(MediaType.APPLICATION_JSON))

    and: 'Save search'
    ResultActions postOneResults = mvc.perform(MockMvcRequestBuilders
        .post("/v1/self/saved-search")
        .contentType("application/json")
        .content('{ "name": "mockMvcUser", "value": "mockMvcUser" }')
        .accept(MediaType.APPLICATION_JSON))
    System.out.println("string ${postOneResults.andReturn().getResponse().getContentAsString()}")
    def jsonObj = new JsonSlurper().parseText(postOneResults.andReturn().getResponse().getContentAsString())
    System.out.println("jsonObj ${jsonObj}")
    String searchId = jsonObj.data.id
    System.out.println("response: " + postOneResults.andReturn().getResponse())
    System.out.println("id: " + searchId)

    postOneResults.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.name").value("mockMvcUser"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.value").value("mockMvcUser"))

    when: 'DELETE saved search'
    def deleteResults = mvc.perform(MockMvcRequestBuilders
        .delete("/v1/self/saved-search/${searchId}")
        .contentType("application/json")
        .accept(MediaType.APPLICATION_JSON))

    then:
    deleteResults.andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data.length()").value(1))
        .andExpect((MockMvcResultMatchers.jsonPath("\$.data.deleted", "TRUE")))
  }

  // How to do this test without a principal passed in? Think the @WithMockUser does that.
//  @WithMockUser(username = "mockMvcUser", roles = [AuthorizationConfiguration.DELETE_SAVED_SEARCH, AuthorizationConfiguration.CREATE_SAVED_SEARCH])
  def "DELETE save search item from id"() {
    setup: 'must have a user to associate the search with'
    mvc.perform(MockMvcRequestBuilders
        .post("/v1/self")
        .contentType("application/json")
        .content(('{ "id":"mockMvcUser"}'))
        .accept(MediaType.APPLICATION_JSON))

    and: 'Save search'
    ResultActions postOneResults = mvc.perform(MockMvcRequestBuilders
        .post("/v1/self/saved-search")
        .contentType("application/json")
        .content('{ "name": "testOne", "value": "valueOne" }')
        .accept(MediaType.APPLICATION_JSON))
    String searchId = MockMvcResultMatchers.jsonPath("\$.data[0].attributes.id")
    System.out.println("id: " + searchId)
    postOneResults.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.name").value("testOne"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.value").value("valueOne"))
//        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.id").value("testOne"))

    when: 'DELETE saved search'
    def deleteResults = mvc.perform(MockMvcRequestBuilders
        .delete("/v1/self/saved-search/${searchId}")
        .contentType("application/json")
//        .content('{ "name": "testOne", "value": "valueOne" }')
        .accept(MediaType.APPLICATION_JSON))

    then:
    deleteResults.andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data.length()").value(1))
        .andExpect((MockMvcResultMatchers.jsonPath("\$.data.deleted", "TRUE")))
  }*/
}