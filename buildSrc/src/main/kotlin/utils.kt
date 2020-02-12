import java.io.File

// constants
object License {
    const val MIT: String = "GPL-2.0"
}

object Registries {
    const val DOCKER_HUB = "registry.hub.docker.com"
    const val GITLAB = "registry.gitlab.com"
}

object Versions {
    // this must be a valid semantic version for some downstream commands
    // like `npm version ${version}` that expect such a format, but having
    // a default allows us to spot when a published docker image was created locally, for example
    const val DEFAULT: String = "0.0.0"

    // https://www.opencontainers.org/
    // https://github.com/opencontainers/image-spec/blob/master/annotations.md#annotations
    const val LABEL_SCHEMA: String = "1.0"

    const val MICRONAUT: String = "1.2.10"

    const val NODE: String = "10.16.3"
    const val NPM: String = "6.9.0"

    const val ELASTIC: String = "7.5.2"
    const val CONFLUENT: String = "5.4.0"
    const val KAFKA: String = "2.4.0"
    const val SPRING_KAFKA: String = "2.4.1.RELEASE"
    const val AVRO: String = "1.9.1"

    const val GROOVY: String = "2.5.8"
    const val SPOCK: String = "1.2-groovy-2.5"
    const val TEST_CONTAINERS: String = "1.12.2"
    const val OPEN_SAML = "3.4.3"
    const val LOGBACK = "1.2.3"
    const val JAVAX_SERVLET_API = "4.0.1"
    const val JUNIT = "4.12"
    const val AUTH0_JAVA_JWT = "3.4.1"
    const val PAC4J = "3.8.3"

    const val ONESTOP_SCHEMAS: String = "0.5.3"
}

// data classes
data class Author(
        val name: String,
        val email: String,
        val website: String
)

data class Publish(
        val created: String,
        val title: String,
        val project: String,
        val description: String,
        val documentation: String,
        val authors: String,
        val url: String,
        val source: String,
        val revision: String,
        val vendor: String,
        val version: String,
        val licenses: String,
        val registryUrl: String,
        val username: String,
        val password: String
)

// utility functions
fun environment(variable: String, default: String = ""): String {
    return (System.getenv(variable) ?: default).trim()
}

fun String.runShell(dir: File? = null, quiet: Boolean = true): String? {
    val builder: ProcessBuilder = ProcessBuilder("/bin/sh", "-c", this)
            .redirectErrorStream(true)
            .directory(dir)
    val process: Process = builder.start()
    val exitCode: Int = process.waitFor()
    var output: String? = process.inputStream.readBytes().toString(Charsets.UTF_8).trim()
    if(exitCode != 0) {
        // print stderr for visibility, since we return `null` on error
        println("\nError running: `${this}`\n")
        println("${output}\n")
        output = null
    }
    if(!quiet && exitCode == 0) {
        // print stdout when `quiet = false` to prevent excessive output by default
        println("\nRunning: `${this}`\n")
        println("${output}\n")
    }
    return output
}

fun isCI(): Boolean {
    return (System.getenv("CI") ?: "").toBoolean()
}

fun branchCI(): String {
    return System.getenv("CIRCLE_BRANCH") ?: ""
}

fun tagCI(): String {
    return System.getenv("CIRCLE_TAG") ?: ""
}

fun isSnapshot(): Boolean {
    val circleBuildNumber: String? = System.getenv("CIRCLE_BUILD_NUM")
    val isCircleBuild: Boolean = circleBuildNumber.isNullOrBlank()
    val circleTag: String? = System.getenv("CIRCLE_TAG")
    var isTag = false
    if(!circleTag.isNullOrBlank()) {
        isTag = circleTag.startsWith("v")
    }
    return isCircleBuild && !isTag
}

//fun shouldPublish(): Boolean {
//    return isCI() && branchCI() == "master" && !
//}

fun versionCICD(): String {
    // if we are in continuous integration environment, building off master branch, then
    return if(isCI() && branchCI() == "master") {
        // if this is a
        // determine if this is just a regular snapshot update or an official release tag
        if(isSnapshot()) "-SNAPSHOT" else ""
    } else {
        // likely we are running in a local dev env, so use a different suffix here
        // if we publish outside our CI environment, the `-SNAPSHOT` version won't be clobbered
        // by local devs running jib, for example
        "${branch()}-${whoAmI()}"
    }
}

