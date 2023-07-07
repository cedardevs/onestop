import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.moowork.gradle.node.npm.NpmTask
import com.moowork.gradle.node.task.NodeTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

buildscript {
  repositories {
    if (gitLabCICD) {
      maven { url = uri("https://artifacts.ncei.noaa.gov/artifactory/gradle-plugins/") }
    }
  }
  extra.apply{ set("gitLabCICD", gitLabCICD) }
}


plugins {
    `kotlin-dsl`

    // The JaCoCo plugin
    // https://docs.gradle.org/current/userguide/jacoco_plugin.html
    // - provides code coverage metrics for Java code via integration with JaCoCo
    jacoco

    // OWASP Dependency Check Gradle Plugin
    // https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/index.html
    // - provides monitoring of the projects dependent libraries;
    //   creating a report of known vulnerable components that are included in the build.
    id("org.owasp.dependencycheck").version("6.5.3")

    // Note: The plugins below are not universally `apply(true)`because subprojects only need them conditionally.

    // Kotlin Plugin
    // - In case we ever want to use Kotlin on a project targeted on the JVM
    kotlin("jvm").version("1.3.61").apply(false)

    // Jib plugin
    // https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin
    // - Jib is a Gradle plugin for building Docker and OCI images for your Java applications.
    id("com.google.cloud.tools.jib").version("2.7.1").apply(false)

    // Node plugin
    // https://github.com/srs/gradle-node-plugin/blob/master/docs/node.md
    // - This plugin enables you to run any NodeJS script as part of your build.
    //   It does not depend on NodeJS (or NPM) being installed on your system.
    // TODO: Using a fork of com.moowork.node until they fix the Gradle 6 issue
    id("com.github.node-gradle.node").version("2.2.1").apply(false)
    //id("com.moowork.node").version("1.3.1").apply(false)

    // Micronaut plugins
    // https://docs.micronaut.io/latest/guide/index.html#_gradle_shadow_plugin
    //  - The shadow plugin provides a shadowJar task to generate a self-contained executable JAR file,
    //    which is suitable for AWS Lambda deployments.
    id("com.github.johnrengelman.shadow").version("5.2.0").apply(false)

    // Spring dependency management plugin
    // https://docs.spring.io/dependency-management-plugin/docs/current/reference/html/
    // - A Gradle plugin that provides Maven-like dependency management and exclusions
    id("io.spring.dependency-management").version("1.0.11.RELEASE").apply(false)

    // Spring Boot plugin
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/
    // - A Gradle plugin that allows you to package executable jar or war archives,
    //   run Spring Boot applications, and use the dependency management provided by spring-boot-dependencies
    id("org.springframework.boot").version("2.7.12").apply(false)

    // Gogradle plugin
    // https://github.com/gogradle/gogradle
    // - A Gradle Plugin Providing Full Support for Go
    id("com.github.blindpirate.gogradle").version("0.11.4").apply(false)

}

