#!/bin/bash

printHelp() {
  echo "Tag and (optionally) increment versions."
  echo "Usage: $0"
  echo "  Follow the prompts."
  echo "Usage: $0 versionTag"
  echo "  Use only to tag hotfixes."
  echo "Usage: $0 versionTag nextVersion"
  echo "  Defaults next version to inclue -SNAPSHOT"
  echo "Usage: $0 versionTag nextVersion n"
  echo "  Tag and increment version without -SNAPSHOT"
  echo "  'n' argument is equivalent to indicating No in the prompts."
  echo "Versions must be formatted as Major.Minor.Patch, but can optionally include extended information at the end."
  echo "For example:"
  echo "  1.0.0"
  echo "  1.0.0-TEST"
  echo "  1.0.0-SNAPSHOT"
  echo "If only a versionTag is provided, the new tag will be created. This is intended ONLY for updating a hotfix version on a release/N.x branch."
  echo "If both versions are provided, the new tag will be created and versions will be updated. This is to prevent accidentally forgetting to increase the version. By default adds -SNAPSHOT."
  echo "Caution: if the version you are providing includes a text label, it is highly recommended that you indicate 'n' for No Snapshot."
  echo "  'promote.sh 1.0.0 1.3.0-TEST' will tag 1.0.0 and set the next version to 1.3.0-TEST-SNAPSHOT"
  echo "  'promote.sh 1.0.0 1.3.0-TEST n' will tag 1.0.0 and set the next version to 1.3.0-TEST"
}

validateVersionInput() {
  echo "$1" | grep '^[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*' >/dev/null
}

prompt() {
  >&2 echo $1 # query
  >&2 echo -n "> "
  read answer
  echo $answer
}

prevVersion=$(cat gradle.properties | grep 'version=' | sed -e 's/version=//g' )
echo "Current Version is: $prevVersion"

declare releaseAsVersion
declare incrementToVersion
declare isSnapshot

if [[ $# -ge 1 ]]; then
  if validateVersionInput $1; then
    echo -n ''
  else
    printHelp
    exit 1
  fi
  releaseAsVersion=$1
  incrementToVersion=$2
  isSnapshot=$3
else
  releaseAsVersion=$(prompt "What version should be tagged?")
  incrementToVersion=$(prompt "What version should be next? (optional)")
  if [[ $incrementToVersion ]]; then
    isSnapshot=$(prompt  "Is the next version a snapshot? (Y/n)")
  fi
fi

# echo "'$releaseAsVersion' '$incrementToVersion' '$isSnapshot'"

if validateVersionInput $releaseAsVersion; then
  echo -n ''
else
  echo "invalid tag: $releaseAsVersion"
  exit 1
fi
if [[ $incrementToVersion ]] && validateVersionInput $incrementToVersion; then
  echo -n ''
else
  echo "invalid next: $incrementToVersion"
  exit 1
fi

# do not continue if there are staged changes which might accidentally be included in the build
if ! git diff --cached --quiet; then
  echo "You cannot trigger this action with staged changes. Please commit or revert them."
  exit 1
fi

# do not continue if there are unmodified changes to the file we need to update that might accidentally be included
if [[ $(git diff-index --name-only HEAD | wc -l) > 0 ]]; then
  echo "You cannot trigger this action with uncommitted changes."
  exit 1
fi

# update the properties
updateVersions() {
  sed -i -- "s/version=.*/version=$1/g" gradle.properties
  sed -i -- "s/\"version\":.*/\"version\": \"$1\",/g" client/package.json
  sed -i -- "s/${prevVersion}/$1/g" skaffold.yaml
}

# commit and push
updateAndCommit() {
  echo "Update version to $1"
  updateVersions $1
  git add gradle.properties
  git add client/package.json
  git add skaffold.yaml
  git commit -m "Updating version to $1"
  git push
}

# update versions and tag
tag="v$releaseAsVersion"
updateAndCommit $releaseAsVersion
echo "Tagging as $tag"
git tag "$tag"; git push origin "$tag"
if [[ $incrementToVersion ]] ; then
  if [[ "$isSnapshot" == 'n' ]]; then
    updateAndCommit $incrementToVersion
  else
    updateAndCommit "$incrementToVersion-SNAPSHOT"
  fi
fi
