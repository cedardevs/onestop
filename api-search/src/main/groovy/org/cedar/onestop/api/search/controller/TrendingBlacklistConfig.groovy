package org.cedar.onestop.api.search.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty("features.trending.search")
@ConfigurationProperties('trending')
class TrendingBlacklistConfig {
  List<String> filterSearchTerms
  List<String> filterCollectionTerms
}
