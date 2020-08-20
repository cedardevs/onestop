package org.cedar.onestop.controller

import org.cedar.onestop.user.controller.SavedSearchController
import org.cedar.onestop.user.repository.SavedSearchRepository
import org.cedar.onestop.user.service.SavedSearch
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class SavedSearchControllerSpec extends Specification {

  private MockMvc mockMvc
  SavedSearchRepository mockSaveSearchRepository = Mock(SavedSearchRepository)
  SavedSearchController controller = new SavedSearchController(mockSaveSearchRepository)

  @Shared
  SavedSearch searchResult1 = new SavedSearch('1', '11', 'name1', 'filter1', 'value1')
  @Shared
  SavedSearch searchResult2 = new SavedSearch('2', '12', 'name2', 'filter2', 'value2')

  def setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
  }

  def "save search items "() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .post("/v1/saved-search")
        .contentType("application/json")
        .content(('{ "userId": "u1", "name": "test", "value": "value" }'))
        .accept(MediaType.APPLICATION_JSON))

    then:
//    results.andExpect(MockMvcResultMatchers.status().isUnauthorized())
    results.andReturn().getResponse().getContentAsString() == """{"meta":null,"id":null,"status":"UNAUTHORIZED","code":"Unauthorized","title":null,"detail":null,"source":null}"""
  }

  def "get save searches by id"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search")
        .accept(MediaType.APPLICATION_JSON))

    then:
//    results.andExpect(MockMvcResultMatchers.status().isUnauthorized())
    results.andReturn().getResponse().getContentAsString() == """{"meta":null,"id":null,"status":"UNAUTHORIZED","code":"Unauthorized","title":null,"detail":null,"source":null}"""
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
//    results.andExpect(MockMvcResultMatchers.status().isUnauthorized())
    results.andReturn().getResponse().getContentAsString() == """{"meta":null,"id":null,"status":"UNAUTHORIZED","code":"Unauthorized","title":null,"detail":null,"source":null}"""
  }

  @Unroll
  def "endpoint 'saved-search/all' returns json api spec response for repository response of #mockRepoResponse"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search/all")
        .accept(MediaType.APPLICATION_JSON))

    then:
    1 * mockSaveSearchRepository.findAll() >> mockRepoResponse
    results.andExpect(MockMvcResultMatchers.status().isOk())
    results.andReturn().getResponse().getContentAsString() == expResponse

    where:
    mockRepoResponse               | expResponse
    []                             | """{"data":[],"meta":null}"""
    [searchResult1, searchResult2] | '{"data":[{"id":"' + searchResult1.id + '",' +
                                    '"type":"' + SavedSearchController.type + '",' +
                                    '"attributes":{' +
                                    '"filter":"' + searchResult1.filter + '",' +
                                    '"name":"' + searchResult1.name + '",' +
                                    '"lastUpdatedOn":null,' +
                                    '"id":"' + searchResult1.id + '",' +
                                    '"userId":"' + searchResult1.userId + '",' +
                                    '"value":"' + searchResult1.value + '",' +
                                    '"createdOn":null}' +
                                    '},' +
                                    '{"id":"' + searchResult2.id + '",' +
                                    '"type":"' + SavedSearchController.type + '",' +
                                    '"attributes":{' +
                                    '"filter":"' + searchResult2.filter + '",' +
                                    '"name":"' + searchResult2.name + '",' +
                                    '"lastUpdatedOn":null,' +
                                    '"id":"' + searchResult2.id + '",' +
                                    '"userId":"' + searchResult2.userId + '",' +
                                    '"value":"' + searchResult2.value + '",' +
                                    '"createdOn":null}' +
                                    '}' +
                                    '],' +
                                    '"meta":null}'
  }

  @Unroll
  def "endpoint (update) 'saved-search/{id}' returns json api spec response for repository response of #mockRepoResponse"() {
    when:
    def results = mockMvc.perform(MockMvcRequestBuilders
        .get("/v1/saved-search/all")
        .content(('{ "userId": "' + searchResult1.userId + '" }'))
        .accept(MediaType.APPLICATION_JSON))

    then:
    1 * mockSaveSearchRepository.findAll() >> mockRepoResponse
    results.andExpect(MockMvcResultMatchers.status().isOk())
    results.andReturn().getResponse().getContentAsString() == expResponse

    where:
    params                                         | mockRepoResponse | expResponse
    '{}'                                           | []               | """{"data":[],"meta":null}"""
    '{ "userId": "' + searchResult1.userId + '" }' | [searchResult1]  | '{"data":[{"id":"' + searchResult1.id + '",' +
                                                                        '"type":"' + SavedSearchController.type + '",' +
                                                                        '"attributes":{' +
                                                                        '"filter":"' + searchResult1.filter + '",' +
                                                                        '"name":"' + searchResult1.name + '",' +
                                                                        '"lastUpdatedOn":null,' +
                                                                        '"id":"' + searchResult1.id + '",' +
                                                                        '"userId":"' + searchResult1.userId + '",' +
                                                                        '"value":"' + searchResult1.value + '",' +
                                                                        '"createdOn":null}' +
                                                                        '}],' +
                                                                        '"meta":null}'
  }
}