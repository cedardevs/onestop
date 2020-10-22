package org.cedar.onestop.user

import org.cedar.onestop.user.config.AuthorizationConfiguration
import org.cedar.onestop.user.controller.SavedSearchController
import org.cedar.onestop.user.domain.OnestopUser
import org.cedar.onestop.user.domain.SavedSearch
import org.cedar.onestop.user.repository.SavedSearchRepository
import org.cedar.onestop.user.service.OnestopUserService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Shared
import spock.lang.Specification

@WebMvcTest(controllers = SavedSearchController.class)
class SavedSearchControllerSpec extends Specification {

  @Autowired
  private MockMvc mockMvc

  @SpringBean
  private OnestopUserService mockUserService = Mock()
  @SpringBean
  SavedSearchRepository mockSaveSearchRepository = Mock()
  @Shared
  OnestopUser user = new OnestopUser("abc123")
  @Shared
  SavedSearch search1 = new SavedSearch(user, '1', 'name1', 'filter1', 'value1')
  @Shared
  SavedSearch search2 = new SavedSearch(user, '2', 'name2', 'filter2', 'value2')
  @Shared
  List <SavedSearch> savedSearches = [search1, search2]
  @Shared
  String mockerUserId = "abc123"
  @Shared
  OnestopUser mockUser = new OnestopUser(mockerUserId)

  @Shared
  String searchResult1Json = '{"id":"' + search1.id + '",' +
    '"type":"' + SavedSearchController.type + '",' +
        '"attributes":{' +
        '"filter":"' + search1.filter + '",' +
        '"name":"' + search1.name + '",' +
        '"lastUpdatedOn":null,' +
        '"id":"' + search1.id + '",' +
        '"value":"' + search1.value + '",' +
        '"user":{"roles":[],"lastUpdatedOn":null,"id":"' + user.id + '","createdOn":null},' +
        '"createdOn":null}' +
        '}'
  @Shared
  String searchResult2Json = '{"data":[' + searchResult1Json + ',' +
      '{"id":"' + search2.id + '",' +
      '"type":"' + SavedSearchController.type + '",' +
      '"attributes":{' +
      '"filter":"' + search2.filter + '",' +
      '"name":"' + search2.name + '",' +
      '"lastUpdatedOn":null,' +
      '"id":"' + search2.id + '",' +
      '"value":"' + search2.value + '",' +
      '"user":{"roles":[],"lastUpdatedOn":null,"id":"' + user.id + '","createdOn":null},' +
      '"createdOn":null}' +
      '}' +
      '],' +
      '"meta":null,"status":200}'

  def setup(){
    mockUser.setSearches(new ArrayList<SavedSearch>(savedSearches))
  }

  def "saved search endpoints are protected"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isUnauthorized())
    results.andExpect(MockMvcResultMatchers.jsonPath("\$.errors").isArray())
  }

  @WithMockUser(username = 'new_search_user', roles = [AuthorizationConfiguration.CREATE_SAVED_SEARCH])
  def "save search item for authenticated user"() {
    given:

    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .post("/v1/self/saved-search")
        .contentType("application/json")
        .content(('{"name": "test", "value": "value" }'))
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isCreated())
    1 * mockUserService.findById('new_search_user') >> Optional.of(mockUser)
    1 * mockSaveSearchRepository.save(_) >> search1

    results.andReturn().getResponse().getContentAsString() == '{"data":[' +  searchResult1Json + '],"meta":null,"status":201}'
  }

  @WithMockUser(username = 'public_getter_by_id', roles = [AuthorizationConfiguration.READ_SAVED_SEARCH])
  def "get save searches for authenticated user by id"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/self/saved-search")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
    1 * mockSaveSearchRepository.findByUserId('public_getter_by_id', _ as Pageable) >> new PageImpl(savedSearches)
    results.andReturn().getResponse().getContentAsString() == searchResult2Json
  }

  @WithMockUser(roles = [AuthorizationConfiguration.LIST_ALL_SAVED_SEARCHES])
  def "get save searches by user id"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search").param("userId", mockerUserId)
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
    1 * mockSaveSearchRepository.findByUserId(mockerUserId, _ as Pageable) >> new PageImpl<SavedSearch>([search1, search2])
    results.andReturn().getResponse().getContentAsString() == searchResult2Json
  }

  @WithMockUser(roles = [AuthorizationConfiguration.LIST_ALL_SAVED_SEARCHES])
  def 'admin user can access saved-search'(){
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search")
        .accept(MediaType.APPLICATION_JSON))
    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
    1 * mockSaveSearchRepository.findAll(_ as Pageable) >> new PageImpl(savedSearches)
    results.andReturn().getResponse().getContentAsString() == searchResult2Json
  }

  @WithMockUser(roles = [AuthorizationConfiguration.PUBLIC_ROLE])
  def 'public user denied to protected endpoints'(){
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search")
        .accept(MediaType.APPLICATION_JSON))
    then:
    results.andExpect(MockMvcResultMatchers.status().isForbidden())

    when:
    def result2 = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search/{id}}", "1")
        .accept(MediaType.APPLICATION_JSON))

    then:
    result2.andExpect(MockMvcResultMatchers.status().isForbidden())
  }

  @WithMockUser(roles = [AuthorizationConfiguration.LIST_ALL_SAVED_SEARCHES])
  def "endpoint 'saved-search' returns json api spec response"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
    1 * mockSaveSearchRepository.findAll(_ as Pageable) >> new PageImpl([])
    results.andReturn().getResponse().getContentAsString() == """{"data":[],"meta":null,"status":200}"""
  }

  @WithMockUser(roles = [AuthorizationConfiguration.READ_SAVED_SEARCH_BY_ID])
  def "admin can hit saved-search/{id}"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search/{id}}", "1-2-3")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
    1 * mockSaveSearchRepository.findById(_) >> Optional.of(search1)
    results.andReturn().getResponse().getContentAsString() == "{\"data\":[" + searchResult1Json + "],\"meta\":null,\"status\":200}"
  }

}