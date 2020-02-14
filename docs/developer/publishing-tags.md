# Publishing Tags and Releases

The goals of our tag publishing conventions are to be seamless and consistent. In general, a developer or deployer should only need to do one of the following things to publish the right tag:

- [release]
  - [git tag] Create a release and tag via the GitHub interface or create, commit, and push a tag to our repo.
  - [manual] Trigger locally via `./gradlew jib -Pversion=3.0.0-beta` (on any branch/state)
    - _WARNINGS:_
      - Be sure your manually-entered version does not conflict with existing published artifact tags. It _will_ be overwritten.
      - Manual releases will publish a semver tag, but no `git tag` is associated with the artifacts, so it's difficult to recreate and best suited to to non-official releases and release candidates like:
        - `-alpha`
        - `-beta`
        - `-RC1`
        - etc.
- [snapshots] push to a remote branch
- [local] ./gradlew jib


_NOTE:_ Running locally, with or without the release flag, the `DOCKER_USER` and `DOCKER_PASSWORD` environment variables will need to be set just as they are in the CI environment to publish to Docker Hub.
 
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
      - checkout:
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
- if build is tagged `CIRCLE_TAG=v?` and `CIRCLE_BRANCH=master`
  - set `jib`'s `to` image to:  
     - `"${version}"` (`CIRCLE_TAG` must be semantic version without the 'v' prefix) 
- else (tag is non-semantic or non-existent):
  - set `jib`'s `to` image to:  
     - `"${branch}-SNAPSHOT"` (branch = `CIRCLE_BRANCH` or "unknown" if unset)
     
## Cleanup Container Registry

Cleanup of the container registry (Docker Hub) is automated without being destructive or confusing about what a published tag represents.
 
The container registry API for DockerHub can be used to delete tags with the a token obtained from the credentials. The continuous integration environment should attempt to clean up the registry before publishing on every run.

### Release Tags (retain always)        
These tags are identified through regex pattern matching and without the `-SNAPSHOT` suffix:
```
"3.0.0"     [RETAINED] (semantic version and not snapshot)
"2.4.2"     [RETAINED] (semantic version and not snapshot)
"2.4.2-RC1" [RETAINED] (semantic version and not snapshot)
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
"456-featureB-SNAPSHOT" [PURGED] ("456-featureB" does NOT exist as branch on remote origin and not prefixed w/"LOCAL-")
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
"2.4"          [PURGED] (truncated semantic is no longer a convention we use)
"2.4-RC1"      [PURGED] (truncated semantic is no longer a convention we use)
"2"            [PURGED] (truncated semantic is no longer a convention we use)
"2-RC1"        [PURGED] (truncated semantic is no longer a convention we use)
"dangling-tag" [PURGED] (not semantic, not derived semantic, not prefixed w/"LOCAL-", and not suffixed w/"-SNAPSHOT")
```

## Local Automation
When the user is running the `jib` Gradle task from their development machine, the rules for publishing change so that custom published images can be distinguished and preserved in the CI cleanup process.

The rules to publish in a CI environment afford a certain amount of built-in safety to prevent overriding critical tagged releases. The only way for a local `jib` task to overwrite a published release would require the following conditions to be met:
- the CI environment variable evaluates to a boolean true
- the CIRCLE_BRANCH environment variable would need to be set and equal "master"
- the CIRCLE_TAG would need to be populated and be a valid semantic version, prefixed with "v"

If the above conditions are _NOT_ all met, then we assume we are in a "local" environment and build docker images and publish accordingly:
- `"LOCAL-${branch}-${whoAmI}"` (e.g. - "LOCAL-123-featureA-elliott")
  - the branch in this case is derived from a `git` command and *not* the CIRCLE_BRANCH env var
  - the `whoAmI` value is literally the output of the `whoami` command
 
The local user may not have a need for or regularly publish manually, so they may not have the `DOCKER_USER` and `DOCKER_PASSWORD` environment variables set at all times. These credentials are required by the Docker Hub container registry API to delete images. Because of this, non-CI environments, should never attempt to clean up old/obsolete images automatically.

## Gradle Task Considerations
In order to pull out clunky logic from the CI configuration itself, we can create an extension function on the Gradle `Project` object and dynamically determine the version at runtime instead of hard-coding it into a `gradle.properties` file.

Then, you can attempt to clean in CI environments before the `jib` (publish) task runs by providing an `doFirst` closure on the task:
```
# this version is calculated based on the environmental conditions described above
version = project.dynamicVersion()

// before jib executes, if we are in a CI environment, attempt to clean the container registry
tasks.getByName("jibDockerBuild") {
    doFirst {
        if(isCI()) {
            publish.cleanContainerRegistry()
        }
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