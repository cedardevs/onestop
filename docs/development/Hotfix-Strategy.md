# Syncing hotfix to Release and Master branches

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


# Tagging a hotfix release

Once a hotfix has been committed to the release branch, tag a hotfix. For example, say the branch is release/N.x, and the current version is N.0.0.

## TLDR; `bash promote.sh N.0.1 N.0.2`.

## The slightly longer description of what I normally do in practice:

1. Checkout the release branch
1. Comment out the line `updateAndCommit $incrementToVersion` from promote.sh (This is important on master, but doesn't really make much sense on the release branch, based on how we normally tag releases.) (It's also not a big deal if you skip this step.
1. Run `bash promote.sh N.0.1 N.0.2`. This will create the tag, and make sure all the versions are correctly set to N.0.1
1. On GitHub, create a release from the `vN.0.1` tag.
1. Revert promote.sh