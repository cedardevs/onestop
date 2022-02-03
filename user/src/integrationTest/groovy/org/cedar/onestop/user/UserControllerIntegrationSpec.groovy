package org.cedar.onestop.user

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.cedar.onestop.user.config.AuthorizationConfiguration
import org.cedar.onestop.user.domain.OnestopPrivilege
import org.cedar.onestop.user.domain.OnestopRole
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles('integration')
@SpringBootTest(classes = [UserApplication.class], webEnvironment = RANDOM_PORT)
class UserControllerIntegrationSpec extends Specification  {

  Logger logger = LoggerFactory.getLogger(UserControllerIntegrationSpec.class)

  @Autowired
  private WebApplicationContext context

  private MockMvc mvc

  JsonSlurper slurper = new JsonSlurper()

  def setup() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build()
  }

  @WithMockUser(username = "mockAdmin", roles = AuthorizationConfiguration.CREATE_USER)
  def "admin can create user"() {
    when:
    def postUser = mvc.perform(MockMvcRequestBuilders
        .post("/v1/user")
        .contentType("application/json")
        .content(('{"id": "test"}'))
        .accept(MediaType.APPLICATION_JSON))

    then:
    postUser.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").value("test"))
  }

  @WithMockUser(username = "mockMvcUser", roles = AuthorizationConfiguration.READ_OWN_PROFILE)
  def "user can get their own data"() {
    setup:
    mvc.perform(MockMvcRequestBuilders
        .post("/v1/self")
        .contentType("application/json")
        .content(('{ "id":"mockMvcUser"}'))
        .accept(MediaType.APPLICATION_JSON))

    when:
    def postUser = mvc.perform(MockMvcRequestBuilders
        .get("/v1/self")
        .contentType("application/json")
        .accept(MediaType.APPLICATION_JSON))

    then:
    postUser.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").value("mockMvcUser"))
  }

  @WithMockUser(username = "mockUser", roles = [AuthorizationConfiguration.READ_OWN_PROFILE, AuthorizationConfiguration.CREATE_SAVED_SEARCH])
  def "public user can initialize their own account and save a search"() {
    when:
    def postUser = mvc.perform(MockMvcRequestBuilders
        .post("/v1/self")
        .accept(MediaType.APPLICATION_JSON))

    then:
    postUser.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").value("mockUser"))

    when:
    def postSearch = mvc.perform(MockMvcRequestBuilders
        .post("/v1/self/saved-search")
        .contentType("application/json")
        .content(('{ "value": "test/test", "name" : "test search"}'))
        .accept(MediaType.APPLICATION_JSON))

    then:
    postSearch.andExpect(MockMvcResultMatchers.status().isCreated()).andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.value").value("test/test"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.name").value("test search"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.user.id").value("mockUser"))
  }

  @WithMockUser(username = "mockAdmin", roles = [AuthorizationConfiguration.CREATE_USER, AuthorizationConfiguration.CREATE_ROLE, AuthorizationConfiguration.CREATE_PRIVILEGE])
  def "user is created with roles and privileges"() {
    given:
    OnestopPrivilege readPriv = new OnestopPrivilege("read")
    OnestopPrivilege writePriv = new OnestopPrivilege("write")

    when:
    def postPriv1 = mvc.perform(MockMvcRequestBuilders
        .post("/v1/privilege")
        .contentType("application/json")
        .content(JsonOutput.toJson(readPriv.toMap()))
        .accept(MediaType.APPLICATION_JSON))

    def postPriv2 = mvc.perform(MockMvcRequestBuilders
        .post("/v1/privilege")
        .contentType("application/json")
        .content(JsonOutput.toJson(writePriv.toMap()))
        .accept(MediaType.APPLICATION_JSON))

    then:
    postPriv1.andExpect(MockMvcResultMatchers.status().isCreated()).andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.name").value("read"))

    postPriv2.andExpect(MockMvcResultMatchers.status().isCreated()).andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.name").value("write"))

    def priv1ResponseJson = postPriv1.andReturn().getResponse().getContentAsString()
    def priv2ResponseJson = postPriv2.andReturn().getResponse().getContentAsString()

    Map priv1Map = slurper.parseText(priv1ResponseJson)["data"][0]
    Map priv2Map = slurper.parseText(priv2ResponseJson)["data"][0]

    OnestopPrivilege savedReadPriv = new OnestopPrivilege(priv1Map["id"] as String, priv1Map["attributes"]["name"] as String)
    OnestopPrivilege savedWritePriv = new OnestopPrivilege(priv2Map["id"] as String, priv2Map["attributes"]["name"] as String)

    OnestopRole role = new OnestopRole("public",  Arrays.asList(savedReadPriv, savedWritePriv))

    and:
    def postRole = mvc.perform(MockMvcRequestBuilders
        .post("/v1/role")
        .contentType("application/json")
        .content(JsonOutput.toJson(role.toMap()))
        .accept(MediaType.APPLICATION_JSON))

    then:
    postRole.andExpect(MockMvcResultMatchers.status().isCreated()).andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.name").value("public"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.privileges[0].name").value("read"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.privileges[1].name").value("write"))


    def role1JsonString = postRole.andReturn().getResponse().getContentAsString()
    Map roleMap = slurper.parseText(role1JsonString)["data"][0]
    OnestopRole savedRole = new OnestopRole(roleMap.id as String, "public",  Arrays.asList(savedReadPriv, savedWritePriv))

    and:
    def postUser = mvc.perform(MockMvcRequestBuilders
        .post("/v1/user")
        .contentType("application/json")
        .content("{\"id\": \"mockUser\", \"roles\":[${JsonOutput.toJson(savedRole.toMap())}]}")
        .accept(MediaType.APPLICATION_JSON))

    then:
    postUser.andExpect(MockMvcResultMatchers.status().isCreated()).andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").value("mockUser"))
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].attributes.roles[0].name").value("public"))
  }
}