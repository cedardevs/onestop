package org.cedar.onestop.user

import org.cedar.onestop.user.config.SecurityConfig
import org.cedar.onestop.user.controller.RoleController
import org.cedar.onestop.user.domain.OnestopRole
import org.cedar.onestop.user.domain.OnestopUser
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
import spock.lang.Shared
import spock.lang.Specification

@WebMvcTest(controllers = RoleController.class)
class RoleControllerSpec extends Specification{

  @Autowired
  private MockMvc mockMvc

  @Autowired
  private RoleController roleController

  @SpringBean
  private OnestopUserService onestopUserService = Mock()
  @SpringBean
  private OnestopUserRepository onestopUserRepository = Mock()
  @SpringBean
  private OnestopRoleRepository onestopRoleRepository = Mock()

  @Shared
  String roleId = "auto-generated-uuid"
  @Shared
  String roleName = "test_role"
  @Shared
  String roleJsonString = "{\"id\":\"${roleId}\", \"name\": \"${roleName}\"}" as String
  @Shared
  OnestopRole mockRole = new OnestopRole(roleId, roleName)

  @WithMockUser(username = "admin_user_roles", roles = ["ADMIN"])
  def "admin user can hit role endpoint"() {
    given:
    String id = "admin_id"
    when:
    def getResults = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/role/{id}", id)
        .accept(MediaType.APPLICATION_JSON))

    then:
    1 * onestopUserRepository.findById(id) >>  Optional.of((OnestopUser) new OnestopUser("admin_id"))

    getResults.andExpect(MockMvcResultMatchers.status().isOk())
  }


  @WithMockUser(username = "role_hacker", roles = [SecurityConfig.PUBLIC_PRIVILEGE])
  def "public user not authorized to hit role endpoint"() {
    when:
    def getResults = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/role/{id}", "hacker_id")
        .accept(MediaType.APPLICATION_JSON))

    then:
    getResults.andExpect(MockMvcResultMatchers.status().isForbidden())
  }

  @WithMockUser(roles = [SecurityConfig.ADMIN_PRIVILEGE])
  def "role is created"() {
    when:
    def postSearch = mockMvc.perform(MockMvcRequestBuilders
        .post("/v1/role")
        .contentType("application/json")
        .content(roleJsonString)
        .accept(MediaType.APPLICATION_JSON))

    then:
    1 * onestopRoleRepository.save(_ as OnestopRole) >> new OnestopRole(roleId, roleName) //>> new OnestopUser("new_user")

    postSearch.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").value(roleId))
  }

  @WithMockUser(roles = [SecurityConfig.ADMIN_PRIVILEGE])
  def 'role is deleted'(){
    when:
    def deleteResult = mockMvc.perform(MockMvcRequestBuilders.delete("/v1/role/{id}", roleId))

    then:
    deleteResult.andExpect(MockMvcResultMatchers.status().isOk())
    1 * onestopRoleRepository.findById(_ as String) >> Optional.of((OnestopRole)mockRole)
    1 * onestopRoleRepository.delete(_ as OnestopRole)
    deleteResult.andExpect(MockMvcResultMatchers.jsonPath("\$.meta.nonStandardMetadata.deleted").value(true))
  }

}
