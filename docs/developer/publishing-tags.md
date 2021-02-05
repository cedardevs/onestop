<div align="center"><a href="/onestop/developer">Developer Documentation Home</a></div>
<hr>

**Estimated Reading Time: 20 minutes**
# Publishing Tags and Releases
## Table of Contents
* [Basics](#basics)
* [Circle CI Configuration](#circle-ci-configuration)
* [Continuous Integration Automation](#continuous-integration-automation)
* [Cleanup Container Registry](#cleanup-container-registry)
* [Release Tags (retain always)](#release-tags-retain-always)
* [Branch Tags](#branch-tags)
    * [Active Branch Tags (retain)](#active-branch-tags-retain)
    * [Inactive Branch Tags (purge)](#inactive-branch-tags-purge)
* [Local Tags (retain)](#local-tags-retain)
* [Dangling Tags (purge)](#dangling-tags-purge)
* [Local Automation](#local-automation)
* [Gradle Task Considerations](#gradle-task-considerations)
* [Verifying Image Contents](#verifying-image-contents)

## Basics
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

The job `tags` filter is defined for every job, in accordance with the CircleCI reference above. For simplicity, any tag is allowed to trigger a build, but the publishing logic will prevent tags containing non-semantic versions from getting published as such.
 
```
jobFilters: &jobFilters
  filters:
    tags:
      only: /.*/

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
- if build is tagged `CIRCLE_TAG=v?` and `CIRCLE_BRANCH=` (other CI environments may set the branch, but CircleCI does not on tagged builds)
  - set `jib`'s `to` image to:  
     - `"${version}"` (`CIRCLE_TAG` must be semantic version without the 'v' prefix) 
- else (tag is non-semantic or non-existent):
  - set `jib`'s `to` image to:  
     - `"${branch}-SNAPSHOT"` (branch = `CIRCLE_BRANCH` or "unknown" if unset)
     
## Cleanup Container Registry

Cleanup of the container registry (Docker Hub) is automated without being destructive or confusing about what a published tag represents.
 
The container registry API for DockerHub can be used to delete tags with the token obtained from the credentials. The continuous integration environment should attempt to clean up the registry before publishing on every run.

## Release Tags (retain always)        
These tags are identified through regex pattern matching and without the `-SNAPSHOT` suffix:
```
"3.0.0"     [RETAINED] (semantic version and not snapshot)
"2.4.2"     [RETAINED] (semantic version and not snapshot)
"2.4.2-RC1" [RETAINED] (semantic version and not snapshot)
```

## Branch Tags
- collect all tags from container registry API that have the `-SNAPSHOT` suffix and NOT the `LOCAL-` prefix
- retrieve all branches on the origin:
```
$ for branch in `git branch -r | grep -v HEAD`; do echo -e ${branch#"origin/"}; done | sort -r
> master
> 123-featureA
```

### Active Branch Tags (retain)
The prefix before `-SNAPSHOT` can be intuited as the branch name which produced the last published snapshot for this tag:

```
"master-SNAPSHOT"       [RETAINED] ("master" branch should always exist on remote origin)
"123-featureA-SNAPSHOT" [RETAINED] ("123-featureA" exists as branch on remote origin)
```

### Inactive Branch Tags (purge)
Purge any tags associated with a branch name that does not exist in the remote origin (e.g. - ["master", "123-featureA"])
```
"456-featureB-SNAPSHOT" [PURGED] ("456-featureB" does NOT exist as branch on remote origin and not prefixed w/"LOCAL-")
```

## Local Tags (retain)
- collect all tags from container registry API that have the `LOCAL-` prefix
- use all branches on origin calculated before

```
"LOCAL-${branch}-${whoAmI}" [RETAINED] (prefixed w/"LOCAL-")
```
         
## Dangling Tags (purge)
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
Utilizing the publishing build logic is a matter of calling the `dynamicVersion` function on the root gradle project and assigning its return value to the root level project version.

The first parameter is the user, organization, or vendor (terminology depends on the container registry being used). The second parameter prepares the appropriate checks depending on which CI environment the build runs in. The last parameter is the container registry published to and cleaned from.

The `dynamicVersion` call, alone, will set some defaults for publishing, but will miss some sub-project specific details that you will want to set on a per-project basis. 

Below is an example of what you probably want to set at the subproject level. Any of the `Publish` object properties can be overwritten, but it is not recommended to set more than what's shown below, unless you are a power user and want specific subprojects to publish to different repositories, have different naming conventions, etc.

```
version = rootProject.dynamicVersion("cedardevs", CI.CIRCLE, Registry.DOCKER_HUB)

subprojects {
        project.setPublish(Publish(
                description = description,
                documentation = docs,
                authors = formatAuthors(authors),
                url = url,
                licenses = License.GPL20,
                cleanBefore = "jib"
        ))
}
```

## Verifying Image Contents
We use the [Open Containers Spec](https://github.com/opencontainers/image-spec/blob/master/annotations.md) to define image annotations.

`docker inspect image ${imageID}`
```
"Labels": {
    "org.opencontainers.image.authors": "Coalition for Earth Data Applications and Research <cedar.cires@colorado.edu> (https://github.com/cedardevs)",
    "org.opencontainers.image.created": "2020-02-22T00:05:37.561983Z",
    "org.opencontainers.image.description": "A browser UI for the OneStop system.",
    "org.opencontainers.image.documentation": "https://cedardevs.github.io/onestop",
    "org.opencontainers.image.licenses": "GPL-2.0",
    "org.opencontainers.image.revision": "7606250a",
    "org.opencontainers.image.title": "onestop-client",
    "org.opencontainers.image.url": "https://data.noaa.gov/onestop",
    "org.opencontainers.image.vendor": "cedardevs",
    "org.opencontainers.image.version": "unknown-SNAPSHOT"
}
```

The same image annotations can be seen via Kubernetes, using the following command:

`kubectl describe pod ${podName}`

<hr>
<div align="center"><a href="#">Top of Page</a></div>
