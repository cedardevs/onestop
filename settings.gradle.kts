pluginManagement {
  repositories {
    if (System.getProperty("gitLabCICD").toBoolean()) {
      maven {
        url = uri("https://artifacts.ncei.noaa.gov/artifactory/gradle-plugins/")
      }
    }
  }
}

rootProject.name = "onestop"

include(
    "client",
    "e2e-tests",
    "elastic-common",
    "data-common",
    "indexer",
    "kafka-common",
    "parsalyzer",
    "registry",
    "search",
    "test-common",
    "search",
    "stream-manager",
    "user",
    "gateway"
)

//plugins {
//    // Gradle Enterprise Plugin
//    // https://docs.gradle.com/enterprise/gradle-plugin/#gradle_6_x_and_later
//    // - Gradle 6+ uses com.gradle.enterprise as the plugin ID.
//    //   The plugin must be applied in the settings file of the build.
//    id("com.gradle.enterprise").version("3.1.1")
//}


//gradleEnterprise {
//    buildScan {
//        termsOfServiceUrl = "https://gradle.com/terms-of-service"
//        termsOfServiceAgree = "yes"
//    }
//}
include("org.cedar.onestop.data")
