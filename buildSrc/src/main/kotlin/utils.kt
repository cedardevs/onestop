import org.gradle.api.Project
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.*

// constants
object License {
    const val MIT: String = "GPL-2.0"
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

// utility functions
fun environment(variable: String, default: String = ""): String {
    return (System.getenv(variable) ?: default).trim()
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

// convert ISO 8601 string to a Date object
fun parseDateISO(date: String): Date {
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    val accessor: TemporalAccessor = timeFormatter.parse(date)
    return Date.from(Instant.from(accessor))
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