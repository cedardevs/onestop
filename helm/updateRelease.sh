#!/usr/bin/env bash

# Run this when any change is made to requirements.yaml, or when changes are made to the core OneStop sub charts that need to be deployed elsewhere. Don't forget to udpate the onestop chart version appropriately!

BASEDIR=$(dirname "$0")

pushd "$BASEDIR/onestop-dev" > /dev/null
rm charts/*.tgz
helm dependency update
popd > /dev/null

pushd "$BASEDIR/onestop" > /dev/null
rm charts/*.tgz
helm dependency update
popd  > /dev/null

pushd "$BASEDIR" > /dev/null
helm package onestop
popd > /dev/null

pushd "$BASEDIR"  > /dev/null
helm repo index . --merge index.yaml
echo "Updated index.yaml"
popd  > /dev/null

exit 0
