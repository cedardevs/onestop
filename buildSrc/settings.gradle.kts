pluginManagement {
  repositories {
    if (System.getProperty("gitLabCICD").toBoolean()) {
      maven { url = uri("https://artifacts.ncei.noaa.gov/artifactory/gradle-plugins/") }
      maven { url = uri("https://artifacts.ncei.noaa.gov/artifactory/gradle.mavencentral/") }
    } else {
      // plugins resolved elsewhere?
    }
  }
}
