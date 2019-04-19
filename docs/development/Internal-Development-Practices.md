This document outlines our Agile practices and how we utilize GitHub for managing & tracking our work. This is the **internal (OneStop team members)** process for development. External contributors should read our [contribution guidelines](/docs/development/contribution-guidelines.md).

## Table of Contents
* [Sprints](Internal-Development-Practices#sprints)
* [Sprint Planning](Internal-Development-Practices#sprint-planning)
* [Backlog Refining](Internal-Development-Practices#backlog-refining)
* [[Epics, User Stories, and Bugs|Internal-Development-Practices#epics-user-stories-and-bugs]]
* [User Interface Development](Internal-Development-Practices#user-interface-development)
* [Versioning](Internal-Development-Practices#versioning)
* [Git Workflow](Internal-Development-Practices#git-workflow)


## Sprints
The work-in-progress is always depicted via the issues and pull requests found on our [current sprint board][sprint board]. 

Issues in the `Todo` column are in priority order and worked on from the top down. In order to track the work done, all commits should be linked to their corresponding issues in one of three ways:
  * Referencing the issue number (n) in your commit message with `#n`
  * With a comment on the commit message (through GitHub) with the issue number appended
  * If working on a branch, prepend pull request with `n-`. In the description, all issues in this branch are automatically linked to the pull request (and moved on the waffle board) if `Resolves #n` is written for every issue.

Completed work is moved into the `Ready to Review` column, where another team member will review the associated commits and, if found acceptable, move the issue to `Done` and close it. All team members should participate in reviewing work and strive to keep the number of issues in the `Ready to Review` column below 10. 

Work represented in the `Done` column is discussed in sprint reviews and the column is emptied at the start of the next sprint.



## Sprint Planning
Helpful links:
* [Current Sprint board][sprint board]
* [Open bugs](https://github.com/cedardevs/onestop/issues?utf8=%E2%9C%93&q=is%3Aopen%20label%3Abug%20no%3Aproject)

Prior to beginning a sprint, the team holds a sprint planning meeting with the following steps to ensure we are working on the highest-priority issues. Except for absolutely critical, world-ending issues that may pop up during the course of our sprint, we agree as a team to stay focused on the work we define for ourselves at sprint planning.

#### #1: Cleanup Sprint Board
In the first step of the meeting we make sure all stories in the `Done` column of our current sprint board are closed before clearing out the column. After this, we verify stories in `Ready to Review` have been reviewed (then close them & remove from the board) or still need to be reviewed.

#### #2: Check Open Bugs
So that we make sure not to forget any bugs that have been reported, before adding any other work to our upcoming sprint we will review all open bugs. If any are critical, add them to the `Todo` column. This is also a good opportunity to review if any bugs have actually been resolved via work in other issues or if they are no longer relevant. In such cases, we can note where the bug was resolved/deemed no longer a concern and close the issue.

#### #3: Review Pending Stories For Current Epics
Looking at the `Groomed` column, we will pull in defined work from the open epic(s) to the current sprint that the team agrees we have the bandwidth to support. This is NOT where we define specific tasks to be worked for an epic; that is done during [backlog refining](Internal-Development-Practices#backlog-refining). Work pulled into the upcoming sprint should already be clearly defined and ready to be worked on.




## Backlog Refining

Halfway through our sprints, the team holds a backlog refining (AKA Grooming) meeting to flesh out user stories in `Backlog` and Epics. These steps are here to help us ensure we have enough clearly defined work before starting the next sprint.

#### #1: Check Open Bugs
This is an opportunity to address whether we need to work on any critical bugs before our regularly scheduled sprint work is addressed. Likewise, we can double check open bugs have not already been resolved or become inapplicable.

#### #2: Review Backlog Tasks of In-Progress Epics
Add sufficient details to enable work on a task. Discuss potential areas of complexity or uncertainty and note them. Once the team agrees a task is sufficiently described, move to `Groomed` column.

#### #3: Review Epics
Add details to upcoming epics and create any tasks, adding them to `Backlog`.



## Epics, User Stories, and Bugs
User stories and bugs are tracked as issues, and epics consist of multiple issues that together comprise a new feature or large chunk of work.

The OneStop team's epics are found in the `Epic` column of our [sprint board][sprint board]. The team works together to define and prioritize epics on the board. We aim to work on no more than two epics at a time.

In the event a bug is spotted, we use the `bug` label to identify the issue as such.

Sometimes there may be work that is not a bug or related to any epics -- these issues are reviewed and pulled into sprints where appropriate and do not need any special labels.



## User Interface Development
UI design, development and implementation can sometimes cause team conflict. In general, this conflict has been positive in nature but can sometimes lead to longer discussions that don't always feel productive. The team seeks to minimize this disruption by following a standard process supporting UI development.

#### #1: Start with wire-frames  
The team has had good success working through initial UI design issues by developing mock-ups with a tool called [balsamiq](https://balsamiq.com/). Wire-frames allow us to document our UI designs more formally after whiteboard sessions, present wire-frames as preliminary designs to our stakeholders, and compare finished UI implementations with original concepts.

#### #2: Iterate
After wire-frames are completed and agreed to by the team, product owner, and stakeholders, implementation can begin.  We expect iteration and discussion to follow during implementation however when we are unable to achieve consensus the team agrees to defer a decision until stakeholders and project managers have had an opportunity to review and guide the team.

#### #3: Throw the pony  
Recognizing that UI decisions can be subjective, the team is encouraged to recognize when a disagreement can not be resolved and to table the discussion by throwing the pony.  Refer to step above to further resolve an issue of contention.

## Versioning
The OneStop project uses [semantic versioning](http://semver.org/) to number release versions. Semantic version numbers take the form `major.minor.patch`, where:
* Bug fixes increment the `patch` number (e.g. `1.0.0` to `1.0.1`)
* New features increment the `minor` number and reset `patch` (e.g. `1.0.1` to `1.1.0`)
* Changes to the public API/breaking changes increment the `major` version and reset `minor` and `patch` (e.g. `1.1.2` to `2.0.0`)



## Git Workflow
We have one main branch:
  * `master` contains changes being prepped for a release, with tags marking each release

Other branches in the project reflect either official releases or features in progress.

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


### Active Development
Development on future releases exists on the `master` branch until it is ready for release. 


#### Bug Fixes
Once a release branch has been created, new commits to it are strictly bug fixes. These may be pushed back to `master` or other working branches if the bug exists in the next release code as well. 

Bug fixes are *only* applied to the most recent release branch; once a new release exists, prior releases are considered deprecated and known bugs will not be resolved in earlier release branches.

#### New Features
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




[draft release]: https://github.com/cedardevs/onestop/releases/new
[git tag]: https://git-scm.com/book/en/v2/Git-Basics-Tagging
[new release]: https://github.com/cedardevs/onestop/releases/new
[pull request]: https://github.com/cedardevs/onestop/compare
[releases]: https://github.com/cedardevs/onestop/releases
[semantic versioning]: http://semver.org/
[jfrog]: https://oss.jfrog.org/artifactory/webapp/#/home
[github issues]: https://github.com/cedardevs/onestop/issues/new
[sprint board]: https://waffle.io/cedardevs/onestop?source=cedardevs%2Fonestop