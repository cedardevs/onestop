package ncei.onestop.api.service

import groovy.util.logging.Slf4j
import org.elasticsearch.action.search.SearchResponse
import org.springframework.stereotype.Service

@Slf4j
@Service
class SearchResponseParserService {

    Map searchResponseParser(SearchResponse response) {
        log.debug("Parsing elasticsearch response: ${response}")
        def result = [
          took : response.tookInMillis,
          items: response.hits.hits.collect {it.source + [id: it.id]},
          total: response.hits.totalHits,
        ]
        log.debug("Parsed result: ${result}")
        return result
    }

}
