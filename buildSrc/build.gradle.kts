plugins {
    `kotlin-dsl`
}

val gitLabCICD: Boolean = System.getProperty("gitLabCICD").toBoolean()

repositories {
    if (gitLabCICD) {
      maven { url = uri("https://artifacts.ncei.noaa.gov/artifactory/gradle-plugins/") }
      maven { url = uri("https://artifacts.ncei.noaa.gov/artifactory/gradle.mavencentral/") }
    } else {
      mavenCentral()
      maven { url = uri("https://jcenter.bintray.com") }
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("khttp:khttp:1.0.0")
    implementation("org.jsonschema2pojo:jsonschema2pojo-core:1.0.2")
}
