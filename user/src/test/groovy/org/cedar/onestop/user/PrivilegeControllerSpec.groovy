package org.cedar.onestop.user

import org.cedar.onestop.user.config.AuthorizationConfiguration
import org.cedar.onestop.user.controller.PrivilegeController
import org.cedar.onestop.user.domain.OnestopPrivilege
import org.cedar.onestop.user.repository.OnestopPrivilegeRepository
import org.cedar.onestop.user.repository.OnestopRoleRepository
import org.cedar.onestop.user.repository.OnestopUserRepository
import org.cedar.onestop.user.service.OnestopUserService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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
@WebMvcTest(controllers = PrivilegeController.class)
class PrivilegeControllerSpec extends Specification{

  @Autowired
  private MockMvc mockMvc

  @Autowired
  private PrivilegeController privilegeController

  @SpringBean
  OnestopPrivilegeRepository onestopPrivilegeRepository = Mock()
  @SpringBean
  OnestopUserService onestopUserService =
      new OnestopUserService(Mock(OnestopUserRepository), Mock(OnestopRoleRepository), onestopPrivilegeRepository)

  @Shared
  String privilegeId = "auto-generated-uuid"
  @Shared
  String privilegeName = "test_privilege"
  @Shared
  String privilegeJsonString = "{\"id\":\"${privilegeId}\", \"name\": \"${privilegeName}\"}" as String
  @Shared
  OnestopPrivilege mockPrivilege = new OnestopPrivilege(privilegeId, privilegeName)

  @WithMockUser(username = "admin_user_privileges", roles = [AuthorizationConfiguration.READ_PRIVILEGE_BY_USER_ID])
  def "admin user can list privileges with #filters"() {
    when:
    def builder = MockMvcRequestBuilders.get("/v1/privilege").accept(MediaType.APPLICATION_JSON)
    if (userId) {
      builder = builder.param("userId", userId)
    }
    if (roleId) {
      builder = builder.param("roleId", roleId)
    }
    def getResults = mockMvc.perform(builder)

    then:
    1 * onestopPrivilegeRepository."$repoMethod"(*_) >>
        new PageImpl<OnestopPrivilege>([new OnestopPrivilege(id: 'abc', name: 'yep')])

    getResults.andExpect(MockMvcResultMatchers.status().isOk())

    where:
    filters         | userId  | roleId  | repoMethod
    "no filter"     | null    | null    | 'findAll'
    "user"          | 'ABC'   | null    | 'findByRolesUsersId'
    "role"          | null    | '123'   | 'findByRolesId'
    "user and role" | 'ABC'   | '123'   | 'findByRolesIdAndRolesUsersId'
  }

  @WithMockUser(username = "admin_user_privileges", roles = [AuthorizationConfiguration.READ_PRIVILEGE_BY_USER_ID])
  def "admin user can get second page of privileges"() {
    def pageable = PageRequest.of(2, 1)

    when:
    def getResults = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/privilege")
        .param("page", pageable.getPageNumber().toString())
        .param("size", pageable.getPageSize().toString())
        .accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())

    then:
    1 * onestopPrivilegeRepository.findAll(pageable) >>
        new PageImpl<OnestopPrivilege>([new OnestopPrivilege(id: 'abc', name: 'wow')], pageable, 2)

    getResults.andExpect(MockMvcResultMatchers.status().isOk())
  }

  @WithMockUser(username = "admin_user_privileges", roles = [AuthorizationConfiguration.READ_PRIVILEGE_BY_USER_ID])
  def "admin user can retrieve a privilege by id"() {
    def id = 'abc'

    when:
    def getResults = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/privilege/{id}", id)
        .accept(MediaType.APPLICATION_JSON))

    then:
    1 * onestopPrivilegeRepository.findById(_) >> Optional.of(new OnestopPrivilege(id: id, name: 'woo'))

    getResults.andExpect(MockMvcResultMatchers.status().isOk())
  }

  @WithMockUser(username = "privilege_hacker", roles = ["BOGUS_PRIV"])
  def "public user not authorized to hit privilege endpoint"() {
    when:
    def getResults = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/privilege")
        .accept(MediaType.APPLICATION_JSON))

    then:
    getResults.andExpect(MockMvcResultMatchers.status().isForbidden())
  }

  @WithMockUser(roles = [AuthorizationConfiguration.CREATE_PRIVILEGE])
  def "privilege is created"() {
    when:
    def postSearch = mockMvc.perform(MockMvcRequestBuilders
        .post("/v1/privilege")
        .contentType("application/json")
        .content(privilegeJsonString)
        .accept(MediaType.APPLICATION_JSON))

    then:
    postSearch.andExpect(MockMvcResultMatchers.status().isCreated())
    1 * onestopPrivilegeRepository.save(_ as OnestopPrivilege) >> new OnestopPrivilege(privilegeId, privilegeName) //>> new OnestopUser("new_user")

    postSearch.andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").value(privilegeId))
  }

  @WithMockUser(roles = [AuthorizationConfiguration.DELETE_PRIVILEGE])
  def 'privilege is deleted'(){
    when:
    def deleteResult = mockMvc.perform(MockMvcRequestBuilders.delete("/v1/privilege/{id}", privilegeId))

    then:
    deleteResult.andExpect(MockMvcResultMatchers.status().isOk())
    1 * onestopPrivilegeRepository.findById(_ as String) >> Optional.of(mockPrivilege)
    1 * onestopPrivilegeRepository.delete(_ as OnestopPrivilege)
    deleteResult.andExpect(MockMvcResultMatchers.jsonPath("\$.meta.nonStandardMetadata.deleted").value(true))
  }

  @WithMockUser(roles = [AuthorizationConfiguration.READ_OWN_PROFILE])
  def 'public user can get own privileges'(){
    def testPrivilege = new OnestopPrivilege('abc', 'yep')

    when:
    def result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/self/privilege"))

    then:
    1 * onestopPrivilegeRepository.findByRolesUsersId(_, _) >> new PageImpl<OnestopPrivilege>([testPrivilege])

    result.andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("\$.data[0].id").value(testPrivilege.id))
  }

  @WithMockUser(roles = [AuthorizationConfiguration.READ_OWN_PROFILE])
  def 'public user cannot #action privileges'(){
    expect:
    mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().is(403))

    where:
    action  | request
    'list'  | MockMvcRequestBuilders.get("/v1/privilege")
    'create'| MockMvcRequestBuilders.post("/v1/privilege").content(privilegeJsonString).contentType('application/json')
    'get'   | MockMvcRequestBuilders.get("/v1/privilege/$privilegeId")
//    'update'| MockMvcRequestBuilders.put("/v1/privilege/$privilegeId")
    'delete'| MockMvcRequestBuilders.delete("/v1/privilege/$privilegeId")
  }

}
