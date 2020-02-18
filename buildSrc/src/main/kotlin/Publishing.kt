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
        val registry: String,
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

    // TODO: remove this once we confirm the CI environment is triggered on tags/branch properly
    println("Publishing:::isRelease::tag/CIRCLE_TAG = $tag, branch/CIRCLE_BRANCH = $branch")
    println("isReleaseTag = $isReleaseTag, isSemanticNonSnapshot = $isSemanticNonSnapshot, isMasterBranch = $isMasterBranch")

    return isReleaseTag && isSemanticNonSnapshot && isMasterBranch
}

fun Project.dynamicVersion(): String {
    return if(isCI()) {
        // inside a CI environment
        println("dynamicVersion::: Determined we are IN the CI environment")
        if(isRelease()) {
            println("dynamicVersion::: Determined we are publishing an actual release tag")
            // publish official release (derived from CIRCLE_TAG)
            // tag = v3.0.0 -> version = 3.0.0
            val tag = tagCI() ?: ""
            val versionRelease = tag.removePrefix("v")
            // tags that don't start with "v" will not be published via jib
            versionRelease
        }
        else {
            println("dynamicVersion::: publishing a branch snapshot")
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
    val containerRegistry: ContainerRegistryInterface = when(this.registry) {
        Registries.DOCKER_HUB -> DockerHubAPI()
        Registries.GITLAB -> GitLabAPI()
        else -> return false
    }
    return containerRegistry.clean(this)
}

// use publishing info to derive standardized container labels

// https://www.opencontainers.org/
// https://github.com/opencontainers/image-spec/blob/master/annotations.md#annotations
fun Publish.ociAnnotations(): MutableMap<String, String> {
    val ociAnnotations: MutableMap<String, String> = mutableMapOf()
    ociAnnotations["org.opencontainers.image.created"] = this.created
    ociAnnotations["org.opencontainers.image.title"] = this.title
    ociAnnotations["org.opencontainers.image.description"] = this.description
    ociAnnotations["org.opencontainers.image.url"] = this.url
    ociAnnotations["org.opencontainers.image.vendor"] = this.vendor
    ociAnnotations["org.opencontainers.image.version"] = this.version
    ociAnnotations["org.opencontainers.image.licenses"] = this.licenses
    // only apply them in our CI/CD environment because they aren't meaningful in local builds
    if(isCI()) {
        ociAnnotations["org.opencontainers.image.documentation"] = this.documentation
        ociAnnotations["org.opencontainers.image.authors"] = this.authors
        ociAnnotations["org.opencontainers.image.source"] = this.source
        ociAnnotations["org.opencontainers.image.revision"] = this.revision
    }
    return ociAnnotations
}

// use publishing info to derive jib's `to.image` destination as ~ `registry/vendor/name:tag`
fun Publish.repository(): String {
    return when(this.registry) {
        // Docker Hub doesn't distinguish groups of containers by project
        Registries.DOCKER_HUB -> "${this.registry}/${this.vendor}/${this.title}:${this.version}"

        // GitLab Container Registry has additional pathway to project name
        Registries.GITLAB -> "${this.registry}/${this.vendor}/${this.project}/${this.title}:${this.version}"

        else -> return ""
    }
}