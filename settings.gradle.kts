rootProject.name = "onestop"

include(
    "client",
    "e2e-tests",
    "elastic-common",
    "data-common",
    "geoportal-search",
    "indexer",
    "kafka-common",
    "parsalyzer",
    "registry",
    "search"
    //"user"
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
