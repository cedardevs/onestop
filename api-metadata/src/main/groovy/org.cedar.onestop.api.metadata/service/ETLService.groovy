package org.cedar.onestop.api.metadata.service

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Slf4j
@Service
class ETLService {

  private String SCROLL_TIMEOUT = '1m'
  private Integer PAGE_SIZE = 10

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.name}')
  private String SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.name}')
  private String STAGING_INDEX

  @Value('${elasticsearch.index.staging.collectionType}')
  private String COLLECTION_TYPE

  @Value('${elasticsearch.index.staging.granuleType}')
  private String GRANULE_TYPE

  private ElasticsearchService elasticsearchService

  @Autowired
  ETLService(ElasticsearchService elasticsearchService) {
    this.elasticsearchService = elasticsearchService
  }

  @Async
  public void rebuildSearchIndexAsync() {
    rebuildSearchIndex()
  }

  @Async
  public void updateSearchIndexAsync() {
    updateSearchIndex()
  }

  public void rebuildSearchIndex() {
    // TODO
  }

  @Scheduled(fixedDelay = 600000L) // 10 minutes after previous run ends
  public void updateSearchIndex() {
    // TODO
  }

}
