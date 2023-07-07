import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.*

// constants
object License {
    const val GPL20: String = "GPL-2.0"
}

object Versions {
    // this must be a valid semantic version for some downstream commands
    // like `npm version ${version}` that expect such a format, but having
    // a default allows us to spot when a published docker image was created locally, for example
    const val DEFAULT: String = "0.0.0"

    // https://www.opencontainers.org/
    // https://github.com/opencontainers/image-spec/blob/master/annotations.md#annotations
    const val LABEL_SCHEMA: String = "1.0"

    const val NODE: String = "16.19.1"
    const val NPM: String = "8.4.1"

    const val ELASTIC: String = "7.17.9"
    const val CONFLUENT: String = "7.3.2"
    const val KAFKA: String = "${CONFLUENT}-ccs"
    const val SPRING_KAFKA: String = "3.1.2.RELEASE"
    const val AVRO: String = "1.11.1"

    const val GROOVY: String = "3.0.9"
    const val SPOCK: String = "2.2-M1-groovy-3.0"
    const val ELASTIC_SERVER: String = "7.17.9" // When changed update references to ElasticsearchVersion and ./circleci/config.yml. This used by TestContainers.
    const val TEST_CONTAINERS: String = "1.16.3"
    const val OPEN_SAML = "3.4.3"
    const val LOGBACK = "1.4.4"
    const val SLF4J = "2.0.3"
    const val JAVAX_SERVLET_API = "4.0.1"
    const val JUNIT = "4.12"
    const val AUTH0_JAVA_JWT = "3.4.1"
    const val PAC4J = "4.5.5"
    const val SNAKE_YAML = "1.33"
    const val REACTOR_BOM = "Dysprosium-SR7"
    const val JSONP = "2.0.0-RC2"
    const val JACKSON_CORE = "2.14.2" // A lot of other dependencies bring this in though.

    const val ONESTOP_SCHEMAS: String = "0.7.6"

    // From schemas subproject
    const val COMMONS_TEXT = "1.10.0"
}

val gitLabCICD: Boolean = System.getProperty("gitLabCICD").toBoolean()

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
