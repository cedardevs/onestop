

## Git Workflow

  * `master` contains changes being prepped for a release, with tags marking each release
  * `release/[version]` contains the existing release paths. These only deviate from the release tagged on the master branch when hotfixes are required.
  * temporary branches are used to work on features, until they are reviewed as a pull request and deleted.

### Releases: Tags and Release Branches
When it's time for the creation of a new release, the code is merged onto both `master` and a new major release branch, if applicable (`major.#.x`). Follow these steps to publish a release:

  1. Run the `promote.sh` script while on the `master` branch and merge the new tag to the appropriate release branch, e.g.,
      ```sh
      bash promote.sh  2.0.0-RC1 2.0.0-RC2
      git checkout release/2.x
      git merge v2.0.0-RC1
      git push
      ```

  2. Write the release notes on GitHub:
     * [Draft the release][draft release] from the corresponding tag on the `master` branch.
     * Have at least one team member review the release notes.
     * Publish the [release](https://github.com/cedardevs/onestop/releases) on GitHub.


### Temporary Branches
We introduce new features and major changes with the following steps:
  1. Make a new branch off `master`. As a naming convention, your branch name can be anything except `master` or with the `release/` or `hotfix/` prefix:

      ```sh
      git fetch origin
      git checkout -b feature/foo origin/master
      ```

  1. Write code, write tests!  

  1. [Create a pull request](https://github.com/cedardevs/onestop/compare) once development is complete and
    request a review.

  1. Reviewer merges changes back into the `master` branch and deletes working branch.

### References

[draft release]: https://github.com/cedardevs/onestop/releases/new
[git tag]: https://git-scm.com/book/en/v2/Git-Basics-Tagging
[new release]: https://github.com/cedardevs/onestop/releases/new
[pull request]: https://github.com/cedardevs/onestop/compare
[releases]: https://github.com/cedardevs/onestop/releases
[semantic versioning]: http://semver.org/
[github issues]: https://github.com/cedardevs/onestop/issues/new
