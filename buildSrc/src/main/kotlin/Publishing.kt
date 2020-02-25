import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate
import java.time.Instant
import java.time.format.DateTimeFormatter

val SEMANTIC_VERSION_REGEX: Regex = "[1-9]\\d*\\.\\d+\\.\\d+(?:-[a-zA-Z0-9]+)?".toRegex()

const val SUFFIX_SNAPSHOT = "-SNAPSHOT"
const val PREFIX_LOCAL = "LOCAL-"
const val BRANCH_MASTER = "master"

data class CIInfo (
        val label: String,
        val envBranch: String,
        val envTag: String
)
enum class CI(val info: CIInfo) {
    CIRCLE(CIInfo(
            label = "Circle CI",
            envBranch = "CIRCLE_BRANCH",
            envTag = "CIRCLE_TAG"
    )),
    GITLAB(CIInfo(
            label = "GitLab CI/CD",
            envBranch = "CI_COMMIT_BRANCH",
            envTag = "CI_COMMIT_TAG"
    )),
    TRAVIS(CIInfo(
            label = "Travis CI",
            envBranch = "TRAVIS_BRANCH",
            envTag = "TRAVIS_TAG"
    ))
}

data class RegistryInfo (
        val label: String,
        val host: String,
        val envUser: String,
        val envPassword: String,
        val envAccessToken: String,
        val api: ContainerRegistryInterface
)
enum class Registry(val info: RegistryInfo) {
    // DockerHub can retrieve access token using username/password automatically
    DOCKER_HUB(RegistryInfo(
            label = "Docker Hub",
            host = "registry.hub.docker.com",
            envUser = "DOCKER_USER",
            envPassword = "DOCKER_PASSWORD",
            envAccessToken = "",
            api = DockerHubAPI()
    )),
    // GitLab uses `accessToken` directly for secure API calls
    GITLAB(RegistryInfo(
            label = "GitLab Container Registry",
            host = "registry.gitlab.com",
            envUser = "",
            envPassword = "",
            envAccessToken = "GITLAB_ACCESS_TOKEN",
            api = GitLabAPI()
    ))
}

data class PublishShared(
        // fields needed to publish and derive information
        // such as repository address and OCI annotations
        val vendor: String,
        val project: String,
        val version: String,
        val created: String,
        val source: String,
        val revision: String,

        // container registry fields
        val registry: Registry,
        val username: String,
        val password: String,
        val accessToken: String,

        // continuous integration fields
        val isCI: Boolean
)

data class Publish(

        // fields needed to publish and derive information
        // such as repository address and OCI annotations
        val vendor: String? = null,
        val project: String? = null,
        val title: String? = null,
        val version: String? = null,
        val created: String? = null,
        val description: String? = null,
        val documentation: String? = null,
        val authors: String? = null,
        val url: String? = null,
        val source: String? = null,
        val revision: String? = null,
        val licenses: String? = null,

        // container registry fields
        val registry: Registry? = null,
        val username: String? = null,
        val password: String? = null,
        val accessToken: String? = null,

        // continuous integration fields
        val isCI: Boolean? = null,

        // an attempt will be made to clean the container registry before *this* task
        // recommended: task that publishes to container registry (e.g. - "jib")
        val cleanBefore: String? = null
)

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

fun isRelease(ci: CI): Boolean {
    // we're in the CI environment and checking conditions for for an official release
    // in order to treat publishing differently than a regular branch snapshot
    val tag: String = tagCI(ci) ?: ""
    val isReleaseTag = tag.startsWith("v")
    val version = tag.removePrefix("v")
    val isSemanticNonSnapshot = isSemanticNonSnapshot(version)
    val branch: String = branchCI(ci) ?: ""
    val isMasterBranch = branch == BRANCH_MASTER
    return isReleaseTag && isSemanticNonSnapshot && isMasterBranch
}