repositories {
    if (gitLabCICD) {
      maven(url = uri("https://artifacts.ncei.noaa.gov/artifactory/gradle.mavencentral/"))
      maven(url = uri("https://artifacts.ncei.noaa.gov/artifactory/gradle-plugins/"))
    } else {
      mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.owasp.dependencycheck")
}

group = "org.cedar.onestop"

version = rootProject.dynamicVersion(vendor ="cedardevs", envBuildTag = "ONESTOP_BUILD_TAG", ci = CI.CIRCLE, registry = Registry.DOCKER_HUB)

val authors: List<Author> = listOf(
        Author(
                name = "Coalition for Earth Data Applications and Research",
                email = "cedar.cires@colorado.edu",
                website = "https://github.com/cedardevs"
        )
)

description = """

------------------------------------------------------------
OneStop is a data discovery system being built by CIRES
researchers on a grant from the NOAA National Centers for
Environmental Information.  We welcome contributions from
the community!
------------------------------------------------------------
group:   $group
version: $version
authors: ${formatAuthors(authors, pretty = true)}

Gradle version: ${gradle.gradleVersion}
------------------------------------------------------------

""".trimIndent()

val projectDescriptions: Map<String, String> = mapOf(
        Pair("client", "A browser UI for the OneStop system."),
        Pair("data-common", "A Shared project with API response POJOs and utilities for data manipulation"),
        Pair("e2e-tests", "End-to-end test project for the OneStop system."),
        Pair("elastic-common", "A shared project used by OneStop applications interacting with Elastic"),
        Pair("geoportal-search", "An application supporting OpenSearch and CSW search standards against the OneStop system."),
        Pair("indexer", "A Kafka Streams app which picks up from the parsed metadata topic, flattens granules of collections, and populates Elasticsearch indices."),
        Pair("kafka-common", "A shared project used by OneStop applications interacting with Kafka"),
        Pair("registry", "A private API to upload ISO metadata to the OneStop system Kafka event stream."),
        Pair("search", "An read-only API for the OneStop system to query data indexed in Elasticsearch."),
        Pair("parsalyzer", "A Kafka Streams app which picks up from the raw metadata topic, parses into a standard format, analyzes some fields for further insight, and places onto a parsed topic."),
        Pair("test-common", "A Utility project to test transformations throughout the system."),
        Pair("user", "An API to authenticate and manage public user data of the OneStop system.")
)

// only apply plugins, configuration, tasks, etc. to projects that need it
val javaProjects: List<String> = listOf("client", "data-common", "indexer", "kafka-common", "e2e-tests", "elastic-common", "search", "registry", "parsalyzer", "test-common", "user", "gateway")
val applicationProjects: List<String> = listOf()
val libraryProjects: List<String> = listOf("kafka-common", "elastic-common", "data-common") // FIXME elastic?
val jibProjects: List<String> = listOf("client", "indexer", "registry", "search", "parsalyzer", "user", "gateway")
val springBootProjects: List<String> = listOf("elastic-common", "search", "registry", "gateway", "user")
val nodeProjects: List<String> = listOf("client", "registry")
val mappingProjects: List<String> = listOf("elastic-common")
//val micronautProjects: List<String> = listOf("user")
val warProjects: List<String> = listOf("search")

// allows projects to monitor dependent libraries for known, published vulnerabilities
dependencyCheck {
    skipConfigurations = listOf("providedRuntime")
    suppressionFile = "${rootDir}/owasp-suppressions.xml"
    failBuildOnCVSS = 4.0F

    // One of our dependencies has an un-parsable pom which causes dependency-checker
    // to throw an exception. However, the checks still run and it still generates a
    // report, so I think it's safe(ish) to ignore the error.
    failOnError = false
}

allprojects {
    // resolve subproject dependencies through repos based on build environment
    repositories {
      if (gitLabCICD) {
        maven(url = "https://artifacts.ncei.noaa.gov/artifactory/spring-milestone/")
        maven(url = "https://artifacts.ncei.noaa.gov/artifactory/confluent-maven/")

        maven(url = "https://artifacts.ncei.noaa.gov/artifactory/gradle.mavencentral/")
        maven(url = "https://artifacts.ncei.noaa.gov/artifactory/gradle-plugins/")
      } else {
        mavenCentral()
        maven(url = "https://repo.spring.io/milestone")
        maven(url = "https://packages.confluent.io/maven/")
      }
      // Switch based on artifactory access. Check logs during build to ensure jitpack isn't used on-prem.
      if (System.getenv("ARTIFACTORY_API_KEY") != null) {
        project.logger.lifecycle("Using NCEI Artifactory for dependency resolution")
        maven {
          name = "NCEI_MAVEN_PROD"
          url = uri("https://artifacts.ncei.noaa.gov/artifactory/ncei-maven/")
          credentials(HttpHeaderCredentials::class) {
            name = "X-JFrog-Art-Api"
            value = System.getenv("ARTIFACTORY_API_KEY")
          }
          authentication {
            create<HttpHeaderAuthentication>("header")
          }
        }
      } else {
        project.logger.lifecycle("*** WARNING: using jitpack for dependency resolution. This is NOT allowed for NCEI on-prem builds ***")
        maven(url = "https://jitpack.io")
      }
    }
}

subprojects {

    // capture project-specific information for publishing and other tasks
    val projectName = "${rootProject.name}-${name}"
    val projectVersion: String = rootProject.version as String
    val description: String = projectDescriptions.getOrDefault(name, defaultValue = "The ${rootProject.name}-${name} project.")

    val gitUrl = "https://github.com/cedardevs/onestop.git"
    val projectUrl = "https://github.com/cedardevs/onestop"
    val issuesUrl = "https://github.com/cedardevs/onestop/issues"

    val url = "https://data.noaa.gov/onestop"
    val docs = "https://cedardevs.github.io/onestop"

    if (javaProjects.contains(name)) {
        // apply java gradle plugin to projects using java
        apply(plugin = "java")

        // in case we are still using groovy on any of our projects
        apply(plugin = "groovy")

        // TODO: apply(plugin =  "kotlin")?

        apply(plugin = "jacoco")
        tasks.jacocoTestReport {
            executionData(fileTree(projectDir).include("build/jacoco/*.exec"))
            reports {
                xml.isEnabled = true
                xml.destination = file("${buildDir}/reports/jacoco/report.xml")
                html.isEnabled = true
                html.destination = file("${buildDir}/reports/jacoco/html")
            }
        }

        tasks.named("check") {
            dependsOn("jacocoTestReport")
        }

        extra.apply {
            set("Versions", Versions)
        }
        tasks.test {
            useJUnitPlatform()
            testLogging {
                events (FAILED, SKIPPED)//STANDARD_ERROR, STANDARD_OUT
                exceptionFormat = FULL
                showExceptions = true
                showCauses = true
                showStackTraces = true
            }
        }
    }
    if (jibProjects.contains(name)) {
        // apply jib gradle plugin to projects using jib
        apply(plugin = "com.google.cloud.tools.jib")

        // apply subproject-specific information to publish object
        // this will augment the information automatically determined by using:
        // `version = rootProject.dynamicVersion(...)` in the root gradle script
        // the final publish info for each project will be available via the "publish" extra property:
        // Kotlin: `val publish: Publish by project.extra`
        // Groovy: `use(PublishingKt) { def publish = project.publish; ... }`
        project.setPublish(Publish(
                description = description,
                documentation = docs,
                authors = formatAuthors(authors),
                url = url,
                licenses = License.GPL20,
                task = "jib"
        ))
    }
    if (springBootProjects.contains(name)) {
        // apply the spring dependency management plugin to projects using spring
        apply(plugin = "io.spring.dependency-management")

        // apply the spring boot plugin to projects using spring
        apply(plugin = "org.springframework.boot")
    }
    if (mappingProjects.contains(name)) {
      tasks.register<ESMappingTask>("esMappingGenerate") {
      }
    }
    if (nodeProjects.contains(name)) {
        // apply node gradle plugin to projects using node/npm
        // TODO: revert to original (non-forked) moowork plugin when Gradle 6 issues are resolved
        apply(plugin = "com.github.node-gradle.node")
        //apply(plugin = "com.moowork.node")

        // apply a common node/npm version to all projects using node
        configure<com.moowork.gradle.node.NodeExtension> {
            version = Versions.NODE
            // pull the node dist from artifactory if in GitLab CI/CD
            if (gitLabCICD) {
              distBaseUrl = "https://artifacts.ncei.noaa.gov/artifactory/node-dist"
              // npmVersion is intentionally not set; use the one bundled with node
              // since we cannot set the url to artifactory for the npm dist
            } else {
              npmVersion = Versions.NPM
            }
            workDir = file("${rootProject.buildDir}/nodejs")
            npmWorkDir = file("${rootProject.buildDir}/npm")
            nodeModulesDir = file("${project.projectDir}")

            // true -> download node using above parameters
            // instead of using globally installed npm (this maintains cross-environment consistency)
            download = true
        }

        val packageJSON = PackageJSON(
                name = projectName,
                version = projectVersion,
                description = description,
                author = formatAuthors(listOf(authors.first())),
                license = License.GPL20,
                homepage = projectUrl,
                repositoryUrl = gitUrl,
                repositoryDirectory = name,
                bugsUrl = issuesUrl
        )

        tasks.register<PackageJSONTask>("updatePackageJSON") {
            updates = packageJSON
        }

        // anything done by npm or node should make sure the `package.json` is up-to-date
        tasks.withType<NpmTask> {
            dependsOn("updatePackageJSON")
        }
        tasks.withType<NodeTask> {
            dependsOn("updatePackageJSON")
        }

        // sync files to the client container jib staging directory
        val jibExtraDir = "${buildDir.path}/jib-extra-dir"

        extra.apply {
            set("packageJSON", packageJSON)
            set("jibExtraDir", jibExtraDir)
        }

    }

    if (warProjects.contains(name)) {
        // If this system property isn't set then resolves to FALSE.
        var enableSearchWar: Boolean = System.getProperty("enableSearchWar").toBoolean()
        if (enableSearchWar) {
            apply(plugin = "war")
        }
    }
    afterEvaluate {
        // override versions of dependencies with vulnerabilities
        configurations.all {
            resolutionStrategy.eachDependency {

                if (requested.group == "org.xerial.snappy" && requested.name == "snappy-java") {
                      useVersion("1.1.10.1")
                      because("override version since kafka-clients only use up to version 1.1.8.4")
                }

                if (requested.group == "org.mozilla" && requested.name == "rhino") {
                      useVersion("1.7.14")
                      because("override version since json-schema-validator only uses up to version 1.7.7.2")
                }

                if (requested.group == "com.github.everit-org.json-schema" && requested.name == "org.everit.json.schema") {
                  useTarget("com.github.erosb:everit-json-schema:1.14.2")
                }

                if (requested.group == "org.apache.santuario" && requested.name == "xmlsec") {
                    if (requested.version!!.startsWith("2.0") && requested.version!! <= "2.1.4") {
                        useVersion("2.1.4")
                        because("fixes CVE-2019-12400")
                    }
                }

                if (requested.group == "org.yaml" && requested.name == "snakeyaml") {
                    if(requested.version!! < "1.33") {
                        useVersion(Versions.SNAKE_YAML)
                        because("multiple CVEs for versions < 1.33")
                    }
                }

                if (requested.group == "org.apache.httpcomponents" && requested.name == "httpclient") {
                    if(requested.version!! < "4.5.14") {
                        useVersion("4.5.14")
                        because("multiple CVEs for versions < 4.5.13")
                    }
                }

                if (requested.group == "org.apache.avro" && requested.name == "avro") {
                    if(requested.version!! < "2.0") {
                        useVersion(Versions.AVRO)
                        because("latest avro does not depend on vulnerable jackson-mapper-asl which has not been updated since 2013")
                    }
                }

                if (requested.group == "org.apache.logging.log4j" && requested.name == "log4j-api") {
                    if (requested.version!! < "2.13") {
                        useVersion("2.17.2")
                        because("fixes vulnerability in 2.11.1 and before")
                    }
                }

                if (requested.group == "org.bouncycastle" && requested.name == "bcprov-jdk15on") {
                    if (requested.version!! < "1.70") {
                        useVersion("1.70")
                        because("fixes vulnerability in 1.6 before")
                    }
                }

//                if (requested.group == "com.fasterxml.jackson.core" && requested.name == "jackson-databind") {
//                    if (requested.version!! < "2.13.1") {
//                        useVersion("2.13.1")
//                        because("fixes vulnerability in 2.9.9 and before")
//                    }
//                }

                if (requested.group == "com.google.guava" && requested.name == "guava") {
                    if (requested.version!! <= "31.0.1") {
                        useVersion("31.0.1-jre")
                        because("fixes CVE-2018-10237")
                    }
                }

                if (requested.group == "org.hibernate.validator" && requested.name == "hibernate-validator") {
                    if (requested.version!! < "6.1.7") {
                        useVersion( "6.1.7.Final")
                        because("fixes vulnerability in 6.1.4-Final and earlier")
                    }
                }

                if (requested.group == "org.apache.commons" && requested.name == "commons-compress") {
                    if (requested.version!! < "2.0") {
                        useVersion("1.21")
                        because("fixes CVE-2021-36090, CVE-2021-35516, CVE-2021-35515, CVE-2021-35517: Crafty ZIPs")
                    }
                }

                if (requested.group == "org.jasig.cas.client" && requested.name == "cas-client-core") {
                    if (requested.version!! <= "3.5.0") {
                        useVersion("3.6.0")
                        because("fixes CWE-611: Improper Restriction of XML External Entity Reference")
                    }
                }

                if (requested.group == "io.netty" && requested.name == "netty-all") {
                    if (requested.version!! < "4.1.42.Final") {
                        useVersion("4.1.42.Final")
                        because("fixes CVE-2019-16869")
                    }
                }
                if (requested.group == "com.nimbusds" && requested.name == "nimbus-jose-jwt") {
                    if (requested.version!! <= "7.8") {
                        useVersion("7.9")
                        because("fixes CVE-2019-17195")
                    }
                }
                if (requested.group.startsWith("org.apache.tomcat") &&
                        requested.name.contains("tomcat") &&
                        requested.version!! < "9.0.75") {
                    useVersion("9.0.75")
                    because("Enforce tomcat 9.0.58+ to avoid vulnerabilities CVE-2022-23181\n" +
                            "9.0.68+ to avoid CVE-2022-34305")
                }
                if (requested.group.startsWith("org.apache.tomcat.embed") &&
                    requested.version!! < "9.0.75") {
                    useVersion("9.0.75")
                    because("Fixes CVE-2022-23181")
                }
                if (requested.group.startsWith("io.netty.incubator") &&
                        requested.name == "netty-incubator-codec-native-quic" &&
                        requested.version!! < "0.0.26.Final") {
                    useVersion("0.0.26.Final")
                    because("Fixes CVE-2019-20444, Brought in by reactor-netty, waiting for release of updated reactor-netty" +
                            " for this commit to take effect: " +
                            "https://github.com/reactor/reactor-netty/commit/857277287671d5b40708064b3afef1a7ae7b7a47")
                }
                if (requested.group.startsWith("junit") &&
                        requested.name == "junit" &&
                        requested.version!! < "4.2") {
                    useVersion("4.13.2")
                    because("Fixes CVE-2020-15250: Local information for the test rule TemporaryFolder.")
                }
            }
        }

    }
}

subprojects {
    tasks.register<DependencyReportTask>("allDeps") {}
}
