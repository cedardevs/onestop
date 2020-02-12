# Publishing Tags and Releases

The goals of our tag publishing conventions are to be seamless and consistent. In general, a developer or deployer should only need to do one of the following things to publish the right tag:

- [release] create a release and tag via the GitHub interface or create, commit, and push a tag to our repo
- [snapshots] push to a remote branch
- [local] ./gradlew jib (requires `DOCKER_USER` and `DOCKER_PASSWORD` to be set in environment)
 
Cleanup of the container registry (Docker Hub) is automated without being destructive or confusing about what a published tag represents.

## Circle CI Configuration
For Circle CI to [triggers builds from tags](https://circleci.com/docs/2.0/configuration-reference/#tags), it cannot be defined at the workflow level:
 
> CircleCI does not run workflows for tags unless you explicitly specify tag filters. Additionally, if a job requires any other jobs (directly or indirectly), you must specify tag filters for those jobs.


The `branches` filter is not used because we want all branches to trigger a Circle CI Build, and we rely on gradle to set the version/tag appropriately based on environment.

The job `tags` filter is defined for every job to prevent -- as much as possible -- random tags from triggering a build/publish. The insanely long regex is a way to capture only tags that match the semantic versioning format; however, gradle still ensures other constraints are met before officially pushing to the container registry.
 
```
jobFilters: &jobFilters
  filters:
    tags:
      only: /(?<=^[Vv]|^)(?:(?<major>(?:0|[1-9](?:(?:0|[1-9])+)*))[.](?<minor>(?:0|[1-9](?:(?:0|[1-9])+)*))[.](?<patch>(?:0|[1-9](?:(?:0|[1-9])+)*))(?:-(?<prerelease>(?:(?:(?:[A-Za-z]|-)(?:(?:(?:0|[1-9])|(?:[A-Za-z]|-))+)?|(?:(?:(?:0|[1-9])|(?:[A-Za-z]|-))+)(?:[A-Za-z]|-)(?:(?:(?:0|[1-9])|(?:[A-Za-z]|-))+)?)|(?:0|[1-9](?:(?:0|[1-9])+)*))(?:[.](?:(?:(?:[A-Za-z]|-)(?:(?:(?:0|[1-9])|(?:[A-Za-z]|-))+)?|(?:(?:(?:0|[1-9])|(?:[A-Za-z]|-))+)(?:[A-Za-z]|-)(?:(?:(?:0|[1-9])|(?:[A-Za-z]|-))+)?)|(?:0|[1-9](?:(?:0|[1-9])+)*)))*))?(?:[+](?<build>(?:(?:(?:[A-Za-z]|-)(?:(?:(?:0|[1-9])|(?:[A-Za-z]|-))+)?|(?:(?:(?:0|[1-9])|(?:[A-Za-z]|-))+)(?:[A-Za-z]|-)(?:(?:(?:0|[1-9])|(?:[A-Za-z]|-))+)?)|(?:(?:0|[1-9])+))(?:[.](?:(?:(?:[A-Za-z]|-)(?:(?:(?:0|[1-9])|(?:[A-Za-z]|-))+)?|(?:(?:(?:0|[1-9])|(?:[A-Za-z]|-))+)(?:[A-Za-z]|-)(?:(?:(?:0|[1-9])|(?:[A-Za-z]|-))+)?)|(?:(?:0|[1-9])+)))*))?)$/

workflows:
  version: 2
  build:
    jobs:
      - checkout
          <<: *jobFilters
      - client:
          requires:
            - checkout
          <<: *jobFilters
      - registry:
          requires:
            - checkout
          <<: *jobFilters
      ...

```
 
## Continuous Integration Automation
 
Many CI environments, including Circle CI set an environment variable `CI` to facilitate detecting the environment of the build. In the case that we have detected `CI=true`:
- if build is tagged `CIRCLE_TAG=?` with semantic version format and `CIRCLE_BRANCH=master`
  - flag the build as `isRelease=true` and leverage `jib`'s ability to publish multiple `to.tags`:  
     - `"${version}"` (release semantic version: `version = CIRCLE_TAG`) 
     - `"${versionNoPatch}"` (e.g. version: `2.4.2` -> `2.4`, or version: `2.4.2-RC1` -> `2.4-RC1`)
     - `"${versionNoPatch}"` (e.g. version: `2.4.2` -> `2`, or version: `2.4.2-RC1` -> `2-RC1`)
- else (tag is non-semantic or non-existent):
  - flag the build as `isRelease=false` and use `jib`'s regular `to` full image format with single tag:
     - `"${branch}-SNAPSHOT"` (branch = `CIRCLE_BRANCH`)
     
## Cleanup Container Registry
 
The container registry API for DockerHub can be used to delete tags with the a token obtained from the credentials. The
continuous integration environment should attempt to clean up the registry before publishing on every run.

### Release Tags (retain always)        
These tags are identified through regex pattern matching and without the `-SNAPSHOT` suffix:
```
"3.0.0"     [RETAINED] (semantic version and not snapshot)
"2.4.2"     [RETAINED] (semantic version and not snapshot)
"2.4.2-RC1" [RETAINED] (semantic version and not snapshot)
"2.4"       [RETAINED] (semantic derivative and not snapshot)
"2.4-RC1"   [RETAINED] (semantic derivative and not snapshot)
"2"         [RETAINED] (semantic derivative and not snapshot)
"2-RC1"     [RETAINED] (semantic derivative and not snapshot)
```

### Branch Tags
- collect all tags from container registry API that have the `-SNAPSHOT` suffix and NOT the `LOCAL-` prefix
- retrieve all branches on the origin:
```
$ for branch in `git branch -r | grep -v HEAD`; do echo -e ${branch#"origin/"}; done | sort -r
> master
> 123-featureA
```

#### Active Branch Tags (retain):
The prefix before `-SNAPSHOT` can be intuited as the branch name which produced the last published snapshot for this tag:

```
"master-SNAPSHOT"       [RETAINED] ("master" branch should always exist on remote origin)
"123-featureA-SNAPSHOT" [RETAINED] ("123-featureA" exists as branch on remote origin)
```

#### Inactive Branch Tags (purge):
Purge any tags associated with a branch name that does not exist in the remote origin (e.g. - ["master", "123-featureA"])
```
"456-featureB-SNAPSHOT" [PURGED] (branch does NOT exist on remote origin and not prefixed w/"LOCAL-")
```

### Local Tags (retain):
- collect all tags from container registry API that have the `LOCAL-` prefix
- use all branches on origin calculated before

```
"LOCAL-${branch}-${whoAmI}" [RETAINED] (prefixed w/"LOCAL-")
```
         
### Dangling Tags (purge):
Purge all tags remaining that do NOT meet the following criteria:
- semantic or semantic derived (e.g. `2.4.2`, `2.4.2-RC1`, `2.4`, `2.4-RC1`, `2`, `2-RC1`, `3-demo`, etc)
- have `-SNAPSHOT` suffix
- have `LOCAL-` prefix
```
"dangling-tag" [PURGED] (not semantic, not derived semantic, not prefixed w/"LOCAL-", and not suffixed w/"-SNAPSHOT")
```

## Local Automation
When the user is running the `jib` Gradle task from their development machine, the rules for publishing change so that custom published images can be distinguished and preserved in the CI cleanup process.

The rules to publish in a CI environment afford a certain amount of built-in safety to prevent overriding critical tagged releases. The only way for a local `jib` task run to overwrite a published release would require the following conditions to be met:
- the Docker Hub credentials, `DOCKER_USER` and `DOCKER_PASSWORD` are correctly set in the environment.
- the CI environment variable evaluates to a boolean true
- the CIRCLE_BRANCH environment variable would need to be set and equal "master"
- the CIRCLE_TAG would need to be populated and be a valid semantic version

If the above conditions are not ALL met (except credentials), then we assume we are in a "local" environment and build docker images and publish accordingly:
- `"LOCAL-${branch}-${whoAmI}"` (e.g. - "LOCAL-123-featureA-elliott")
  - the branch in this case is derived from a `git` command and *not* the CIRCLE_BRANCH env var
  - the `whoAmI` value is literally the output of the `whoami` command
 
The local user may not have a need for or regularly publish manually, so they may not have the `DOCKER_USER` and `DOCKER_PASSWORD` environment variables set at all times. These credentials are required by the Docker Hub container registry API to delete images. Because of this, non-CI environments, should never attempt to clean up old/obsolete images automatically.

## Gradle Task Considerations
In order to pull out clunky logic from the CI configuration itself, we can provide an `onlyIf` closure to the `jib` task on all projects, which takes into account :
```
# these values are calculated based on the environmental conditions shown above
version = versionCICD()
val shouldPublish: boolean = shouldPublish(...)

tasks.getByName("jib") {
    onlyIf {
        shouldPublish
    }
}
```

## Verifying Image Contents
We use the [Open Containers Spec](https://github.com/opencontainers/image-spec/blob/master/annotations.md) to define image annotations.

`docker inspect image ${imageID}`
```
"Labels": {
    "org.opencontainers.image.created": "2020-02-11T17:34:37.930Z",
    "org.opencontainers.image.description": "A search API for the OneStop search software.",
    "org.opencontainers.image.ref.name": "onestop-client",
    "org.opencontainers.image.revision": "873be791", # Git Commit Hash 
    "org.opencontainers.image.source": "https://github.com/cedardevs/onestop.git",
    "org.opencontainers.image.vendor": "cedardevs",
    "org.opencontainers.image.version": "2.4.2"
}
```

The same image annotations can be seen via Kubernetes, using the following command:

`kubectl describe pod ${podName}`

## Using Docker Hub API

### Request all public tags (no credentials or token needed)
```
fun requestDockerHubTags(publish: Publish, url: String? = null, tags: JSONArray = JSONArray()): JSONArray {
    val urlTags = url ?: "${RegistryAPI.DOCKER_HUB}/repositories/${publish.vendor}/${publish.title}/tags"
    val response: Response = khttp.get(urlTags)
    val jsonResponse: JSONObject = response.jsonObject
    val urlNext: String = try {
        jsonResponse.getString("next")
    } catch (e: JSONException) {
        ""
    } as String

    if (urlNext.isBlank()) return tags

    val jsonTags: JSONArray = jsonResponse.getJSONArray("results")
    val jsonTagsRefined = JSONArray()
    jsonTags.forEach { t ->
        val tag: JSONObject = t as JSONObject

        val name = tag.getString("name")
        val lastUpdated = tag.getString("last_updated")
        val refinedTag = JSONObject(mapOf(Pair("name", name), Pair("last_updated", lastUpdated)))
        jsonTagsRefined.put(refinedTag)
    }
    return requestDockerHubTags(publish, urlNext, concat(tags, jsonTagsRefined))
}
```

### Retrieve token needed to call delete tag endpoint
```
# Retrieve Token needed to call delete tag endpoint
fun requestDockerHubToken(publish: Publish): String? {
    val urlToken = "${RegistryAPI.DOCKER_HUB}/users/login"
    val payload = mapOf(Pair("username", publish.username), Pair("password", publish.password))
    val response: Response = khttp.post(
            url = urlToken,
            headers = mapOf(Pair("Content-Type", "application/json")),
            data = JSONObject(payload)
    )
    if (response.statusCode == 200) {
        val jsonResponse: JSONObject = response.jsonObject
        return jsonResponse.getString("token")
    }
    return null
}
```

### Delete a tag
```
fun requestDockerHubRemoveTag(publish: Publish, tag: String): Boolean {
    val jwt: String? = requestDockerHubToken(publish)
    val urlDelete = "${RegistryAPI.DOCKER_HUB}/repositories/${publish.vendor}/${publish.title}/tags/${tag}"
    val response: Response = khttp.delete(
            url = urlDelete,
            headers = mapOf(Pair("Authorization", "JWT ${jwt ?: ""}"))
    )
    println("DELETE STATUS CODE = " + response.statusCode)
    return response.statusCode == 202
}
```