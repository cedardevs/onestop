package ncei.onestop.api.service

import groovy.util.logging.Slf4j
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.aggregations.Aggregations
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.springframework.stereotype.Service

@Slf4j
@Service
class SearchResponseParserService {

  Map searchResponseParser(SearchResponse response) {
    def data = response.hits.hits.collect({ [type: 'collection', id: it.id, attributes: it.source] })

    def metadata
    if(response.aggregations) {
      def aggs = prepareAggregationsForUI(response.aggregations)
      metadata = [
          took : response.tookInMillis,
          total: response.hits.totalHits,
          aggregations: aggs
      ]
    } else {
      metadata = [
          took : response.tookInMillis,
          total: response.hits.totalHits
      ]
    }

    def result = [data: data, meta: metadata]
    log.debug("Parsed elasticsearch response with ${data.size()}/${metadata.total} results")
    return result
  }

  private Map prepareAggregationsForUI(Aggregations aggs) {

    def topLevelLocations = ['Continent', 'Geographic Region', 'Ocean', 'Solid Earth', 'Space', 'Vertical Location']
    def topLevelScience =
        ['Agriculture', 'Atmosphere', 'Biological Classification', 'Biosphere', 'Climate Indicators',
         'Cryosphere', 'Human Dimensions', 'Land Surface', 'Oceans', 'Paleoclimate', 'Solid Earth',
         'Spectral/Engineering', 'Sun-Earth Interactions', 'Terrestrial Hydrosphere']

    def scienceAgg = cleanAggregation(topLevelScience, aggs.get('science').getBuckets())
    def locationsAgg = cleanAggregation(topLevelLocations, aggs.get('locations').getBuckets())

    def instrumentsAgg = cleanAggregation(null, aggs.get('instruments').getBuckets())
    def platformsAgg = cleanAggregation(null, aggs.get('platforms').getBuckets())
    def projectsAgg = cleanAggregation(null, aggs.get('projects').getBuckets())
    def dataCentersAgg = cleanAggregation(null, aggs.get('dataCenters').getBuckets())
    def dataResolutionAgg = cleanAggregation(null, aggs.get('dataResolution').getBuckets())

    return [
        science: scienceAgg,
        locations: locationsAgg,
        instruments: instrumentsAgg,
        platforms: platformsAgg,
        projects: projectsAgg,
        dataCenters: dataCentersAgg,
        dataResolution: dataResolutionAgg
    ]
  }

  private List cleanAggregation(List<String> topLevelKeywords, List<Terms.Bucket> originalAgg) {

    def cleanAgg = []
    originalAgg.each { e ->
      def term = e.key as String
      def count = e.docCount

      if(!topLevelKeywords) {
        cleanAgg.add([[term: term, count: count]])

      } else {
        if(term.contains('>')) {
          def splitTerms = term.split('>', 2)
          if(topLevelKeywords.contains(splitTerms[0].trim())) {
            cleanAgg.add([term: term, count: count])
          }

        } else {
          if(topLevelKeywords.contains(term)) {
            cleanAgg.add([term: term, count: count])
          }
        }
      }
    }
    return cleanAgg
  }
}