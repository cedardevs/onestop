<div align="center"><a href="/onestop/developer">Developer Documentation Home</a></div>
<hr>

**Estimated Reading Time: 5 minutes**
# Hotfix Strategy

## Table of Contents
- [Syncing hotfix to Release and Master branches](#syncing-hotfix-to-release-and-master-branches)
- [Tagging a hotfix release](#tagging-a-hotfix-release)
- [Manually publishing with jib](#manually-publishing-with-jib)

## Syncing hotfix to Release and Master branches

These instructions assume the hotfix is a single commit, with changes that should be applied to both the current release branch and master. The git commands can be altered to handle multiple commits, but this is left as an exercise to the reader. If the change should not be applied, or differs between the two branches, just manually create two separate changes for each branch instead.

1. Make the change needed on branch A, increment the patch version using `manageVersions.sh`, and commit it.
1. Use `git log` (on branch A) to look up the hash for the commit. In later instructions, this will be referred to as HASH.
1. Use `git format-patch -1 HASH` (on branch A) to generate a patch file. It will be named something like `0001-commit-message.patch`
1. Switch to branch B (target branch to be hotfixed).
1. Use `git apply 0001-commit-message.patch` to apply the changes.
1. Add and commit the changes to branch B as well.
1. Delete the patch file.

Note that it doesn't make any difference if the patch is written on master and then applied to the release branch, or the reverse.

For example:
```
git checkout release/2.x
edit build.gradle
manageVersions.sh GET
manageVersions.sh SET 2.x.<p+1>
manageVersions.sh GET #confirm desired changes
git add .
git commit -m "test"
git format-patch -1 HASH
git checkout master
git apply 0001-test.patch
git add .
git commit -m "test"
```

## Tagging a hotfix release

Once a hotfix and incremented patched version number has been merged to the release branch, tag it.
 ```
 git tag <tag>
 git push origin <tag>
 ```
Write the release notes on GitHub:
 * pushing that tag should have created a new release in the "releases" tab on github.
 * [Draft the release][draft release] from the corresponding tag on the `master` branch.
 * Have at least one team member review the release notes.
 * Publish the [release](https://github.com/cedardevs/onestop/releases) on GitHub.

## Manually publishing with jib

`(export DOCKER_USER=<user>; export DOCKER_PASSWORD=<password>; ./gradlew jib)`

<hr>
<div align="center"><a href="#">Top of Page</a></div>