fun sourceUrl(): String {
    return "git config --get remote.origin.url".runShell() ?: "unknown"
}

fun branch(): String {
    return "git symbolic-ref --short HEAD".runShell() ?: "unknown"
}

fun activeBranches(): List<String> {
    return "for branch in `git branch -r --no-merged | grep -v HEAD`; do printf  \"%s\\n\" \"\${branch#\"origin/\"}\"; done | sort -r"
            .runShell()?.lines() ?: listOf()
}

fun remoteBranchExists(branch: String): Boolean {
    return "git ls-remote --heads --exit-code origin $branch".runShell().isNullOrBlank().not()
}

fun revision(): String {
    return "git rev-parse --short HEAD".runShell() ?: "unknown"
}

fun whoAmI(): String {
    return "whoami".runShell() ?: "unknown"
}

fun formatAuthors(authors: Collection<Author>, pretty: Boolean = false): String {
    // Your Name <email@example.com> (http://example.com)
    val multipleAuthors: Boolean = authors.size > 1
    val prefix: String = if (pretty && multipleAuthors) "\n" else ""
    val separator: CharSequence = if(pretty) "\n" else  ", "
    val formattedAuthors: String = authors.joinToString(separator = separator) { (name, email, website) ->
        val formatted = "$name <${email}> (${website})"
        formatted.prependIndent(if(pretty && multipleAuthors) "\t- " else "")
    }
    return prefix+formattedAuthors
}

fun printMap(map: Map<String, String>) {
    var n = 0
    for((k,_) in map) {
        n = if (k.length > n) k.length else n
    }
    for ((k, v) in map) {
        println(String.format("> %-" + n + "s = %s", k, v))
    }
}

// use publishing info to derive standardized container labels

// https://www.opencontainers.org/
// https://github.com/opencontainers/image-spec/blob/master/annotations.md#annotations
fun ociAnnotations(publish: Publish): MutableMap<String, String> {
    val ociAnnotations: MutableMap<String, String> = mutableMapOf()
    ociAnnotations["org.opencontainers.image.created"] = publish.created
    ociAnnotations["org.opencontainers.image.title"] = publish.title
    ociAnnotations["org.opencontainers.image.description"] = publish.description
    ociAnnotations["org.opencontainers.image.url"] = publish.url
    ociAnnotations["org.opencontainers.image.vendor"] = publish.vendor
    ociAnnotations["org.opencontainers.image.version"] = publish.version
    ociAnnotations["org.opencontainers.image.licenses"] = publish.licenses
    // only apply them in our CI/CD environment because they aren't meaningful in local builds
    if(isCI()) {
        ociAnnotations["org.opencontainers.image.documentation"] = publish.documentation
        ociAnnotations["org.opencontainers.image.authors"] = publish.authors
        ociAnnotations["org.opencontainers.image.source"] = publish.source
        ociAnnotations["org.opencontainers.image.revision"] = publish.revision
    }
    return ociAnnotations
}

// use publishing info to derive jib's `to.image` destination as ~ `registry/vendor/name:tag`
fun repository(publish: Publish): String {
    return if(publish.registryUrl == Registries.GITLAB) {
        // GitLab Container Registry has additional pathway to project name
        "${publish.registryUrl}/${publish.vendor}/${publish.project}/${publish.title}:${publish.version}"
    } else {
        // Docker Hub doesn't distinguish groups of containers by project
        "${publish.registryUrl}/${publish.vendor}/${publish.title}:${publish.version}"
    }
}

// use publishing info to derive simple image name without registry info `vendor/name:tag`
fun image(publish: Publish): String {
    return if(publish.registryUrl == Registries.GITLAB) {
        // GitLab Container Registry has additional pathway to project name
        "${publish.vendor}/${publish.project}/${publish.title}:${publish.version}"
    } else {
        // Docker Hub doesn't distinguish groups of containers by project
        "${publish.vendor}/${publish.title}:${publish.version}"
    }
}