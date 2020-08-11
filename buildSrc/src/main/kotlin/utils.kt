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

    const val NODE: String = "10.16.3"
    const val NPM: String = "6.14.0"

    const val ELASTIC: String = "7.6.2"
    const val CONFLUENT: String = "5.4.0"
    const val KAFKA: String = "2.4.0"
    const val SPRING_KAFKA: String = "2.4.1.RELEASE"
    const val AVRO: String = "1.9.1"

    const val GROOVY: String = "2.5.8"
    const val SPOCK: String = "1.2-groovy-2.5"
    const val TEST_CONTAINERS: String = "1.12.2"
    const val OPEN_SAML = "3.4.3"
    const val LOGBACK = "1.2.3"
    const val SLF4J = "1.7.28"
    const val JAVAX_SERVLET_API = "4.0.1"
    const val JUNIT = "4.12"
    const val AUTH0_JAVA_JWT = "3.4.1"
    const val PAC4J = "3.8.3"
    const val SNAKE_YAML = "1.24"
    const val REACTOR_BOM = "Dysprosium-SR7"
    const val JSONP = "2.0.0-RC2"

    const val ONESTOP_SCHEMAS: String = "0.7.0"
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
