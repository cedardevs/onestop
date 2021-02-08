<div align="center"><a href="/onestop/developer">Developer Documentation Home</a></div>
<hr>

**Estimated Reading Time: 10 minutes**
# Contribution Guidelines

## Table of Contents
* [Guidelines](#guidelines)
    * [Submitting an issue](#submitting-an-issue)
    * [Submitting a pull request](#submitting-a-pull-request)
    * [Running locally](#running-locally)
* [Coding guidelines](#coding-guidelines)
    * [Code coverage](#code-coverage)
* [Our Use of Branches](#our-use-of-branches)

We’re so glad you’re thinking about contributing to the OneStop project! If you’re unsure about anything, just ask — or submit your issue or pull request anyway. The worst that can happen is we’ll politely ask you to change something. We appreciate all friendly contributions.

One of our goals is to ensure a welcoming environment for all contributors to our projects.

We encourage you to read this project’s contributing policy (you are here), its [LICENSE](https://github.com/cedardevs/onestop/blob/master/license.txt) and [README](https://github.com/cedardevs/onestop/blob/master/README.md).


## Guidelines

### Submitting an issue

To help us get a better understanding of the issue you’re submitting, include detailed steps to replicate the issue along with expected behavior.

### Submitting a pull request

Here are a few guidelines to follow when submitting a pull request:

1. Create a Github account or sign in to your existing account
1. Fork this repo into your Github account (or just clone it if you’re a OneStop team member). Read more about [forking a repo on Github][github fork]

1. Create a branch from `master` that lightly defines what you’re working on (for example, add-styles).
1. Once you’re ready, submit a pull request against the `master` branch.

Have questions or need help with setup? Open [an issue][github issues] via Github Issues

### Running locally

See the [Quickstart](quickstart) deployment overview pages.

## Coding guidelines

The purpose of our coding styleguides are to create consistent coding practices for OneStop. The styleguide should be treated as a guide — rules can be modified according to project needs.

### Code coverage

We use [code coverage][code coverage] tools to understand how much of our JavaScript is suffciciently tested. Code coverage is one way (among many) of measuring code _quality_ more generally. Here's how it works for contributions:

1. Each pull request creates a new coverage report on [Code Climate][code climate].
1. Code Climate then posts a status message back to GitHub that lists the coverage percentage on that branch, and the difference between that number and the one last reported on our default branch.

For JavaScript contributions, we will review the code coverage percentage and change to ensure that the quality of our code is not dramatically affected.

High code coverage numbers are generally good, and we would prefer that our coverage increases over time. We will not categorically reject contributions that reduce code coverage, but we may ask contributors to refactor their code, add new unit tests, or modify existing tests to avoid significant reductions in coverage.

## Our Use of Branches

See [our branching model guidelines](internal-practices#git-workflow) for more information on our internal git/GitHub release workflow.


[license]: https://github.com/cires-ncei/onestop/wiki#legal
[readme]: https://github.com/cires-ncei/onestop/blob/master/readme.md
[github fork]: https://help.github.com/articles/fork-a-repo/
[github issues]: https://github.com/cires-ncei/onestop/issues
[code coverage]: https://en.wikipedia.org/wiki/Code_coverage
[code climate]: https://codeclimate.com/

<hr>
<div align="center"><a href="#">Top of Page</a></div>
