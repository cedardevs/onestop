package org.cedar.onestop.api.search.controller

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties('ui')
class UiConfig {

  BannerConfig banner
  List<FeaturedConfig> featured

  static class BannerConfig {
    String message
    BannerColorConfig colors
  }

  static class BannerColorConfig {
    String text
    String background
  }

  static class FeaturedConfig { // featured datasets
    String title
    String searchTerm
    String imageUrl
  }

  List<FeatureToggles> enabledFeatureToggles

  static class FeatureToggles {
    String featureName
  }

  GoogleAnalytics googleAnalytics

  static class GoogleAnalytics{
    List<Map> profiles
    Map reactGaOptions
  }

// * This is simply a reference to the kinds of properties that could be provided under reactGaOptions
//  class ReactGAOptions {
//    Boolean debug
//    String titleCase
//    String gaOptions
//    String gaAddress
//    Boolean alwaysSendToDefaultTracker
//  }

  Auth auth
  static class Auth{
    String loginEndpoint
    String logoutEndpoint
    String userProfileEndpoint
  }

}
