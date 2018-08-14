package org.cedar.onestop.api.search.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties('trending')
class FilterConfig {
    List<String> filterTerms
}