package org.cedar.onestop.api.search.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("feature-trending-search")
@ConfigurationProperties('trending')
class TrendingBlacklistConfig {

  List<String> defaultBlacklistedSearchTerms
  List<String> additionalBlacklistedSearchTerms

  List<String> blacklistedCollections
}
