plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.github.jkcclemens:khttp:-SNAPSHOT")
    implementation("org.jsonschema2pojo:jsonschema2pojo-core:1.0.2")
}
