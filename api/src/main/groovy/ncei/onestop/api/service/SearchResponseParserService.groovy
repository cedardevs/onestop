package ncei.onestop.api.service

import org.elasticsearch.action.search.SearchResponse
import org.springframework.stereotype.Service

@Service
class SearchResponseParserService {

  Map searchResponseParser(SearchResponse response) {
    [items: response.hits.hits*.source]
  }

}
