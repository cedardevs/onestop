package ncei.onestop.api.controller

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties('ui')
class UiConfig {

  Boolean granuleDetails
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

  static class FeaturedConfig {
    String title
    String searchTerm
    String imageUrl
  }

}
