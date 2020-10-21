package org.cedar.onestop.api.search

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.search.service.ElasticsearchService
import org.cedar.onestop.data.util.FileUtils
import org.cedar.onestop.elastic.common.ElasticsearchVersion
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.RequestUtil
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient

@Slf4j
class TestUtil {

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

  static void resetLoadAndRefreshGenericTestIndex(String alias, RestHighLevelClient restHighLevelClient, ElasticsearchService esService) {

    ElasticsearchVersion elasticVersion = esService.esConfig.version
    RestClient restClient = restHighLevelClient.lowLevelClient

    // wipe out all the indices
    RequestUtil.deleteAllIndices(restClient)

    // recreate the generic index alias with its JSON mapping
    // ------------------------------------------------------
    // Elasticsearch alias names are configurable, and this allows a central mapping between
    // the alias names configured (including the prefix) and the JSON mappings
    // https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html
    String jsonMappingGeneric
    jsonMappingGeneric = FileUtils.textFromClasspathFile("test/data/generic/${alias}/index.json")

    RequestUtil.resetIndices(alias, jsonMappingGeneric, restClient)

    // load bulk data into generic index alias
    String bulkDataFile = "test/data/generic/${alias}/bulkData.txt"
    if(elasticVersion.isMajorVersion(6)) {
      bulkDataFile = "test/data/generic/${alias}/bulkDataES6.txt"
    }

    String bulkData = FileUtils.textFromClasspathFile(bulkDataFile.toString())
    RequestUtil.bulk(alias, bulkData, restClient)

    // refresh all indices
    RequestUtil.refreshAllIndices(restClient)
  }

  static void resetLoadAndRefreshSearchIndices(RestClient restClient, ElasticsearchConfig esConfig) {

    // wipe out all the indices
    RequestUtil.deleteAllIndices(restClient)
    
    // recreate the indices with their JSON mappings
    RequestUtil.resetSearchCollectionsIndices(esConfig, restClient)
    RequestUtil.resetSearchGranulesIndices(esConfig, restClient)
    RequestUtil.resetSearchFlattenedGranulesIndices(esConfig, restClient)

    // iterate through test data and load into appropriate indices
    testData.each{ name, dataset ->
      // each data set contains a map of test collections
      dataset.each { collection, collectionData ->
        // each test collection has an id and a corresponding JSON found in the shared `elastic-common` resources
        String collectionId = collectionData.id
        String collectionMetadata = FileUtils.textFromClasspathFile("test/data/json/${name}/${collection}.json")
        // load the collection record into the collection index
        log.info("LOADING COLLECTION - test/data/json/${name}/${collection}.json")
        RequestUtil.putSearchCollectionMetadataRecord(collectionId, collectionMetadata, esConfig, restClient)

        // each test collection has an array of test granules
        collectionData.granules.each { granule, granuleData ->
          // if there are any granules, each has an id and corresponding JSON found in the shared `elastic-common` resources
          String granuleId = granuleData.id
          log.info("LOADING GRANULE - test/data/json/${name}/${collection}.json")
          String granuleMetadata = FileUtils.textFromClasspathFile("test/data/json/${name}/${granule}.json")
          // load the new granule record into the granule index
          RequestUtil.putSearchGranuleMetadataRecord(granuleId, granuleMetadata, esConfig, restClient)
        }

        // each test collection has an array of test flattened granules
        collectionData.flattenedGranules.each { flattenedGranule, flattenedGranuleData ->
          // if there are any flattened granules, each has an id and corresponding JSON found in the shared `elastic-common` resources
          String flattenedGranuleId = flattenedGranuleData.id
          String flattenedGranuleMetadata = FileUtils.textFromClasspathFile("test/data/json/${name}/${flattenedGranule}.json")
          // load the new flattened granule record into the flattened granule index
          RequestUtil.putSearchFlattenedGranuleMetadataRecord(flattenedGranuleId, flattenedGranuleMetadata, esConfig, restClient)
        }
      }
    }

    // refresh all indices
    RequestUtil.refreshAllIndices(restClient)
  }
}