fun logInfo(ci: CI, registry: Registry, version: String) {
    val isCI = isCI(ci)
    val branchCI = branchCI(ci)
    val branchLocal = branch()
    val tagCI = tagCI(ci)
    val logEnvironment: String = if(isCI) "${ci.info.label} [CI=true]" else "Local [CI=false|null]"
    val logBranch: String = if(isCI) "${ci.info.envBranch}=${branchCI}" else branchLocal
    val logTag: String = if(isCI) "${ci.info.envTag}=${tagCI}" else "N/A"

    val logReleaseCI: String = """
[Official Release]
    ✓ in CI environment
    ✓ tag has 'v' prefix before version
    ✓ version is semantic without '$SUFFIX_SNAPSHOT' suffix
    ✓ building on release branch = '$BRANCH_MASTER'
""".trimIndent()

    val logReleaseLocal: String = """
[Unofficial Release]
    ✓ in local environment
    ✓ version set in gradle task with '-Pversion=' 
    ✓ version is semantic without '$SUFFIX_SNAPSHOT' suffix
    """.trimIndent()

    val logRelease = when {
        isCI && isRelease(ci) -> logReleaseCI
        !isCI && !version.startsWith(PREFIX_LOCAL) -> logReleaseLocal
        else -> "[NOT a release]"
    }

    val info: String = """
--------------------------------------
  _____ _____       __   _____ _____  
 / ____|_   _|     / /  / ____|  __ \ 
| |      | |      / /  | |    | |  | |
| |      | |     / /   | |    | |  | |
| |____ _| |_   / /    | |____| |__| |
 \_____|_____| /_/      \_____|_____/
  
--------------------------------------
 Environment: $logEnvironment
      Branch: $logBranch
         Tag: $logTag
    Registry: ${registry.info.label} (${registry.info.host})
 
     Version: $version
--------------------------------------
 $logRelease
--------------------------------------
""".trimIndent()

    print(info)
}

