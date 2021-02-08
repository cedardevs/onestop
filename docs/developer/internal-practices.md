**Estimated Reading Time: 10 minutes**
# Internal Practices
We have several google documents on this topic in Google Drive at `Agile Project> Team Norms`.

## Table of Contents
* [Git Workflow](#git-workflow)
    * [Branches](#branches)
    * [Temporary Branches and Code Commits](#temporary-branches-and-code-commits)
    * [Versioning](#versioning)
    * [Releases: Tags and Release Branches](#releases-tags-and-release-branches)
* [References](#references)

## Git Workflow

### Branches
  * `master` contains changes being prepped for a release, with tags marking each release. 
    The master branch should be kept in a working state but represents the "bleeding edge" 
    version of the project. By "working" we mean it compiles, passes tests and results in 
    a deployable artifact.
  * `release/[version]` contains the existing release paths. These only deviate from the 
    release tagged on the master branch when hotfixes are required. A new release branch 
    should be created for every major version. Minor releases can be merged into their 
    major version branch. Patches should also be merged to their respective branch, but 
    follows a different work flow. Reference [Hotfix Strategy](hotfix-strategy) for more 
    information.  

### Temporary Branches and Code Commits
Temporary branches are used to work on features and major changes via the following steps:
  1. Make a new branch off `master`. As a naming convention, please follow the convention 
  in Google Drive at `Agile Project> Team Norms> Foam Cat Team Norms` document. Do not to 
  use `master` or the `release/` or `hotfix/` prefix:

      ```sh
      git fetch origin
      git checkout -b feature/foo origin/master
      ```
     If the work is related to an issue, the branch is typically named `<issue#>-short-description`, 
     for example "1404-dev_docs_pass2", where the description is derived from the issue title. 
     Spaces are not allowed in branch names, so either hyphens or underscores can be used in their place.

  1. Write code with tests!  

  1. Upon completion move the Github/Zenhub story into the Review column
  
  1. [Create a pull request](https://github.com/cedardevs/onestop/compare).
     Please link with the story via "Linked issues"

  1. The **reviewer** merges the pull request back into the `master` branch and deletes the 
     working branch. Verify story in github/zenhub got moved to Closed column (may have to manually do).

### Versioning:
Our version numbers follow the pattern of `<major>.<minor>.<patch>`:
  * Ideally the current version indicates what release you're working toward, not what version you are on;
    [citation stackoverflow issue](https://softwareengineering.stackexchange.com/questions/166215/when-do-you-change-your-major-minor-patch-version-number).
    This is to avoid new artifacts from colliding with older artifacts built prior to the last tag.
  * To accomplish that, it is important we always increment the minor version after a release. 
  * It's difficult to know if the next release will be a new major version. Theoretically incrementing 
    the major version should be done when the breaking change is introduced (e.g. dropping support for ES5). 
  * But that may not always happen when it should. For that reason it is important we review 
    changes prior to cutting a release and determine if should increment the major version. 
    It is important we avoid merging breaking changes to an existing release branch. 
  * The patch version is incremented when a patch is applied. See [Hotfix Strategy](hotfix-strategy) 
    documentation for patching. 
  * Find more on versioning best practices here - https://semver.org/

### Releases: Tags and Release Branches
When all the features in master are ready for production, it is time to cut a release. 
A "release" is either a new major or minor version. For a patch, see the 
[Hotfix Strategy](hotfix-strategy) documentation. Follow these steps to publish a release:

  1. Run the `manageVersions.sh` script to get the version number throughout the project.
  2. It may not be necessary to increment the version before tagging. After the last release, 
     we bumped the minor version, as *you will* after tagging. 
  3. It is only necessary to "bump the version" if this release is determined to be a new 
     major version. If the changes in master broke backward compatibility, bump the major 
     version, review changes and commit.
  4. If the version number was incremented, make a PR (git Pull Request) to master to have
     a reviewer ensure proper semantic version. If not, proceed to the next step. 
  5. After merging the branch to master, tag master. The tag needs to match the version
     and be prefixed with a "v", e.g. "**v**Major.minor.patch" like `v2.13.1`.
     ```
     git tag <tag>
     git push origin <tag>
     ```
  6. If this is a new major version, create a branch from that commit named for this
     version `release/[version]`, e.g. `release/3.x`.
  7. If this is a minor version, create a PR into the release branch. 
  8. IMPORTANT: On master, increment the minor version number using `manageVersions.sh`,
     commit to directly to master (this is a small change, so no need for a PR).
  9. Write the release notes on GitHub:
     * Pushing that tag should have created a new release in the "releases" tab on github.
     * [Draft the release][draft release] from the corresponding tag on the `master` branch.
     * Have at least one team member review the release notes.
     * Publish the [release](https://github.com/cedardevs/onestop/releases) on GitHub.

### References

[draft release]: https://github.com/cedardevs/onestop/releases/new
[git tag]: https://git-scm.com/book/en/v2/Git-Basics-Tagging
[new release]: https://github.com/cedardevs/onestop/releases/new
[pull request]: https://github.com/cedardevs/onestop/compare
[releases]: https://github.com/cedardevs/onestop/releases
[semantic versioning]: http://semver.org/
[github issues]: https://github.com/cedardevs/onestop/issues/new

<hr>
<div align="center"><a href="#">Top of Page</a></div>
