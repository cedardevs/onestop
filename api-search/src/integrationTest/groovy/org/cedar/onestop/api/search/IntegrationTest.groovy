package org.cedar.onestop.api.search

import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles("integration")
@SpringBootTest(classes = [Application, IntegrationTestConfig], webEnvironment = RANDOM_PORT)
@Unroll
class IntegrationTest extends Specification {

  @Autowired
  RestClient restClient

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.collection.name}')
  String COLLECTION_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.granule.name}')
  String GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.flattened-granule.name}')
  String FLATTENED_GRANULE_SEARCH_INDEX

  @Value('${elasticsearch.index.universal-type}')
  String TYPE

  static final Map testData = [
      'DEM': [
          'C1': [
              id: 'e7a36e60-1bcb-47b1-ac0d-3c2a2a743f9b',
              granules: [],
              flattenedGranules: []
          ],
          'C2': [
              id: 'e5820283-3686-44d0-8edd-28a086eb500e',
              granules: [],
              flattenedGranules: []
          ],
          'C3': [
              id: '1415b3db-c602-4dbb-a502-4091fe9df1cf',
              granules: [],
              flattenedGranules: []
          ],
      ],
      'GHRSST': [
          'C1': [
              id: '920d8155-f764-4777-b7e5-14442b7275b8',
              granules: [],
              flattenedGranules: []
          ],
          'C2': [
              id: '882511bc-e99e-4597-b634-47a59ddf9fda',
              granules: [],
              flattenedGranules: []
          ],
          'C3': [
              id: '42ea683d-e4e7-434c-8823-abff32e00f34',
              granules: [],
              flattenedGranules: []
          ],
      ],
      'COOPS': [
          'C1': [
              id: 'fcf83ec9-964b-45b9-befe-378ea6ce52cb',
              granules: [
                  'G1': [id: '783089c4-3484-4f70-ac8d-d4818d0cd0dd'],
                  'G2': [id: 'a207b48f-29fc-4d79-a676-1f265cd9971f'],
              ],
              flattenedGranules: [
                  'FG1': [id: '783089c4-3484-4f70-ac8d-d4818d0cd0dd'],
                  'FG2': [id: 'a207b48f-29fc-4d79-a676-1f265cd9971f']
              ]
          ]
      ]
  ]

  void refreshAndLoadGenericTestIndex(String index) {
        // See /docs/development/integration-tests/README.md for more information about bulk data file and their structure

    Response response = restClient.performRequest('DELETE', '_all')
    println("DELETE _all: ${response}")

    def cl = ClassLoader.systemClassLoader
    def genericIndexJson = cl.getResourceAsStream("generic/${index}/index.json").text
    def genericIndexMapping = new NStringEntity(genericIndexJson, ContentType.APPLICATION_JSON)
    def bulkRequests = cl.getResourceAsStream("generic/${index}/bulkData.txt").text
    def bulkRequestBody = new NStringEntity(bulkRequests, ContentType.APPLICATION_JSON)

    def newIndexResponse = restClient.performRequest('PUT', index, Collections.EMPTY_MAP, genericIndexMapping)
    println("PUT new $index index: ${newIndexResponse}")

    def dataLoadResponse = restClient.performRequest('POST', '_bulk', Collections.EMPTY_MAP, bulkRequestBody)
    println("POST bulk data load to $index: $dataLoadResponse")

    restClient.performRequest('POST', '_refresh')
  }

  void refreshAndLoadSearchIndices() {
    Response response = restClient.performRequest('DELETE', '_all')
    println("DELETE _all: ${response}")

    def cl = ClassLoader.systemClassLoader
    def searchCollectionIndexJson = cl.getResourceAsStream('search_collectionIndex.json').text
    def collectionIndexSettings = new NStringEntity(searchCollectionIndexJson, ContentType.APPLICATION_JSON)
    def searchGranuleIndexJson = cl.getResourceAsStream('search_granuleIndex.json').text
    def granuleIndexSettings = new NStringEntity(searchGranuleIndexJson, ContentType.APPLICATION_JSON)
    def searchFlattenedGranuleIndexJson = cl.getResourceAsStream('search_flattened_granuleIndex.json').text
    def flattenedGranuleIndexSettings = new NStringEntity(searchFlattenedGranuleIndexJson, ContentType.APPLICATION_JSON)

    response = restClient.performRequest('PUT', COLLECTION_SEARCH_INDEX, Collections.EMPTY_MAP, collectionIndexSettings)
    println("PUT new collection index: ${response}")

    response = restClient.performRequest('PUT', GRANULE_SEARCH_INDEX, Collections.EMPTY_MAP, granuleIndexSettings)
    println("PUT new granule index: ${response}")

    response = restClient.performRequest('PUT', FLATTENED_GRANULE_SEARCH_INDEX, Collections.EMPTY_MAP, flattenedGranuleIndexSettings)
    println("PUT new flattened-granule index: ${response}")

    testData.each{ name, dataset ->
      dataset.each { collection, collectionData ->
        def metadata = cl.getResourceAsStream("data/${name}/${collection}.json").text
        def id = collectionData.id
        def collectionEndpoint = "/$COLLECTION_SEARCH_INDEX/$TYPE/$id"
        HttpEntity record = new NStringEntity(metadata, ContentType.APPLICATION_JSON)
        response = restClient.performRequest('PUT', collectionEndpoint, Collections.EMPTY_MAP, record)
        println("PUT new collection: ${response}")

        collectionData.granules.each { granule, granuleData ->
          metadata = cl.getResourceAsStream("data/${name}/${granule}.json").text
          def granuleEndpoint = "/$GRANULE_SEARCH_INDEX/$TYPE/$granuleData.id"
          record = new NStringEntity(metadata, ContentType.APPLICATION_JSON)
          response = restClient.performRequest('PUT', granuleEndpoint, Collections.EMPTY_MAP, record)
          println("PUT new granule: ${response}")
        }

        collectionData.flattenedGranules.each { flattenedGranule, flattenedGranuleData ->
          metadata = cl.getResourceAsStream("data/${name}/${flattenedGranule}.json").text
          def flattenedGranuleEndpoint = "/$FLATTENED_GRANULE_SEARCH_INDEX/$TYPE/$flattenedGranuleData.id"
          record = new NStringEntity(metadata, ContentType.APPLICATION_JSON)
          response = restClient.performRequest('PUT', flattenedGranuleEndpoint, Collections.EMPTY_MAP, record)
          println("PUT new flattened granule: ${response}")
        }
      }
    }

    restClient.performRequest('POST', '_refresh')
  }
}
