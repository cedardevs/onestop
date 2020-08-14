package org.cedar.onestop.controller

import org.cedar.onestop.user.controller.SavedSearchController
import org.cedar.onestop.user.repository.SavedSearchRepository
import org.cedar.onestop.user.service.SavedSearch
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

class SavedSearchControllerSpec extends Specification {

  private MockMvc mockMvc
  SavedSearchRepository mockSaveSearchRepository = Mock(SavedSearchRepository)
  SavedSearchController controller = new SavedSearchController(mockSaveSearchRepository)

  SavedSearch searchResult1 = new SavedSearch('1', '11', 'name1', 'filter1', 'value1')
  SavedSearch searchResult2 = new SavedSearch('2', '12', 'name2', 'filter2', 'value2')

  def setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
  }

  def "save search items "() {
    when:
    def postSearch = mockMvc.perform(MockMvcRequestBuilders
        .post("/v1/saved-search")
        .contentType("application/json")
        .content(('{ "userId": "u1", "name": "test", "value": "value" }'))
        .accept(MediaType.APPLICATION_JSON))

    then:
    postSearch.andExpect(MockMvcResultMatchers.status().isUnauthorized())
  }

  def "get save searches by id"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isUnauthorized())
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

  def "get save searches by user id"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search/user/{userId}", "u1")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isUnauthorized())
  }

  def "get saved search all returns empty data response"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search/all")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
    String content = results.andReturn().getResponse().getContentAsString()
    content == """{"data":[],"meta":null}"""
  }

  def "getAll() returns expected Json Api Spec response for multiple results"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search/all")
        .accept(MediaType.APPLICATION_JSON))

    then: 'Return conforms to Json Api Spec'
    1 * mockSaveSearchRepository.findAll() >> [searchResult1, searchResult2]

    results.andExpect(MockMvcResultMatchers.status().isOk())
    String content = results.andReturn().getResponse().getContentAsString()
    content == '{"data":[{"id":"' + searchResult1.id + '",' +
        '"type":"' + SavedSearchController.type + '",' +
        '"attributes":{' +
        '"filter":"'+ searchResult1.filter + '",' +
        '"name":"'+ searchResult1.name + '",' +
        '"lastUpdatedOn":null,' +
        '"id":"'+ searchResult1.id + '",' +
        '"userId":"'+ searchResult1.userId + '",' +
        '"value":"'+ searchResult1.value + '",' +
        '"createdOn":null}' +
        '},' +
        '{"id":"' + searchResult2.id + '",' +
        '"type":"' + SavedSearchController.type + '",' +
        '"attributes":{' +
        '"filter":"'+ searchResult2.filter + '",' +
        '"name":"'+ searchResult2.name + '",' +
        '"lastUpdatedOn":null,' +
        '"id":"'+ searchResult2.id + '",' +
        '"userId":"'+ searchResult2.userId + '",' +
        '"value":"'+ searchResult2.value + '",' +
        '"createdOn":null}' +
        '}' +
        '],' +
        '"meta":null}'
  }
}