// ROOT PROJECT
fun Project.dynamicVersion(vendor: String, ci: CI, registry: Registry): String {

    val isCI: Boolean = isCI(ci)
    val isRelease: Boolean = isRelease(ci)

    val dynamicVersion: String =  if(isCI) {
        // inside a CI environment
        if(isRelease) {
            // publish official release (derived from CIRCLE_TAG)
            // tag = v3.0.0 -> version = 3.0.0
            val tag = tagCI(ci) ?: ""
            val versionRelease = tag.removePrefix("v")
            // tags that don't start with "v" will not be published via jib
            versionRelease
        }
        else {
            // otherwise publish branch snapshot (if CI doesn't have branch name for any reason, assume "unknown" name)
            // e.g -  "123-featureA-SNAPSHOT"
            val versionBranchSnapshot = "${branchCI(ci) ?: "unknown"}${SUFFIX_SNAPSHOT}"
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

    // create the default publishing information, prior to the user filling in the gaps/details
    this.extra.apply {
        set("publishShared", PublishShared(
                vendor = vendor,
                project = rootProject.name,
                version = dynamicVersion,
                created = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                source = sourceUrl(),
                revision = revision(),
                registry = registry,
                username = registryUser(registry),
                password = registryPassword(registry),
                accessToken = registryAccessToken(registry),
                isCI = isCI
        ))
    }

    logInfo(ci, registry, dynamicVersion)
    return dynamicVersion
}

// SUBPROJECT
fun Project.setPublish(publish: Publish) {

    // we get the default publishing data that was set in the `dynamicVersion` at the root project level
    val publishShared: PublishShared by rootProject.extra

    // allow the user to override any defaults
    // default any subproject-specific gaps which weren't be known at the root project level
    val publishMerged = Publish(
            vendor = publish.vendor ?: publishShared.vendor,
            project = publishShared.project,
            title = publish.title ?: "${publishShared.project}-${name}",
            version = publish.version ?: publishShared.version,
            created = publish.created ?: publishShared.created,
            description = publish.description ?: "The ${publishShared.project}-${name} project.",
            documentation = publish.documentation,
            authors = publish.authors,
            url = publish.url,
            source = publish.source ?: publishShared.source,
            revision = publish.revision ?: publishShared.revision,
            licenses = publish.licenses,
            registry = publish.registry ?: publishShared.registry,
            username = publish.username ?: publishShared.username,
            password = publish.password ?: publishShared.password,
            accessToken = publish.accessToken ?: publishShared.accessToken,
            isCI = publish.isCI ?: publishShared.isCI,
            cleanBefore = publish.cleanBefore
    )

    // place the merged publish object onto the extra properties of this project
    this.extra.apply {
        set("publish", publishMerged)
    }

    // attempt to clean the container registry before specified `cleanBefore` task name if in CI environment
    if(publishMerged.cleanBefore != null) {
        this.tasks.register("cleanContainerRegistry") {
            // only attempt regular cleanup while building in the CI environment
            if(publishMerged.isCI == true) {
                publishMerged.cleanContainerRegistry()
            }
        }
        this.tasks.getByName(publishMerged.cleanBefore) {
            dependsOn("cleanContainerRegistry")
        }
    }
}

// Continuous Integration Environment Utilities
fun isCI(ci: CI): Boolean {
    return when(ci) {
        CI.CIRCLE -> (System.getenv("CI") ?: "").toBoolean()
        CI.GITLAB -> (System.getenv("CI") ?: "").toBoolean()
        CI.TRAVIS -> (System.getenv("CI") ?: "").toBoolean()
    }
}

fun branchCI(ci: CI): String? {
    return System.getenv(ci.info.envBranch)
}

fun tagCI(ci: CI): String? {
    return System.getenv(ci.info.envTag)
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
fun registryUser(registry: Registry): String {
    return System.getenv(registry.info.envUser) ?: ""
}

fun registryPassword(registry: Registry): String {
    return System.getenv(registry.info.envPassword) ?: ""
}

fun registryAccessToken(registry: Registry): String {
    return System.getenv(registry.info.envAccessToken) ?: ""
}

fun Publish.cleanContainerRegistry(): Boolean {
    val containerRegistry: ContainerRegistryInterface = when(this.registry) {
        Registry.DOCKER_HUB -> DockerHubAPI()
        Registry.GITLAB -> GitLabAPI()
        else -> return false
    }
    return containerRegistry.clean(this)
}

// use publishing info to derive standardized container labels

// https://www.opencontainers.org/
// https://github.com/opencontainers/image-spec/blob/master/annotations.md#annotations
fun Publish.ociAnnotations(): MutableMap<String, String> {

    val ociAnnotations: MutableMap<String, String> = mutableMapOf()

    ociAnnotations["org.opencontainers.image.created"] = this.created ?: ""
    ociAnnotations["org.opencontainers.image.title"] = this.title ?: ""
    ociAnnotations["org.opencontainers.image.description"] = this.description ?: ""
    ociAnnotations["org.opencontainers.image.url"] = this.url ?: ""
    ociAnnotations["org.opencontainers.image.vendor"] = this.vendor ?: ""
    ociAnnotations["org.opencontainers.image.version"] = this.version ?: ""
    ociAnnotations["org.opencontainers.image.licenses"] = this.licenses ?: ""
    ociAnnotations["org.opencontainers.image.documentation"] = this.documentation ?: ""
    ociAnnotations["org.opencontainers.image.authors"] = this.authors ?: ""

    // only apply revision (git hash) in our CI/CD environment because it could be misleading in local builds
    if(this.isCI == true) {
        ociAnnotations["org.opencontainers.image.revision"] = this.revision ?: ""
    }
    else {
        ociAnnotations["org.opencontainers.image.revision"] = "local"
    }
    return ociAnnotations
}

// use publishing info to derive jib's `to.image` destination as ~ `registry/vendor/name:tag`
fun Publish.repository(): String {
    return when(this.registry) {
        // Docker Hub doesn't distinguish groups of containers by project
        Registry.DOCKER_HUB -> "${this.registry.info.host}/${this.vendor}/${this.title}:${this.version}"

        // GitLab Container Registry has additional pathway to project name
        Registry.GITLAB -> "${this.registry.info.host}/${this.vendor}/${this.project}/${this.title}:${this.version}"

        else -> return ""
    }
}