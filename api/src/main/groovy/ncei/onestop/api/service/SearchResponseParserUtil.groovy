package ncei.onestop.api.service

import org.elasticsearch.action.search.SearchResponse

class SearchResponseParserUtil {

  static Map searchResponseParser(SearchResponse response) {
    [
        took: response.tookInMillis,
        items: response.hits.hits.collect { it.source + [id: it.id] },
        total: response.hits.totalHits,
    ]
  }

}
