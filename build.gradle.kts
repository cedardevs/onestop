import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.moowork.gradle.node.npm.NpmTask
import com.moowork.gradle.node.task.NodeTask
import java.time.Instant
import java.time.format.DateTimeFormatter

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
    id("org.owasp.dependencycheck").version("5.3.2.1")

    // Note: The plugins below are not universally `apply(true)`because subprojects only need them conditionally.

    // Kotlin Plugin
    // - In case we ever want to use Kotlin on a project targeted on the JVM
    kotlin("jvm").version("1.3.61").apply(false)

    // Jib plugin
    // https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin
    // - Jib is a Gradle plugin for building Docker and OCI images for your Java applications.
    id("com.google.cloud.tools.jib").version("2.2.0").apply(false)

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
    id("io.spring.dependency-management").version("1.0.9.RELEASE").apply(false)

    // Spring Boot plugin
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/
    // - A Gradle plugin that allows you to package executable jar or war archives,
    //   run Spring Boot applications, and use the dependency management provided by spring-boot-dependencies
    id("org.springframework.boot").version("2.3.1.RELEASE").apply(false)

    // Gogradle plugin
    // https://github.com/gogradle/gogradle
    // - A Gradle Plugin Providing Full Support for Go
    id("com.github.blindpirate.gogradle").version("0.11.4").apply(false)

}

// resolve build dependencies from Bintray jcenter
repositories {
    jcenter()
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
        Pair("cli", "A command-line interface to query the OneStop search API."),
        Pair("e2e-tests", "End-to-end test project for the OneStop system."),
        Pair("elastic-common", "A shared project used by OneStop applications interacting with Elastic"),
        Pair("geoportal-search", "An application supporting OpenSearch and CSW search standards against the OneStop system."),
        Pair("indexer", "A Kafka Streams app which picks up from the parsed metadata topic, flattens granules of collections, and populates Elasticsearch indices."),
        Pair("kafka-common", "A shared project used by OneStop applications interacting with Kafka"),
        Pair("registry", "A private API to upload ISO metadata to the OneStop system Kafka event stream."),
        Pair("search", "An read-only API for the OneStop system to query data indexed in Elasticsearch."),
        Pair("stream-manager", "A Kafka Streams app which picks up from the raw metadata topic, parses into a standard format, analyzes some fields for further insight, and places onto a parsed topic."),
        Pair("user", "An API to authenticate and manage public user data of the OneStop system.")
)

// only apply plugins, configuration, tasks, etc. to projects that need it
val javaProjects: List<String> = listOf("client", "cli", "indexer", "e2e-tests", "elastic-common", "kafka-common", "search", "registry", "stream-manager", "user")
val jibProjects: List<String> = listOf("client", "cli", "indexer", "registry", "search", "stream-manager", "user")
val springBootProjects: List<String> = listOf("elastic-common", "search", "registry")
val nodeProjects: List<String> = listOf("client", "registry")
val micronautProjects: List<String> = listOf("user")
val goProjects: List<String> = listOf("cli")

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
    // resolve all subproject dependencies from Bintray jcenter and jitpack
    repositories {
        jcenter()
        maven(url= "https://repo.spring.io/milestone")
        maven(url = "https://packages.confluent.io/maven/")
        maven(url = "https://jitpack.io")
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
    if (nodeProjects.contains(name)) {
        // apply node gradle plugin to projects using node/npm
        // TODO: revert to original (non-forked) moowork plugin when Gradle 6 issues are resolved
        apply(plugin = "com.github.node-gradle.node")
        //apply(plugin = "com.moowork.node")

        // apply a common node/npm version to all projects using node
        configure<com.moowork.gradle.node.NodeExtension> {
            version = Versions.NODE
            npmVersion = Versions.NPM
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

    if(goProjects.contains(name)) {
        // apply the Gogradle plugin to projects using Go
        apply(plugin = "com.github.blindpirate.gogradle")
    }

    afterEvaluate {
        // override versions of dependencies with vulnerabilities
        configurations.all {
            resolutionStrategy.eachDependency {

                if (requested.group == "org.apache.santuario" && requested.name == "xmlsec") {
                    if (requested.version!!.startsWith("2.0") && requested.version!! <= "2.1.4") {
                        useVersion("2.1.4")
                        because("fixes CVE-2019-12400")
                    }
                }

                if (requested.group == "org.apache.avro" && requested.name == "avro") {
                    if(requested.version!! < Versions.AVRO) {
                        useVersion(Versions.AVRO)
                        because("latest avro does not depend on vulnerable jackson-mapper-asl which has not been updated since 2013")
                    }
                }

                if (requested.group == "org.apache.logging.log4j" && requested.name == "log4j-api") {
                    if (requested.version!!.startsWith("2.11.1")) {
                        useVersion("2.13.3")
                        because("fixes vulnerability in 2.11.1 and before")
                    }
                }

                if (requested.group == "org.bouncycastle" && requested.name == "bcprov-jdk15on") {
                    if (requested.version!!.startsWith("1.63")) {
                        useVersion("1.65")
                        because("fixes vulnerability in 1.63 and before")
                    }
                }

                if (requested.group == "com.fasterxml.jackson.core" && requested.name == "jackson-databind") {
                    if (requested.version!!.startsWith("2.9.") || requested.version!!.startsWith("2.10.") ) {
                        useVersion("2.10.1")
                        because("fixes vulnerability in 2.9.9 and before")
                    }
                }

                if (requested.group == "com.google.guava" && requested.name == "guava") {
                    if (requested.version!! <= "27.0.1") {
                        useVersion("27.0.1-jre")
                        because("fixes CVE-2018-10237")
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
                        requested.version!! <= "9.0.29") {
                    useVersion("9.0.37")
                    because("Enforce tomcat 9.0.20+ to avoid vulnerabilities CVE-2019-0199, CVE-2019-0232, and CVE-2019-10072")
                }
            }
        }

    }


}