package org.cedar.onestop.controller

import org.cedar.onestop.user.controller.SaveSearchController
import org.cedar.onestop.user.repository.SaveSearchRepository
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

class SaveSearchControllerSpec extends Specification {

  private MockMvc mockMvc
  SaveSearchRepository mockSaveSearchRepository = Mock(SaveSearchRepository)
  SaveSearchController controller = new SaveSearchController(mockSaveSearchRepository)

  def setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
  }

  def "save search items "() {
    when:
    def postSearch = mockMvc.perform(MockMvcRequestBuilders
        .post("/api/v1/savesearches")
        .contentType("application/json")
        .content(('{ "userId": "u1", "name": "test", "value": "value" }'))
        .accept(MediaType.APPLICATION_JSON))

    then:
    postSearch.andExpect(MockMvcResultMatchers.status().isOk())
  }

  def "get save searches by id"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/api/v1/savesearches")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
  }

  def "bad request"() {
    when:
    def postSearch = mockMvc.perform(MockMvcRequestBuilders
        .post("/api/v1/savesearches")
        .contentType("application/json")
        .content(('{"name": "test", "value": "value" }'))
        .accept(MediaType.APPLICATION_JSON))

    then:
    postSearch.andExpect(MockMvcResultMatchers.status().isBadRequest())

  }

  def "get save searches by user id"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/api/v1/savesearches/user/{userId}", "u1")
        .accept(MediaType.APPLICATION_JSON))

    then:
    results.andExpect(MockMvcResultMatchers.status().isOk())
  }
}
