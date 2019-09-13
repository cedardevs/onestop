package org.cedar.onestop.api.search.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@ConfigurationProperties('search')
class SearchConfig {

  String minimumShouldMatch
  Integer phraseSlop
  Float tieBreaker
  Map<String, Object> fields = [:]

  private Map<String, Float> boosts
  Map<String, Float> getBoosts() { return boosts }

  @PostConstruct
  initialize() {
    boosts = flattenBoosts(fields)
  }

  static Map<String, Float> flattenBoosts(Map<String, Object> fieldsConfig, String prefix = '') {
    return fieldsConfig.collectEntries { k, v ->
      if (v instanceof Map) {
        return flattenBoosts(v, k)
      }
      else {
        def key = prefix ? "${prefix}.${k}".toString() : k
        if (v == null) {
          return [(key): 1f]
        }
        if (v instanceof Number) {
          return [(key): v.toFloat()]
        }
        throw new RuntimeException("Invalid configuration. Field boost values must be numbers")
      }
    }
  }

}
