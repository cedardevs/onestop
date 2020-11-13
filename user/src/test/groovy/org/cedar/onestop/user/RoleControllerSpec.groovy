package org.cedar.onestop.user

import org.cedar.onestop.user.config.AuthorizationConfiguration
import org.cedar.onestop.user.controller.RoleController
import org.cedar.onestop.user.domain.OnestopRole
import org.cedar.onestop.user.repository.OnestopRoleRepository
import org.cedar.onestop.user.service.OnestopUserService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
@WebMvcTest(controllers = RoleController.class)
class RoleControllerSpec extends Specification{

  @Autowired
  private MockMvc mockMvc

  @Autowired
  private RoleController roleController

  @SpringBean
  private OnestopUserService onestopUserService = Mock()
  @SpringBean
  private OnestopRoleRepository onestopRoleRepository = Mock()

  @Shared
  String roleId = "auto-generated-uuid"
  @Shared
  String roleName = "test_role"
  @Shared
  String roleJsonString = "{\"id\":\"${roleId}\", \"name\": \"${roleName}\"}" as String
  @Shared
  OnestopRole testRole = new OnestopRole(roleId, roleName)

  @WithMockUser(username = "admin_user_roles", roles = [AuthorizationConfiguration.READ_ROLES_BY_USER_ID])
  def "admin user can hit roles endpoint"() {
    when:
    def getResults = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/role")
        .accept(MediaType.APPLICATION_JSON))

    then:
    1 * onestopRoleRepository.findAll(_) >> new PageImpl<OnestopRole>([testRole])
    getResults.andExpect(MockMvcResultMatchers.status().isOk())
  }

  @WithMockUser(username = "admin_user_roles", roles = [AuthorizationConfiguration.READ_ROLES_BY_USER_ID])
  def "admin user can get second page of roles"() {
    def pageable = PageRequest.of(2, 1)

    when:
    def getResults = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/role")
        .param("page", pageable.getPageNumber().toString())
        .param("size", pageable.getPageSize().toString())
        .accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())

    then:
    1 * onestopRoleRepository.findAll(pageable) >> new PageImpl<OnestopRole>([testRole], pageable, 2)
    getResults.andExpect(MockMvcResultMatchers.status().isOk())
  }

  @WithMockUser(username = "admin_user_roles", roles = [AuthorizationConfiguration.READ_ROLES_BY_USER_ID])
  def "admin user can get roles for a single user"() {
    def userId = 'ABC'

    when:
    def getResults = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/role")
        .param("userId", userId)
        .accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())

    then:
    1 * onestopRoleRepository.findByUsersId(userId, _ as Pageable) >> new PageImpl<OnestopRole>([testRole])
    getResults.andExpect(MockMvcResultMatchers.status().isOk())
  }

  @WithMockUser(username = "admin_user_roles", roles = [AuthorizationConfiguration.READ_ROLES_BY_USER_ID])
  def "admin user can get role by id"() {
    given:
    String id = "admin_id"
    when:
    def getResults = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/role/{id}", id)
        .accept(MediaType.APPLICATION_JSON))

    then:
    1 * onestopRoleRepository.findById(id) >> Optional.of(testRole)
    getResults.andExpect(MockMvcResultMatchers.status().isOk())
  }


  @WithMockUser(username = "role_hacker", roles = ["BOGUS_PRIV"])
  def "public user not authorized to hit role endpoint"() {
    when:
    def getResults = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/role/{id}", "hacker_id")
        .accept(MediaType.APPLICATION_JSON))

    then:
    getResults.andExpect(MockMvcResultMatchers.status().isForbidden())
  }

  @WithMockUser(roles = [AuthorizationConfiguration.CREATE_ROLE])
  def "role is created"() {
    when:
    def postSearch = mockMvc.perform(MockMvcRequestBuilders
        .post("/v1/role")
        .contentType("application/json")
        .content(roleJsonString)
        .accept(MediaType.APPLICATION_JSON))

    then:
    1 * onestopRoleRepository.save(_ as OnestopRole) >> testRole

    postSearch.andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").value(roleId))
  }

  @WithMockUser(roles = [AuthorizationConfiguration.DELETE_ROLE])
  def 'role is deleted'(){
    when:
    def deleteResult = mockMvc.perform(MockMvcRequestBuilders.delete("/v1/role/{id}", roleId))

    then:
    deleteResult.andExpect(MockMvcResultMatchers.status().isOk())
    1 * onestopRoleRepository.findById(_ as String) >> Optional.of(testRole)
    1 * onestopRoleRepository.delete(_ as OnestopRole)
    deleteResult.andExpect(MockMvcResultMatchers.jsonPath("\$.meta.nonStandardMetadata.deleted").value(true))
  }

  @WithMockUser(roles = [AuthorizationConfiguration.READ_OWN_PROFILE])
  def 'public user can get own roles'(){
    when:
    def result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/self/role"))

    then:
    1 * onestopRoleRepository.findByUsersId(_, _) >> new PageImpl<OnestopRole>([testRole])

    result.andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").value(testRole.id))
  }

  @WithMockUser(roles = [AuthorizationConfiguration.READ_OWN_PROFILE])
  def 'public user cannot #action roles'(){
    expect:
    mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().is(403))

    where:
    action  | request
    'list'  | MockMvcRequestBuilders.get("/v1/role")
    'create'| MockMvcRequestBuilders.post("/v1/role").content('{}').contentType('application/json')
    'get'   | MockMvcRequestBuilders.get("/v1/role/$roleId")
//    'update'| MockMvcRequestBuilders.put("/v1/role/$roleId")
    'delete'| MockMvcRequestBuilders.delete("/v1/role/$roleId")
  }

}
