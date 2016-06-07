package ncei.onestop.api.service

import groovy.util.logging.Slf4j
import org.elasticsearch.action.search.SearchResponse
import org.springframework.stereotype.Service

@Slf4j
@Service
class SearchResponseParserService {

    Map searchResponseParser(SearchResponse response) {
        log.debug("Parsing elasticsearch response: ${response}")
        def data = response.hits.hits.collect({[type: 'collection', id: it.id, attributes: it.source]})
        def metadata = [
          took : response.tookInMillis,
          total: response.hits.totalHits
        ]
        def result = [data: data, meta: metadata]
        log.debug("Parsed result: ${result}")
        return result
    }

}
