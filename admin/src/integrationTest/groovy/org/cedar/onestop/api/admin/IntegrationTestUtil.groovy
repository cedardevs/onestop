package org.cedar.onestop.api.admin

import org.cedar.onestop.api.admin.service.ElasticsearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import static org.cedar.onestop.elastic.common.DocumentUtil.*

@Component
class IntegrationTestUtil{
  @Autowired
  ElasticsearchService elasticsearchService

  Map documentsByType(String collectionIndex, String granuleIndex, String flatGranuleIndex = null) {
    log.debug("Getting documents by type...")
    log.debug("Refreshing collection and granule indices...")
    elasticsearchService.refresh(collectionIndex, granuleIndex)
    if (flatGranuleIndex) {
      log.debug("Refreshing flattened granule indices...")
      elasticsearchService.refresh(flatGranuleIndex)
    }
    log.debug("Searching ${collectionIndex}, ${granuleIndex}${flatGranuleIndex ? ", and ${flatGranuleIndex}" : ''} indices...")
    def endpoint = "${collectionIndex},${granuleIndex}${flatGranuleIndex ? ",${flatGranuleIndex}" : ''}/_search"
    def request = [version: true]
    def response = elasticsearchService.performRequest('GET', endpoint, request)
    Map groupedDocuments =  getDocuments(response).groupBy({ elasticsearchService.esConfig.typeFromIndex(getIndex(it)) })

    List<Map> collectionDocuments = groupedDocuments[ElasticsearchConfig.TYPE_COLLECTION] ?: []
    List<Map> granuleDocuments = groupedDocuments[ElasticsearchConfig.TYPE_GRANULE] ?: []
    List<Map> flattenedGranuleDocuments = groupedDocuments[ElasticsearchConfig.TYPE_FLATTENED_GRANULE] ?: []

    int numCollectionsFound = collectionDocuments.size()
    int numGranulesFound = granuleDocuments.size()
    int numFlattenedGranulesFound = flattenedGranuleDocuments.size()

    log.debug("Found ${numCollectionsFound.toString()} collections.")
    log.debug("Found ${numGranulesFound.toString()} granules.")
    if(flatGranuleIndex) {
      log.debug("Found ${numFlattenedGranulesFound.toString()} flattened granules.")
    }
    return groupedDocuments
  }

}