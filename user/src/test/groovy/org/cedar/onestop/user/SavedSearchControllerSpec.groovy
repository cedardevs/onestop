package org.cedar.onestop.user

import org.cedar.onestop.user.controller.SavedSearchController
import org.cedar.onestop.user.domain.OnestopUser
import org.cedar.onestop.user.repository.OnestopUserRepository
import org.cedar.onestop.user.repository.SavedSearchRepository
import org.cedar.onestop.user.domain.SavedSearch
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

@WebMvcTest(controllers = SavedSearchController.class)
class SavedSearchControllerSpec extends Specification {

  @Autowired
  private MockMvc mockMvc

  @SpringBean
  OnestopUserRepository mockUserRepository = Mock()
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
        '"createdOn":null}' +
        '}'
  @Shared
  String searchResultJsonString = '{"data":[' + searchResult1Json + ',' +
      '{"id":"' + search2.id + '",' +
      '"type":"' + SavedSearchController.type + '",' +
      '"attributes":{' +
      '"filter":"' + search2.filter + '",' +
      '"name":"' + search2.name + '",' +
      '"lastUpdatedOn":null,' +
      '"id":"' + search2.id + '",' +
      '"value":"' + search2.value + '",' +
      '"createdOn":null}' +
      '}' +
      '],' +
      '"meta":null,"status":200}'

  def setup(){
    mockUser.setSearches((Set)new ArrayList<SavedSearch>(savedSearches))
  }

  def "saved search endpoints are protected"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isUnauthorized())
    //todo make this work with controller advice - throws org.springframework.security.access.AccessDeniedException
//    results.andReturn().getResponse().getContentAsString() == """{"meta":null,"id":null,"status":"UNAUTHORIZED","code":"Unauthorized","title":null,"detail":null,"source":null}"""
    results.andReturn().getResponse().getContentAsString() == ""
  }

  @WithMockUser(username = 'new_search_user', roles = ["PUBLIC"])
  def "save search item for authenticated user"() {
    given:

    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .post("/v1/saved-search")
        .contentType("application/json")
        .content(('{"name": "test", "value": "value" }'))
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isCreated())
    1 * mockUserRepository.findById('new_search_user') >> Optional.of((OnestopUser)mockUser)
    1 * mockSaveSearchRepository.save(_) >> search1
    1 * mockUserRepository.save(mockUser)

//    1 * mockSaveSearchRepository.save(_ as SavedSearch) >> new SavedSearch( user, "new_search_user",  "test",  "filter",  "value")
    results.andReturn().getResponse().getContentAsString() == '{"data":[' +  searchResult1Json + '],"meta":null,"status":201}'
  }

  @WithMockUser(username = 'public_getter_by_id', roles = ["PUBLIC"])
  def "get save searches for authenticated user by id"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
    1 * mockUserRepository.findById('public_getter_by_id') >> Optional.of((OnestopUser)mockUser)
    results.andReturn().getResponse().getContentAsString() == searchResultJsonString
  }

//  def "bad request"() {
//    when:
//    def postSearch = mockMvc.perform(MockMvcRequestBuilders
//        .post("/v1/saved-search")
//        .contentType("application/json")
//        .content(('{"name": "test", "value": "value" }'))
//        .accept(MediaType.APPLICATION_JSON))
//
//    then:
//    postSearch.andExpect(MockMvcResultMatchers.status().isBadRequest())
//
//  }

  @WithMockUser(roles = ["ADMIN"])
  def "get save searches by user id"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search/user/{userId}", mockerUserId)
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
    1 * mockUserRepository.findById(mockerUserId) >> Optional.of((OnestopUser)mockUser)
    results.andReturn().getResponse().getContentAsString() == searchResultJsonString
  }

  @WithMockUser(roles = ["ADMIN"])
  def 'admin user can access saved-search/all'(){
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search/all")
        .accept(MediaType.APPLICATION_JSON))
    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
    1 * mockSaveSearchRepository.findAll() >> savedSearches
    results.andReturn().getResponse().getContentAsString() == searchResultJsonString
  }

  @WithMockUser(roles = ["PUBLIC"])
  def 'public user denied to protected endpoints'(){
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search/all")
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

  @WithMockUser(roles = ["ADMIN"])
  def "endpoint 'saved-search/all' returns json api spec response"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search/all")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
    1 * mockSaveSearchRepository.findAll() >> []
    results.andReturn().getResponse().getContentAsString() == """{"data":[],"meta":null,"status":200}"""
  }

  @WithMockUser(roles = ["ADMIN"])
  def "admin can hit saved-search/{id}"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search/{id}}", "1")
        .accept(MediaType.APPLICATION_JSON))

    then:
    1 * mockSaveSearchRepository.findById(_) >> Optional.of((SavedSearch) search1)
    results.andExpect(MockMvcResultMatchers.status().isOk())
    results.andReturn().getResponse().getContentAsString() == "{\"data\":[" + searchResult1Json + "],\"meta\":null,\"status\":200}"
  }
}