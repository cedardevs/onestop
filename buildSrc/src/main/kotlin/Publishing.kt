import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate
import java.time.Instant
import java.time.format.DateTimeFormatter

val SEMANTIC_VERSION_REGEX: Regex = "[1-9]\\d*\\.\\d+\\.\\d+(?:-[a-zA-Z0-9]+)?".toRegex()

const val SUFFIX_SNAPSHOT = "-SNAPSHOT"
const val PREFIX_LOCAL = "LOCAL-"
const val BRANCH_MASTER = "master"
const val ENV_BUILD_TAG = "BUILD_TAG"

enum class CI(val label: String, val envBranch: String, val envTag: String) {
    CIRCLE(
        label = "Circle CI",
        envBranch = "CIRCLE_BRANCH",
        envTag = "CIRCLE_TAG"
    ),
    GITLAB(
        label = "GitLab CI/CD",
        envBranch = "CI_COMMIT_BRANCH",
        envTag = "CI_COMMIT_TAG"
    ),
    TRAVIS(
        label = "Travis CI",
        envBranch = "TRAVIS_BRANCH",
        envTag = "TRAVIS_TAG"
    )
}

enum class Registry(
        val label: String,
        val host: String,
        val envUser: String,
        val envPassword: String,
        val envAccessToken: String,
        val api: ContainerRegistryInterface
) {
    // DockerHub can retrieve access token using username/password automatically
    DOCKER_HUB(
        label = "Docker Hub",
        host = "registry.hub.docker.com",
        envUser = "DOCKER_USER",
        envPassword = "DOCKER_PASSWORD",
        envAccessToken = "",
        api = DockerHubAPI()
    ),
    // GitLab uses `accessToken` directly for secure API calls
    GITLAB(
        label = "GitLab Container Registry",
        host = "registry.gitlab.com",
        envUser = "",
        envPassword = "",
        envAccessToken = "GITLAB_ACCESS_TOKEN",
        api = GitLabAPI()
    )
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
        val isCI: Boolean,

        val isBuildTag: Boolean
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

        // REQUIRED:
        // this is the task gradle uses to publish based on the info in this class (e.g. - "jib")
        // this clean task for the container registry will run before this task in CI environments,
        // and this task will be prevented from running if `enabled = false`
        val task: String
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

fun isBuildTag(envBuildTag: String): Boolean {
    val buildTag: String = buildTag(envBuildTag) ?: ""
    val isBuildTagPrefixed = buildTag.startsWith("v")
    val buildVersion = buildTag.removePrefix("v")
    val isTagMatch = !tagDiff(buildTag)
    val isSemanticNonSnapshot = isSemanticNonSnapshot(buildVersion)
    // the build tag is specified in the environment and must:
    // - exist in repo and match the `git diff` against the real tag
    // - have the expected "v" prefix
    // - be a semantic, non-snapshot
    return isTagMatch && isBuildTagPrefixed && isSemanticNonSnapshot
}

fun isReleaseBranch(ci: CI): Boolean {
    val branch: String = branchCI(ci) ?: ""
    return when(ci) {
        CI.CIRCLE -> branch.isBlank()        // CircleCI does not set the CIRCLE_BRANCH env var when building tags
        CI.GITLAB -> branch == BRANCH_MASTER // TODO: determine if GitLab has an empty branch env var or sets == 'master' when tag env var is set
        CI.TRAVIS -> branch == BRANCH_MASTER // TODO: determine if Travis has an empty branch env var or sets == 'master' when tag env var is set
    }
}

fun isRelease(ci: CI): Boolean {
    // we're in the CI environment and checking conditions for for an official release
    // in order to treat publishing differently than a regular branch snapshot
    val tag: String = tagCI(ci) ?: ""
    val isReleaseTag = tag.startsWith("v")
    val version = tag.removePrefix("v")

    return isReleaseTag && isSemanticNonSnapshot(version) && isReleaseBranch(ci)
}

fun logInfo(ci: CI, registry: Registry, version: String, envBuildTag: String) {

    val isBuildTag = isBuildTag(envBuildTag)
    val buildTag = buildTag(envBuildTag)

    val isCI = isCI(ci)
    val branchCI = branchCI(ci)
    val logEnvironment: String = if(isBuildTag) "Tag-Based Build [$envBuildTag is defined]" else if(isCI) "${ci.label} [CI=true]" else "Local [CI=false|null]"
    val logBranch: String = if(isBuildTag) "N/A" else if(isCI) "${ci.envBranch}=${branchCI}" else branch()
    val logTag: String = if(isBuildTag) "$envBuildTag=$buildTag" else if(isCI) "${ci.envTag}=${tagCI(ci)}" else "N/A"
    val logRegistry: String = if(isBuildTag) "publishing disabled in tag-based builds" else "${registry.label} (${registry.host})"


    val logReleaseCI: String = """
[Official Release]
    ✓ in CI environment (${ci.label})
    ✓ tag has 'v' prefix before version
    ✓ version is semantic without '$SUFFIX_SNAPSHOT' suffix
    ✓ ${ci.envBranch}='${branchCI}', as expected for tagged builds in ${ci.label}
""".trimIndent()

    val logReleaseLocal: String = """
[Unofficial Release]
    ✓ in local environment
    ✓ version set in gradle task with '-Pversion=' 
    ✓ version is semantic without '$SUFFIX_SNAPSHOT' suffix
    """.trimIndent()

    val logRelease = when {
        isBuildTag -> "[Tag-Based Build]"
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
    Registry: $logRegistry
 
     Version: $version
--------------------------------------
 $logRelease
--------------------------------------
""".trimIndent()

    print(info)
}

// ROOT PROJECT
fun Project.dynamicVersion(vendor: String, envBuildTag: String = ENV_BUILD_TAG, ci: CI, registry: Registry): String {

    var calculatedVersion: String

    val isCI: Boolean = isCI(ci)
    val isRelease: Boolean = isRelease(ci)

    calculatedVersion = if(isCI) {
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

    // the build env has set an build tag which overrides any calculated version and prevents publishing
    val buildTag = buildTag(envBuildTag)
    val isBuildTag = isBuildTag(envBuildTag)
    if(buildTag != null) {
        if (isBuildTag) {
            // env var has been set and is valid for a tag-based build
            calculatedVersion = buildTag.removePrefix("v")
        }
        else {
            // env var has been set and is NOT valid for a tag-based build (exit before something bad happens)
            throw GradleException("The $envBuildTag='$buildTag' environment variable is set, but it is not a valid tag!")
        }
    }

    // create the default publishing information, prior to the user filling in the gaps/details
    this.extra.apply {
        set("publishShared", PublishShared(
                vendor = vendor,
                project = rootProject.name,
                version = calculatedVersion,
                created = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                source = sourceUrl(),
                revision = revision(),
                registry = registry,
                username = registryUser(registry),
                password = registryPassword(registry),
                accessToken = registryAccessToken(registry),
                isCI = isCI,
                isBuildTag = isBuildTag
        ))
    }

    logInfo(ci, registry, calculatedVersion, envBuildTag)
    return calculatedVersion
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
            task = publish.task
    )

    // place the merged publish object onto the extra properties of this project
    this.extra.apply {
        set("publish", publishMerged)
    }

    // clean the container registry
    this.tasks.register("cleanContainerRegistry") {
        // only attempt regular cleanup while building in the CI environment
        if(publishMerged.isCI == true) {
            publishMerged.cleanContainerRegistry()
        }
    }

    this.tasks.getByName(publishMerged.task) {
        // only run the publish task if it's not disabled
        onlyIf { !publishShared.isBuildTag }

        // the publish task tries to clean the container registry before publishing
        dependsOn("cleanContainerRegistry")
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

fun buildTag(envBuildTag: String): String? {
    return System.getenv(envBuildTag)
}

fun branchCI(ci: CI): String? {
    return System.getenv(ci.envBranch)
}

fun tagCI(ci: CI): String? {
    return System.getenv(ci.envTag)
}

// Git Utilities
fun sourceUrl(): String {
    return "git config --get remote.origin.url".runShell() ?: "unknown"
}

fun branch(): String {
    return "git symbolic-ref --short HEAD".runShell() ?: "unknown"
}

fun tags(): Set<String> {
    return "git tag | sort".runShell()?.lines()?.toSet() ?: setOf()
}

fun tagDiff(tag: String): Boolean {
    val exitCode: Int = "git diff tags/${tag} --quiet".runShellExitCode()
    return exitCode != 0
}

fun revision(): String {
    return "git rev-parse --short HEAD".runShell() ?: "unknown"
}

fun whoAmI(): String {
    return "whoami".runShell() ?: "unknown"
}

// Container Registry Utilities
fun registryUser(registry: Registry): String {
    return System.getenv(registry.envUser) ?: ""
}

fun registryPassword(registry: Registry): String {
    return System.getenv(registry.envPassword) ?: ""
}

fun registryAccessToken(registry: Registry): String {
    return System.getenv(registry.envAccessToken) ?: ""
}

fun Publish.cleanContainerRegistry(): Boolean {
    return if(this.registry != null) {
        this.registry.api.clean(this)
    } else {
        false
    }
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
        Registry.DOCKER_HUB -> "${this.registry.host}/${this.vendor}/${this.title}:${this.version}"

        // GitLab Container Registry has additional pathway to project name
        Registry.GITLAB -> "${this.registry.host}/${this.vendor}/${this.project}/${this.title}:${this.version}"

        else -> return ""
    }
}