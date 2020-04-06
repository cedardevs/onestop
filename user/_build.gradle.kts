//
////plugins {
////  id("org.springframework.boot") version "2.2.5.RELEASE"
////  id("io.spring.dependency-management") version "1.0.9.RELEASE"
////  kotlin("jvm") version "1.3.61"
////  kotlin("plugin.spring") version "1.3.61"
//  kotlin("plugin.jpa") version "1.3.61"
////}
////
////group = "org.cedar.onestop"
////version = "LOCAL-master-semere"
//
//dependencies {
////  implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
//  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//  implementation("org.springframework.boot:spring-boot-starter-web")
////  implementation("org.springframework.boot:spring-boot-starter-webflux")
//  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//  implementation("org.jetbrains.kotlin:kotlin-reflect")
//  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
//  testImplementation("org.springframework.boot:spring-boot-starter-test") {
//    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
//  }
////  testImplementation("io.projectreactor:reactor-test")
//}
//
//tasks.withType<Test> {
//  useJUnitPlatform()
//}
//
//tasks.withType<KotlinCompile> {
//  kotlinOptions {
//    freeCompilerArgs = listOf("-Xjsr305=strict")
//    jvmTarget = "11"
//  }
////}