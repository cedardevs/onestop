import org.gradle.api.Project

val SEMANTIC_VERSION_REGEX: Regex = "[1-9]\\d*\\.\\d+\\.\\d+(?:-[a-zA-Z0-9]+)?".toRegex()

const val SUFFIX_SNAPSHOT = "-SNAPSHOT"
const val PREFIX_LOCAL = "LOCAL-"
const val BRANCH_MASTER = "master"

object Registries {
    const val DOCKER_HUB = "registry.hub.docker.com"
    const val GITLAB = "registry.gitlab.com"
}

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

fun Project.booleanProperty(prop: String): Boolean {
    return if(this.hasProperty(prop)) this.property(prop).toString().toBoolean() else false
}

fun Project.stringProperty(prop: String): String? {
    return if(this.hasProperty(prop)) this.property(prop).toString() else null
}

fun isSemantic(version: String): Boolean {
    return SEMANTIC_VERSION_REGEX.matches(version)
}

fun isSemanticNonSnapshot(version: String): Boolean {
    val isSemantic: Boolean = isSemantic(version)
    val isSnapshot: Boolean = isSnapshot(version)
    return isSemantic && !isSnapshot
}

fun isLocal(version: String): Boolean {
    return version.startsWith(PREFIX_LOCAL)
}

fun isLocalNonSnapshot(version: String): Boolean {
    val isLocal: Boolean = isLocal(version)
    val isSnapshot: Boolean = isSnapshot(version)
    return isLocal && !isSnapshot
}

fun isSnapshot(version: String): Boolean {
    return version.endsWith(SUFFIX_SNAPSHOT)
}

fun Project.versionProperty(): String? {
    val version: String = this.stringProperty("version") ?: ""
    val isSemanticNonSnapshot: Boolean = isSemanticNonSnapshot(version)
    return if (isSemanticNonSnapshot) version else null
}

fun isRelease(): Boolean {
    // we're in the CI environment and checking conditions for for an official release
    // in order to treat publishing differently than a regular branch snapshot
    val tag: String = tagCI() ?: ""
    val isReleaseTag = tag.startsWith("v")
    val version = tag.removePrefix("v")
    val isSemanticNonSnapshot = isSemanticNonSnapshot(version)
    val branch: String = branchCI() ?: ""
    val isMasterBranch = branch == BRANCH_MASTER
    return isReleaseTag && isSemanticNonSnapshot && isMasterBranch
}

fun Project.dynamicVersion(): String {
    return if(isCI()) {
        // inside a CI environment
        if(isRelease()) {
            // publish official release (derived from CIRCLE_TAG)
            // tag = v3.0.0 -> version = 3.0.0
            val tag = tagCI() ?: ""
            val versionRelease = tag.removePrefix("v")
            // tags that don't start with "v" will not be published via jib
            versionRelease
        }
        else {
            // otherwise publish branch snapshot (if CI doesn't have branch name for any reason, assume "unknown" name)
            // e.g -  "123-featureA-SNAPSHOT"
            val versionBranchSnapshot = "${branchCI() ?: "unknown"}${SUFFIX_SNAPSHOT}"
            versionBranchSnapshot
        }
    }
    else {
        // outside a CI environment

        // manual publishing from local environment
        // if the `-Pversion=${versionProperty}` gradle flag is semantic and not with a `-SNAPSHOT` suffix,
        // e.g. - `./gradlew jib -Pversion=3.0.0-RC1`

        // otherwise, default to the `LOCAL-` prefix convention
        // e.g. - "LOCAL-featureA-elliott"
        this.versionProperty() ?: "${PREFIX_LOCAL}${branch()}-${whoAmI()}"
    }
}

// Continuous Integration Environment Utilities
fun isCI(): Boolean {
    return (System.getenv("CI") ?: "").toBoolean()
}

fun branchCI(): String? {
    return System.getenv("CIRCLE_BRANCH")
}

fun tagCI(): String? {
    return System.getenv("CIRCLE_TAG")
}

// Git Utilities
fun sourceUrl(): String {
    return "git config --get remote.origin.url".runShell() ?: "unknown"
}

fun branch(): String {
    return "git symbolic-ref --short HEAD".runShell() ?: "unknown"
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

// Container Registry Utilities
fun Publish.cleanContainerRegistry(): Boolean {
    val containerRegistry: ContainerRegistryInterface = when(this.registryUrl) {
        Registries.DOCKER_HUB -> DockerHubAPI()
        Registries.GITLAB -> GitLabAPI()
        else -> return false
    }
    return containerRegistry.clean(this)
}