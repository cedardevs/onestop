import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.moowork.gradle.node.npm.NpmTask
import com.moowork.gradle.node.task.NodeTask
import java.time.Instant
import java.time.format.DateTimeFormatter

plugins {
    `kotlin-dsl`

    // OWASP Dependency Check Gradle Plugin
    // https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/index.html
    // - provides monitoring of the projects dependent libraries;
    //   creating a report of known vulnerable components that are included in the build.
    id("org.owasp.dependencycheck").version("5.3.0")

    // Note: The plugins below are not universally `apply(true)`because subprojects only need them conditionally.

    // Kotlin Plugin
    // - In case we ever want to use Kotlin on a project targeted on the JVM
    kotlin("jvm").version("1.3.61").apply(false)

    // Jib plugin
    // https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin
    // - Jib is a Gradle plugin for building Docker and OCI images for your Java applications.
    id("com.google.cloud.tools.jib").version("2.0.0").apply(false)

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
    id("io.spring.dependency-management").version("1.0.6.RELEASE").apply(false)

    // Spring Boot plugin
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/
    // - A Gradle plugin that allows you to package executable jar or war archives,
    //   run Spring Boot applications, and use the dependency management provided by spring-boot-dependencies
    id("org.springframework.boot").version("2.2.4.RELEASE").apply(false)

    // Gogradle plugin
    // https://github.com/gogradle/gogradle
    // - A Gradle Plugin Providing Full Support for Go
    id("com.github.blindpirate.gogradle").version("0.11.4").apply(false)

}

// resolve build dependencies from Bintray jcenter
repositories {
    jcenter()
}

group = "org.cedar.onestop"

version = versionCICD()

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
val jibProjects: List<String> = listOf("client", "cli", "indexer", "search", "registry", "search", "stream-manager", "user")
val springBootProjects: List<String> = listOf("search", "registry")
val nodeProjects: List<String> = listOf("client", "registry")
val micronautProjects: List<String> = listOf("user")
val goProjects: List<String> = listOf("cli")

allprojects {
    // resolve all subproject dependencies from Bintray jcenter and jitpack
    repositories {
        jcenter()
        maven(url = "https://jitpack.io")
    }
}

subprojects {

    // capture build metadata for common naming conventions, publishing, etc.
    val created: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    val projectName: String = "${rootProject.name}-${name}"
    val projectVersion: String = rootProject.version as String
    val description: String = projectDescriptions.getOrDefault(name, defaultValue = "The ${rootProject.name}-${name} project.")

    // version control
    val sourceUrl: String = sourceUrl() // git remote repository url
    val revision: String = revision()   // git commit hash

    val gitUrl: String = "https://github.com/cedardevs/onestop.git"
    val projectUrl: String = "https://github.com/cedardevs/onestop"
    val issuesUrl: String = "https://github.com/cedardevs/onestop/issues"

    val url: String = "https://data.noaa.gov/onestop"
    val docs: String = "https://cedardevs.github.io/onestop"

    if (javaProjects.contains(name)) {
        // apply java gradle plugin to projects using java
        apply(plugin = "java")

        // in case we are still using groovy on any of our projects
        apply(plugin = "groovy")

        // TODO: apply(plugin =  "kotlin")?

        extra.apply {
            set("Versions", Versions)
        }
    }
    if (jibProjects.contains(name)) {
        // apply jib gradle plugin to projects using jib
        apply(plugin = "com.google.cloud.tools.jib")

        // apply a common publishing pattern to all projects using jib
        val publish = Publish(
                created = created,
                title = projectName,
                project = rootProject.name,
                description = description,
                documentation = docs,
                authors = formatAuthors(authors),
                url = url,
                source = sourceUrl,
                revision = revision,
                vendor = "cedardevs",
                version = projectVersion,
                licenses = License.MIT,
                registryUrl = Registries.DOCKER_HUB,
                username = environment("DOCKER_USER"),
                password = environment("DOCKER_PASSWORD")
        )
        extra.apply {
            set("publish", publish)
            set("repo", repository(publish))
            set("ociAnnotations", ociAnnotations(publish))
        }


        tasks.getByName("jib") {
            onlyIf {
                false
            }
        }
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
                license = License.MIT,
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
    if(micronautProjects.contains(name)) {

        apply(plugin = "application")

        // for creating fat/uber JARs
        apply(plugin = "com.github.johnrengelman.shadow")

        val developmentOnly: Configuration by configurations.creating

        dependencies {
            annotationProcessor(platform("io.micronaut:micronaut-bom:${Versions.MICRONAUT}"))
            annotationProcessor("io.micronaut:micronaut-inject-java")
            annotationProcessor("io.micronaut:micronaut-validation")
            implementation(platform("io.micronaut:micronaut-bom:${Versions.MICRONAUT}"))
            implementation("io.micronaut:micronaut-management")
            implementation("io.micronaut.kubernetes:micronaut-kubernetes-discovery-client")
            implementation("io.micronaut:micronaut-inject")
            implementation("io.micronaut:micronaut-validation")
            implementation("io.micronaut:micronaut-runtime")
            implementation("javax.annotation:javax.annotation-api")
            implementation("io.micronaut:micronaut-http-server-netty")
            implementation("io.micronaut:micronaut-http-client")
            implementation("io.micronaut.configuration:micronaut-kafka")
            runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
            testAnnotationProcessor(platform("io.micronaut:micronaut-bom:${Versions.MICRONAUT}"))
            testAnnotationProcessor("io.micronaut:micronaut-inject-java")
            testImplementation(platform("io.micronaut:micronaut-bom:${Versions.MICRONAUT}"))
            testImplementation("org.junit.jupiter:junit-jupiter-api")
            testImplementation("io.micronaut.test:micronaut-test-junit5")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
        }

        // use JUnit 5 platform
        tasks.withType<Test> {
            classpath += developmentOnly
            useJUnitPlatform()
        }
        tasks.withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.compilerArgs.add("-parameters")
        }

        tasks.withType<ShadowJar> {
            mergeServiceFiles()
        }

        tasks.named<JavaExec>("run") {
            classpath += developmentOnly
            jvmArgs("-noverify", "-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote")
        }
    }
    if(goProjects.contains(name)) {
        // apply the Gogradle plugin to projects using Go
        apply(plugin = "com.github.blindpirate.gogradle")
    }
}