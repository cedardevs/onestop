plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("khttp:khttp:1.0.0")
    implementation("org.jsonschema2pojo:jsonschema2pojo-core:1.0.2")
}
