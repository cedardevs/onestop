package org.cedar.onestop.api.search

import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles("integration")
@SpringBootTest(classes = [Application, IntegrationTestConfig], webEnvironment = RANDOM_PORT)
class IntegrationTest extends Specification {

  @Autowired
  RestClient restClient

  void refreshAndLoadGenericTestIndex(String index) {
    def cl = ClassLoader.systemClassLoader
    def datesIndexJson = cl.getResourceAsStream("testIndices/${index}.json").text
    def datesIndexMapping = new NStringEntity(datesIndexJson, ContentType.APPLICATION_JSON)
    def bulkRequests = cl.getResourceAsStream("data/GenericFilterData/${index}.txt").text
    def bulkRequestBody = new NStringEntity(bulkRequests, ContentType.APPLICATION_JSON)

    Response response = restClient.performRequest('DELETE', '_all')
    println("DELETE _all: ${response}")

    def newIndexResponse = restClient.performRequest('PUT', index, Collections.EMPTY_MAP, datesIndexMapping)
    println("PUT new $index index: ${newIndexResponse}")

    def dataLoadResponse = restClient.performRequest('POST', '_bulk', Collections.EMPTY_MAP, bulkRequestBody)
    println("POST bulk data load to $index: $dataLoadResponse")

    restClient.performRequest('POST', '_refresh')
  }
}
