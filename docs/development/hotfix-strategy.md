# Hotfix Strategy

## Syncing hotfix to Release and Master branches

These instructions assume the hotfix is a single commit, with changes that should be applied to both the current release branch and master. The git commands can be altered to handle multiple commits, but this is left as an exercise to the reader. If the change should not be applied, or differs between the two branches, just manually create two separate changes for each branch instead.

1. Make the change needed on branch A, and commit it.
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
git add .
git commit -m "test"
git format-patch -1 HASH
git checkout master
git apply 0001-test.patch
git add .
git commit -m "test"
```


## Tagging a hotfix release

Once a hotfix has been committed to the release branch, tag a hotfix. For example, say the branch is release/N.x, and the current version is N.0.0.

Nominally, the current version is represented by what the last tag in GitHub is. Ideally, the value in  `gradle.properties` represents what the second parameter (e.g. - N.0.2) of the promote script was from the last time it was called and created a tag.

Having the promote script update to the next anticipated version prevents subsequent builds from making snapshot versions on a tag that's already been released. 

### TLDR; `bash promote.sh N.0.1 N.0.2`.

Where, in general:

Current tag: N.0.0
Tag to be created from promote script: N.0.1
Assumed next version: N.0.2

Due to the expectations on the release branch specifically, we need to be a little more careful and follow this procedure:

### The slightly longer description of what I normally do in practice:

1. Checkout the release branch
1. Comment out the line `updateAndCommit $incrementToVersion` from promote.sh (This is used on master, but causes problems with the system that builds for production from the release branch, and the script hasn't been updated to account for these variations yet.)
1. Run `bash promote.sh N.0.1 N.0.2`. This will create the tag, and make sure all the versions are correctly set to N.0.1
1. On GitHub, create a release from the `vN.0.1` tag.
1. When finished, your working directory should be clean except for the commented line in promote.sh. Make sure to restore it to it's normal state now.

