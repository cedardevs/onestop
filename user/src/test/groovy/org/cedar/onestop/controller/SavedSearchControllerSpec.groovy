package org.cedar.onestop.controller

import org.cedar.onestop.user.controller.SavedSearchController
import org.cedar.onestop.user.repository.SavedSearchRepository
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

//  def "access denied to saved search"() {
//    when:
//    def results = mockMvc.perform(MockMvcRequestBuilders
//        .get("/v1/saved-search/user")
//        .accept(MediaType.APPLICATION_JSON))
//
//    then:
//    results.andExpect(MockMvcResultMatchers.status().isUnauthorized())
//  }

}
