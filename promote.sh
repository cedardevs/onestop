#!/bin/bash

if [[ $# != 2 ]]; then
  echo "Usage: $0 releaseAsVersion incrementToVersion"
  echo "    This will update the version in gradle.proprties to <releaseAsVersion>, if possible. Then it will tag to trigger promotion strategy, and finally update and push the gradle.properties with <incrementToVersion>"
  exit 1
fi

releaseAsVersion=$1
incrementToVersion=$2

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
  prevVersion=$(cat gradle.properties | grep 'version=' | sed -e 's/version=//g' )
  sed -i -- "s/version=.*/version=$1/g" gradle.properties
  sed -i -- "s/\"version\":.*/\"version\": \"$1\",/g" client/package.json
  sed -i -- "s/${prevVersion}/$1/g" skaffold.yaml
  sed -i -- "s/version: .*/version: $1/g" api-search/schema/openapi.yml
}

# commit and push
updateAndCommit() {
  updateVersions $1
  git add gradle.properties
  git add client/package.json
  git add skaffold.yaml
  git add api-search/schema/openapi.yml
  git commit -m "Updating version to $1"
  git push
}

# update versions and tag
tag="v$releaseAsVersion"
updateAndCommit $releaseAsVersion
git tag "$tag"; git push origin "$tag"
updateAndCommit $incrementToVersion
